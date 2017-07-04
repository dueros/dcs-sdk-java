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
package com.baidu.duer.dcs.systeminterface;

/**
 * WebView接口
 * <p>
 * Created by zhoujianliang01 on 2017/6/17.
 */
public interface IWebView {
    void loadUrl(String url);
    void linkClicked(String url);

    void addWebViewListener(IWebViewListener listener);

    interface IWebViewListener {
        void onLinkClicked(String url);
    }
}
