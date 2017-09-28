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

import com.baidu.dcs.okhttp3.RequestBody;
import com.baidu.duer.dcs.http.request.PostMultipartRequest;
import com.baidu.duer.dcs.http.request.RequestCall;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;


/**
 * post请求多part构建
 * <p>
 * Created by guxiuzhong@baidu.com on 2017/5/15.
 */
public class PostMultipartBuilder extends OkHttpRequestBuilder<PostMultipartBuilder>
        implements HasParamInterface {
    private LinkedList<Multipart> multiParts = new LinkedList<>();

    @Override
    public RequestCall build() {
        return new PostMultipartRequest(url, tag, params, headers, multiParts, id).build();
    }

    @Override
    public PostMultipartBuilder params(Map<String, String> params) {
        this.params = params;
        return this;
    }

    @Override
    public PostMultipartBuilder addParams(String key, String val) {
        if (this.params == null) {
            params = new LinkedHashMap<>();
        }
        params.put(key, val);
        return this;
    }

    /**
     * 添加多个Multipart
     *
     * @param map map集合
     * @return 当前对象
     */
    public PostMultipartBuilder multiParts(Map<String, RequestBody> map) {
        if (map != null) {
            multiParts = new LinkedList<>();
        }
        for (String k : map.keySet()) {
            this.multiParts.add(new Multipart(k, map.get(k)));
        }
        return this;
    }

    /**
     * 添加一个Multipart
     *
     * @param name name
     * @param body body
     * @return 当前对象
     */
    public PostMultipartBuilder addMultiPart(String name, RequestBody body) {
        multiParts.add(new Multipart(name, body));
        return this;
    }

    /**
     * 请求体body-Multipart
     */
    public static final class Multipart implements Serializable {
        // body的key
        public String key;
        // body的内容
        public RequestBody requestBody;

        public Multipart(String name, RequestBody requestBody) {
            this.key = name;
            this.requestBody = requestBody;
        }
    }
}