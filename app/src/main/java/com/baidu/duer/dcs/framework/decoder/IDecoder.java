/*
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

import java.io.InputStream;

/**
 * MP3->PCM 解码接口
 * <p>
 * Created by guxiuzhong@baidu.com on 2017/8/3.
 */
public interface IDecoder {
    /**
     * mp3输入流
     *
     * @param inputStream inputStream
     * @throws Exception Exception
     */
    void decode(InputStream inputStream) throws Exception;

    /**
     * 打断解码
     */
    void interruptDecode();

    /**
     * 释放资源，比如解码器
     */
    void release();

    /**
     * 设置解码回调
     *
     * @param decodeListener decodeListener
     */
    void addOnDecodeListener(IDecodeListener decodeListener);

    /**
     * 移除解码回调
     *
     * @param decodeListener decodeListener
     */
    void removeOnDecodeListener(IDecodeListener decodeListener);

    /**
     * 解码回调
     */
    interface IDecodeListener {

        /**
         * mp3的信息
         *
         * @param sampleRate 采样率
         * @param channels   声道数
         */
        void onDecodeInfo(int sampleRate, int channels);

        /**
         * 实时解码回调
         *
         * @param pcmData 解码后的pcm数据
         */
        void onDecodePcm(byte[] pcmData);

        /**
         * 当mp3输入流读取完成，即解码完成后回调
         */
        void onDecodeFinished();
    }
}