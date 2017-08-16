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

import com.baidu.duer.dcs.framework.message.AttachedContentPayload;
import com.baidu.duer.dcs.framework.message.DcsResponseBody;
import com.baidu.duer.dcs.framework.DialogRequestIdHandler;
import com.baidu.duer.dcs.framework.message.DialogRequestIdHeader;
import com.baidu.duer.dcs.framework.message.Directive;
import com.baidu.duer.dcs.framework.message.Header;
import com.baidu.duer.dcs.framework.message.Payload;
import com.baidu.duer.dcs.util.LogUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * 带有dialogRequestId response进入dependentQueue队列，否则进入independentQueue
 * 带有attached的directive需要知道找到对应的二进制数据才放到上述队列中
 * <p>
 * Created by wuruisheng on 2017/6/1.
 */
public class DcsResponseBodyEnqueue {
    private static final String TAG = DcsResponseBodyEnqueue.class.getSimpleName();
    private final DialogRequestIdHandler dialogRequestIdHandler;
    private final Queue<DcsResponseBody> dependentQueue;
    private final Queue<DcsResponseBody> independentQueue;
    private final Queue<DcsResponseBody> incompleteResponseQueue;
    private final Map<String, AudioData> audioDataMap;

    public DcsResponseBodyEnqueue(DialogRequestIdHandler dialogRequestIdHandler,
                                  Queue<DcsResponseBody> dependentQueue,
                                  Queue<DcsResponseBody> independentQueue) {
        this.dialogRequestIdHandler = dialogRequestIdHandler;
        this.dependentQueue = dependentQueue;
        this.independentQueue = independentQueue;
        incompleteResponseQueue = new LinkedList<>();
        audioDataMap = new HashMap<>();
    }

    public synchronized void handleResponseBody(DcsResponseBody responseBody) {
        if (responseBody.getDirective() == null) {
            return;
        }
        
        incompleteResponseQueue.add(responseBody);
        matchAudioDataWithResponseBody();
    }

    public synchronized void handleAudioData(AudioData audioData) {
        audioDataMap.put(audioData.contentId, audioData);
        matchAudioDataWithResponseBody();
    }

    private void matchAudioDataWithResponseBody() {
        for (DcsResponseBody responseBody : incompleteResponseQueue) {
            Directive directive = responseBody.getDirective();
            if (directive == null) {
                return;
            }

            Payload payload = responseBody.getDirective().payload;
            if (payload instanceof AttachedContentPayload) {
                AttachedContentPayload attachedContentPayload = (AttachedContentPayload) payload;
                String contentId = attachedContentPayload.getAttachedContentId();
                AudioData audioData = audioDataMap.remove(contentId);
                if (audioData != null) {
                    attachedContentPayload.setAttachedContent(contentId, audioData.partBytes);
                }
            }
        }

        findCompleteResponseBody();
    }

    private void findCompleteResponseBody() {
        Iterator<DcsResponseBody> iterator = incompleteResponseQueue.iterator();
        while (iterator.hasNext()) {
            DcsResponseBody responseBody = iterator.next();
            Payload payload = responseBody.getDirective().payload;
            if (payload instanceof AttachedContentPayload) {
                AttachedContentPayload attachedContentPayload = (AttachedContentPayload) payload;

                if (!attachedContentPayload.requiresAttachedContent()) {
                    // The front most directive IS complete.
                    enqueueResponseBody(responseBody);
                    iterator.remove();
                } else {
                    break;
                }
            } else {
                // Immediately enqueue any directive which does not contain audio content
                enqueueResponseBody(responseBody);
                iterator.remove();
            }
        }
    }

    private void enqueueResponseBody(DcsResponseBody responseBody) {
        LogUtil.d(TAG, "DcsResponseBodyEnqueue-RecordThread:" + responseBody.getDirective().rawMessage);
        Header header = responseBody.getDirective().header;
        DialogRequestIdHeader dialogRequestIdHeader = (DialogRequestIdHeader) header;
        if (dialogRequestIdHeader.getDialogRequestId() == null) {
            LogUtil.d(TAG, "DcsResponseBodyEnqueue-DialogRequestId  is null ,add to independentQueue");
            independentQueue.add(responseBody);
        } else if (dialogRequestIdHandler.isActiveDialogRequestId(dialogRequestIdHeader.getDialogRequestId())) {
            LogUtil.d(TAG, "DcsResponseBodyEnqueue-DialogRequestId  not  null,add to dependentQueue");
            dependentQueue.add(responseBody);
        }
    }
}
