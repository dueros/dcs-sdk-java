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

import android.content.Intent;
import android.text.TextUtils;

import com.baidu.duer.dcs.androidapp.DcsSampleApplication;
import com.baidu.duer.dcs.androidapp.DcsSampleOAuthActivity;

/**
 * 用户认证接口
 * <p>
 * Created by zhangyan42@baidu.com on 2017/6/8.
 */
public class OauthImpl implements IOauth {
    @Override
    public String getAccessToken() {
        return OauthPreferenceUtil.getAccessToken(DcsSampleApplication.getInstance());
    }

    @Override
    public void authorize() {
        Intent intent = new Intent(DcsSampleApplication.getInstance(), DcsSampleOAuthActivity.class);
        intent.putExtra("START_TAG", "RESTART");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        DcsSampleApplication.getInstance().startActivity(intent);
    }

    @Override
    public boolean isSessionValid() {
        String accessToken = getAccessToken();
        long createTime = OauthPreferenceUtil.getCreateTime(DcsSampleApplication.getInstance());
        long expires = OauthPreferenceUtil.getExpires(DcsSampleApplication.getInstance()) + createTime;
        return !TextUtils.isEmpty(accessToken) && expires != 0 && System.currentTimeMillis() < expires;
    }
}
