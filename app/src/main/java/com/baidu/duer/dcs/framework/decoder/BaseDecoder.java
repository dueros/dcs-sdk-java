/*
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
package com.baidu.duer.dcs.framework.decoder;

import android.util.Log;

import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * BaseDecoder解码实现mp3->pcm
 * <p>
 * Created by guxiuzhong@baidu.com on 2017/8/4.
 */
abstract class BaseDecoder implements IDecoder {
    protected static final String TAG = "Decoder";
    // 读取缓存
    private static final int BUFFER_SIZE = 8 * 1024;
    // 是否正在解码
    volatile boolean isDecoding = false;
    // 是否停止读取
    volatile boolean isStopRead = false;
    // 回调
    private final List<IDecodeListener> decodeListeners;
    // 是否获取到了mp3的信息
    volatile boolean isGetMp3InfoFinished;

    BaseDecoder() {
        this.decodeListeners = Collections.synchronizedList(
                new LinkedList<IDecodeListener>());
    }

    @Override
    public void decode(InputStream inputStream) throws Exception {
        byte[] buffer = new byte[BUFFER_SIZE];
        int readBytes;
        isStopRead = false;
        while (!isStopRead && (readBytes = inputStream.read(buffer, 0, buffer.length)) != -1) {
            Log.i(TAG, "read one tts data readBytes:" + readBytes + "," + System.currentTimeMillis());
            isDecoding = true;
            byte[] temp = new byte[readBytes];
            System.arraycopy(buffer, 0, temp, 0, readBytes);
            read(temp);
        }
        isDecoding = false;
        Log.i(TAG, "decoder finished.");
        fireOnDecodeFinished();
        inputStream.close();
    }

    @Override
    public void interruptDecode() {
        if (isDecoding) {
            isStopRead = true;
        }
    }

    @Override
    public void release() {
        isStopRead = true;
        isDecoding = false;
        decodeListeners.clear();
    }

    @Override
    public void addOnDecodeListener(IDecodeListener decodeListener) {
        synchronized (decodeListeners) {
            if (!decodeListeners.contains(decodeListener)) {
                this.decodeListeners.add(decodeListener);
            }
        }
    }

    @Override
    public void removeOnDecodeListener(IDecodeListener decodeListener) {
        synchronized (decodeListeners) {
            if (decodeListeners.contains(decodeListener)) {
                decodeListeners.remove(decodeListener);
            }
        }
    }

    /**
     * 去除首帧无效数据，以达到快速听到声音的目的
     *
     * @param pcm pcm
     * @return new pcm
     */
    protected byte[] avoidNullPcm(byte[] pcm) {
        // Log.i(TAG, "Decoder:" + ArrayUtils.toString(pcm));
        int index = -1;
        for (int j = 0; j < pcm.length; j++) {
            if (pcm[j] != 0) {
                index = j;
                break;
            }
        }
        Log.d(TAG, "index=" + index);
        if (index != -1) {
            byte[] buffer = new byte[pcm.length - index];
            System.arraycopy(pcm, index, buffer, 0, buffer.length);
            return buffer;
        }
        return null;
    }

    void fireOnDecodeFinished() {
        synchronized (decodeListeners) {
            for (IDecoder.IDecodeListener listener : decodeListeners) {
                listener.onDecodeFinished();
            }
        }
    }

    void fireOnDecodePcm(byte[] pcm) {
        synchronized (decodeListeners) {
            for (IDecoder.IDecodeListener listener : decodeListeners) {
                listener.onDecodePcm(pcm);
            }
        }
    }

    void fireOnDecodeInfo(int sampleRate, int channels) {
        synchronized (decodeListeners) {
            for (IDecoder.IDecodeListener listener : decodeListeners) {
                listener.onDecodeInfo(sampleRate, channels);
            }
        }
    }

    protected abstract void read(byte[] mp3Data);
}