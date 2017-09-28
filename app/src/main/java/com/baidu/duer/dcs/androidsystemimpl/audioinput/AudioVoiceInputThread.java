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

import com.baidu.duer.dcs.util.LogUtil;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingDeque;

import okio.BufferedSink;

/**
 * 将语音发送到服务器的线程-发送消费者
 * <p>
 * Created by guxiuzhong@baidu.com on 2017/6/26.
 */
public class AudioVoiceInputThread extends Thread {
    private static final String TAG = AudioVoiceInputThread.class.getSimpleName();
    private volatile boolean isStart = false;
    private BufferedSink bufferedSink;
    // 音频数据
    private LinkedBlockingDeque<byte[]> linkedBlockingDeque;
    private Handler handler;

    public AudioVoiceInputThread(LinkedBlockingDeque<byte[]> linkedBlockingDeque,
                                 BufferedSink bufferedSink,
                                 Handler handler) {
        this.linkedBlockingDeque = linkedBlockingDeque;
        this.bufferedSink = bufferedSink;
        this.handler = handler;
    }

    /**
     * 开始写入数据
     */
    public void startWriteStream() {
        if (isStart) {
            return;
        }
        isStart = true;
        this.start();
    }

    /**
     * 停止写入数据
     */
    public void stopWriteStream() {
        isStart = false;
    }

    private boolean isfirst = true;

    @Override
    public void run() {
        super.run();
        while (isStart) {
            try {
                byte[] recordAudioData = linkedBlockingDeque.pollFirst();
                if (null != recordAudioData) {
                    if (isfirst) {
                        isfirst = false;
                    }
                    bufferedSink.write(recordAudioData);
                }
            } catch (IOException e) {
                e.printStackTrace();
                LogUtil.d(TAG, "writeTo IOException", e);
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.d(TAG, "writeTo Exception", e);
            }
        }
        if (linkedBlockingDeque.size() > 0) {
            byte[] recordAudioData = linkedBlockingDeque.pollFirst();
            if (null != recordAudioData) {
                LogUtil.d(TAG, "finally writeTo size:" + recordAudioData.length);
                try {
                    bufferedSink.write(recordAudioData);
                } catch (IOException e) {
                    e.printStackTrace();
                    LogUtil.d(TAG, " >0 writeTo IOException", e);
                }
            }
        }
        try {
            bufferedSink.flush();
            bufferedSink.close();
            LogUtil.d(TAG, "closed");
        } catch (IOException e) {
            e.printStackTrace();
            LogUtil.d(TAG, "IOException ", e);
        }
        LogUtil.d(TAG, "onWriteFinished ");
        // 写入完成
        if (listener != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onWriteFinished();
                }
            });
        }
    }

    private IAudioInputListener listener;

    public void setAudioInputListener(IAudioInputListener listener) {
        this.listener = listener;
    }

    /**
     * 写入完成的回调接口，比如当接收到StopListen指令后回触发
     */
    public interface IAudioInputListener {
        /**
         * 写入完成后回调此方法
         */
        void onWriteFinished();
    }
}