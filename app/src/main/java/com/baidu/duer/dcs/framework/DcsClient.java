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

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.dcs.okhttp3.Call;
import com.baidu.dcs.okhttp3.Response;
import com.baidu.duer.dcs.api.IConnectionStatusListener;
import com.baidu.duer.dcs.devicemodule.system.message.ThrowExceptionPayload;
import com.baidu.duer.dcs.framework.decoder.IDecoder;
import com.baidu.duer.dcs.framework.decoder.JLayerDecoderImpl;
import com.baidu.duer.dcs.framework.dispatcher.AudioData;
import com.baidu.duer.dcs.framework.dispatcher.MultipartParser;
import com.baidu.duer.dcs.framework.heartbeat.HeartBeat;
import com.baidu.duer.dcs.framework.message.DcsRequestBody;
import com.baidu.duer.dcs.framework.message.DcsResponseBody;
import com.baidu.duer.dcs.framework.message.DcsStreamRequestBody;
import com.baidu.duer.dcs.framework.message.Directive;
import com.baidu.duer.dcs.framework.message.Payload;
import com.baidu.duer.dcs.http.HttpConfig;
import com.baidu.duer.dcs.http.HttpRequestInterface;
import com.baidu.duer.dcs.http.OkHttpRequestImpl;
import com.baidu.duer.dcs.http.callback.ResponseCallback;
import com.baidu.duer.dcs.util.LogUtil;
import com.baidu.duer.dcs.util.NetWorkStateReceiver;
import com.baidu.duer.dcs.util.NetWorkUtil;
import com.baidu.duer.dcs.util.SystemServiceManager;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.baidu.duer.dcs.api.IConnectionStatusListener.ConnectionStatus.CONNECTED;


/**
 * 和服务器端保持长连接、发送events和接收directives和维持心跳
 * <p>
 * Created by wuruisheng on 2017/6/1.
 */
public class DcsClient {
    public static final String TAG = DcsClient.class.getSimpleName();
    public static final long HTTP_DIRECTIVES_TIME = 60 * 60 * 1000;
    private final DcsResponseDispatcher dcsResponseDispatcher;
    private final HttpRequestInterface httpRequestImp;
    private final HeartBeat heartBeat;
    private IDcsClientListener dcsClientListener;
    private IDecoder decoder;
    private final MultipartParser directiveParser;
    private final MultipartParser eventParser;
    private final List<IConnectionStatusListener> connectStatusListeners;
    private volatile boolean isReleased;
    private NetWorkStateReceiver netWorkStateReceiver;

    private Context context = SystemServiceManager.getAppContext();
    private volatile IConnectionStatusListener.ConnectionStatus connectStatus;
    private Handler handlerMain = new Handler(Looper.getMainLooper());
    private CalculateRetryTime calculateRetryTime;
    private volatile boolean isNeedConnect;
    private Runnable startConnectRunnable = new Runnable() {
        @Override
        public void run() {
            // 下一次连接时重制状态
            connect();
        }
    };
    /**
     * 建立连接
     */
    private final IResponseListener connectListener = new SimpleResponseListener() {
        @Override
        public void onSucceed(int statusCode) {
            LogUtil.d(TAG, "getDirectives statusCode: " + statusCode);
            if (statusCode == 200) {
                connectStatus = CONNECTED;
                fireConnectStatus(connectStatus);
                heartBeat.start();
                stopTryConnect();
            } else {
                tryConnect();
            }
        }

        @Override
        public void onFailed(String errorMessage) {
            LogUtil.d(TAG, "getDirectives onFailed，" + errorMessage);
            tryConnect();
        }
    };

