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

import com.baidu.dcs.okhttp3.Call;
import com.baidu.dcs.okhttp3.Response;
import com.baidu.duer.dcs.http.HttpConfig;
import com.baidu.duer.dcs.http.HttpRequestInterface;
import com.baidu.duer.dcs.http.callback.ResponseCallback;
import com.baidu.duer.dcs.util.LogUtil;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import bolts.Continuation;
import bolts.Task;
import bolts.TaskCompletionSource;

/**
 * 用于检测Directives 是否正常
 * <p>
 * Created by guxiuzhong@baidu.com on 2017/6/17.
 */
public class HeartBeat {
    private static final String TAG = HeartBeat.class.getSimpleName();
    // 设置ping成功后的(ping成功即代表directive请求成功了)请求的时间间隔
    // 修改成270s原因：国内网关保持一个连接时间一般都是300s
    private static final long PING_TIME_SUCCEED = 270 * 1000;

    // 设置ping失败后的(ping失败即代表directive请求失败了)请求的时间间隔
    private static final long PING_TIME_FAILED = 5 * 1000;
    private final HttpRequestInterface httpRequest;
    private Timer timer;
    private PingTask pingTask;
    private HeartBeatTimeoutTask heartBeatTimeoutTask;
    private IHeartbeatListener listener;
    // 连接状态
    private ConnectState connectState;

    // 心跳task
    private TaskCompletionSource<Boolean> heartBeatTcs;

    private enum ConnectState {
        CONNECTED,
        UNCONNECTED
    }

    public HeartBeat(HttpRequestInterface httpRequest) {
        this.httpRequest = httpRequest;
        timer = new Timer();
    }

    /**
     * 停止ping探测，释放资源
     */
    public void release() {
        if (heartBeatTcs != null) {
            heartBeatTcs.trySetCancelled();
        }
        httpRequest.cancelRequest(HttpConfig.HTTP_PING_TAG);

        if (pingTask != null) {
            pingTask.cancel();
        }

        if (heartBeatTimeoutTask != null) {
            heartBeatTimeoutTask.cancel();
        }
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

    private void startPing() {
        final ArrayList<Task<Boolean>> tasks = new ArrayList<Task<Boolean>>();
//        tasks.add(createHeartBeatTask());
        tasks.add(createPingTask());
        Task.whenAll(tasks).continueWithTask(new Continuation<Void, Task<Void>>() {
            public Task<Void> then(Task<Void> task) throws Exception {
                if (task.isFaulted()) {
                    connectState = ConnectState.UNCONNECTED;
                    fireOnConnect();
                }

                return task;
            }
        }).onSuccess(new Continuation<Void, Void>() {
            public Void then(Task<Void> task) throws Exception {
                if (connectState == ConnectState.UNCONNECTED) {
                    fireOnConnect();
                }
                return null;
            }
        });
    }

    private void startHeartBeatTimeout() {
        if (heartBeatTimeoutTask != null) {
            heartBeatTimeoutTask.cancel();
        }

        heartBeatTimeoutTask = new HeartBeatTimeoutTask();
        timer.schedule(heartBeatTimeoutTask, 5000L);
    }

    public void receiveHeartbeat() {
        if (heartBeatTcs != null) {
            heartBeatTcs.trySetResult(Boolean.TRUE);
        }
    }

    private Task<Boolean> createHeartBeatTask() {
        startHeartBeatTimeout();
        if (heartBeatTcs != null) {
            heartBeatTcs.trySetCancelled();
        }
        heartBeatTcs = new TaskCompletionSource<>();
        Task<Boolean> heartBeatTask = heartBeatTcs.getTask();

        return heartBeatTask;
    }

    private void cancelHeartbeatTimeoutTask() {
        if (heartBeatTcs != null) {
            heartBeatTcs.trySetError(new Exception());
        }

        if (heartBeatTimeoutTask != null) {
            heartBeatTimeoutTask.cancel();
            heartBeatTimeoutTask = null;
        }
    }

    private Task<Boolean> createPingTask() {
        final TaskCompletionSource<Boolean> tcs = new TaskCompletionSource<>();
        httpRequest.cancelRequest(HttpConfig.HTTP_PING_TAG);
        httpRequest.doGetPingAsync(null, new ResponseCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                super.onError(call, e, id);
                LogUtil.d(TAG, "ping onError");
                tcs.trySetError(new Exception());
            }

            @Override
            public void onResponse(Response response, int id) {
                super.onResponse(response, id);
                LogUtil.d(TAG, "ping onResponse, code :" + response.code());
                if (response.code() == 200 || response.code() == 204) {
                    tcs.trySetResult(Boolean.TRUE);
                } else {
                    tcs.trySetError(new Exception());
                }
            }
        });

        return tcs.getTask();
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
            startPing();
        }
    }

    private final class HeartBeatTimeoutTask extends TimerTask {
        @Override
        public void run() {
            cancelHeartbeatTimeoutTask();
        }
    }
}
