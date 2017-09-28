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
package com.baidu.duer.dcs.http;

import com.baidu.dcs.okhttp3.Call;
import com.baidu.dcs.okhttp3.Callback;
import com.baidu.dcs.okhttp3.OkHttpClient;
import com.baidu.dcs.okhttp3.Response;
import com.baidu.duer.dcs.http.builder.GetBuilder;
import com.baidu.duer.dcs.http.builder.PostMultipartBuilder;
import com.baidu.duer.dcs.http.builder.PostStringBuilder;
import com.baidu.duer.dcs.http.callback.DcsCallback;
import com.baidu.duer.dcs.http.intercepter.LoggingInterceptor;
import com.baidu.duer.dcs.http.request.RequestCall;
import com.baidu.duer.dcs.http.utils.Platform;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 网络请求-单例
 * <p>
 * Created by zhangyan@baidu.com on 2017/5/15.
 */
public class DcsHttpManager {
    // 默认时间，比如超时时间
    public static final long DEFAULT_MILLISECONDS = 60 * 1000L;
    private OkHttpClient mOkHttpClient;
    private Platform mPlatform;
    private volatile static DcsHttpManager mInstance;

    public static DcsHttpManager getInstance() {
        return initClient(null);
    }

    public DcsHttpManager(OkHttpClient okHttpClient) {
        if (mOkHttpClient == null) {
            // http数据log，日志中打印出HTTP请求&响应数据
            // HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            // 包含header、body数据
            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .retryOnConnectionFailure(false)
                    .readTimeout(DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS)
                    .writeTimeout(DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS)
                    .connectTimeout(DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS)
                    // .addInterceptor(new RetryInterceptor(3))
                    .addInterceptor(new LoggingInterceptor());
            mOkHttpClient = builder.build();
        } else {
            mOkHttpClient = okHttpClient;
        }
        mPlatform = Platform.get();
    }

    public static DcsHttpManager initClient(OkHttpClient okHttpClient) {
        if (mInstance == null) {
            synchronized (DcsHttpManager.class) {
                if (mInstance == null) {
                    mInstance = new DcsHttpManager(okHttpClient);
                }
            }
        }
        return mInstance;
    }

    public OkHttpClient getOkHttpClient() {
        return mOkHttpClient;
    }

    public static GetBuilder get() {
        return new GetBuilder();
    }

    public static PostMultipartBuilder post() {
        return new PostMultipartBuilder();
    }

    public static PostStringBuilder postString() {
        return new PostStringBuilder();
    }

    public void execute(final RequestCall requestCall, DcsCallback dcsCallback) {

        if (dcsCallback == null) {
            dcsCallback = DcsCallback.backDefaultCallBack;
        }
        final DcsCallback finalDCSCallback = dcsCallback;
        final int id = requestCall.getOkHttpRequest().getId();

        requestCall.getCall().enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                sendFailResultCallback(call, e, finalDCSCallback, id);
            }

            @Override
            public void onResponse(final Call call, final Response response) {
                try {
                    if (call.isCanceled()) {
                        sendFailResultCallback(call, new IOException("Canceled!"), finalDCSCallback, id);
                        return;
                    }
                    if (!finalDCSCallback.validateResponse(response, id)) {
                        IOException exception = new IOException("request failed , response's code is : "
                                + response.code());
                        sendFailResultCallback(call, exception, finalDCSCallback, id);
                        return;
                    }
                    sendSuccessResultCallback(response, finalDCSCallback, id);
                    Object o = finalDCSCallback.parseNetworkResponse(response, id);
                } catch (Exception e) {
                    sendFailResultCallback(call, e, finalDCSCallback, id);
                } finally {
                    if (response.body() != null) {
                        response.body().close();
                    }
                }
            }
        });
    }

    /**
     * 网络请求失败处理
     *
     * @param call        okhttp的请求call
     * @param e           异常信息
     * @param dcsCallback 回调
     * @param id          请求标识
     */
    private void sendFailResultCallback(final Call call,
                                        final Exception e,
                                        final DcsCallback dcsCallback,
                                        final int id) {
        if (dcsCallback == null) {
            return;
        }
        mPlatform.execute(new Runnable() {
            @Override
            public void run() {
                dcsCallback.onError(call, e, id);
                dcsCallback.onAfter(id);
            }
        });
    }

    /**
     * 网络请求成功处理
     *
     * @param object      object 返回的对象
     * @param dcsCallback dcsCallback 回调
     * @param id          id 请求标识
     */
    private void sendSuccessResultCallback(final Object object,
                                           final DcsCallback dcsCallback,
                                           final int id) {
        if (dcsCallback == null) {
            return;
        }
        mPlatform.execute(new Runnable() {
            @Override
            public void run() {
                dcsCallback.onResponse(object, id);
                dcsCallback.onAfter(id);
            }
        });
    }

    /**
     * 取消请求
     *
     * @param tag 请求的时候设置的tag
     */
    public void cancelTag(Object tag) {
        for (Call call : mOkHttpClient.dispatcher().queuedCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
        for (Call call : mOkHttpClient.dispatcher().runningCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
    }
}