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

import java.util.Timer;
import java.util.TimerTask;

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

    public HeartBeat(HttpRequestInterface httpRequest) {
        this.httpRequest = httpRequest;
        timer = new Timer();
    }

    /**
     * 停止ping探测，释放资源
     */
    public void release() {
        stop();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void start() {
        startPing(PING_TIME_SUCCEED, PING_TIME_SUCCEED);
    }

    public void stop() {
        if (pingTask != null) {
            pingTask.cancel();
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
        httpRequest.cancelRequest(HttpConfig.HTTP_PING_TAG);
        httpRequest.doGetPingAsync(null, new ResponseCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                super.onError(call, e, id);
            }

            @Override
            public Response parseNetworkResponse(Response response, int id) throws Exception {
                return super.parseNetworkResponse(response, id);
            }
        });
    }

    private final class PingTask extends TimerTask {
        @Override
        public void run() {
            startPing();
        }
    }
}
