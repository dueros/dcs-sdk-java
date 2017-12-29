/*
 * *
 * Copyright (c) 2017 Baidu, Inc. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.baidu.duer.dcs.framework;

/**
 * Created by wuruisheng on 2017/11/23.
 */

public class SimpleResponseListener implements IResponseListener {
    /**
     * 成功回调
     *
     * @param statusCode http返回statusCode
     */
    @Override
    public void onSucceed(int statusCode) {

    }

    /**
     * 失败回调
     *
     * @param errorMessage 出错的异常信息
     */
    @Override
    public void onFailed(String errorMessage) {

    }
}
