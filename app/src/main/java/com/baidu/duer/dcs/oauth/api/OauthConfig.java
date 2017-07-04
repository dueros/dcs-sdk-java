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
package com.baidu.duer.dcs.oauth.api;

/**
 * OauthConfig
 * <p>
 * Created by zhangyan42@baidu.com on 2017/6/3.
 */
public class OauthConfig {
    public static class BundleKey {
        // 在多个Activity中传递AccessTokenManager的键值
        public static final String KEY_ACCESS_TOKEN = "baidu_token_manager_access_token";
        public static final String KEY_EXPIRE_TIME = "baidu_token_manager_expire_time";
    }

    public static class PrefenenceKey {
        // 持久化token信息的各种监制
        public static final String SP_ACCESS_TOKEN = "baidu_oauth_config_prop_access_token";
        public static final String SP_CREATE_TIME = "baidu_oauth_config_prop_create_time";
        public static final String SP_EXPIRE_SECONDS = "baidu_oauth_config_prop_expire_secends";
    }
}