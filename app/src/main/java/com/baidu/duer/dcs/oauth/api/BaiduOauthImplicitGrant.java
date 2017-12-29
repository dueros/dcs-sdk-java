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

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieSyncManager;

import com.baidu.duer.dcs.oauth.api.BaiduDialog.BaiduDialogListener;
import com.baidu.duer.dcs.util.CommonUtil;
import com.baidu.duer.dcs.util.LogUtil;

/**
 * 封装了oauth2授权，我们采用的是百度Oauth的implicit grant的方式
 * 该方式的地址：http://developer.baidu.com/wiki/index.php?title=docs/oauth/implicit
 * <p>
 * Created by zhangyan42@baidu.com on 2017/5/24.
 * TODO: 百度Oauth2授权方式完善
 */
public class BaiduOauthImplicitGrant implements Parcelable {
    private static final String LOG_TAG = "BaiduOauth";
    public static final String CANCEL_URI = "bdconnect://cancel";
    // 百度Oauth授权回调需要在DUEROS开放平台的控制平台
    // 应用编辑-->>OAUTH CONFIG URL的链接地址-->>授权回调页-->>安全设置-->>授权回调页
    // 需要注意
    public static final String SUCCESS_URI = "bdconnect://success";
    private static final String OAUTHORIZE_URL = "https://openapi.baidu.com/oauth/2.0/authorize";
    // 账号登录
    private static final String DISPLAY_STRING = "mobile";
    // 扫码登录
    // private static final String DISPLAY_STRING = "popup";
    private static final String[] DEFAULT_PERMISSIONS = {"basic"};
    private static final String KEY_CLIENT_ID = "clientId";
    // 应用注册的api key信息
    private String cliendId;
    private AccessTokenManager accessTokenManager;

    /**
     * 使用应用的基本信息构建Baidu对象
     *
     * @param clientId 应用注册的api key信息
     * @param context  当前应用的上下文环境
     */
    public BaiduOauthImplicitGrant(String clientId, Context context) {
        if (clientId == null) {
            throw new IllegalArgumentException("apiKey信息必须提供！");
        }
        this.cliendId = clientId;
        init(context);
    }

    /**
     * 使用Parcel流构建Baidu对象
     *
     * @param in Parcel流信息
     */
    public BaiduOauthImplicitGrant(Parcel in) {
        Bundle bundle = Bundle.CREATOR.createFromParcel(in);
        this.cliendId = bundle.getString(KEY_CLIENT_ID);
        this.accessTokenManager = AccessTokenManager.CREATOR.createFromParcel(in);
    }

    /**
     * 初始化accesTokenManager等信息
     *
     * @param context 当前执行的上下文环境
     */
    public void init(Context context) {
        if (context.checkCallingOrSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            Log.w(LOG_TAG, "App miss permission android.permission.ACCESS_NETWORK_STATE! "
                    + "Some mobile's WebView don't display page!");
        }
        this.accessTokenManager = new AccessTokenManager(context);
        this.accessTokenManager.initToken();
    }

    /**
     * 完成登录并获取token信息(User-Agent Flow)，该方法使用默认的用户权限
     *
     * @param activity       需要展示Dialog UI的Activity
     * @param isForceLogin   是否强制登录，如果该参数为true的话，会强制用户进行登录。
     * @param isConfirmLogin 是否确认登录，如果该参数为true的话，如果用户当前为登录状态，
     *                       则会显示用户的登录头像，点击头像后完成登录操作
     * @param listener       Dialog回调接口如Activity跳转
     */
    public void authorize(Activity activity,
                          boolean isForceLogin,
                          boolean isConfirmLogin,
                          final BaiduDialogListener listener) {
        this.authorize(activity, null, isForceLogin, isConfirmLogin, listener);
    }

