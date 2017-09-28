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
package com.baidu.duer.dcs.devicemodule.screen.extend.card;

import com.baidu.duer.dcs.devicemodule.screen.extend.card.ApiConstants.Directives.RenderDate;
import com.baidu.duer.dcs.devicemodule.screen.extend.card.ApiConstants.Directives.RenderStock;
import com.baidu.duer.dcs.devicemodule.system.HandleDirectiveException;
import com.baidu.duer.dcs.framework.BaseDeviceModule;
import com.baidu.duer.dcs.framework.IMessageSender;
import com.baidu.duer.dcs.framework.message.ClientContext;
import com.baidu.duer.dcs.framework.message.Directive;

import java.util.ArrayList;
import java.util.List;

import static com.baidu.duer.dcs.devicemodule.screen.extend.card.ApiConstants.Directives.RenderAirQuality;
import static com.baidu.duer.dcs.devicemodule.screen.extend.card.ApiConstants.Directives.RenderTrafficRestriction;
import static com.baidu.duer.dcs.devicemodule.screen.extend.card.ApiConstants.Directives.RenderWeather;
import static com.baidu.duer.dcs.devicemodule.system.HandleDirectiveException.ExceptionType.UNSUPPORTED_OPERATION;

public class ScreenExtendDeviceModule extends BaseDeviceModule {

    private final List<IRenderExtendListener> listeners = new ArrayList<>();

    public ScreenExtendDeviceModule(IMessageSender messageSender) {
        super(ApiConstants.NAMESPACE, messageSender);
    }

    @Override
    public ClientContext clientContext() {
        return null;
    }

    @Override
    public void handleDirective(Directive directive) throws HandleDirectiveException {
        String name = directive.header.getName();
        if (RenderWeather.NAME.equals(name)
                || RenderDate.NAME.equals(name)
                || RenderStock.NAME.equals(name)
                || RenderAirQuality.NAME.equals(name)
                || RenderTrafficRestriction.NAME.equals(name)) {
            handleExtendCardDirective(directive);
        } else {
            String message = "VoiceOutput cannot handle the directive";
            throw new HandleDirectiveException(UNSUPPORTED_OPERATION, message);
        }
    }

    private void handleExtendCardDirective(Directive directive) {
        for (IRenderExtendListener listener : listeners) {
            listener.onRenderDirective(directive);
        }
    }

    @Override
    public void release() {
        listeners.clear();
    }

    public void addListener(IRenderExtendListener listener) {
        listeners.add(listener);
    }

    public void removeListener(IRenderExtendListener listener) {
        listeners.remove(listener);
    }

    public interface IRenderExtendListener {
        /**
         * Handle extend card payload.
         */
        void onRenderDirective(Directive directive);
    }
}