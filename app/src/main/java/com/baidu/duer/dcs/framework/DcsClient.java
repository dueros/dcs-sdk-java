/*
 * Copyright (c) 2017 Baidu, Inc. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.baidu.duer.dcs.framework;

import com.baidu.duer.dcs.framework.dispatcher.AudioData;
import com.baidu.duer.dcs.framework.dispatcher.MultipartParser;
import com.baidu.duer.dcs.framework.heartbeat.HeartBeat;
import com.baidu.duer.dcs.framework.message.DcsRequestBody;
import com.baidu.duer.dcs.framework.message.DcsResponseBody;
import com.baidu.duer.dcs.framework.message.DcsStreamRequestBody;
import com.baidu.duer.dcs.http.HttpConfig;
import com.baidu.duer.dcs.http.HttpRequestInterface;
import com.baidu.duer.dcs.http.OkHttpRequestImpl;
import com.baidu.duer.dcs.http.callback.ResponseCallback;
import com.baidu.duer.dcs.util.LogUtil;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

/**
 * 和服务器端保持长连接、发送events和接收directives和维持心跳
 * <p>
 * Created by wuruisheng on 2017/6/1.
 */
public class DcsClient {
    public static final String TAG = DcsClient.class.getSimpleName();
    private final DcsResponseDispatcher dcsResponseDispatcher;
    private final HttpRequestInterface httpRequestImp;
    private final HeartBeat heartBeat;
    private IDcsClientListener dcsClientListener;

    public DcsClient(DcsResponseDispatcher dcsResponseDispatcher, IDcsClientListener dcsClientListener) {
        this.dcsResponseDispatcher = dcsResponseDispatcher;
        this.dcsClientListener = dcsClientListener;
        httpRequestImp = new OkHttpRequestImpl();
        heartBeat = new HeartBeat(httpRequestImp);
        heartBeat.setHeartbeatListener(new HeartBeat.IHeartbeatListener() {
            @Override
            public void onStartConnect() {
                startConnect();
            }
        });
    }

    public void release() {
        heartBeat.release();
        httpRequestImp.cancelRequest(HttpConfig.HTTP_DIRECTIVES_TAG);
        httpRequestImp.cancelRequest(HttpConfig.HTTP_EVENT_TAG);
    }

    /**
     * 建立连接
     */
    public void startConnect() {
        httpRequestImp.cancelRequest(HttpConfig.HTTP_DIRECTIVES_TAG);
        getDirectives(new IResponseListener() {
            @Override
            public void onSucceed(int statusCode) {
                LogUtil.d(TAG, "getDirectives onSucceed");
                fireOnConnected();
                heartBeat.startNormalPing();
            }

            @Override
            public void onFailed(String errorMessage) {
                LogUtil.d(TAG, "getDirectives onFailed");
                fireOnUnconnected();
                heartBeat.startExceptionalPing();
            }
        });
    }

    /**
     * 发送带流式请求
     *
     * @param requestBody       消息体
     * @param streamRequestBody stream式消息体
     * @param listener          回调
     */
    public void sendRequest(DcsRequestBody requestBody,
                            DcsStreamRequestBody streamRequestBody, final IResponseListener listener) {
        httpRequestImp.doPostEventMultipartAsync(requestBody,
                streamRequestBody, getResponseCallback(dcsResponseDispatcher, new IResponseListener() {
                    @Override
                    public void onSucceed(int statusCode) {
                        if (listener != null) {
                            listener.onSucceed(statusCode);
                        }
                    }

                    @Override
                    public void onFailed(String errorMessage) {
                        if (listener != null) {
                            listener.onFailed(errorMessage);
                        }

                        heartBeat.startImmediatePing();
                    }
                }));
    }

    /**
     * 发送普通请求
     *
     * @param requestBody 消息体
     * @param listener    回调
     */
    public void sendRequest(DcsRequestBody requestBody, IResponseListener listener) {
        httpRequestImp.doPostEventStringAsync(requestBody,
                getResponseCallback(dcsResponseDispatcher, listener));
    }

    private void getDirectives(IResponseListener listener) {
        httpRequestImp.cancelRequest(HttpConfig.HTTP_DIRECTIVES_TAG);
        httpRequestImp.doGetDirectivesAsync(null,
                getResponseCallback(dcsResponseDispatcher, listener));
    }

    private void fireOnConnected() {
        if (dcsClientListener != null) {
            dcsClientListener.onConnected();
        }
    }

    private void fireOnUnconnected() {
        if (dcsClientListener != null) {
            dcsClientListener.onUnconnected();
        }
    }

    private ResponseCallback getResponseCallback(final DcsResponseDispatcher dcsResponseDispatcher,
                                                 final IResponseListener responseListener) {
        ResponseCallback responseCallback = new ResponseCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                LogUtil.d(TAG, "onError,", e);
                if (responseListener != null) {
                    responseListener.onFailed(e.getMessage());
                }
            }

            @Override
            public void onResponse(Response response, int id) {
                super.onResponse(response, id);
                LogUtil.d(TAG, "onResponse OK ," + response.request().url());
                LogUtil.d(TAG, "onResponse code ," + response.code());
                if (responseListener != null) {
                    responseListener.onSucceed(response.code());
                }
            }

            @Override
            public Response parseNetworkResponse(Response response, int id) throws Exception {
                int statusCode = response.code();
                if (statusCode == 200) {
                    MultipartParser multipartParser = new MultipartParser(
                            new MultipartParser.IMultipartParserListener() {
                                @Override
                                public void onResponseBody(DcsResponseBody responseBody) {
                                    dcsResponseDispatcher.onResponseBody(responseBody);
                                }

                                @Override
                                public void onAudioData(AudioData audioData) {
                                    dcsResponseDispatcher.onAudioData(audioData);
                                }

                                @Override
                                public void onParseFailed(String unParseMessage) {
                                    dcsResponseDispatcher.onParseFailed(unParseMessage);
                                }
                            });

                    try {
                        multipartParser.parseResponse(response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return response;
            }
        };

        return responseCallback;
    }

    public interface IDcsClientListener {
        void onConnected();

        void onUnconnected();
    }
}