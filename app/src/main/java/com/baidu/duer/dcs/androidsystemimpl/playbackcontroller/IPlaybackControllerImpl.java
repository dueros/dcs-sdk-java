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
package com.baidu.duer.dcs.androidsystemimpl.playbackcontroller;

import com.baidu.duer.dcs.framework.IResponseListener;
import com.baidu.duer.dcs.systeminterface.IPlaybackController;

/**
 * 播放控制
 * <p>
 * Created by zhoujianliang01@baidu.com on 2017/6/17.
 */
public class IPlaybackControllerImpl implements IPlaybackController {
    private IPlaybackListener playbackListener;

    @Override
    public void play(IResponseListener responseListener) {
        if (playbackListener == null) {
            return;
        }
        playbackListener.onPlay(responseListener);
    }

    @Override
    public void pause(IResponseListener responseListener) {
        if (playbackListener == null) {
            return;
        }
        playbackListener.onPause(responseListener);
    }

    @Override
    public void previous(IResponseListener responseListener) {
        if (playbackListener == null) {
            return;
        }
        playbackListener.onPrevious(responseListener);
    }

    @Override
    public void next(IResponseListener responseListener) {
        if (playbackListener == null) {
            return;
        }
        playbackListener.onNext(responseListener);
    }

    @Override
    public void registerPlaybackListener(IPlaybackListener listener) {
        this.playbackListener = listener;
    }
}
