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
package com.baidu.duer.dcs.devicemodule.speakcontroller.message;

import com.baidu.duer.dcs.framework.message.Payload;

/**
 * setMuted指令对应的Payload结构
 * <p>
 * Created by wuruisheng on 2017/6/5.
 */
public class SetMutePayload extends Payload {
    private boolean mute;

    public SetMutePayload() {
    }

    public SetMutePayload(boolean mute) {
        this.mute = mute;
    }

    public void setMute(boolean mute) {
        this.mute = mute;
    }

    public boolean getMute() {
        return this.mute;
    }
}