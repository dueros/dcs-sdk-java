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
package com.baidu.duer.dcs.devicemodule.voiceinput;

import com.baidu.duer.dcs.devicemodule.system.HandleDirectiveException;
import com.baidu.duer.dcs.devicemodule.voiceinput.message.ListenStartedPayload;
import com.baidu.duer.dcs.framework.BaseDeviceModule;
import com.baidu.duer.dcs.framework.DcsResponseDispatcher;
import com.baidu.duer.dcs.framework.DialogRequestIdHandler;
import com.baidu.duer.dcs.framework.IMessageSender;
import com.baidu.duer.dcs.framework.IResponseListener;
import com.baidu.duer.dcs.framework.message.ClientContext;
import com.baidu.duer.dcs.framework.message.DcsStreamRequestBody;
import com.baidu.duer.dcs.framework.message.DialogRequestIdHeader;
import com.baidu.duer.dcs.framework.message.Directive;
import com.baidu.duer.dcs.framework.message.Event;
import com.baidu.duer.dcs.framework.message.Payload;
import com.baidu.duer.dcs.systeminterface.IAudioInput;
import com.baidu.duer.dcs.systeminterface.IMediaPlayer;
import com.baidu.duer.dcs.util.LogUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Voice Input模块处理并执行服务下发的StopListen、Listen指令，上报ListenStarted、ListenTimedOut事件
 * <p>
 * Created by wuruisheng on 2017/5/31.
 */
public class VoiceInputDeviceModule extends BaseDeviceModule {
    public static final String TAG = VoiceInputDeviceModule.class.getSimpleName();
    private final IAudioInput audioInput;
    private final List<IVoiceInputListener> voiceInputListeners;
    private final IMediaPlayer mediaPlayer;
    private final DialogRequestIdHandler dialogRequestIdHandler;
    private final DcsResponseDispatcher dcsResponseDispatcher;

    public VoiceInputDeviceModule(final IMediaPlayer mediaPlayer,
                                  IMessageSender messageSender,
                                  final IAudioInput audioInput,
                                  DialogRequestIdHandler dialogRequestIdHandler,
                                  DcsResponseDispatcher dcsResponseDispatcher) {
        super(ApiConstants.NAMESPACE, messageSender);
        this.audioInput = audioInput;
        this.voiceInputListeners = Collections.synchronizedList(new ArrayList<IVoiceInputListener>());
        this.mediaPlayer = mediaPlayer;
        this.dialogRequestIdHandler = dialogRequestIdHandler;
        this.dcsResponseDispatcher = dcsResponseDispatcher;

        this.audioInput.registerAudioInputListener(new IAudioInput.IAudioInputListener() {
            @Override
            public void onStartRecord(DcsStreamRequestBody dcsStreamRequestBody) {
                stopSpeaker();
                // 发送网络请求
                sendListenStartedEvent(dcsStreamRequestBody, new IResponseListener() {
                    @Override
                    public void onSucceed(int statusCode) {
                        fireOnSucceed(statusCode);
                        // 没有下发新的语音speak-stream
                        if (statusCode == 204) {
                            // 设置对话通道为非活跃状态
                            mediaPlayer.setActive(false);
                        } else {
                            mediaPlayer.setActive(true);
                        }
                    }

                    @Override
                    public void onFailed(String errorMessage) {
                        LogUtil.d(TAG, "onFailed,errorMessage:" + errorMessage);
                        fireOnFailed(errorMessage);
                        audioInput.stopRecord();
                        mediaPlayer.setActive(false);
                    }
                });

                fireOnStartRecord();
            }

            @Override
            public void onStopRecord() {
                fireFinishRecord();
            }
        });
    }


    @Override
    public ClientContext clientContext() {
        return null;
    }

    @Override
    public void handleDirective(Directive directive) throws HandleDirectiveException {
        String name = directive.getName();
        if (name.equals(ApiConstants.Directives.StopListen.NAME)) {
            audioInput.stopRecord();
        } else if (name.equals(ApiConstants.Directives.Listen.NAME)) {
            audioInput.startRecord();
        } else {
            String message = "No device to handle the directive";
            throw new HandleDirectiveException(
                    HandleDirectiveException.ExceptionType.UNSUPPORTED_OPERATION,
                    message);
        }
    }

    @Override
    public void release() {
        voiceInputListeners.clear();
    }

    /**
     * 停止speaker对话通道的语音播放
     */
    private void stopSpeaker() {
        mediaPlayer.setActive(true);
        mediaPlayer.stop();
        dcsResponseDispatcher.interruptDispatch();
    }

    private void sendListenStartedEvent(DcsStreamRequestBody streamRequestBody, IResponseListener responseListener) {
        String dialogRequestId = dialogRequestIdHandler.createActiveDialogRequestId();
        String name = ApiConstants.Events.ListenStarted.NAME;
        DialogRequestIdHeader header = new DialogRequestIdHeader(getNameSpace(), name, dialogRequestId);
        Payload payload = new ListenStartedPayload(ListenStartedPayload.FORMAT);
        Event event = new Event(header, payload);
        messageSender.sendEvent(event, streamRequestBody, responseListener);
    }

    private void fireOnStartRecord() {
        for (IVoiceInputListener listener : voiceInputListeners) {
            listener.onStartRecord();
        }
    }

    private void fireFinishRecord() {
        for (IVoiceInputListener listener : voiceInputListeners) {
            listener.onFinishRecord();
        }
    }

    private void fireOnSucceed(int statusCode) {
        for (IVoiceInputListener listener : voiceInputListeners) {
            listener.onSucceed(statusCode);
        }
    }

    private void fireOnFailed(String errorMessage) {
        for (IVoiceInputListener listener : voiceInputListeners) {
            listener.onFailed(errorMessage);
        }
    }

    public void addVoiceInputListener(IVoiceInputListener listener) {
        this.voiceInputListeners.add(listener);
    }

    public interface IVoiceInputListener {
        /**
         * 开始录音的回调
         */
        void onStartRecord();

        /**
         * 结束录音的回调
         */
        void onFinishRecord();

        /**
         * 录音-网络请求成功
         *
         * @param statusCode 网络返回状态码
         */
        void onSucceed(int statusCode);

        /**
         * 录音-网络请求失败
         *
         * @param errorMessage 错误信息
         */
        void onFailed(String errorMessage);
    }
}