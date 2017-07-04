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
package com.baidu.duer.dcs.http.builder;

import com.baidu.duer.dcs.http.request.RequestCall;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 请求构建基类
 * <p>
 * Created by guxiuzhong@baidu.com on 2017/5/15.
 */
public abstract class OkHttpRequestBuilder<T extends OkHttpRequestBuilder> {
    // 请求url
    protected String url;
    // 请求tag
    protected Object tag;
    // 请求的headers
    protected Map<String, String> headers;
    // 请求的参数集合
    protected Map<String, String> params;
    // 请求的表识id
    protected int id;

    public T id(int id) {
        this.id = id;
        return (T) this;
    }

    public T url(String url) {
        this.url = url;
        return (T) this;
    }


    public T tag(Object tag) {
        this.tag = tag;
        return (T) this;
    }

    public T headers(Map<String, String> headers) {
        this.headers = headers;
        return (T) this;
    }

    public T addHeader(String key, String val) {
        if (this.headers == null) {
            headers = new LinkedHashMap<>();
        }
        headers.put(key, val);
        return (T) this;
    }

    /**
     * 构建请求
     *
     * @return RequestCall
     */
    public abstract RequestCall build();
}