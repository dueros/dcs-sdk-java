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
 * 录音采集接口
 * <p>
 * Created by guxiuzhong@baidu.com on 2017/6/26.
 */
public interface IAudioRecord {
    /**
     * 开始录音，采集音频数据
     */
    void startRecord();

    /**
     * 停止录音
     */
    void stopRecord();
}