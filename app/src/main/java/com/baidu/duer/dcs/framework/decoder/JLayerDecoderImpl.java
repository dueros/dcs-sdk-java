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

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;

/**
 * 开源库JLayer将mp3解码为pcm
 * <p>
 * Created by guxiuzhong@baidu.com on 2017/8/2.
 */
public class JLayerDecoderImpl extends BaseDecoder {

    public JLayerDecoderImpl() {
        super();
        Log.d(TAG, "Decoder-JLayerDecoderImpl");
    }

    @Override
    public void decode(InputStream inputStream) throws Exception {
        Decoder decoder = new Decoder();
        Bitstream bitstream = new Bitstream(inputStream);
        Header header;
        isStopRead = false;
        isGetMp3InfoFinished = false;
        int count = 0;
        while (!isStopRead && (header = bitstream.readFrame()) != null) {
            isDecoding = true;
            long start = System.currentTimeMillis();
            SampleBuffer sampleBuffer = (SampleBuffer) decoder.decodeFrame(header, bitstream);
            // 获取采样率等
            if (!isGetMp3InfoFinished) {
                fireOnDecodeInfo(sampleBuffer.getSampleFrequency(), sampleBuffer.getChannelCount());
                isGetMp3InfoFinished = true;
            }
            short[] buffer = sampleBuffer.getBuffer();
            byte[] pcm = new byte[buffer.length / 2];
            for (int i = 0; i < buffer.length / 2 / 2; i++) {
                int j = i * 2;
                pcm[j] = (byte) (buffer[i] & 0xff);
                pcm[j + 1] = (byte) ((buffer[i] >> 8) & 0xff);
            }
            if (count == 0 || count == 1) {
                byte[] newPcm = avoidNullPcm(pcm);
                if (newPcm != null) {
                    fireOnDecodePcm(newPcm);
                }
            } else {
                fireOnDecodePcm(pcm);
            }
            count++;
            bitstream.closeFrame();
            long end = System.currentTimeMillis();
            Log.i(TAG, "after decode pcm.length:" + pcm.length + "," + (end - start));
        }
        isDecoding = false;
        fireOnDecodeFinished();
        inputStream.close();
    }

    @Override
    protected void read(byte[] mp3Data) {

    }
}