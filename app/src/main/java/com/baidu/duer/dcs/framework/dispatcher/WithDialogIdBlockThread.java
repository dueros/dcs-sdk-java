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
package com.baidu.duer.dcs.framework.dispatcher;

import com.baidu.duer.dcs.devicemodule.voiceoutput.ApiConstants;
import com.baidu.duer.dcs.framework.DcsResponseDispatcher;
import com.baidu.duer.dcs.framework.message.DcsResponseBody;

import java.util.concurrent.BlockingQueue;

/**
 * 服务器返回的指令header中有dialogRequestId调度线程
 * <p>
 * Created by guxiuzhong@baidu.com on 2017/6/12.
 */
public class WithDialogIdBlockThread extends BaseBlockResponseThread {

    public WithDialogIdBlockThread(BlockingQueue<DcsResponseBody> responseBodyDeque,
                                   DcsResponseDispatcher.IDcsResponseHandler responseHandler,
                                   String threadName) {
        super(responseBodyDeque, responseHandler, threadName);
    }

    @Override
    boolean shouldBlock(DcsResponseBody responseBody) {
        // 如果是speak指令就立马阻塞
        String directiveName = responseBody.getDirective().getName();
        return directiveName != null && directiveName.length() > 0
                && directiveName.equals(ApiConstants.Directives.Speak.NAME);
    }
}
