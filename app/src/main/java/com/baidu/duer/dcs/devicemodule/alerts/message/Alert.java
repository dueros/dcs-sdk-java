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

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Alert 数据对象（model）
 * <p>
 * Created by guxiuzhong@baidu.com on 2017/5/18.
 */
public class Alert {
    private final String token;
    private final SetAlertPayload.AlertType type;
    // 触发时间，ISO 8601格式
    private final String scheduledTime;

    @JsonCreator
    public Alert(@JsonProperty("token") String token, @JsonProperty("type") SetAlertPayload.AlertType type,
                 @JsonProperty("scheduledTime") String scheduledTime) {
        this.token = token;
        this.type = type;
        this.scheduledTime = scheduledTime;
    }

    public String getToken() {
        return this.token;
    }

    public SetAlertPayload.AlertType getType() {
        return this.type;
    }

    public String getScheduledTime() {
        return scheduledTime;
    }

    @Override
    public int hashCode() {
        return ((token == null) ? 0 : token.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Alert other = (Alert) obj;
        if (token == null) {
            if (other.token != null) {
                return false;
            }
        } else if (!token.equals(other.token)) {
            return false;
        }
        return true;
    }
}