    /**
     * 根据相应的permissions信息，完成登录并获取token信息(User-Agent Flow)
     *
     * @param activity     需要展示Dialog UI的Activity
     * @param permissions  需要获得的授权权限信息
     * @param isForceLogin 是否强制登录，如果该参数为true的话，会强制用户进行登录。
     * @param listener     回调的listener接口，如Activity跳转等
     */
    private void authorize(Activity activity,
                           String[] permissions,
                           boolean isForceLogin,
                           boolean isConfirmLogin,
                           final BaiduDialogListener listener) {
        if (this.isSessionValid()) {
            listener.onComplete(new Bundle());
            return;
        }

        // 使用匿名的BaiduDialogListener对listener进行了包装，并进行一些存储token信息和当前登录用户的逻辑，
        // 外部传进来的listener信息不需要在进行存储相关的逻辑
        this.authorize(activity,
                permissions,
                isForceLogin,
                isConfirmLogin,
                new BaiduDialogListener() {
                    @Override
                    public void onError(BaiduDialogError e) {
                        LogUtil.d(LOG_TAG, "DialogError " + e);
                        listener.onError(e);
                    }

                    @Override
                    public void onComplete(Bundle values) {
                        // 存储相应的token信息
                        getAccessTokenManager().storeToken(values);
                        // 完成授权操作，使用listener进行回调，eg。跳转到其他的activity
                        listener.onComplete(values);
                    }

                    @Override
                    public void onCancel() {
                        LogUtil.d(LOG_TAG, "login cancel");
                        listener.onCancel();
                    }

                    @Override
                    public void onBaiduException(BaiduException e) {
                        Log.d(LOG_TAG, "BaiduException : " + e);
                        listener.onBaiduException(e);
                    }
                }, SUCCESS_URI, "token");
    }

    /**
     * 通过Dialog UI展示用户登录、授权页
     *
     * @param activity     需要展示Dialog UI的Activity
     * @param permissions  需要请求的环境
     * @param listener     用于回调的listener接口方法
     * @param redirectUrl  回调地址
     * @param responseType 授权请求的类型
     */
    private void authorize(Activity activity,
                           String[] permissions,
                           boolean isForceLogin,
                           boolean isConfirmLogin,
                           final BaiduDialogListener listener,
                           String redirectUrl, String responseType) {
        CookieSyncManager.createInstance(activity);
        Bundle params = new Bundle();
        params.putString("client_id", this.cliendId);
        params.putString("redirect_uri", redirectUrl);
        params.putString("response_type", responseType);
        params.putString("display", DISPLAY_STRING);
        if (isForceLogin) {
            params.putString("force_login", "1");
        }
        if (isConfirmLogin) {
            params.putString("confirm_login", "1");
        }
        if (permissions == null) {
            permissions = DEFAULT_PERMISSIONS;
        }
        if (permissions != null && permissions.length > 0) {
            String scope = TextUtils.join(" ", permissions);
            params.putString("scope", scope);
        }
        params.putString("qrcode", "1");
        String url = OAUTHORIZE_URL + "?" + CommonUtil.encodeUrl(params);
        LogUtil.d(LOG_TAG, "url:" + url);
        if (activity.checkCallingOrSelfPermission(Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            CommonUtil.showAlert(activity, "没有权限", "应用需要访问互联网的权限");
        } else {
            new BaiduDialog(activity, url, listener).show();
        }
    }

    /**
     * 将清除存储的token信息
     */
    public void clearAccessToken() {
        if (this.accessTokenManager != null) {
            this.accessTokenManager.clearToken();
            this.accessTokenManager = null;
        }
    }

    /**
     * 判断token信息是否有效。
     *
     * @return boolean true/false
     */
    public boolean isSessionValid() {
        return this.accessTokenManager.isSessionValid();
    }

    /**
     * 获取AccessTokenManager对象
     *
     * @return accessTokenManager对象
     */
    public AccessTokenManager getAccessTokenManager() {
        return this.accessTokenManager;
    }

    /**
     * 获取AccessToken信息
     *
     * @return accessToken信息
     */
    public String getAccessToken() {
        return this.accessTokenManager.getAccessToken();
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.os.Parcelable#describeContents()
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_CLIENT_ID, this.cliendId);
        bundle.writeToParcel(dest, flags);
        this.accessTokenManager.writeToParcel(dest, flags);
    }

    public static final Creator<BaiduOauthImplicitGrant> CREATOR = new Creator<BaiduOauthImplicitGrant>() {
        public BaiduOauthImplicitGrant createFromParcel(Parcel in) {
            return new BaiduOauthImplicitGrant(in);
        }

        public BaiduOauthImplicitGrant[] newArray(int size) {
            return new BaiduOauthImplicitGrant[size];
        }
    };
}