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

import com.baidu.duer.dcs.framework.message.DcsRequestBody;
import com.baidu.duer.dcs.framework.message.DcsStreamRequestBody;
import com.baidu.duer.dcs.http.callback.DcsCallback;
import com.baidu.duer.dcs.util.LogUtil;
import com.baidu.duer.dcs.util.ObjectMapperUtil;

import java.util.LinkedHashMap;
import java.util.Map;

import okhttp3.RequestBody;

/**
 * 请求实现
 * <p>
 * Created by zhangyan42@baidu.com on 2017/6/2.
 */
public class OkHttpRequestImpl implements HttpRequestInterface {
    private static final String TAG = "OkHttpRequestImpl";
    private final DcsHttpManager dcsHttpManager;

    public OkHttpRequestImpl() {
        dcsHttpManager = DcsHttpManager.getInstance();
    }

    @Override
    public void doPostEventStringAsync(DcsRequestBody requestBody, DcsCallback dcsCallback) {
        String bodyJson = ObjectMapperUtil.instance().objectToJson(requestBody);
        LogUtil.d(TAG, "doPostEventStringAsync-bodyJson:" + bodyJson);
        Map<String, RequestBody> multiParts = new LinkedHashMap<>();
        multiParts.put(HttpConfig.Parameters.DATA_METADATA,
                RequestBody.create(OkHttpMediaType.MEDIA_JSON_TYPE, bodyJson));
        DcsHttpManager.post()
                .url(HttpConfig.getEventsUrl())
                .headers(HttpConfig.getDCSHeaders())
                .multiParts(multiParts)
                .tag(HttpConfig.HTTP_EVENT_TAG)
                .build()
                .execute(dcsCallback);
    }

    @Override
    public void doPostEventMultipartAsync(DcsRequestBody requestBody,
                                          DcsStreamRequestBody streamRequestBody,
                                          DcsCallback dcsCallback) {
        String bodyJson = ObjectMapperUtil.instance().objectToJson(requestBody);
        LogUtil.d(TAG, "doPostEventMultipartAsync-bodyJson:" + bodyJson);
        Map<String, RequestBody> multiParts = new LinkedHashMap<>();
        multiParts.put(HttpConfig.Parameters.DATA_METADATA,
                RequestBody.create(OkHttpMediaType.MEDIA_JSON_TYPE, bodyJson));
        multiParts.put(HttpConfig.Parameters.DATA_AUDIO, streamRequestBody);
        DcsHttpManager.post()
                .url(HttpConfig.getEventsUrl())
                .headers(HttpConfig.getDCSHeaders())
                .multiParts(multiParts)
                .tag(HttpConfig.HTTP_EVENT_TAG)
                .build()
                .execute(dcsCallback);
    }

    @Override
    public void doGetDirectivesAsync(DcsRequestBody requestBody, DcsCallback dcsCallback) {
        LogUtil.d(TAG, "doGetDirectivesAsync");
        final long time = 60 * 60 * 1000L;
        DcsHttpManager.get()
                .url(HttpConfig.getDirectivesUrl())
                .headers(HttpConfig.getDCSHeaders())
                .tag(HttpConfig.HTTP_DIRECTIVES_TAG)
                .build()
                .connTimeOut(time)
                .readTimeOut(time)
                .writeTimeOut(time)
                .execute(dcsCallback);
    }

    @Override
    public void doGetPingAsync(DcsRequestBody requestBody, DcsCallback dcsCallback) {
        LogUtil.d(TAG, "doGetPingAsync");
        DcsHttpManager.get()
                .url(HttpConfig.getPingUrl())
                .headers(HttpConfig.getDCSHeaders())
                .tag(HttpConfig.HTTP_PING_TAG)
                .build()
                .execute(dcsCallback);
    }

    @Override
    public void cancelRequest(Object requestTag) {
        dcsHttpManager.cancelTag(requestTag);
    }
}