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
package com.baidu.duer.dcs.framework.heartbeat;

import com.baidu.duer.dcs.http.HttpConfig;
import com.baidu.duer.dcs.http.HttpRequestInterface;
import com.baidu.duer.dcs.http.callback.ResponseCallback;
import com.baidu.duer.dcs.util.LogUtil;

import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Response;

/**
 * 用于检测Directives 是否正常
 * <p>
 * Created by guxiuzhong@baidu.com on 2017/6/17.
 */
public class HeartBeat {
    private static final String TAG = HeartBeat.class.getSimpleName();
    // 设置ping成功后的(ping成功即代表directive请求成功了)请求的时间间隔
    // 修改成270s原因：国内网关保持一个连接时间一般都是300s
    private static final long PING_TIME_SUCCEED = 30 * 1000;
    // 设置ping失败后的(ping失败即代表directive请求失败了)请求的时间间隔
    private static final long PING_TIME_FAILED = 5 * 1000;
    private final HttpRequestInterface httpRequest;
    private Timer timer;
    private PingTask pingTask;
    private IHeartbeatListener listener;
    // 连接状态
    private ConnectState connectState;

    private enum ConnectState {
        CONNECTED,
        UNCONNECTED
    }

    public HeartBeat(HttpRequestInterface httpRequest) {
        this.httpRequest = httpRequest;
        timer = new Timer();
    }

    /**
     * 如果Directives连接成功了，调用此方法进行探测
     */
    public void startNormalPing() {
        connectState = ConnectState.CONNECTED;
        startPing(PING_TIME_SUCCEED, PING_TIME_SUCCEED);
    }

    /**
     * 如果Directives连接异常了，调用此方法进行探测
     */
    public void startExceptionalPing() {
        connectState = ConnectState.UNCONNECTED;
        startPing(PING_TIME_FAILED, PING_TIME_FAILED);
    }

    /**
     * 如果Events连接异常了，调用此方法进行探测
     */
    public void startImmediatePing() {
        connectState = ConnectState.UNCONNECTED;
        startPing(0, PING_TIME_FAILED);
    }

    private void fireOnConnect() {
        if (listener != null) {
            listener.onStartConnect();
        }
    }

    private void startPing(long delay, long timeInterval) {
        if (pingTask != null) {
            pingTask.cancel();
        }
        pingTask = new PingTask();
        if (timer != null) {
            timer.schedule(pingTask, delay, timeInterval);
        }
    }

    /**
     * 停止ping探测，释放资源
     */
    public void release() {
        if (pingTask != null) {
            pingTask.cancel();
            pingTask = null;
        }

        timer.cancel();
        timer = null;
        httpRequest.cancelRequest(HttpConfig.HTTP_PING_TAG);
    }

    private void ping() {
        httpRequest.cancelRequest(HttpConfig.HTTP_PING_TAG);
        httpRequest.doGetPingAsync(null, new ResponseCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                super.onError(call, e, id);
                LogUtil.d(TAG, "ping onError");
                connectState = ConnectState.UNCONNECTED;
                fireOnConnect();
            }

            @Override
            public void onResponse(Response response, int id) {
                super.onResponse(response, id);
                LogUtil.d(TAG, "ping onResponse, code :" + response.code());
                if (response.code() == 200 || response.code() == 204) {
                    if (connectState == ConnectState.UNCONNECTED) {
                        fireOnConnect();
                    }
                } else {
                    connectState = ConnectState.UNCONNECTED;
                    fireOnConnect();
                }
            }
        });
    }

    public void setHeartbeatListener(IHeartbeatListener listener) {
        this.listener = listener;
    }

    /**
     * ping 回调接口
     */
    public interface IHeartbeatListener {
        /**
         * 需要建立长连接时回调
         */
        void onStartConnect();
    }

    private final class PingTask extends TimerTask {
        @Override
        public void run() {
            ping();
        }
    }
}