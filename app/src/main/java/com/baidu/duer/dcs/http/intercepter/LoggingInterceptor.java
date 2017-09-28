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
package com.baidu.duer.dcs.http.intercepter;

import com.baidu.dcs.okhttp3.Interceptor;
import com.baidu.dcs.okhttp3.Request;
import com.baidu.dcs.okhttp3.Response;
import com.baidu.duer.dcs.util.LogUtil;

import java.io.IOException;
import java.util.Locale;

/**
 * http Interceptor 拦截打印http-log
 * <p>
 * Created by guxiuzhong@baidu.com on 2017/5/25.
 */
public class LoggingInterceptor implements Interceptor {
    private static final String TAG = "HttpLog";

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        long t1 = System.nanoTime();
        LogUtil.d(TAG, String.format("request: %s [%s] %s%n%s",
                request.method(), request.url(), chain.connection(), request.headers()));

        Response response = chain.proceed(request);
        long t2 = System.nanoTime();
        LogUtil.d(TAG, String.format(Locale.CANADA, "response: %d [%s] %.1fms%n%s",
                response.code(), response.request().url(), (t2 - t1) / 1e6d, response.headers()));
        return response;
    }
}