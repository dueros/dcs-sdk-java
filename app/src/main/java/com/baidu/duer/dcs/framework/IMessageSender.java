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
package com.baidu.duer.dcs.framework;

import com.baidu.duer.dcs.framework.message.DcsStreamRequestBody;
import com.baidu.duer.dcs.framework.message.Event;

/**
 * 发送event接口
 * <p>
 * Created by wuruisheng on 2017/6/15.
 */
public interface IMessageSender {
    /**
     * 发送event
     *
     * @param event
     */
    void sendEvent(Event event);

    /**
     * 发送event
     *
     * @param event
     * @param responseListener 回调
     */
    void sendEvent(Event event, IResponseListener responseListener);

    /**
     * 发送event且带有stream
     *
     * @param event             event对象
     * @param streamRequestBody stream对象
     * @param responseListener  回调
     */
    void sendEvent(Event event, DcsStreamRequestBody streamRequestBody, IResponseListener responseListener);

    /**
     * 发送event带上clientContext
     *
     * @param event            event对象
     * @param responseListener 回调
     */
    void sentEventWithClientContext(Event event, IResponseListener responseListener);
}