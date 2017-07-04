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
package com.baidu.duer.dcs.devicemodule.system;

import com.baidu.duer.dcs.devicemodule.system.message.ExceptionEncounteredPayload;
import com.baidu.duer.dcs.devicemodule.system.message.SetEndPointPayload;
import com.baidu.duer.dcs.devicemodule.system.message.ThrowExceptionPayload;
import com.baidu.duer.dcs.devicemodule.system.message.UserInactivityReportPayload;
import com.baidu.duer.dcs.framework.BaseDeviceModule;
import com.baidu.duer.dcs.framework.IMessageSender;
import com.baidu.duer.dcs.framework.message.ClientContext;
import com.baidu.duer.dcs.framework.message.Directive;
import com.baidu.duer.dcs.framework.message.Event;
import com.baidu.duer.dcs.framework.message.Header;
import com.baidu.duer.dcs.framework.message.MessageIdHeader;
import com.baidu.duer.dcs.framework.message.Payload;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * System模块处理ResetUserInactivity、SetEndpoint、ThrowException指令，发送SynchronizeState、UserInactivityReport等事件
 * <p>
 * Created by wuruisheng on 2017/5/31.
 */
public class SystemDeviceModule extends BaseDeviceModule {
    private final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(1);
    private static final long MILLISECONDS_PER_SECOND = 1000;
    private static final long USER_INACTIVITY_REPORT_PERIOD_HOURS = 1;
    private final List<IDeviceModuleListener> deviceModuleListeners;
    private AtomicLong lastUserInteractionInSeconds;
    private Provider provider;

    public SystemDeviceModule(final IMessageSender messageSender) {
        super(ApiConstants.NAMESPACE, messageSender);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                long inactiveTimeInSeconds = currentTimeSeconds() - lastUserInteractionInSeconds.get();
                Event event = userInactivityReportEvent(inactiveTimeInSeconds);
                messageSender.sendEvent(event);
            }
        };

        lastUserInteractionInSeconds = new AtomicLong(currentTimeSeconds());
        scheduledExecutor.scheduleAtFixedRate(runnable, USER_INACTIVITY_REPORT_PERIOD_HOURS,
                USER_INACTIVITY_REPORT_PERIOD_HOURS, TimeUnit.HOURS);

        deviceModuleListeners = Collections.synchronizedList(new ArrayList<IDeviceModuleListener>());
    }


    @Override
    public ClientContext clientContext() {
        return null;
    }

    @Override
    public void handleDirective(Directive directive) throws HandleDirectiveException {
        String name = directive.header.getName();
        if (ApiConstants.Directives.ResetUserInactivity.NAME.equals(name)) {
            userActivity();
        } else if (ApiConstants.Directives.SetEndpoint.NAME.equals(name)) {
            handleSetEndpointDirective(directive);
        } else if (ApiConstants.Directives.ThrowException.NAME.equals(name)) {
            handleThrowException(directive);
        } else {
            String message = "system cannot handle this directive";
            throw (new HandleDirectiveException(
                    HandleDirectiveException.ExceptionType.UNSUPPORTED_OPERATION, message));
        }
    }

    @Override
    public void release() {
        if (!scheduledExecutor.isShutdown()) {
            scheduledExecutor.shutdownNow();
        }
        deviceModuleListeners.clear();
    }

    private void handleSetEndpointDirective(Directive directive) {
        Payload payload = directive.getPayload();
        if (payload instanceof SetEndPointPayload) {
            SetEndPointPayload setEndPointPayload = (SetEndPointPayload) payload;
            fireSetEndpoint(setEndPointPayload);
        }
    }

    private void handleThrowException(Directive directive) {
        Payload payload = directive.getPayload();
        if (payload instanceof ThrowExceptionPayload) {
            ThrowExceptionPayload throwExceptionPayload = (ThrowExceptionPayload) payload;
            fireThrowException(throwExceptionPayload);
        }
    }

    public void sendSynchronizeStateEvent() {
        String name = ApiConstants.Events.SynchronizeState.NAME;
        Header header = new MessageIdHeader(getNameSpace(), name);
        Payload payload = new Payload();
        Event event = new Event(header, payload);

        if (messageSender != null) {
            messageSender.sentEventWithClientContext(event, null);
        }
    }

    private Event userInactivityReportEvent(long inactiveTimeInSeconds) {
        final String name = ApiConstants.Events.UserInactivityReport.NAME;
        Header header = new MessageIdHeader(getNameSpace(), name);
        Payload payload = new UserInactivityReportPayload(inactiveTimeInSeconds);
        Event event = new Event(header, payload);
        return event;
    }

    public void sendExceptionEncounteredEvent(String directiveJson,
                                              HandleDirectiveException.ExceptionType type,
                                              String message) {
        final String name = ApiConstants.Events.ExceptionEncountered.NAME;
        Header header = new MessageIdHeader(getNameSpace(), name);

        ExceptionEncounteredPayload exceptionEncounteredPayLoad = new ExceptionEncounteredPayload(
                directiveJson, type, message);
        Event event = new Event(header, exceptionEncounteredPayLoad);
        messageSender.sentEventWithClientContext(event, null);
    }

    private void userActivity() {
        lastUserInteractionInSeconds.set(currentTimeSeconds());
    }

    private long currentTimeSeconds() {
        return System.currentTimeMillis() / MILLISECONDS_PER_SECOND;
    }

    public Provider getProvider() {
        if (provider == null) {
            provider = new Provider();
        }

        return provider;
    }

    public class Provider {
        public void userActivity() {
            SystemDeviceModule.this.userActivity();
        }
    }

    private void fireSetEndpoint(SetEndPointPayload setEndPointPayload) {
        for (IDeviceModuleListener listener : deviceModuleListeners) {
            listener.onSetEndpoint(setEndPointPayload);
        }
    }

    private void fireThrowException(ThrowExceptionPayload throwExceptionPayload) {
        for (IDeviceModuleListener listener : deviceModuleListeners) {
            listener.onThrowException(throwExceptionPayload);
        }
    }

    public void addModuleListener(IDeviceModuleListener listener) {
        deviceModuleListeners.add(listener);
    }

    public interface IDeviceModuleListener {
        void onSetEndpoint(SetEndPointPayload endPointPayload);

        void onThrowException(ThrowExceptionPayload throwExceptionPayload);
    }
}