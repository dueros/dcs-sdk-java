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
package com.baidu.duer.dcs.systeminterface;

/**
 * 唤醒接口
 * <p>
 * Created by guxiuzhong@baidu.com on 2017/6/25.
 */
public interface IWakeUp {
    /**
     * 开始唤醒
     * 1.初始化唤醒词
     * 2.打开麦克风，并开始音频唤醒的解码
     */
    void startWakeUp();

    /**
     * 停止唤醒，调用停止录音
     */
    void stopWakeUp();

    /**
     * 释放资源，比如调用底层库释放资源等
     */
    void releaseWakeUp();

    /**
     * 唤醒结果的回调监听
     *
     * @param listener 监听实现
     */
    void addWakeUpListener(IWakeUpListener listener);

    /**
     * 唤醒结果的回调接口
     */
    interface IWakeUpListener {
        /**
         * 唤醒成功后回调
         */
        void onWakeUpSucceed();
    }
}