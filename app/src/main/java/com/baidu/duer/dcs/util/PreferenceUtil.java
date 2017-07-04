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
package com.baidu.duer.dcs.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;

import java.util.Map;

/**
 * 提供统一的Preference util方法
 * <p>
 * Created by zhangyan42@baidu.com on 2017/5/24.
 */
public class PreferenceUtil {
    private static final String APP_SHARD = "com.baidu.duer.dcs";

    /**
     * 保存数据的方法，拿到保存数据的具体类型，然后根据类型调用不同的保存方法
     *
     * @param context 上下文
     * @param key     key
     * @param object  value
     */
    public static void put(Context context, String key, Object object) {
        put(context, APP_SHARD, key, object);
    }

    public static void put(Context context, String spName, String key, Object object) {
        SharedPreferences sp = context.getSharedPreferences(spName,
                Context.MODE_PRIVATE);
        Editor editor = sp.edit();

        if (object instanceof String) {
            editor.putString(key, (String) object);
        } else if (object instanceof Integer) {
            editor.putInt(key, (Integer) object);
        } else if (object instanceof Boolean) {
            editor.putBoolean(key, (Boolean) object);
        } else if (object instanceof Float) {
            editor.putFloat(key, (Float) object);
        } else if (object instanceof Long) {
            editor.putLong(key, (Long) object);
        } else {
            editor.putString(key, object.toString());
        }
        editSubmit(editor);
    }

    private static void editSubmit(Editor editor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            editor.apply();
        } else {
            editor.commit();
        }
    }

    /**
     * 得到保存数据的方法，
     * 根据默认值得到保存的数据的具体类型，
     * 然后调用相对于的方法获取值
     *
     * @param context       上下文
     * @param key           key
     * @param defaultObject default-value
     */
    public static Object get(Context context, String key, Object defaultObject) {
        return get(context, APP_SHARD, key, defaultObject);
    }

    public static Object get(Context context, String spName, String key, Object defaultObject) {
        SharedPreferences sp = context.getSharedPreferences(spName,
                Context.MODE_PRIVATE);

        if (defaultObject instanceof String) {
            return sp.getString(key, (String) defaultObject);
        } else if (defaultObject instanceof Integer) {
            return sp.getInt(key, (Integer) defaultObject);
        } else if (defaultObject instanceof Boolean) {
            return sp.getBoolean(key, (Boolean) defaultObject);
        } else if (defaultObject instanceof Float) {
            return sp.getFloat(key, (Float) defaultObject);
        } else if (defaultObject instanceof Long) {
            return sp.getLong(key, (Long) defaultObject);
        }
        return null;
    }

    /**
     * 移除某个key值已经对应的值
     *
     * @param context 上下文
     * @param key     key
     */
    public static void remove(Context context, String key) {
        remove(context, APP_SHARD, key);
    }

    public static void remove(Context context, String spName, String key) {
        SharedPreferences sp = context.getSharedPreferences(spName,
                Context.MODE_PRIVATE);
        Editor editor = sp.edit();
        editor.remove(key);
        editSubmit(editor);
    }

    /**
     * 清除所有数据
     *
     * @param context 上下文
     */
    public static void clear(Context context) {
        clear(context, APP_SHARD);
    }

    public static void clear(Context context, String spName) {
        SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
        Editor editor = sp.edit();
        editor.clear();
        editSubmit(editor);
    }

    /**
     * 查询某个key是否已经存在
     *
     * @param context 上下文
     * @param key     key
     */
    public static boolean contains(Context context, String key) {
        return contains(context, APP_SHARD, key);
    }

    public static boolean contains(Context context, String spName, String key) {
        SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
        return sp.contains(key);
    }

    /**
     * 返回所有的键值对
     *
     * @param context 上下文
     */
    public static Map<String, ?> getAll(Context context) {
        return getAll(context, APP_SHARD);
    }

    public static Map<String, ?> getAll(Context context, String spName) {
        SharedPreferences sp = context.getSharedPreferences(spName,
                Context.MODE_PRIVATE);
        return sp.getAll();
    }
}
