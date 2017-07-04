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
package com.baidu.duer.dcs.systeminterface;

import com.baidu.duer.dcs.devicemodule.alerts.message.Alert;

import java.util.List;

/**
 * alert存储的回调接口
 *
 * Created by guxiuzhong@baidu.com on 2017/5/18.
 */
public interface IAlertsDataStore {

    void readFromDisk(ReadResultListener listener);

    void writeToDisk(List<Alert> alerts, WriteResultListener listener);

    /**
     * 读取回调
     */
    interface ReadResultListener {
        void onSucceed(List<Alert> alerts);

        void onFailed(String errMsg);
    }

    /**
     * 写入回调
     */
    interface WriteResultListener {
        void onSucceed();

        void onFailed(String errMsg);
    }
}