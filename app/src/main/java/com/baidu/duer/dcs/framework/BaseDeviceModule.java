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

import com.baidu.duer.dcs.devicemodule.system.HandleDirectiveException;
import com.baidu.duer.dcs.framework.message.ClientContext;
import com.baidu.duer.dcs.framework.message.Directive;

/**
 * 定义deviceModule端状态和处理指令的接口
 * <p>
 * Created by wuruisheng on 2017/5/31.
 */
public abstract class BaseDeviceModule {
    private final String nameSpace;
    protected final IMessageSender messageSender;

    public BaseDeviceModule(String nameSpace) {
        this(nameSpace, null);
    }

    public BaseDeviceModule(String nameSpace, IMessageSender messageSender) {
        this.nameSpace = nameSpace;
        this.messageSender = messageSender;
    }

    /**
     * 端状态：服务端在处理某些事件时，需要了解在请求当时设备端各模块所处的状态。比如端上是否正在播放音乐，
     * 是否有闹钟在响，是否正在播报等等
     *
     * @return 端状态
     */
    public abstract ClientContext clientContext();

    /**
     * 处理服务端下发的指令
     *
     * @param directive
     * @throws HandleDirectiveException
     */
    public abstract void handleDirective(Directive directive) throws HandleDirectiveException;

    public abstract void release();

    public String getNameSpace() {
        return nameSpace;
    }
}