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
package com.baidu.duer.dcs.http.request;

import com.baidu.duer.dcs.http.DcsHttpManager;
import com.baidu.duer.dcs.http.callback.DcsCallback;

import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * 请求设置
 * <p>
 * Created by guxiuzhong@baidu.com on 2017/5/15.
 */
public class RequestCall {
    private OkHttpRequest okHttpRequest;
    private Request request;
    private Call call;
    private OkHttpClient clone;
    // 读取超时时间
    private long readTimeOut;
    // 写入数据超时时间
    private long writeTimeOut;
    // 连接超时时间
    private long connTimeOut;

    public RequestCall(OkHttpRequest request) {
        this.okHttpRequest = request;
    }

    public RequestCall readTimeOut(long readTimeOut) {
        this.readTimeOut = readTimeOut;
        return this;
    }

    public RequestCall writeTimeOut(long writeTimeOut) {
        this.writeTimeOut = writeTimeOut;
        return this;
    }

    public RequestCall connTimeOut(long connTimeOut) {
        this.connTimeOut = connTimeOut;
        return this;
    }

    /**
     * 构建请求
     *
     * @param dcsCallback 回调
     * @return Call
     */
    public Call buildCall(DcsCallback dcsCallback) {
        request = generateRequest(dcsCallback);
        if (readTimeOut > 0 || writeTimeOut > 0 || connTimeOut > 0) {
            readTimeOut = readTimeOut > 0 ? readTimeOut : DcsHttpManager.DEFAULT_MILLISECONDS;
            writeTimeOut = writeTimeOut > 0 ? writeTimeOut : DcsHttpManager.DEFAULT_MILLISECONDS;
            connTimeOut = connTimeOut > 0 ? connTimeOut : DcsHttpManager.DEFAULT_MILLISECONDS;
            clone = DcsHttpManager.getInstance().getOkHttpClient().newBuilder()
                    .readTimeout(readTimeOut, TimeUnit.MILLISECONDS)
                    .writeTimeout(writeTimeOut, TimeUnit.MILLISECONDS)
                    .connectTimeout(connTimeOut, TimeUnit.MILLISECONDS)
                    .build();

            call = clone.newCall(request);
        } else {
            call = DcsHttpManager.getInstance().getOkHttpClient().newCall(request);
        }
        return call;
    }

    private Request generateRequest(DcsCallback dcsCallback) {
        return okHttpRequest.generateRequest(dcsCallback);
    }

    public void execute(DcsCallback dcsCallback) {
        buildCall(dcsCallback);
        if (dcsCallback != null) {
            dcsCallback.onBefore(request, getOkHttpRequest().getId());
        }
        DcsHttpManager.getInstance().execute(this, dcsCallback);
    }

    public OkHttpRequest getOkHttpRequest() {
        return okHttpRequest;
    }

    public Call getCall() {
        return call;
    }
}