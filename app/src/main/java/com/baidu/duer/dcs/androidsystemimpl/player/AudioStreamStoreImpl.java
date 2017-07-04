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
package com.baidu.duer.dcs.androidsystemimpl.player;

import org.json.JSONObject;

import java.io.InputStream;

/**
 * 保存音频mp3流到文件中-android中实现
 * <p>
 * Created by guxiuzhong@baidu.com on 2017/6/1.
 */
public class AudioStreamStoreImpl implements IAudioStreamStore {
    private AudioStoreThread mAudioStoreThread;
    private OnStoreListener onStoreListener;

    @Override
    public void save(InputStream inputStream) {
        mAudioStoreThread = new AudioStoreThread(inputStream);
        mAudioStoreThread.setOnDownListener(simpleOnDownListener);
        mAudioStoreThread.start();
    }

    @Override
    public void cancel() {
        if (mAudioStoreThread != null) {
            mAudioStoreThread.stopDown();
        }
    }

    @Override
    public void speakAfter() {
        if (mAudioStoreThread != null) {
            mAudioStoreThread.delDownFile();
        }
    }

    @Override
    public void setOnStoreListener(OnStoreListener listener) {
        onStoreListener = listener;
    }

    private AudioStoreThread.SimpleOnDownListener simpleOnDownListener = new AudioStoreThread.SimpleOnDownListener() {
        @Override
        public void onDownComplete(String path) {
            super.onDownComplete(path);
            if (onStoreListener != null) {
                onStoreListener.onComplete(path);
            }
        }

        @Override
        public void onDownError(JSONObject jsonObject) {
            super.onDownError(jsonObject);
            if (onStoreListener != null) {
                onStoreListener.onError(jsonObject.toString());
            }
        }
    };
}
