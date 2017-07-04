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
package com.baidu.duer.dcs.devicemodule.voiceoutput;

import com.baidu.duer.dcs.devicemodule.system.HandleDirectiveException;
import com.baidu.duer.dcs.devicemodule.voiceoutput.message.SpeakPayload;
import com.baidu.duer.dcs.devicemodule.voiceoutput.message.SpeechLifecyclePayload;
import com.baidu.duer.dcs.devicemodule.voiceoutput.message.VoiceOutputStatePayload;
import com.baidu.duer.dcs.framework.BaseDeviceModule;
import com.baidu.duer.dcs.framework.IMessageSender;
import com.baidu.duer.dcs.framework.IResponseListener;
import com.baidu.duer.dcs.framework.message.ClientContext;
import com.baidu.duer.dcs.framework.message.Directive;
import com.baidu.duer.dcs.framework.message.Event;
import com.baidu.duer.dcs.framework.message.Header;
import com.baidu.duer.dcs.framework.message.MessageIdHeader;
import com.baidu.duer.dcs.systeminterface.IMediaPlayer;
import com.baidu.duer.dcs.util.LogUtil;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Voice Output模块处理并执行服务下发的Speak指令，上报SpeechStarted、SpeechFinished事件，以及维护自身的端状态
 * <p>
 * Created by guxiuzhong@baidu.com on 2017/5/31.
 */
public class VoiceOutputDeviceModule extends BaseDeviceModule {
    private static final String TAG = VoiceOutputDeviceModule.class.getSimpleName();
    // 播放回调
    private final List<IVoiceOutputListener> voiceOutputListeners;
    // 播放队列
    private final LinkedList<SpeakPayload> speakQueue = new LinkedList<>();
    // 语音播放的播放器
    private final IMediaPlayer mediaPlayer;
    private SpeechState speechState = SpeechState.FINISHED;

    // 上一次的token
    private String lastSpeakToken = "";

    // 当前播放状态
    private enum SpeechState {
        PLAYING,
        FINISHED
    }

    public VoiceOutputDeviceModule(IMediaPlayer mediaPlayer,
                                   IMessageSender messageSender) {
        super(ApiConstants.NAMESPACE, messageSender);
        this.mediaPlayer = mediaPlayer;
        this.mediaPlayer.addMediaPlayerListener(mediaPlayerListener);
        this.voiceOutputListeners = Collections.synchronizedList(new ArrayList<IVoiceOutputListener>());
    }

    @Override
    public ClientContext clientContext() {
        String namespace = ApiConstants.NAMESPACE;
        String name = ApiConstants.Events.SpeechState.NAME;
        Header header = new Header(namespace, name);
        VoiceOutputStatePayload payload = new VoiceOutputStatePayload(lastSpeakToken,
                mediaPlayer.getCurrentPosition(),
                speechState.name());
        return new ClientContext(header, payload);
    }

    @Override
    public void handleDirective(Directive directive) throws HandleDirectiveException {
        String directiveName = directive.getName();
        LogUtil.d(TAG, "rawMessage:" + directive.rawMessage);
        LogUtil.d(TAG, "directiveName:" + directiveName);
        if (directiveName.equals(ApiConstants.Directives.Speak.NAME)) {
            SpeakPayload speak = (SpeakPayload) directive.payload;
            handleSpeak(speak);
        } else {
            String message = "VoiceOutput cannot handle the directive";
            throw (new HandleDirectiveException(
                    HandleDirectiveException.ExceptionType.UNSUPPORTED_OPERATION, message));
        }
    }

    private void handleSpeak(SpeakPayload speak) {
        speakQueue.add(speak);
        // 如果已经有了，就只入队列，等待下一次的调度
        if (speakQueue.size() == 1) {
            startSpeech();
        }
    }

