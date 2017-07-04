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
package com.baidu.duer.dcs.devicemodule.alerts.message;

import com.baidu.duer.dcs.framework.message.Payload;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;

/**
 * SetAlert指令对应的payload结构
 * <p>
 * Created by guxiuzhong@baidu.com on 2017/5/17.
 */
public class SetAlertPayload extends Payload implements Serializable {
    public enum AlertType {
        ALARM,
        TIMER
    }

    // alert唯一token
    private String token;
    private AlertType type;
    // 格式：2017-05-17T03:00:00+0000
    private String scheduledTime;
    private String content;

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setType(String type) {
        this.type = AlertType.valueOf(type.toUpperCase());
    }

    public AlertType getType() {
        return type;
    }

    @JsonProperty("scheduledTime")
    public void setScheduledTime(String dateTime) {
        scheduledTime = dateTime;
    }

    public String getScheduledTime() {
        return scheduledTime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "SetAlertPayload{"
                + "token='"
                + token
                + '\''
                + ", type="
                + type
                + ", scheduledTime='"
                + scheduledTime
                + '\''
                + '}';
    }
}
