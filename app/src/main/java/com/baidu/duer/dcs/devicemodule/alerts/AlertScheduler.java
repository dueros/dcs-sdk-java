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
package com.baidu.duer.dcs.devicemodule.alerts;

import com.baidu.duer.dcs.devicemodule.alerts.message.Alert;
import com.baidu.duer.dcs.util.DateFormatterUtil;
import com.baidu.duer.dcs.util.LogUtil;

import java.text.ParseException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 单个的闹铃/提醒计时器
 * <p>
 * Created by guxiuzhong@baidu.com on 2017/6/5.
 */
public class AlertScheduler extends Timer {
    private static final String TAG = "AlertScheduler";
    private final Alert alert;
    private final AlertHandler handler;
    private boolean active = false;

    public AlertScheduler(final Alert alert, final AlertHandler handler) {
        super();
        String scheduledTimeStr = alert.getScheduledTime();
        if (scheduledTimeStr != null && scheduledTimeStr.length() > 0) {
            try {
                Date date = DateFormatterUtil.toDate(alert.getScheduledTime());
                long scheduledTime = date.getTime();
                long delay = scheduledTime - System.currentTimeMillis();
                LogUtil.d(TAG, "alert-delay start:" + delay);
                if (delay > 0) {
                    this.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            // 时间到了，开始闹铃
                            setActive(true);
                            handler.startAlert(alert.getToken());
                        }
                    }, delay);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
        this.alert = alert;
        this.handler = handler;
    }

    public synchronized boolean isActive() {
        return active;
    }

    public synchronized void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public void cancel() {
        super.cancel();
        if (isActive()) {
            handler.stopAlert(alert.getToken());
            setActive(false);
        }
    }

    public Alert getAlert() {
        return alert;
    }
}
