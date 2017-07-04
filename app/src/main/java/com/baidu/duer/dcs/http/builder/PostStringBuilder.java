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

import com.baidu.duer.dcs.http.request.PostStringRequest;
import com.baidu.duer.dcs.http.request.RequestCall;

import okhttp3.MediaType;

/**
 * post请求的构建类
 * <p>
 * Created by guxiuzhong@baidu.com on 2017/5/16.
 */
public class PostStringBuilder extends OkHttpRequestBuilder<PostStringBuilder> {
    // 内容
    private String content;
    // 类型
    private MediaType mediaType;

    public PostStringBuilder content(String content) {
        this.content = content;
        return this;
    }

    public PostStringBuilder mediaType(MediaType mediaType) {
        this.mediaType = mediaType;
        return this;
    }

    @Override
    public RequestCall build() {
        return new PostStringRequest(url, tag, params, headers, content, mediaType, id).build();
    }
}