    private final NetWorkStateReceiver.INetWorkStateListener netWorkStateListener =
            new NetWorkStateReceiver.INetWorkStateListener() {
                @Override
                public void onNetWorkStateChange(int netType) {
                    Log.d(TAG, "onNetWorkStateChange-netType:" + netType);
                    if (netType != NetWorkUtil.NETWORK_NONE) {
                        Log.d(TAG, " onNetWorkStateChange ");
                        tryConnectAtOnce();
                    } else {
                        connectStatus = IConnectionStatusListener.ConnectionStatus.DISCONNECTED;
                        fireConnectStatus(connectStatus);
                    }
                }
            };

    public DcsClient(DcsResponseDispatcher dcsResponseDispatcher, IDcsClientListener dcsClientListener) {
        this.dcsResponseDispatcher = dcsResponseDispatcher;
        this.dcsClientListener = dcsClientListener;
        httpRequestImp = new OkHttpRequestImpl();
        heartBeat = new HeartBeat(httpRequestImp);
        decoder = new JLayerDecoderImpl();

        MultipartParser.IMultipartParserListener parserListener = new ClientParserListener();

        directiveParser = new MultipartParser(decoder, new ClientParserListener() {
            @Override
            public void onClose() {
                super.onClose();
                Log.d(TAG, "directiveParser-onClose");

                if (connectStatus != IConnectionStatusListener.ConnectionStatus.PENDING) {
                    connectStatus = IConnectionStatusListener.ConnectionStatus.DISCONNECTED;
                    fireConnectStatus(connectStatus);
                }

                tryConnectAtOnce();
            }

            @Override
            public void onResponseBody(DcsResponseBody responseBody) {
                super.onResponseBody(responseBody);
                Directive directive = responseBody.getDirective();
                if (directive != null) {
                    Payload payload = directive.getPayload();
                    if (payload != null) {
                        if (payload instanceof ThrowExceptionPayload) {
                            // 服务器下发了ThrowException异常
                            isNeedConnect = false;
                        }
                    }
                }
            }
        });
        eventParser = new MultipartParser(decoder, parserListener);
        netWorkStateReceiver = new NetWorkStateReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(netWorkStateReceiver, filter);

        netWorkStateReceiver.setOnNetWorkStateListener(netWorkStateListener);

        connectStatusListeners = new CopyOnWriteArrayList<>();
        connectStatus = IConnectionStatusListener.ConnectionStatus.DISCONNECTED;
        calculateRetryTime = new CalculateRetryTime();
        isNeedConnect = true;
    }

