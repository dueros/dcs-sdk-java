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
package com.baidu.duer.dcs.devicemodule.screen;

import com.baidu.duer.dcs.devicemodule.screen.message.HtmlPayload;
import com.baidu.duer.dcs.devicemodule.screen.message.LinkClickedPayload;
import com.baidu.duer.dcs.framework.BaseDeviceModule;
import com.baidu.duer.dcs.framework.IMessageSender;
import com.baidu.duer.dcs.framework.message.ClientContext;
import com.baidu.duer.dcs.framework.message.Directive;
import com.baidu.duer.dcs.framework.message.Event;
import com.baidu.duer.dcs.framework.message.Header;
import com.baidu.duer.dcs.framework.message.MessageIdHeader;
import com.baidu.duer.dcs.framework.message.Payload;
import com.baidu.duer.dcs.systeminterface.IWebView;

/**
 * Screen模块处理并执行服务下发的指令，如HtmlView指令，以及发送事件，如LinkClicked事件
 * <p>
 * Created by wuruisheng on 2017/5/31.
 */
public class ScreenDeviceModule extends BaseDeviceModule {
    private final IWebView webView;

    public ScreenDeviceModule(IWebView webView, IMessageSender messageSender) {
        super(ApiConstants.NAMESPACE, messageSender);
        this.webView = webView;
        webView.addWebViewListener(new IWebView.IWebViewListener() {
            @Override
            public void onLinkClicked(String url) {
                sendLinkClickedEvent(url);
            }
        });
    }

    @Override
    public ClientContext clientContext() {
        return null;
    }

    @Override
    public void handleDirective(Directive directive) {
        String name = directive.header.getName();
        if (name.equals(ApiConstants.Directives.HtmlView.NAME)) {
            handleHtmlPayload(directive.getPayload());
        }
    }

    @Override
    public void release() {
    }

    private void handleHtmlPayload(Payload payload) {
        if (payload instanceof HtmlPayload) {
            HtmlPayload htmlPayload = (HtmlPayload) payload;
            webView.loadUrl(htmlPayload.getUrl());
        }
    }

    private void sendLinkClickedEvent(String url) {
        String name = ApiConstants.Events.LinkClicked.NAME;
        Header header = new MessageIdHeader(getNameSpace(), name);

        LinkClickedPayload linkClickedPayload = new LinkClickedPayload(url);
        Event event = new Event(header, linkClickedPayload);
        if (messageSender != null) {
            messageSender.sendEvent(event);
        }
    }
}
