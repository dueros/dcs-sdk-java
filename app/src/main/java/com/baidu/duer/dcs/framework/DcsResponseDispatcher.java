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

import com.baidu.duer.dcs.framework.dispatcher.AudioData;
import com.baidu.duer.dcs.framework.dispatcher.DcsResponseBodyEnqueue;
import com.baidu.duer.dcs.framework.dispatcher.WithDialogIdBlockThread;
import com.baidu.duer.dcs.framework.dispatcher.WithoutDialogIdBlockThread;
import com.baidu.duer.dcs.framework.message.DcsResponseBody;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * response调度中心，带有dialogRequestId directives按照顺序执行，对于speak指令播报完才执行下一个指令；
 * 设备端收到不带dialogRequestId的directives立即执行
 * <p>
 * Created by wuruisheng on 2017/6/1.
 */
public class DcsResponseDispatcher {
    private final WithDialogIdBlockThread withDialogIdBlockThread;
    private final WithoutDialogIdBlockThread withoutDialogIdBlockThread;
    private final BlockingQueue<DcsResponseBody> dependentQueue;
    private final BlockingQueue<DcsResponseBody> independentQueue;
    private final DcsResponseBodyEnqueue dcsResponseBodyEnqueue;
    private final IDcsResponseHandler responseHandler;

    public DcsResponseDispatcher(final DialogRequestIdHandler dialogRequestIdHandler,
                                 final IDcsResponseHandler responseHandler) {
        this.responseHandler = responseHandler;
        dependentQueue = new LinkedBlockingDeque<>();
        independentQueue = new LinkedBlockingDeque<>();
        dcsResponseBodyEnqueue = new DcsResponseBodyEnqueue(dialogRequestIdHandler, dependentQueue,
                independentQueue);

        withDialogIdBlockThread = new WithDialogIdBlockThread(dependentQueue, responseHandler,
                "withDialogIdBlockThread");
        withoutDialogIdBlockThread = new WithoutDialogIdBlockThread(independentQueue, responseHandler,
                "withoutDialogIdBlockThread");
        withDialogIdBlockThread.start();
        withoutDialogIdBlockThread.start();
    }

    public void interruptDispatch() {
        // 先清空队列，比如播放一首歌：speak+play指令组合的方式，在speak播报过程中进行打断，play就不需要执行了
        withDialogIdBlockThread.clear();
        withoutDialogIdBlockThread.clear();
        // 让其处于等待新的指令处理
        unBlockDependentQueue();
    }

    public void blockDependentQueue() {
        withDialogIdBlockThread.block();
        withoutDialogIdBlockThread.block();
    }

    public void unBlockDependentQueue() {
        withDialogIdBlockThread.unblock();
        withoutDialogIdBlockThread.unblock();
    }

    public void onResponseBody(DcsResponseBody responseBody) {
        dcsResponseBodyEnqueue.handleResponseBody(responseBody);
    }

    public void onAudioData(AudioData audioData) {
        dcsResponseBodyEnqueue.handleAudioData(audioData);
    }

    public void onParseFailed(String unParseMessage) {
        if (responseHandler != null) {
            responseHandler.onParseFailed(unParseMessage);
        }
    }

    public void release() {
        withDialogIdBlockThread.stopThread();
        withoutDialogIdBlockThread.stopThread();
    }

    public interface IDcsResponseHandler {
        void onResponse(DcsResponseBody responseBody);

        void onParseFailed(String unParseMessage);
    }
}
