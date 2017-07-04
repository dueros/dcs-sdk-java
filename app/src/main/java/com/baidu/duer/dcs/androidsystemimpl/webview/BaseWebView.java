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
package com.baidu.duer.dcs.androidsystemimpl.webview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.util.AttributeSet;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.baidu.duer.dcs.systeminterface.IWebView;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * common webView
 * <p>
 * Created by zhangyan42@baidu.com on 2017/5/19.
 */
@SuppressLint("SetJavaScriptEnabled")
public class BaseWebView extends WebView implements IWebView {
    private final List<IWebViewListener> webViewListeners =
            Collections.synchronizedList(new ArrayList<IWebViewListener>());

    public BaseWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BaseWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public BaseWebView(Context context) {
        super(context);
        init(context);
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    private void init(Context context) {
        this.setVerticalScrollBarEnabled(false);
        this.setHorizontalScrollBarEnabled(false);
        if (Build.VERSION.SDK_INT < 19) {
            removeJavascriptInterface("searchBoxJavaBridge_");
        }

        WebSettings localWebSettings = this.getSettings();
        try {
            // 禁用file协议,http://www.tuicool.com/articles/Q36ZfuF, 防止Android WebView File域攻击
            localWebSettings.setAllowFileAccess(false);
            localWebSettings.setSupportZoom(false);
            localWebSettings.setBuiltInZoomControls(false);
            localWebSettings.setUseWideViewPort(true);
            localWebSettings.setDomStorageEnabled(true);
            localWebSettings.setLoadWithOverviewMode(true);
            localWebSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
            localWebSettings.setPluginState(PluginState.ON);
            // 启用数据库
            localWebSettings.setDatabaseEnabled(true);
            // 设置定位的数据库路径
            String dir = context.getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath();
            localWebSettings.setGeolocationDatabasePath(dir);
            localWebSettings.setGeolocationEnabled(true);
            localWebSettings.setJavaScriptEnabled(true);
            localWebSettings.setSavePassword(false);
            String agent = localWebSettings.getUserAgentString();

            localWebSettings.setUserAgentString(agent);
            // setCookie(context, ".baidu.com", bdussCookie);

        } catch (Exception e1) {
            e1.printStackTrace();
        }
        this.setWebViewClient(new BridgeWebViewClient());
    }

    private void setCookie(Context context, String domain, String sessionCookie) {
        CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        if (sessionCookie != null) {
            // delete old cookies
            cookieManager.removeSessionCookie();
        }
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        cookieManager.setCookie(domain, sessionCookie);

        CookieSyncManager.createInstance(context);
        CookieSyncManager.getInstance().sync();
    }

    public void setDownloadListener(DownloadListener listener) {
        super.setDownloadListener(listener);
    }

    private WebViewClientListener webViewClientListen;

    public void setWebViewClientListen(WebViewClientListener webViewClientListen) {
        this.webViewClientListen = webViewClientListen;
    }

    /**
     * 枚举网络加载返回状态 STATUS_FALSE:false
     * STATUS_TRUE:true
     * STATUS_UNKNOW:不知道
     * NET_UNKNOWN:未知网络
     */
    public enum LoadingWebStatus {
        STATUS_FALSE, STATUS_TRUE, STATUS_UNKNOW
    }

    @Override
    public void linkClicked(String url) {
        fireLinkClicked(url);
    }

    @Override
    public void addWebViewListener(IWebViewListener listener) {
        this.webViewListeners.add(listener);
    }

    void fireLinkClicked(String url) {
        for (IWebViewListener listener : webViewListeners) {
            listener.onLinkClicked(url);
        }
    }

    public interface WebViewClientListener {
        LoadingWebStatus shouldOverrideUrlLoading(WebView view, String url);

        void onPageStarted(WebView view, String url, Bitmap favicon);

        void onPageFinished(WebView view, String url);

        void onReceivedError(WebView view, int errorCode, String description, String failingUrl);
    }

    public class BridgeWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            LoadingWebStatus loadWebStatus = LoadingWebStatus.STATUS_UNKNOW;
            String mUrl = url;
            try {
                mUrl = URLDecoder.decode(url, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            if (null != webViewClientListen) {
                loadWebStatus = webViewClientListen.shouldOverrideUrlLoading(view, url);
            }
            if (LoadingWebStatus.STATUS_FALSE == loadWebStatus) {
                return false;
            } else if (LoadingWebStatus.STATUS_TRUE == loadWebStatus) {
                return true;
            } else {
                return super.shouldOverrideUrlLoading(view, url);
            }
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            if (null != webViewClientListen) {
                webViewClientListen.onPageStarted(view, url, favicon);
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (null != webViewClientListen) {
                webViewClientListen.onPageFinished(view, url);
            }
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            if (null != webViewClientListen) {
                webViewClientListen.onReceivedError(view, errorCode, description, failingUrl);
            }
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            // 当发生证书认证错误时，采用默认的处理方法handler.cancel()，停止加载问题页面
            handler.cancel();
        }
    }
}