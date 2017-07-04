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
package com.baidu.duer.dcs.systeminterface;

import com.baidu.duer.dcs.framework.IResponseListener;

/**
 * PlaybackController接口
 * <p>
 * Created by zhoujianliang01 on 2017/6/17.
 */
public interface IPlaybackController {
    void play(IResponseListener responseListener);

    void pause(IResponseListener responseListener);

    void previous(IResponseListener responseListener);

    void next(IResponseListener responseListener);

    void registerPlaybackListener(IPlaybackListener listener);

    interface IPlaybackListener {
        void onPlay(IResponseListener responseListener);

        void onPause(IResponseListener responseListener);

        void onPrevious(IResponseListener responseListener);

        void onNext(IResponseListener responseListener);
    }
}
