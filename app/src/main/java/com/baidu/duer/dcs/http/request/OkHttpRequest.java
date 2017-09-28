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

import com.baidu.dcs.okhttp3.Headers;
import com.baidu.dcs.okhttp3.Request;
import com.baidu.dcs.okhttp3.RequestBody;
import com.baidu.duer.dcs.http.callback.DcsCallback;
import com.baidu.duer.dcs.http.exceptions.Exceptions;

import java.util.Map;


/**
 * 请求基类
 * <p>
 * Created by guxiuzhong@baidu.com on 2017/5/15.
 */
public abstract class OkHttpRequest {
    // url
    protected String url;
    // 用于取消请求的tag
    protected Object tag;
    // 请求参数
    protected Map<String, String> params;
    // 请求header
    protected Map<String, String> headers;
    // 请求的标识id
    protected int id;
    // okhttp的请求构建类
    protected Request.Builder builder = new Request.Builder();

    protected OkHttpRequest(String url, Object tag,
                            Map<String, String> params,
                            Map<String, String> headers,
                            int id) {
        this.url = url;
        this.tag = tag;
        this.params = params;
        this.headers = headers;
        this.id = id;
        if (url == null) {
            Exceptions.illegalArgument("url can not be null.");
        }
        initBuilder();
    }

    /**
     * 初始化okhttp的请求builder
     */
    private void initBuilder() {
        builder.url(url).tag(tag);
        appendHeaders();
    }

    /**
     * 拼接请求的Header
     */
    protected void appendHeaders() {
        Headers.Builder headerBuilder = new Headers.Builder();
        if (headers == null || headers.isEmpty()) {
            return;
        }
        for (String key : headers.keySet()) {
            headerBuilder.add(key, headers.get(key));
        }
        builder.headers(headerBuilder.build());
    }

    public int getId() {
        return id;
    }

    public RequestCall build() {
        return new RequestCall(this);
    }

    public Request generateRequest(DcsCallback dcsCallback) {
        RequestBody requestBody = buildRequestBody();
        RequestBody wrappedRequestBody = wrapRequestBody(requestBody, dcsCallback);
        Request request = buildRequest(wrappedRequestBody);
        return request;
    }

    protected RequestBody wrapRequestBody(RequestBody requestBody, final DcsCallback dcsCallback) {
        return requestBody;
    }

    protected abstract RequestBody buildRequestBody();

    protected abstract Request buildRequest(RequestBody requestBody);
}
