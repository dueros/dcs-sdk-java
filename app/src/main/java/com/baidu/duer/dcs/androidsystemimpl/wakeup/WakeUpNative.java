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
package com.baidu.duer.dcs.androidsystemimpl.wakeup;

/**
 * 唤醒服务-native-声明
 * <p>
 * Created by guxiuzhong@baidu.com on 2017/6/21.
 */
public class WakeUpNative {

    // 加载动态库.so文件
    static {
        System.loadLibrary("wakeup");
        // 唤醒词解码so库
        System.loadLibrary("bdEASRAndroid");
    }

    /**
     * 初始化唤醒词成功
     *
     * @param wakeUpWd 唤醒词，例如“小度小度”
     * @param sFile    附件中的资源文件, 唤醒词模型数据的文件绝对路径
     * @param mode     缺省0即可
     * @return 返回0表示初始化唤醒词成功
     */
    public native int wakeUpInitial(String wakeUpWd, String sFile, int mode);

    /**
     * 重制唤醒词
     *
     * @return 返回0表示成功
     */
    public native int wakeUpReset();

    /**
     * 唤醒解码
     *
     * @param data               音频数据
     * @param dataLen            数据长度
     * @param senArr             返回结果，如果唤醒词是小度小度”，唤醒成功后会赋值
     * @param expectNum          传1
     * @param wakeWord_frame_len 唤醒成功返回唤醒词长度
     * @param is_confidence      true表示置信，false表示疑似唤醒
     * @param voice_offset       唤醒词位置，需要用户传入，同时内部处理后传出
     * @param bEd                是否为最后一帧数据
     * @return 返回1表示唤醒，其他表示未唤醒
     */
    public native int wakeUpDecode(short[] data,
                                   int dataLen,
                                   String senArr,
                                   int expectNum,
                                   int wakeWord_frame_len,
                                   boolean is_confidence,
                                   int voice_offset,
                                   boolean bEd
    );

    /**
     * 释放资源
     *
     * @return 返回0表示成功
     */
    public native int wakeUpFree();
}