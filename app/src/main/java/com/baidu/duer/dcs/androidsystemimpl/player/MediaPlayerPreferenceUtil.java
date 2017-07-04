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

import android.content.Context;

import com.baidu.duer.dcs.util.PreferenceUtil;

/**
 * Media Player 保存音量和静音的状态
 * <p>
 * Created by guxiuzhong@baidu.com on 2017/6/17.
 */
public class MediaPlayerPreferenceUtil extends PreferenceUtil {
    // 保存到的文件名字
    private static final String BAIDU_MEDIA_CONFIG = "baidu_media_config";

    /**
     * 保存音量或者静音的数据状态
     *
     * @param context 上下文
     * @param key     键
     * @param object  值
     */
    public static void put(Context context, String key, Object object) {
        put(context, BAIDU_MEDIA_CONFIG, key, object);
    }

    /**
     * 读取音量或者静音的数据状态
     *
     * @param context       上下文
     * @param key           键
     * @param defaultObject 默认值
     */
    public static Object get(Context context, String key, Object defaultObject) {
        return get(context, BAIDU_MEDIA_CONFIG, key, defaultObject);
    }
}
