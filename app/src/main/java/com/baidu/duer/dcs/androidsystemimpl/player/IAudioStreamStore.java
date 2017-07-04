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

import java.io.InputStream;

/**
 * mp3流保存接口
 * <p>
 * Created by guxiuzhong@baidu.com on 2017/6/1.
 */
public interface IAudioStreamStore {
    /**
     * 保存mp3流到文件中(存储设备中)
     */
    void save(InputStream inputStream);

    /**
     * 取消保存
     */
    void cancel();

    /**
     * 播报完成后处理的操作，比如删除保存的文件
     */
    void speakAfter();

    /**
     * 保存回调监听
     *
     * @param listener listener
     */
    void setOnStoreListener(OnStoreListener listener);

    /**
     * 保存回调接口
     */
    interface OnStoreListener {
        void onStart();

        void onComplete(String path);

        void onError(String errorMessage);
    }

    class SimpleOnStoreListener implements OnStoreListener {
        @Override
        public void onStart() {
        }

        @Override
        public void onComplete(String path) {
        }

        @Override
        public void onError(String errorMessage) {
        }
    }
}
