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

import com.baidu.duer.dcs.framework.DcsResponseDispatcher;
import com.baidu.duer.dcs.framework.message.DcsResponseBody;

import java.util.concurrent.BlockingQueue;

/**
 * 单独线程阻塞控制response回调处理
 * <p>
 * Created by wuruisheng on 2017/6/1.
 */
public abstract class BaseBlockResponseThread extends Thread {
    private static final String TAG = BaseBlockResponseThread.class.getSimpleName();
    private BlockingQueue<DcsResponseBody> responseBodyDeque;
    private DcsResponseDispatcher.IDcsResponseHandler responseHandler;
    private volatile boolean block;
    private volatile boolean isStop;

    public BaseBlockResponseThread(BlockingQueue<DcsResponseBody> responseBodyDeque,
                                   DcsResponseDispatcher.IDcsResponseHandler responseHandler, String threadName) {
        this.responseBodyDeque = responseBodyDeque;
        this.responseHandler = responseHandler;
        setName(threadName);
    }

    public synchronized void block() {
        block = true;
    }

    public synchronized void unblock() {
        block = false;
        notify();
    }

    public synchronized void clear() {
        responseBodyDeque.clear();
    }

    public synchronized void stopThread() {
        clear();
        isStop = true;
        this.interrupt();
    }

    @Override
    public void run() {
        while (!isStop) {
            try {
                synchronized (this) {
                    if (block) {
                        wait();
                    }
                }

                if (responseHandler != null) {
                    DcsResponseBody responseBody = responseBodyDeque.take();
                    responseHandler.onResponse(responseBody);

                    if (shouldBlock(responseBody)) {
                        block = true;
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    abstract boolean shouldBlock(DcsResponseBody responseBody);
}
