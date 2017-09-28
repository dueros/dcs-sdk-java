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
package com.baidu.duer.dcs.devicemodule.screen.message;

import com.baidu.duer.dcs.framework.message.Payload;

import java.io.Serializable;

/**
 * RenderVoiceInputText指令的payload内容
 * <p>
 * Created by guxiuzhong@baidu.com on 2017/7/20.
 */
public class RenderVoiceInputTextPayload extends Payload implements Serializable {
    // 语音识别的结果
    public String text;
    // 用于表示query的状态,
    // 取值
    // "INTERMEDIATE": 中间结果
    // "FINAL": 最终结果
    public Type type;

    public enum Type {
        // 中间结果
        INTERMEDIATE,
        // 最终结果
        FINAL
    }

    public RenderVoiceInputTextPayload() {
    }

    public RenderVoiceInputTextPayload(String text, Type type) {
        this.text = text;
        this.type = type;
    }

    @Override
    public String toString() {
        return "RenderVoiceInputTextPayload{"
                + "text='" + text + '\''
                + ", type='" + type + '\''
                + '}';
    }
}