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
 * 带请求会话Id头部
 * <p>
 * Created by wuruisheng@baidu.com on 2017/5/31.
 */
public class DialogRequestIdHeader extends MessageIdHeader {
    // 请求会话id
    private String dialogRequestId;

    public DialogRequestIdHeader() {

    }

    public DialogRequestIdHeader(String nameSpace, String name, String dialogRequestId) {
        super(nameSpace, name);
        this.dialogRequestId = dialogRequestId;
    }

    public final String getDialogRequestId() {
        return dialogRequestId;
    }

    public final void setDialogRequestId(String dialogRequestId) {
        this.dialogRequestId = dialogRequestId;
    }

    @Override
    public String toString() {
        return String.format("%1$s dialogRequestId:%2$s", super.toString(), dialogRequestId);
    }
}