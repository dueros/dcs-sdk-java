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
package com.baidu.duer.dcs.androidapp;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.RelativeLayout;

import com.baidu.duer.dcs.R;
import com.baidu.duer.dcs.androidsystemimpl.webview.BaseWebView;
import com.baidu.duer.dcs.devicemodule.screen.message.HtmlPayload;

/**
 * show html UI.
 * <p>
 * Created by zhangyan42@baidu.com on 2017/5/31.
 */
public class DcsSampleScreenHtmlActivity extends DcsSampleBaseActivity {
    private HtmlPayload htmlPayLoad;
    private RelativeLayout relativeLayout;
    private BaseWebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dcs_sample_activity_screen_html);
        htmlPayLoad = (HtmlPayload) this.getIntent().getSerializableExtra("HTML_PLAY_LOAD");
        initView();
        addView();
    }

    private void initView() {
        relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);
    }

    private void addView() {
        webView = new BaseWebView(DcsSampleScreenHtmlActivity.this.getApplicationContext());
        webView.setWebViewClientListen(new BaseWebView.WebViewClientListener() {
            @Override
            public BaseWebView.LoadingWebStatus shouldOverrideUrlLoading(WebView view, String url) {
                return BaseWebView.LoadingWebStatus.STATUS_UNKNOW;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {

            }

            @Override
            public void onPageFinished(WebView view, String url) {

            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {

            }
        });
        relativeLayout.addView(webView);
        if (null != htmlPayLoad) {
            webView.loadUrl(htmlPayLoad.getUrl());
        }
    }
}
