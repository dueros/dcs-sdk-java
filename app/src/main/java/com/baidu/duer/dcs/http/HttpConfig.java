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

import android.util.Log;

import com.baidu.duer.dcs.util.CommonUtil;
import com.baidu.duer.dcs.util.DcsVersion;
import com.baidu.duer.dcs.util.StandbyDeviceIdUtil;
import com.baidu.duer.dcs.util.SystemServiceManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * HttpConfig
 * <p>
 * Created by zhangyan42@baidu.com on 2017/6/1.
 */
public class HttpConfig {
    // 请求https
    public static final String HTTP_PREFIX = "https://";
    // 请求host
    public static final String HOST = "dueros-h2.baidu.com";
    public static String endpoint = null;
    // 请求event事件
    public static final String EVENTS = "/dcs/v1/events";
    // 请求directives事件
    public static final String DIRECTIVES = "/dcs/v1/directives";
    // ping
    public static final String PING = "/dcs/v1/ping";
    // 请求event事件TAG
    public static final String HTTP_EVENT_TAG = "event";
    // 请求voice
    public static final String HTTP_VOICE_TAG = "voice";
    // 请求directives事件TAG
    public static final String HTTP_DIRECTIVES_TAG = "directives";
    // 请求ping的TAG
    public static final String HTTP_PING_TAG = "ping";

    public static String getEndpoint() {
        if (null == endpoint || "".equals(endpoint)) {
            endpoint = HTTP_PREFIX + HOST;
        }
        return endpoint;
    }

    public static void setEndpoint(String endpoint) {
        HttpConfig.endpoint = endpoint;
    }

    public static String getEventsUrl() {
        return getEndpoint() + EVENTS;
    }

    public static String getDirectivesUrl() {
        return getEndpoint() + DIRECTIVES;
    }

    public static String getPingUrl() {
        return getEndpoint() + PING;
    }

    public static class HttpHeaders {
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String DUEROS_DEVICE_ID = "dueros-device-id";
        public static final String DUEROS_STANDBY_DEVICE_ID = "StandbyDeviceId";
        public static final String AUTHORIZATION = "Authorization";
        public static final String CONTENT_ID = "Content-ID";
        public static final String BEARER = "Bearer ";
        public static final String DEBUG = "debug";
        public static final String DEBUG_PARAM = "0";
        public static final String SAIYA_LOGID = "saiyalogid";
        public static final String USER_AGENT = "User-Agent";
    }

    public static class ContentTypes {
        public static final String JSON = "application/json";
        public static final String FORM_MULTIPART = "multipart/form-data; boundary=dumi-boundory";
        public static final String APPLICATION_JSON = JSON + ";" + " charset=UTF-8";
        public static final String APPLICATION_AUDIO = "application/octet-stream";
    }

    public static class Parameters {
        public static final String BOUNDARY = "boundary";
        public static final String DATA_METADATA = "metadata";
        public static final String DATA_AUDIO = "audio";
    }

    public static String accessToken = "";

    public static String getAccessToken() {
        return accessToken;
    }

    public static void setAccessToken(String accessToken) {
        HttpConfig.accessToken = accessToken;
    }

    /**
     * 获取dcs的headers
     *
     * @return header集合
     */
    public static Map<String, String> getDCSHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpConfig.HttpHeaders.AUTHORIZATION,
                HttpConfig.HttpHeaders.BEARER + getAccessToken());
        headers.put(HttpConfig.HttpHeaders.CONTENT_TYPE,
                HttpConfig.ContentTypes.FORM_MULTIPART);
        headers.put(HttpConfig.HttpHeaders.DUEROS_DEVICE_ID,
                CommonUtil.getDeviceUniqueID());
        headers.put(HttpConfig.HttpHeaders.DUEROS_STANDBY_DEVICE_ID,
                StandbyDeviceIdUtil.getStandbyDeviceId(SystemServiceManager.getAppContext()));
        headers.put(HttpConfig.HttpHeaders.DEBUG,
                HttpConfig.HttpHeaders.DEBUG_PARAM);
        // headers.put(HttpHeaders.DEBUG_BOSS, "nj03-rp-m22nlp156.nj03.baidu.com:8486");
        String logId = UUID.randomUUID().toString();
        Log.d("time", "logid：" + logId);
        headers.put(HttpHeaders.SAIYA_LOGID, logId);
        headers.put(HttpHeaders.USER_AGENT,
                "sampleapp/" + DcsVersion.VERSION_NAME);
        return headers;
    }
}