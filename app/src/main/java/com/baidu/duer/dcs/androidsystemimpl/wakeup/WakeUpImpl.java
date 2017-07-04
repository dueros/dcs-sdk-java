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

import android.content.Context;
import android.os.Handler;

import com.baidu.duer.dcs.systeminterface.IWakeUp;
import com.baidu.duer.dcs.util.LogUtil;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * 唤醒模块-android中实现
 * <p>
 * Created by guxiuzhong@baidu.com on 2017/6/25.
 */
public class WakeUpImpl implements IWakeUp {
    private static final String TAG = WakeUpImpl.class.getSimpleName();
    // 初始化唤醒词成功
    private static final int WAKEUP_INIT_SUCCEED = 0;
    // 唤醒词
    private static final String WAKEUP_WORD = "小度小度";
    // 唤醒词声学模型模型文件
    private static final String WAKEUP_FILENAME = "libbdEasrS1MergeNormal.so";
    // jni
    private WakeUpNative wakeUpNative;
    // Decode消费线程
    private WakeUpDecodeThread wakeUpDecodeThread;
    // callback
    private List<IWakeUpListener> wakeUpListeners;
    private Handler handler = new Handler();
    // 音频数据
    private LinkedBlockingDeque<byte[]> linkedBlockingDeque;
    // 初始化唤醒词的返回值
    private int wakeUpInitialRet;
    private Context context;

    public WakeUpImpl(Context context, LinkedBlockingDeque<byte[]> linkedBlockingDeque) {
        this.linkedBlockingDeque = linkedBlockingDeque;
        this.context = context.getApplicationContext();
        this.wakeUpNative = new WakeUpNative();
        this.wakeUpListeners = Collections.synchronizedList(new LinkedList<IWakeUpListener>());
        this.initWakeUp();
    }

    private void initWakeUp() {
        // 方法1：加载声学模型文件,当作so库进行加载到nativeLibraryDir目录中
        // 方法2：当然你也可以放到assets目录或者raw下，然后进行拷贝到应用的私有目录或者sd卡
        // 方法2需要处理声学模型文件覆盖安装问题，以及不可预料的拷贝失败问题！
        String path = this.context.getApplicationInfo().nativeLibraryDir
                + File.separatorChar + WAKEUP_FILENAME;
        LogUtil.d(TAG, "wakeup path:" + path);
        LogUtil.d(TAG, "wakeup exists:" + new File(path).exists());
        // 1.初始化唤醒词，0 是初始化成功
        wakeUpInitialRet = wakeUpNative.wakeUpInitial(WAKEUP_WORD, path, 0);
        LogUtil.d(TAG, "wakeUpInitialRet:" + wakeUpInitialRet);
    }

    @Override
    public void startWakeUp() {
        if (wakeUpDecodeThread != null && wakeUpDecodeThread.isStart()) {
            LogUtil.d(TAG, "wakeup wakeUpDecodeThread  is Started !");
            return;
        }
        // 2.开始唤醒
        if (wakeUpInitialRet == WAKEUP_INIT_SUCCEED) {
            wakeUp();
        } else {
            LogUtil.d(TAG, "wakeup wakeUpInitialRet failed, not startWakeUp ");
        }
    }

    @Override
    public void stopWakeUp() {
        if (wakeUpDecodeThread != null) {
            wakeUpDecodeThread.stopWakeUp();
        }
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void releaseWakeUp() {
        // 3.释放资源
        int ret = wakeUpNative.wakeUpFree();
        LogUtil.d(TAG, "wakeUpFree-ret:" + ret);
    }

    @Override
    public void addWakeUpListener(IWakeUpListener listener) {
        wakeUpListeners.add(listener);
    }

    /**
     * 开始音频解码进行唤醒操作
     */
    private void wakeUp() {
        wakeUpDecodeThread = new WakeUpDecodeThread(linkedBlockingDeque, wakeUpNative, handler);
        wakeUpDecodeThread.setWakeUpListener(new WakeUpDecodeThread.IWakeUpListener() {
            @Override
            public void onWakeUpSucceed() {
                // 唤醒成功
                fireOnWakeUpSucceed();
            }
        });
        wakeUpDecodeThread.startWakeUp();
    }

    private void fireOnWakeUpSucceed() {
        for (IWakeUpListener listener : wakeUpListeners) {
            listener.onWakeUpSucceed();
        }
    }
}