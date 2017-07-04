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
package com.baidu.duer.dcs.devicemodule.playbackcontroller;

import com.baidu.duer.dcs.devicemodule.alerts.AlertsDeviceModule;
import com.baidu.duer.dcs.devicemodule.system.HandleDirectiveException;
import com.baidu.duer.dcs.framework.BaseDeviceModule;
import com.baidu.duer.dcs.framework.IMessageSender;
import com.baidu.duer.dcs.framework.IResponseListener;
import com.baidu.duer.dcs.framework.message.ClientContext;
import com.baidu.duer.dcs.framework.message.Directive;
import com.baidu.duer.dcs.framework.message.Event;
import com.baidu.duer.dcs.framework.message.Header;
import com.baidu.duer.dcs.framework.message.MessageIdHeader;
import com.baidu.duer.dcs.framework.message.Payload;
import com.baidu.duer.dcs.systeminterface.IPlaybackController;

/**
 * 音频播放控制
 * <p>
 * 用户按了端上的播放/暂停等控制按钮，或者通过端上的GUI进行了此类操作时，上报PlayCommandIssued、PauseCommandIssued等事件
 * <p>
 * Created by wuruisheng on 2017/5/31.
 */
public class PlaybackControllerDeviceModule extends BaseDeviceModule {
    private AlertsDeviceModule mAlertsDeviceModule;

    public enum PlaybackAction {
        PLAY,
        PAUSE,
        PREVIOUS,
        NEXT
    }

    public PlaybackControllerDeviceModule(IPlaybackController playback, IMessageSender messageSender,
                                          AlertsDeviceModule alertsDeviceModule) {
        super(ApiConstants.NAMESPACE, messageSender);
        this.mAlertsDeviceModule = alertsDeviceModule;
        playback.registerPlaybackListener(new IPlaybackController.IPlaybackListener() {
            @Override
            public void onPlay(IResponseListener responseListener) {
                handlePlaybackAction(PlaybackAction.PLAY, responseListener);
            }

            @Override
            public void onPause(IResponseListener responseListener) {
                handlePlaybackAction(PlaybackAction.PAUSE, responseListener);
            }

            @Override
            public void onPrevious(IResponseListener responseListener) {
                handlePlaybackAction(PlaybackAction.PREVIOUS, responseListener);
            }

            @Override
            public void onNext(IResponseListener responseListener) {
                handlePlaybackAction(PlaybackAction.NEXT, responseListener);
            }
        });
    }

    @Override
    public ClientContext clientContext() {
        return null;
    }

    @Override
    public void handleDirective(Directive directive) throws HandleDirectiveException {
    }

    @Override
    public void release() {
    }

    private void handlePlaybackAction(PlaybackAction action, IResponseListener responseListener) {
        switch (action) {
            case PLAY:
                if (mAlertsDeviceModule.hasActiveAlerts()) {
                    mAlertsDeviceModule.stopActiveAlert();
                } else {
                    Event event = createPlaybackControllerEvent(ApiConstants.Events.PlayCommandIssued.NAME);
                    messageSender.sentEventWithClientContext(event, responseListener);
                }
                break;
            case PAUSE:
                if (mAlertsDeviceModule.hasActiveAlerts()) {
                    mAlertsDeviceModule.stopActiveAlert();
                } else {
                    Event event = createPlaybackControllerEvent(ApiConstants.Events.PauseCommandIssued.NAME);
                    messageSender.sentEventWithClientContext(event, responseListener);
                }
                break;
            case PREVIOUS:
                Event event = createPlaybackControllerEvent(ApiConstants.Events.PreviousCommandIssued.NAME);
                messageSender.sentEventWithClientContext(event, responseListener);
                break;
            case NEXT:
                Event eventNext = createPlaybackControllerEvent(ApiConstants.Events.NextCommandIssued.NAME);
                messageSender.sentEventWithClientContext(eventNext, responseListener);
                break;
            default:
                break;
        }
    }

    private Event createPlaybackControllerEvent(String name) {
        Header header = new MessageIdHeader(ApiConstants.NAMESPACE, name);
        return new Event(header, new Payload());
    }
}
