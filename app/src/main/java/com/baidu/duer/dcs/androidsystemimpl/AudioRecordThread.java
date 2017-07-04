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
package com.baidu.duer.dcs.androidsystemimpl;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.baidu.duer.dcs.systeminterface.IAudioRecord;
import com.baidu.duer.dcs.util.LogUtil;

import java.util.concurrent.LinkedBlockingDeque;

/**
 * 录音采集线程-音频数据生产者
 * <p>
 * Created by guxiuzhong@baidu.com on 2017/6/26.
 */
public class AudioRecordThread extends Thread implements IAudioRecord {
    private static final String TAG = AudioRecordThread.class.getSimpleName();
    // 采样率
    private static final int SAMPLE_RATE_HZ = 16000;
    private int bufferSize;
    private AudioRecord audioRecord;
    private volatile boolean isStartRecord = false;
    private LinkedBlockingDeque<byte[]> linkedBlockingDeque;

    public AudioRecordThread(LinkedBlockingDeque<byte[]> linkedBlockingDeque) {
        this.linkedBlockingDeque = linkedBlockingDeque;
        bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_HZ, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE_HZ, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize);
    }

    @Override
    public void startRecord() {
        if (isStartRecord) {
            return;
        }
        isStartRecord = true;
        this.start();
    }

    @Override
    public void stopRecord() {
        isStartRecord = false;
    }

    @Override
    public void run() {
        super.run();
        LogUtil.i(TAG, "audioRecord startRecording ");
        audioRecord.startRecording();
        byte[] buffer = new byte[bufferSize];
        while (isStartRecord) {
            int readBytes = audioRecord.read(buffer, 0, bufferSize);
            if (readBytes > 0) {
                linkedBlockingDeque.add(buffer);
            }
        }
        // 清空数据
        linkedBlockingDeque.clear();
        // 释放资源
        audioRecord.stop();
        audioRecord.release();
        audioRecord = null;
        LogUtil.i(TAG, "audioRecord release ");
    }
}