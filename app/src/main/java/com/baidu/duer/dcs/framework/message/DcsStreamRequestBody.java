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

import com.baidu.dcs.okhttp3.MediaType;
import com.baidu.dcs.okhttp3.RequestBody;
import com.baidu.duer.dcs.http.OkHttpMediaType;

import java.io.IOException;

import okio.BufferedSink;
import okio.Okio;
import okio.Pipe;

/**
 * 流请求消息体
 * <p>
 * Created by wuruisheng on 2017/6/1.
 */
public class DcsStreamRequestBody extends RequestBody {
    private final Pipe pipe = new Pipe(8192);
    private final BufferedSink mSink = Okio.buffer(pipe.sink());

    public BufferedSink sink() {
        return mSink;
    }

    @Override
    public MediaType contentType() {
        return OkHttpMediaType.MEDIA_STREAM_TYPE;
    }

    @Override
    public void writeTo(BufferedSink bufferedSink) throws IOException {
        bufferedSink.writeAll(pipe.source());
    }
}
