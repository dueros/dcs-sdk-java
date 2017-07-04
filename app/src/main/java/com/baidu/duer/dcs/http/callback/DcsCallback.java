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
package com.baidu.duer.dcs.http.callback;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 网络回调基类
 * <p>
 * Created by zhangyan42@baidu.com on 2017/6/2.
 */
public abstract class DcsCallback<T> {
    /**
     * UI线程
     *
     * @param request 请求
     * @param id      请求id
     */
    public void onBefore(Request request, int id) {
    }

    /**
     * UI线程
     *
     * @param id 请求id
     */
    public void onAfter(int id) {
    }

    public static DcsCallback backDefaultCallBack = new DcsCallback() {

        @Override
        public Object parseNetworkResponse(Response response, int id) throws Exception {
            return null;
        }

        @Override
        public void onError(Call call, Exception e, int id) {

        }

        @Override
        public void onResponse(Object response, int id) {

        }
    };

    public boolean validateResponse(Response response, int id) {
        return response.isSuccessful();
    }

    /**
     * 非UI线程
     *
     * @param response response
     */
    public abstract T parseNetworkResponse(Response response, int id) throws Exception;

    /**
     * 错误处理，UI线程
     *
     * @param call call
     * @param e    e  错误异常信息
     * @param id   id 请求id
     */
    public abstract void onError(Call call, Exception e, int id);

    /**
     * 数据回调，UI线程
     *
     * @param response response
     * @param id       id
     */
    public abstract void onResponse(T response, int id);
}