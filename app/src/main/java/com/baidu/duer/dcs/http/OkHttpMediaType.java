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
package com.baidu.duer.dcs.http;


import com.baidu.dcs.okhttp3.MediaType;

import static com.baidu.duer.dcs.http.HttpConfig.ContentTypes.APPLICATION_AUDIO;
import static com.baidu.duer.dcs.http.HttpConfig.ContentTypes.APPLICATION_JSON;

/**
 * okhttp media_type
 * <p>
 * Created by zhangyan42@baidu.com on 2017/6/1.
 */
public class OkHttpMediaType {
    // json类型
    public static final MediaType MEDIA_JSON_TYPE = MediaType.parse(APPLICATION_JSON);
    // 数据流类型
    public static final MediaType MEDIA_STREAM_TYPE = MediaType.parse(APPLICATION_AUDIO);
}