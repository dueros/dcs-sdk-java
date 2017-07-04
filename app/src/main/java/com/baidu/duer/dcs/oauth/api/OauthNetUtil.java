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

import android.os.Bundle;

import com.baidu.duer.dcs.util.CommonUtil;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Util类封装了一些基本的方法
 * <p>
 * Created by zhangyan42@baidu.com on 2017/5/19.
 */
public class OauthNetUtil {
    /**
     * 提取回调url中的token信息，用于User-Agent Flow中的授权操作
     *
     * @param url 回调的url，包括token信息
     * @return 返回bundle类型的token信息
     */
    public static Bundle parseUrl(String url) {
        Bundle ret;
        url = url.replace("bdconnect", "http");
        try {
            URL urlParam = new URL(url);
            ret = CommonUtil.decodeUrl(urlParam.getQuery());
            ret.putAll(CommonUtil.decodeUrl(urlParam.getRef()));
            return ret;
        } catch (MalformedURLException e) {
            return new Bundle();
        }
    }
}