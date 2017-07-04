/*
 * *
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
package com.baidu.duer.dcs.androidsystemimpl.audioinput;

import android.os.Handler;

import com.baidu.duer.dcs.framework.message.DcsStreamRequestBody;
import com.baidu.duer.dcs.systeminterface.IAudioInput;

import java.util.concurrent.LinkedBlockingDeque;

/**
 * AudioInput实现，控制麦克风获取语音数据流
 * <p>
 * Created by guxiuzhong@baidu.com on 2017/6/26.
 */
public class AudioVoiceInputImpl implements IAudioInput {
    // 消费线程
    private AudioVoiceInputThread audioVoiceInputThread;
    // 音频数据
    private LinkedBlockingDeque<byte[]> linkedBlockingDeque;
    private IAudioInputListener audioInputListener;
    private Handler handler = new Handler();

    public AudioVoiceInputImpl(LinkedBlockingDeque<byte[]> linkedBlockingDeque) {
        this.linkedBlockingDeque = linkedBlockingDeque;
    }

    @Override
    public void startRecord() {
        DcsStreamRequestBody dcsStreamRequestBody = new DcsStreamRequestBody();
        audioInputListener.onStartRecord(dcsStreamRequestBody);
        audioVoiceInputThread = new AudioVoiceInputThread(
                linkedBlockingDeque,
                dcsStreamRequestBody.sink(),
                handler);
        audioVoiceInputThread.setAudioInputListener(new AudioVoiceInputThread.IAudioInputListener() {
            @Override
            public void onWriteFinished() {
                if (audioInputListener != null) {
                    audioInputListener.onStopRecord();
                }
            }
        });
        audioVoiceInputThread.startWriteStream();
    }

    @Override
    public void stopRecord() {
        audioVoiceInputThread.stopWriteStream();
    }

    @Override
    public void registerAudioInputListener(IAudioInputListener audioInputListener) {
        this.audioInputListener = audioInputListener;
    }
}