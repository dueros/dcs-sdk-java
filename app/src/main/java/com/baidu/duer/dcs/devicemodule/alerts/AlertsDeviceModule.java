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
import com.baidu.duer.dcs.devicemodule.alerts.message.AlertPayload;
import com.baidu.duer.dcs.devicemodule.alerts.message.AlertsStatePayload;
import com.baidu.duer.dcs.devicemodule.alerts.message.DeleteAlertPayload;
import com.baidu.duer.dcs.devicemodule.alerts.message.SetAlertPayload;
import com.baidu.duer.dcs.devicemodule.system.HandleDirectiveException;
import com.baidu.duer.dcs.framework.BaseDeviceModule;
import com.baidu.duer.dcs.framework.IMessageSender;
import com.baidu.duer.dcs.framework.message.ClientContext;
import com.baidu.duer.dcs.framework.message.Directive;
import com.baidu.duer.dcs.framework.message.Event;
import com.baidu.duer.dcs.framework.message.Header;
import com.baidu.duer.dcs.framework.message.MessageIdHeader;
import com.baidu.duer.dcs.framework.message.Payload;
import com.baidu.duer.dcs.systeminterface.IAlertsDataStore;
import com.baidu.duer.dcs.systeminterface.IHandler;
import com.baidu.duer.dcs.systeminterface.IMediaPlayer;
import com.baidu.duer.dcs.util.CommonUtil;
import com.baidu.duer.dcs.util.DateFormatterUtil;
import com.baidu.duer.dcs.util.LogUtil;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Alerts模块的核心处理逻辑，如执行SetAlert、DeleteAlert指令，上传SetAlertSucceeded、DeleteAlertSucceeded等事件，
 * 以及维护自身的端状态
 * <p>
 * Created by guxiuzhong@baidu.com on 2017/5/31.
 */
public class AlertsDeviceModule extends BaseDeviceModule implements AlertHandler {
    private static final String TAG = AlertsDeviceModule.class.getSimpleName();
    // 闹铃开始是播放的音频文件
    private static final String ALARM_NAME = "assets://alarm.mp3";
    // 已经离alert时间点超过30分钟了
    private static final int MINUTES_AFTER_PAST_ALERT_EXPIRES = 30;
    // 当前的闹钟/提醒
    private final Map<String, AlertScheduler> schedulers;
    // 当前处于播放的
    private final Set<String> activeAlerts;
    private final IAlertsDataStore dataStore;
    // 播放闹铃/提醒的mediaPlayer
    private IMediaPlayer mediaPlayer;
    private IHandler handler;
    private List<IAlertListener> alertListeners;

    // 提醒的状态
    private enum AlertState {
        PLAYING,
        INTERRUPTED,
        FINISHED
    }

    private AlertState alertState = AlertState.FINISHED;

    public AlertsDeviceModule(IMediaPlayer mediaPlayer, IAlertsDataStore dataStore,
                              IMessageSender messageSender, IHandler handler) {
        super(ApiConstants.NAMESPACE, messageSender);
        this.schedulers = new ConcurrentHashMap<>();
        this.activeAlerts = new HashSet<>();
        this.mediaPlayer = mediaPlayer;
        this.dataStore = dataStore;
        this.handler = handler;
        this.mediaPlayer.addMediaPlayerListener(mediaPlayerListener);
        this.alertListeners = Collections.synchronizedList(new ArrayList<IAlertListener>());
        // 先读取历史保存到文件中的提醒
        this.loadFromDisk();
    }

    @Override
    public ClientContext clientContext() {
        String namespace = ApiConstants.NAMESPACE;
        String name = ApiConstants.Events.AlertsState.NAME;
        Header header = new Header(namespace, name);
        Payload payload = getState();
        return new ClientContext(header, payload);
    }

