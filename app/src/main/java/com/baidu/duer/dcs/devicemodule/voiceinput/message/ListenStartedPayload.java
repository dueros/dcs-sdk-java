/*
 * Copyright (c) 2017 Baidu, Inc. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.baidu.duer.dcs.devicemodule.voiceinput.message;

import com.baidu.duer.dcs.framework.message.Payload;

/**
 * ListenStarted事件对应的payload结构
 * <p>
 * Created by wuruisheng on 2017/6/1.
 */
public class ListenStartedPayload extends Payload {
    public static final String FORMAT = "AUDIO_L16_RATE_16000_CHANNELS_1";
    private String format;

    public ListenStartedPayload(String format) {
        this.format = format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getFormat() {
        return format;
    }
}