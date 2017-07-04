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

import android.os.Handler;

import com.baidu.duer.dcs.util.LogUtil;

import java.util.concurrent.LinkedBlockingDeque;

/**
 * 唤醒声音处理-唤醒消费者
 * <p>
 * Created by guxiuzhong@baidu.com on 2017/6/22.
 */
public class WakeUpDecodeThread extends Thread {
    private static final String TAG = WakeUpDecodeThread.class.getSimpleName();
    // 唤醒成功
    private static final int WAKEUP_SUCCEED = 1;
    // 唤醒词位置
    private int voiceOffset;
    private boolean isWakeUp;
    private WakeUpNative wakeUpNative;
    private Handler handler;
    private volatile boolean isStart = false;
    private LinkedBlockingDeque<byte[]> linkedBlockingDeque;

    public WakeUpDecodeThread(LinkedBlockingDeque<byte[]> linkedBlockingDeque,
                              WakeUpNative wakeUpNative,
                              Handler handler) {
        this.linkedBlockingDeque = linkedBlockingDeque;
        this.wakeUpNative = wakeUpNative;
        this.handler = handler;
    }

    /**
     * 开始唤醒
     */
    public void startWakeUp() {
        if (isStart) {
            return;
        }
        isStart = true;
        this.start();
    }

    public boolean isStart() {
        return isStart;
    }

    /**
     * 停止唤醒
     */
    public void stopWakeUp() {
        isStart = false;
    }

    @Override
    public void run() {
        super.run();
        LogUtil.i(TAG, "wakeup wakeUpDecode start" );
        while (isStart) {
            try {
                byte[] data = linkedBlockingDeque.take();

                // 暂时不检测vad
                // int volume = calculateVolume(data, 16);
                // LogUtil.i(TAG, "wakeup audioRecord Volume:" + volume);
                // if (volume <= 0) {
                //  continue;
                // }

                if (data.length > 0) {
                    // 是否为最后一帧数据
                    boolean isEnd = false;
                    short[] arr = byteArray2ShortArray(data, data.length / 2);
                    int ret = wakeUpNative.wakeUpDecode(
                            arr,
                            arr.length,
                            "",
                            1,
                            -1,
                            true,
                            voiceOffset++,
                            isEnd
                    );
                    // 唤醒成功
                    if (ret == WAKEUP_SUCCEED) {
                        LogUtil.i(TAG, "wakeup wakeUpDecode ret:" + ret);
                        isWakeUp = true;
                        stopWakeUp();
                        LogUtil.i(TAG, "wakeup wakeUpDecode succeed !!");
                        break;
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        LogUtil.i(TAG, "wakeup after wakeUpDecode over !!");
        LogUtil.i(TAG, "wakeup after linkedBlockingDeque size:" + linkedBlockingDeque.size());

        if (isWakeUp) {
            if (listener != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onWakeUpSucceed();
                    }
                });
            }
        }
        // 重置
        voiceOffset = 0;
        isWakeUp = false;
    }

    /**
     * 将byte数组转为short数组
     *
     * @param data  byte数组的音频数据
     * @param items short数组的大小
     * @return 转换后的short数组
     */
    private short[] byteArray2ShortArray(byte[] data, int items) {
        short[] retVal = new short[items];
        for (int i = 0; i < retVal.length; i++) {
            retVal[i] = (short) ((data[i * 2] & 0xff) | (data[i * 2 + 1] & 0xff) << 8);
        }
        return retVal;
    }

    /**
     * 根据输入的音量大小来判断用户是否有语音输入
     *
     * @param var0 原始音频数据
     * @param var1 音频数据格式位PCM，16位每个样本，比如如果音频数据格式设为AudioFormat.ENCODING_PCM_16BIT，则参数传16
     * @return >0 代表有音频输入
     */
    private int calculateVolume(byte[] var0, int var1) {
        int[] var3 = null;
        int var4 = var0.length;
        int var2;
        if (var1 == 8) {
            var3 = new int[var4];
            for (var2 = 0; var2 < var4; ++var2) {
                var3[var2] = var0[var2];
            }
        } else if (var1 == 16) {
            var3 = new int[var4 / 2];
            for (var2 = 0; var2 < var4 / 2; ++var2) {
                byte var5 = var0[var2 * 2];
                byte var6 = var0[var2 * 2 + 1];
                int var13;
                if (var5 < 0) {
                    var13 = var5 + 256;
                } else {
                    var13 = var5;
                }
                short var7 = (short) (var13 + 0);
                if (var6 < 0) {
                    var13 = var6 + 256;
                } else {
                    var13 = var6;
                }
                var3[var2] = (short) (var7 + (var13 << 8));
            }
        }
        int[] var8 = var3;
        if (var3 != null && var3.length != 0) {
            float var10 = 0.0F;
            for (int var11 = 0; var11 < var8.length; ++var11) {
                var10 += (float) (var8[var11] * var8[var11]);
            }
            var10 /= (float) var8.length;
            float var12 = 0.0F;
            for (var4 = 0; var4 < var8.length; ++var4) {
                var12 += (float) var8[var4];
            }
            var12 /= (float) var8.length;
            var4 = (int) (Math.pow(2.0D, (double) (var1 - 1)) - 1.0D);
            double var14 = Math.sqrt((double) (var10 - var12 * var12));
            int var9;
            if ((var9 = (int) (10.0D * Math.log10(var14 * 10.0D * Math.sqrt(2.0D) / (double) var4 + 1.0D))) < 0) {
                var9 = 0;
            }
            if (var9 > 10) {
                var9 = 10;
            }
            return var9;
        } else {
            return 0;
        }
    }

    private IWakeUpListener listener;

    public void setWakeUpListener(IWakeUpListener listener) {
        this.listener = listener;
    }

    /**
     * 唤醒成功回调接口
     */
    public interface IWakeUpListener {
        /**
         * 唤醒成功后回调
         */
        void onWakeUpSucceed();
    }
}