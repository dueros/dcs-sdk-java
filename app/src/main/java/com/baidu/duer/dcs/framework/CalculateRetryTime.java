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
package com.baidu.duer.dcs.framework;

/**
 * Created by wuruisheng on 2017/10/31.
 */

public class CalculateRetryTime {

    // 重连DCS时间间隔（ms），同时进行随机分布，防止dos攻击
    private static final int[] RETRY_TIME = {250, 1000, 3000, 5000, 10000, 20000, 30000, 60000};
    static final double RETRY_RANDOMIZATION_FACTOR = 0.5;
    static final double RETRY_DECREASE_FACTOR = 1 / (RETRY_RANDOMIZATION_FACTOR + 1);
    static final double RETRY_INCREASE_FACTOR = (RETRY_RANDOMIZATION_FACTOR + 1);
    private int retryCount = 0;

    public CalculateRetryTime() {

    }

    public void reset() {
        retryCount = 0;
    }

    public int getRetryTime() {
        final int length = RETRY_TIME.length;

        if (retryCount >= length) {
            retryCount = length - 1;
        }

        int retryTime = RETRY_TIME[retryCount];
        ++retryCount;
        double min = retryTime * RETRY_DECREASE_FACTOR;
        double max = retryTime * RETRY_INCREASE_FACTOR;

        int delayTime = (int) (min + (Math.random() * (max - min)));
        return delayTime;
    }
}