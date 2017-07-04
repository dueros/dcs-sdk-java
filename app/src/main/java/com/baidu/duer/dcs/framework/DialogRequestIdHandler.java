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

import java.util.UUID;

/**
 * 生成dialogRequestId
 * <p>
 * Created by wuruisheng on 2017/5/31.
 */
public class DialogRequestIdHandler {
    private String activeDialogRequestId;

    public DialogRequestIdHandler() {
    }

    public String createActiveDialogRequestId() {
        activeDialogRequestId = UUID.randomUUID().toString();
        return activeDialogRequestId;
    }

    /**
     * 判断当前dialogRequestId是否活跃的
     *
     * @param dialogRequestId dialogRequestId
     * @return dialogRequestId与activeDialogRequestId相等则返回true，否则返回false
     */
    public Boolean isActiveDialogRequestId(String dialogRequestId) {
        return activeDialogRequestId != null && activeDialogRequestId.equals(dialogRequestId);
    }
}
