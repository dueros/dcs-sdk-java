/*
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
package com.baidu.duer.dcs.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

/**
 * 监听网络变化
 * <p>
 * Created by guxiuzhong@baidu.com on 2017/8/31.
 */
public class NetWorkStateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            int netWorkState = NetWorkUtil.getNetWorkType(context);
            if (listener != null) {
                listener.onNetWorkStateChange(netWorkState);
            }
        }
    }


    private INetWorkStateListener listener;

    public interface INetWorkStateListener {
        void onNetWorkStateChange(int netType);
    }

    public void setOnNetWorkStateListener(INetWorkStateListener listener) {
        this.listener = listener;
    }
}