    @Override
    public void handleDirective(Directive directive) throws HandleDirectiveException {
        String directiveName = directive.getName();
        if (directiveName.equals(ApiConstants.Directives.SetAlert.NAME)) {
            // 设置一个闹铃／提醒的指令处理逻辑
            LogUtil.d(TAG, "alert-SetAlertPayload");
            SetAlertPayload payload = (SetAlertPayload) directive.payload;

            String alertToken = payload.getToken();
            String scheduledTime = payload.getScheduledTime();
            LogUtil.d(TAG, "alert-scheduledTime:" + scheduledTime);

            // scheduledTime 时间为ISO8601格式转换为Date
            try {
                Date date = DateFormatterUtil.toDate(scheduledTime);
                LogUtil.d(TAG, "alert-ms:" + date.getTime());
                LogUtil.d(TAG, "alert-format:" + CommonUtil.formatToDataTime(date.getTime()));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            SetAlertPayload.AlertType type = payload.getType();
            // 如果存在一样的闹铃／提醒 就cancel掉
            if (hasAlert(alertToken)) {
                AlertScheduler scheduler = getScheduler(alertToken);
                if (scheduler.getAlert().getScheduledTime().equals(scheduledTime)) {
                    return;
                } else {
                    scheduler.cancel();
                }
            }
            // 设置一个闹铃／提醒
            Alert alert = new Alert(alertToken, type, scheduledTime);
            add(alert, false);
        } else if (ApiConstants.Directives.DeleteAlert.NAME.equals(directiveName)) {
            // 删除一个闹铃／提醒的指令处理
            LogUtil.d(TAG, "alert-DeleteAlertPayload");
            DeleteAlertPayload payload = (DeleteAlertPayload) directive.getPayload();
            delete(payload.getToken());
        } else {
            String message = "Alert cannot handle the directive";
            throw (new HandleDirectiveException(
                    HandleDirectiveException.ExceptionType.UNSUPPORTED_OPERATION, message));
        }
    }

    @Override
    public void startAlert(final String alertToken) {
        LogUtil.d(TAG, "alert-startAlert");
        handler.post(new Runnable() {
            @Override
            public void run() {
                activeAlerts.add(alertToken);
                sendAlertsRequest(ApiConstants.Events.AlertStarted.NAME, alertToken);
                fireOnAlertStarted(alertToken);
                if (!isAlarming()) {
                    alertState = AlertState.PLAYING;
                    if (mediaPlayer != null) {
                        mediaPlayer.play(new IMediaPlayer.MediaResource(ALARM_NAME));
                    }
                }
            }
        });
    }

    @Override
    public void stopAlert(final String alertToken) {
        LogUtil.d(TAG, "alert-stopAlert");
        handler.post(new Runnable() {
            @Override
            public void run() {
                activeAlerts.remove(alertToken);
                schedulers.remove(alertToken);
                alertStopped(alertToken);
                if (!hasActiveAlerts()) {
                    alertState = AlertState.FINISHED;
                    if (mediaPlayer != null) {
                        mediaPlayer.pause();
                    }
                }
            }
        });
    }

    private void sendAlertEnteredBackgroundEvent(String alertToken) {
        sendAlertsRequest(ApiConstants.Events.AlertEnteredBackground.NAME, alertToken);
    }

    private void sendAlertEnteredBackgroundEvent() {
        if (hasActiveAlerts()) {
            for (String alertToken : getActiveAlerts()) {
                sendAlertsRequest(ApiConstants.Events.AlertEnteredBackground.NAME, alertToken);
            }
        }
    }

    private void sendAlertEnteredForegroundEvent(String alertToken) {
        sendAlertsRequest(ApiConstants.Events.AlertEnteredForeground.NAME, alertToken);
    }

    public void sendAlertEnteredForegroundEvent() {
        if (hasActiveAlerts()) {
            for (String alertToken : getActiveAlerts()) {
                sendAlertsRequest(ApiConstants.Events.AlertEnteredForeground.NAME, alertToken);
            }
        }
    }

    public void sendAlertStartedEvent(boolean isSpeaking, String alertToken) {
        if (isSpeaking) {
            sendAlertEnteredBackgroundEvent(alertToken);
        } else {
            sendAlertEnteredForegroundEvent(alertToken);
        }
    }

    private void sendAlertsRequest(String eventName, String alertToken) {
        Header header = new MessageIdHeader(ApiConstants.NAMESPACE, eventName);
        Payload payload = new AlertPayload(alertToken);
        Event event = new Event(header, payload);
        messageSender.sendEvent(event);
    }

    /**
     * 设置提醒/闹钟成功或者失败的上报
     *
     * @param alertToken alertToken
     * @param success    success
     */
    private void setAlert(String alertToken, boolean success) {
        String eventName = success ? ApiConstants.Events.SetAlertSucceeded.NAME :
                ApiConstants.Events.SetAlertFailed.NAME;
        sendAlertsRequest(eventName, alertToken);
    }

    /**
     * 删除提醒/闹钟成功或者失败的上报
     *
     * @param alertToken alertToken
     * @param success    success
     */
    private void deleteAlert(String alertToken, boolean success) {
        String eventName = success ? ApiConstants.Events.DeleteAlertSucceeded.NAME :
                ApiConstants.Events.DeleteAlertFailed.NAME;
        sendAlertsRequest(eventName, alertToken);
    }

    /**
     * 到了定点时间，触发了提醒/闹钟 时上报
     *
     * @param alertToken alertToken
     */
    private void fireOnAlertStarted(String alertToken) {
        for (IAlertListener listener : alertListeners) {
            listener.onAlertStarted(alertToken);
        }
    }

    /**
     * 上报AlertStopped事件
     *
     * @param alertToken alertToken 闹钟／提醒 唯一的标识
     */
    private void alertStopped(String alertToken) {
        sendAlertsRequest(ApiConstants.Events.AlertStopped.NAME, alertToken);
    }

    /**
     * 从文件里读取之前设置的闹铃/提醒
     */
    private void loadFromDisk() {
        dataStore.readFromDisk(new IAlertsDataStore.ReadResultListener() {

            @Override
            public void onSucceed(List<Alert> alerts) {
                if (alerts == null || alerts.size() <= 0) {
                    return;
                }
                List<Alert> droppedAlerts = new LinkedList<>();
                for (final Alert alert : alerts) {
                    String scheduledTime = alert.getScheduledTime();
                    if (scheduledTime != null && scheduledTime.length() > 0) {
                        try {
                            Date date = DateFormatterUtil.toDate(alert.getScheduledTime());
                            long scheduledTimeLong = date.getTime();
                            // 已经离alert时间点超过30分钟了
                            long cur = System.currentTimeMillis();
                            if (scheduledTimeLong + MINUTES_AFTER_PAST_ALERT_EXPIRES * 60 * 1000 < cur) {
                                droppedAlerts.add(alert);
                            } else {
                                add(alert, true);
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
                for (Alert alert : droppedAlerts) {
                    drop(alert);
                }
            }

            @Override
            public void onFailed(String errMsg) {

            }
        });
    }

    public boolean isAlarming() {
        return alertState == AlertState.PLAYING;
    }

    private synchronized boolean hasAlert(String alertToken) {
        return schedulers.containsKey(alertToken);
    }

    public synchronized boolean hasActiveAlerts() {
        return activeAlerts != null && activeAlerts.size() > 0;
    }

    private synchronized Set<String> getActiveAlerts() {
        return activeAlerts;
    }

    private synchronized AlertScheduler getScheduler(String alertToken) {
        return schedulers.get(alertToken);
    }

    /**
     * 设置一个闹钟／提醒
     *
     * @param alert         alert
     * @param suppressEvent suppressEvent 是否需要上报事件
     */
    private synchronized void add(final Alert alert, final boolean suppressEvent) {
        LogUtil.d(TAG, "add alertToken: " + alert.getToken());
        final AlertScheduler scheduler = new AlertScheduler(alert, this);
        schedulers.put(alert.getToken(), scheduler);
        dataStore.writeToDisk(getAllAlerts(), new IAlertsDataStore.WriteResultListener() {

            @Override
            public void onSucceed() {
                if (!suppressEvent) {
                    setAlert(alert.getToken(), true);
                }
            }

            @Override
            public void onFailed(String errMsg) {
                if (!suppressEvent) {
                    setAlert(alert.getToken(), false);
                }
                schedulers.remove(alert.getToken());
                scheduler.cancel();
            }
        });
    }

    /**
     * 删除一个闹钟
     *
     * @param alertToken alertToken
     */
    private synchronized void delete(final String alertToken) {
        LogUtil.d(TAG, "delete alertToken: " + alertToken);
        final AlertScheduler scheduler = schedulers.remove(alertToken);
        if (scheduler != null) {
            final Alert alert = scheduler.getAlert();
            dataStore.writeToDisk(getAllAlerts(), new IAlertsDataStore.WriteResultListener() {
                @Override
                public void onSucceed() {
                    LogUtil.d(TAG, "delete  onSucceed");
                    scheduler.cancel();
                    deleteAlert(alert.getToken(), true);
                }

                @Override
                public void onFailed(String errMsg) {
                    LogUtil.d(TAG, "delete  onFailed");
                    deleteAlert(alert.getToken(), false);
                }
            });
        } else {
            //  本地没有查询到就上报删除失败的事件
            LogUtil.d(TAG, "delete  scheduler is  null");
            deleteAlert(alertToken, false);
        }
    }

    private synchronized List<Alert> getAllAlerts() {
        List<Alert> list = new ArrayList<>(schedulers.size());
        for (AlertScheduler scheduler : schedulers.values()) {
            list.add(scheduler.getAlert());
        }
        return list;
    }

    /**
     * 如果有正在播放的闹铃/提醒，就停止播放并删除该闹铃/提醒
     */
    public synchronized void stopActiveAlert() {
        for (String alertToken : activeAlerts) {
            stopAlert(alertToken);
            return;
        }
    }

    private void drop(final Alert alert) {
        alertStopped(alert.getToken());
    }

    private synchronized AlertsStatePayload getState() {
        List<Alert> all = new ArrayList<>(schedulers.size());
        List<Alert> active = new ArrayList<>(activeAlerts.size());
        for (AlertScheduler scheduler : schedulers.values()) {
            Alert alert = scheduler.getAlert();
            all.add(alert);

            if (activeAlerts.contains(alert.getToken())) {
                active.add(alert);
            }
        }
        return new AlertsStatePayload(all, active);
    }

    @Override
    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer.removeMediaPlayerListener(mediaPlayerListener);
            mediaPlayer = null;
        }
        for (AlertScheduler scheduler : schedulers.values()) {
            scheduler.cancel();
        }
        alertListeners.clear();
    }

    private IMediaPlayer.IMediaPlayerListener mediaPlayerListener = new IMediaPlayer.SimpleMediaPlayerListener() {
        @Override
        public void onPrepared() {
            super.onPrepared();
            mediaPlayer.setActive(true);
        }

        @Override
        public void onCompletion() {
            alertState = AlertState.FINISHED;
            mediaPlayer.setActive(false);
        }

        @Override
        public void onError(String error, IMediaPlayer.ErrorType errorType) {
            super.onError(error, errorType);
            alertState = AlertState.FINISHED;
            mediaPlayer.setActive(false);
        }
    };


    /**
     * 闹铃/提醒回调
     */
    public interface IAlertListener {
        /**
         * 到了定点时间，触发了提醒/闹钟 时上报
         *
         * @param alertToken alertToken
         */
        void onAlertStarted(String alertToken);
    }

    public void addAlertListener(IAlertListener listener) {
        this.alertListeners.add(listener);
    }
}
