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

import com.baidu.dcs.okhttp3.MediaType;
import com.baidu.dcs.okhttp3.Request;
import com.baidu.dcs.okhttp3.RequestBody;
import com.baidu.duer.dcs.http.exceptions.Exceptions;

import java.util.Map;


/**
 * 用于发送字符串请求
 * <p>
 * Created by guxiuzhong@baidu.com on 2017/5/16.
 */
public class PostStringRequest extends OkHttpRequest {
    private static MediaType MEDIA_TYPE_PLAIN = MediaType.parse("text/plain;charset=utf-8");
    private String content;
    private MediaType mediaType;

    public PostStringRequest(String url,
                             Object tag,
                             Map<String, String> params,
                             Map<String, String> headers,
                             String content,
                             MediaType mediaType,
                             int id) {
        super(url, tag, params, headers, id);
        this.content = content;
        this.mediaType = mediaType;
        if (this.content == null) {
            Exceptions.illegalArgument("the content can not be null !");
        }
        if (this.mediaType == null) {
            this.mediaType = MEDIA_TYPE_PLAIN;
        }
    }

    @Override
    protected RequestBody buildRequestBody() {
        return RequestBody.create(mediaType, content);
    }

    @Override
    protected Request buildRequest(RequestBody requestBody) {
        return builder.post(requestBody).build();
    }
}