    private void startSpeech() {
        final SpeakPayload speak = speakQueue.getFirst();
        if (null != speak) {
            lastSpeakToken = speak.token;
            InputStream inputStream = new ByteArrayInputStream(speak.attachedContent);
            mediaPlayer.play(new IMediaPlayer.MediaResource(inputStream));
        }
    }

    private IMediaPlayer.IMediaPlayerListener mediaPlayerListener = new IMediaPlayer.SimpleMediaPlayerListener() {
        @Override
        public void onPrepared() {
            super.onPrepared();
            speechState = SpeechState.PLAYING;
            sendStartedEvent(lastSpeakToken);
            fireOnVoiceOutputStarted();
        }

        @Override
        public void onStopped() {
            super.onStopped();
            speakQueue.clear();
        }

        @Override
        public void onError(String error, IMediaPlayer.ErrorType errorType) {
            super.onError(error, errorType);
            finishedSpeechItem();
        }

        @Override
        public void onCompletion() {
            LogUtil.d(TAG, " IMediaPlayer onCompletion");
            finishedSpeechItem();
        }
    };

    /**
     * 播放完一条后开始下一条的处理
     */
    private void finishedSpeechItem() {
        speakQueue.poll();
        LogUtil.d(TAG, "finishedSpeechItem speakQueue size :" + speakQueue.size());
        if (speakQueue.isEmpty()) {
            speechState = SpeechState.FINISHED;
            sendFinishedEvent(lastSpeakToken);
            fireOnVoiceOutputFinished();
        } else {
            startSpeech();
        }
    }

    @Override
    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer.removeMediaPlayerListener(mediaPlayerListener);
        }
        if (isSpeaking()) {
            speechState = SpeechState.FINISHED;
        }
        speakQueue.clear();
        voiceOutputListeners.clear();
    }

    public boolean isSpeaking() {
        return speechState == SpeechState.PLAYING;
    }

    /**
     * 播放开始时上报的事件
     *
     * @param token token  一条speak的唯一标识
     */
    private void sendStartedEvent(String token) {
        String namespace = ApiConstants.NAMESPACE;
        String name = ApiConstants.Events.SpeechStarted.NAME;
        MessageIdHeader header = new MessageIdHeader(namespace, name);
        Event event = new Event(header, new SpeechLifecyclePayload(token));
        messageSender.sendEvent(event);
    }

    /**
     * 播放结束时上报的事件
     *
     * @param token token  一条speak的唯一标识
     */
    private void sendFinishedEvent(String token) {
        String namespace = ApiConstants.NAMESPACE;
        String name = ApiConstants.Events.SpeechFinished.NAME;
        MessageIdHeader header = new MessageIdHeader(namespace, name);
        Event event = new Event(header, new SpeechLifecyclePayload(token));
        messageSender.sendEvent(event, new IResponseListener() {
            @Override
            public void onSucceed(int statusCode) {
                // 没有新的语音speak-stream
                if (statusCode == 204) {
                    mediaPlayer.setActive(false);
                } else {
                    mediaPlayer.setActive(true);
                }
            }

            @Override
            public void onFailed(String errorMessage) {
                mediaPlayer.setActive(false);
            }
        });
    }

    private void fireOnVoiceOutputStarted() {
        for (IVoiceOutputListener listener : voiceOutputListeners) {
            listener.onVoiceOutputStarted();
        }
    }

    private void fireOnVoiceOutputFinished() {
        for (IVoiceOutputListener listener : voiceOutputListeners) {
            listener.onVoiceOutputFinished();
        }
    }

    /**
     * 添加播放监听
     *
     * @param listener listener
     */
    public void addVoiceOutputListener(IVoiceOutputListener listener) {
        this.voiceOutputListeners.add(listener);
    }

    /**
     * 播报监听器接口
     */
    public interface IVoiceOutputListener {
        /**
         * 开始播放时回调
         */
        void onVoiceOutputStarted();

        /**
         * 播放完成时回调
         */
        void onVoiceOutputFinished();
    }
}