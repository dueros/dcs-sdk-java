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

import org.codehaus.jackson.JsonProcessingException;

/**
 * json序列化成异常对象
 * <p>
 * Created by wuruisheng on 2017/6/8.
 */
public class DcsJsonProcessingException extends JsonProcessingException {
    private String unparsedCotent;

    public DcsJsonProcessingException(String message, JsonProcessingException exception, String unparsedCotent) {
        super(message, exception);
        this.unparsedCotent = unparsedCotent;
    }

    public String getUnparsedCotent() {
        return unparsedCotent;
    }
}