    public void release() {
        Log.d(TAG, "release");
        stopTryConnect();
        try {
            if (netWorkStateReceiver != null) {
                context.unregisterReceiver(netWorkStateReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            isReleased = true;
            decoder.release();
            heartBeat.release();
            httpRequestImp.cancelRequest(HttpConfig.HTTP_DIRECTIVES_TAG);
            httpRequestImp.cancelRequest(HttpConfig.HTTP_EVENT_TAG);
        }
    }

    public void startConnect() {
        if (!isReleased) {
            getDirectives(connectListener);
        }
    }

    public boolean isConnected() {
        return connectStatus == IConnectionStatusListener.ConnectionStatus.CONNECTED;
    }

    private void tryConnectAtOnce() {
        resetRetryTime();
        tryConnect();
    }

    private void tryConnect() {
        if (isNeedConnect) {
            heartBeat.stop();
            connectStatus = IConnectionStatusListener.ConnectionStatus.DISCONNECTED;
            fireConnectStatus(connectStatus);
            handlerMain.removeCallbacks(startConnectRunnable);
            handlerMain.postDelayed(startConnectRunnable, getRetryTime());
        }
    }

    private void stopTryConnect() {
        resetRetryTime();
        handlerMain.removeCallbacks(startConnectRunnable);
    }

    private void connect() {
        if (TextUtils.isEmpty(HttpConfig.getAccessToken())) {
            Log.d(TAG, "connect-accessToken is null !");
            return;
        }
        if (!isReleased && connectStatus == IConnectionStatusListener.ConnectionStatus.DISCONNECTED) {
            if (NetWorkUtil.isNetworkConnected(context)) {
                connectStatus = IConnectionStatusListener.ConnectionStatus.PENDING;
                fireConnectStatus(connectStatus);
                getDirectives(connectListener);
            } else {
                connectStatus = IConnectionStatusListener.ConnectionStatus.DISCONNECTED;
                fireConnectStatus(connectStatus);
            }
        }
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
        Log.e("logId", "logId send  stream start");
        decoder.interruptDecode();
        httpRequestImp.cancelRequest(HttpConfig.HTTP_VOICE_TAG);
        httpRequestImp.doPostEventMultipartAsync(requestBody,
                streamRequestBody, getResponseCallback(eventParser, new IResponseListener() {
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
                getResponseCallback(eventParser, listener));
    }

    private void getDirectives(IResponseListener listener) {
        httpRequestImp.cancelRequest(HttpConfig.HTTP_DIRECTIVES_TAG);
        httpRequestImp.doGetDirectivesAsync(getResponseCallback(directiveParser, listener));
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

    private ResponseCallback getResponseCallback(final MultipartParser multipartParser,
                                                 final IResponseListener responseListener) {
        return new ResponseCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                LogUtil.d(TAG, "onError,", e);
                if (call.request().tag().equals(HttpConfig.DIRECTIVES)
                        || call.request().tag().equals(HttpConfig.HTTP_VOICE_TAG)) {
                    fireOnFailed(e.getMessage());
                }
            }

            @Override
            public void onResponse(Response response, int id) {
                super.onResponse(response, id);
                LogUtil.d(TAG, "onResponse OK ," + response.request().url());
                LogUtil.d(TAG, "onResponse code ," + response.code());
                if (response.isSuccessful()) {
                    if (responseListener != null) {
                        responseListener.onSucceed(response.code());
                    }
                }
            }

            @Override
            public Response parseNetworkResponse(Response response, int id) throws Exception {
                int statusCode = response.code();
                if (statusCode == 200) {
                    Log.d(TAG, "onResponse OK ," + response.request().tag());
                    multipartParser.parseResponse(response);
                }
                return response;
            }

            void fireOnFailed(String errorMessage) {
                if (responseListener != null) {
                    responseListener.onFailed(errorMessage);
                }
            }


        };
    }

    public interface IDcsClientListener {
        void onConnected();

        void onUnconnected();
    }

    /**
     * 添加连接状态listener
     *
     * @param
     */
    public void addConnectStatusListener(IConnectionStatusListener connectionStatusListener) {
        connectStatusListeners.add(connectionStatusListener);
    }

    public void removeConnectStatusListeners(IConnectionStatusListener connectionStatusListener) {
        connectStatusListeners.remove(connectionStatusListener);
    }

    private void fireConnectStatus(final IConnectionStatusListener.ConnectionStatus connectStatus) {
        handlerMain.post(new Runnable() {
            @Override
            public void run() {
                for (IConnectionStatusListener listener : connectStatusListeners) {
                    listener.onConnectStatus(connectStatus);
                }
            }
        });
    }

    private int getRetryTime() {
        return calculateRetryTime.getRetryTime();
    }

    public void resetRetryTime() {
        calculateRetryTime.reset();
    }

    private class ClientParserListener implements MultipartParser.IMultipartParserListener {
        @Override
        public void onResponseBody(DcsResponseBody responseBody) {
            DcsClient.this.dcsResponseDispatcher.onResponseBody(responseBody);
        }

        @Override
        public void onAudioData(AudioData audioData) {
            DcsClient.this.dcsResponseDispatcher.onAudioData(audioData);
        }

        @Override
        public void onParseFailed(String unParseMessage) {
            DcsClient.this.dcsResponseDispatcher.onParseFailed(unParseMessage);
        }

        @Override
        public void onHeartBeat() {
            // heartBeat.receiveHeartbeat();
        }

        @Override
        public void onClose() {

        }
    }

}