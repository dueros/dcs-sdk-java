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

import java.util.Map;

import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * 普通GetRequest
 * <p>
 * Created by guxiuzhong@baidu.com on 2017/5/15.
 */
public class GetRequest extends OkHttpRequest {
    public GetRequest(String url,
                      Object tag,
                      Map<String, String> params,
                      Map<String, String> headers,
                      int id) {
        super(url, tag, params, headers, id);
    }

    @Override
    protected RequestBody buildRequestBody() {
        return null;
    }

    @Override
    protected Request buildRequest(RequestBody requestBody) {
        return builder.get().build();
    }
}