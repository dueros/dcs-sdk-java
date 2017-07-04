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
package com.baidu.duer.dcs.wakeup;

import com.baidu.duer.dcs.systeminterface.IAudioRecord;
import com.baidu.duer.dcs.systeminterface.IWakeUp;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 本地唤醒服务
 * <p>
 * Created by guxiuzhong@baidu.com on 2017/6/22.
 */
public class WakeUp {
    private IWakeUp iWakeUp;
    private List<IWakeUp.IWakeUpListener> wakeUpListeners;
    private IAudioRecord iAudioRecord;

    public WakeUp(IWakeUp iWakeUp, IAudioRecord iAudioRecord) {
        this.iWakeUp = iWakeUp;
        this.iAudioRecord = iAudioRecord;
        this.wakeUpListeners = Collections.synchronizedList(new LinkedList<IWakeUp.IWakeUpListener>());
        this.iWakeUp.addWakeUpListener(new IWakeUp.IWakeUpListener() {
            @Override
            public void onWakeUpSucceed() {
                fireOnWakeUpSucceed();
            }
        });
        // 启动音频采集
        this.iAudioRecord.startRecord();
    }

    private void fireOnWakeUpSucceed() {
        for (IWakeUp.IWakeUpListener listener : wakeUpListeners) {
            listener.onWakeUpSucceed();
        }
    }

    /**
     * 开始唤醒，麦克风处于打开状态，一旦检测到有音频开始唤醒解码
     */
    public void startWakeUp() {
        iWakeUp.startWakeUp();
    }

    /**
     * 停止唤醒，关闭麦克风
     */
    public void stopWakeUp() {
        iWakeUp.stopWakeUp();
    }

    /**
     * 释放资源-解码so库资源
     */
    public void releaseWakeUp() {
        iAudioRecord.stopRecord();
        iWakeUp.releaseWakeUp();
    }

    /**
     * 添加唤醒成功后的监听
     */
    public void addWakeUpListener(IWakeUp.IWakeUpListener listener) {
        wakeUpListeners.add(listener);
    }

    public void removeWakeUpListener(IWakeUp.IWakeUpListener listener) {
        if (wakeUpListeners.contains(listener)) {
            wakeUpListeners.remove(listener);
        }
    }
}