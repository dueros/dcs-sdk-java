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
package com.baidu.duer.dcs.framework.message;

/**
 * 带有header和payload的消息体基类
 * <p>
 * Created by guxiuzhong@baidu.com on 2017/6/1.
 */
public class BaseMessage {
    private Header header;
    private Payload payload;

    public BaseMessage(Header header, Payload payload) {
        this.header = header;
        this.payload = payload;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public Header getHeader() {
        return header;
    }

    public void setPayload(Payload payload) {
        this.payload = payload;
    }

    public Payload getPayload() {
        return payload;
    }
}