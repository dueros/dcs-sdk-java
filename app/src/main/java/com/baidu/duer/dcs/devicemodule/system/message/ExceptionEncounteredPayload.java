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
package com.baidu.duer.dcs.devicemodule.system.message;

import com.baidu.duer.dcs.devicemodule.system.HandleDirectiveException.ExceptionType;
import com.baidu.duer.dcs.framework.message.Payload;

/**
 * ExceptionEncountered事件对应的payload结构
 * <p>
 * Created by wuruisheng on 2017/6/3.
 */
public class ExceptionEncounteredPayload extends Payload {
    private String unparsedDirective;
    private Error error;

    public ExceptionEncounteredPayload(String unparsedDirective, ExceptionType type, String message) {
        this.unparsedDirective = unparsedDirective;
        Error error = new Error(type, message);
        this.error = error;
    }

    public void setUnparsedDirective(String unparsedDirective) {
        this.unparsedDirective = unparsedDirective;
    }

    public String getUnparsedDirective() {
        return unparsedDirective;
    }

    public void setError(Error error) {
        this.error = error;
    }

    public Error getError() {
        return error;
    }

    public static class Error {
        public ExceptionType type;
        public String message;

        public Error(ExceptionType type, String message) {
            this.type = type;
            this.message = message;
        }
    }
}