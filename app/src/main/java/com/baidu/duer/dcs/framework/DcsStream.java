/*
 *
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

import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by wuruisheng on 2017/7/28.
 */
public class DcsStream {
    public String token;
    public final LinkedBlockingDeque<byte[]> dataQueue = new LinkedBlockingDeque<>();
    public volatile boolean isFin;
    // 采样率
    public int sampleRate;
    // 声道数
    public int channels;

    public boolean isFinished() {
        return isFin && dataQueue.size() == 0;
    }
}