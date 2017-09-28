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
package com.baidu.duer.dcs.devicemodule.voiceoutput.message;

import com.baidu.duer.dcs.framework.message.AttachedContentPayload;
import com.baidu.duer.dcs.framework.message.Payload;
import com.baidu.duer.dcs.framework.DcsStream;

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * Speak指令对应的payload结构
 * <p>
 * Created by wuruisheng on 2017/6/1.
 */
public class SpeakPayload extends Payload implements AttachedContentPayload {
    public String url;
    public String format;
    public String token;

    @JsonIgnore
    public DcsStream dcsStream;

    // start with cid:
    public void setUrl(String url) {
        this.url = url.substring(4);
    }

    @Override
    public boolean requiresAttachedContent() {
        return !hasAttachedContent();
    }

    @Override
    public boolean hasAttachedContent() {
        return dcsStream != null;
    }

    @Override
    public String getAttachedContentId() {
        return url;
    }

    @Override
    public DcsStream getAttachedContent() {
        return dcsStream;
    }

    @Override
    public void setAttachedContent(String cid, DcsStream dcsStream) {
        if (getAttachedContentId().equals(cid)) {
            this.dcsStream = dcsStream;
        } else {
            throw new IllegalArgumentException(
                    "Tried to add the wrong audio content to a Speak directive. This cid: "
                            + getAttachedContentId() + " other cid: " + cid);
        }
    }
}