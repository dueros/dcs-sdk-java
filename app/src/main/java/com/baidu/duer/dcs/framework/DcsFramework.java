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
package com.baidu.duer.dcs.framework;

import com.baidu.duer.dcs.devicemodule.system.HandleDirectiveException;
import com.baidu.duer.dcs.devicemodule.system.SystemDeviceModule;
import com.baidu.duer.dcs.framework.DcsResponseDispatcher.IDcsResponseHandler;
import com.baidu.duer.dcs.framework.message.ClientContext;
import com.baidu.duer.dcs.framework.message.DcsRequestBody;
import com.baidu.duer.dcs.framework.message.DcsResponseBody;
import com.baidu.duer.dcs.framework.message.DcsStreamRequestBody;
import com.baidu.duer.dcs.framework.message.Directive;
import com.baidu.duer.dcs.framework.message.Event;
import com.baidu.duer.dcs.systeminterface.IPlatformFactory;
import com.baidu.duer.dcs.util.LogUtil;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * dcs业务框架，用于发送事件、处理指令、长连接维护和多channel播放优先级策略等
 * <p>
 * DeviceModule层提供通过工厂来创建设备支持端能力如voiceInput、voiceOutput、speakerController、audioPlayer、playController、alerts和screen等
 * <p>
 * DcsResponseDispatcher用于分发指令到对应的deviceModule进行处理
 * <p>
 * MultiChannelMediaPlayer基于活跃channel策略用于语音输入打断，播放遵循优先级如dialog(voiceOutput) > alerts > mediaPlayer
 * <p>
 * 网络层DcsClient负责发送请求、心跳和长连接
 * <p>
 * Created by shenguanghao, wuruisheng on 2017/5/31.
 */
public class DcsFramework {
    private static final String TAG = DcsFramework.class.getSimpleName();
    // 管理平台相关的对象
    private final IPlatformFactory platformFactory;
    // 管理deviceModules
    private final HashMap<String, BaseDeviceModule> dispatchDeviceModules;
    // 创建会话Id
    private final DialogRequestIdHandler dialogRequestIdHandler;
    // 基于通道活跃状态和优先级进行mediaPlayer调度
    private final BaseMultiChannelMediaPlayer multiChannelMediaPlayer;
    // 创建deviceModule工厂
    private DeviceModuleFactory deviceModuleFactory;
    // 和服务器端保持长连接、发送events和接收directives和维持心跳
    private DcsClient dcsClient;
    // 用于DeviceModules发送events
    private IMessageSender messageSender;
    // 服务器端返回response调度中心
    private DcsResponseDispatcher dcsResponseDispatcher;

    public DcsFramework(IPlatformFactory platformFactory) {
        this.platformFactory = platformFactory;
        dispatchDeviceModules = new HashMap<>();
        dialogRequestIdHandler = new DialogRequestIdHandler();
        multiChannelMediaPlayer = new PauseStrategyMultiChannelMediaPlayer(platformFactory);

        createMessageSender();
        createDcsClient();
        createDeviceModuleFactory();
    }

    public void release() {
        for (BaseDeviceModule deviceModule : dispatchDeviceModules.values()) {
            deviceModule.release();
        }
        dcsClient.release();
        dcsResponseDispatcher.release();
    }

    public DeviceModuleFactory getDeviceModuleFactory() {
        return deviceModuleFactory;
    }

    private ArrayList<ClientContext> clientContexts() {
        ArrayList<ClientContext> clientContexts = new ArrayList<>();
        for (BaseDeviceModule deviceModule : dispatchDeviceModules.values()) {
            ClientContext clientContext = deviceModule.clientContext();
            if (clientContext != null) {
                clientContexts.add(clientContext);
            }
        }

        return clientContexts;
    }

    private void handleDirective(Directive directive) {
        String namespace = directive.header.getNamespace();
        try {
            BaseDeviceModule deviceModule = dispatchDeviceModules.get(namespace);
            if (deviceModule != null) {
                deviceModule.handleDirective(directive);
            } else {
                String message = "No device to handle the directive";
                throw new HandleDirectiveException(
                        HandleDirectiveException.ExceptionType.UNSUPPORTED_OPERATION, message);
            }
        } catch (HandleDirectiveException exception) {
            getSystemDeviceModule().sendExceptionEncounteredEvent(directive.rawMessage,
                    exception.getExceptionType(), exception.getMessage());
        } catch (Exception exception) {
            getSystemDeviceModule().sendExceptionEncounteredEvent(directive.rawMessage,
                    HandleDirectiveException.ExceptionType.INTERNAL_ERROR,
                    exception.getMessage()
            );
        }
    }

    private void createDcsClient() {
        IDcsResponseHandler responseHandler = new IDcsResponseHandler() {
            @Override
            public void onResponse(final DcsResponseBody responseBody) {
                platformFactory.getMainHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        LogUtil.d(TAG, "DcsResponseBodyEnqueue-handleDirective-MSG:"
                                + responseBody.getDirective().rawMessage);
                        handleDirective(responseBody.getDirective());
                    }
                });
            }

            @Override
            public void onParseFailed(String unParsedMessage) {
                LogUtil.d(TAG, "DcsResponseBodyEnqueue-handleDirective-onParseFailed");
                String message = "parse failed";
                getSystemDeviceModule().sendExceptionEncounteredEvent(unParsedMessage,
                        HandleDirectiveException.ExceptionType.UNEXPECTED_INFORMATION_RECEIVED,
                        message);
            }
        };

        dcsResponseDispatcher = new DcsResponseDispatcher(dialogRequestIdHandler, responseHandler);
        dcsClient = new DcsClient(dcsResponseDispatcher, new DcsClient.IDcsClientListener() {
            @Override
            public void onConnected() {
                LogUtil.d(TAG, "onConnected");
                getSystemDeviceModule().sendSynchronizeStateEvent();
            }

            @Override
            public void onUnconnected() {
                LogUtil.d(TAG, "onUnconnected");
            }
        });
        dcsClient.startConnect();
    }

    private void createMessageSender() {
        messageSender = new IMessageSender() {
            @Override
            public void sendEvent(Event event, DcsStreamRequestBody streamRequestBody,
                                  IResponseListener responseListener) {
                DcsRequestBody requestBody = new DcsRequestBody(event);
                requestBody.setClientContext(clientContexts());
                dcsClient.sendRequest(requestBody, streamRequestBody, responseListener);
            }

            @Override
            public void sendEvent(Event event, IResponseListener responseListener) {
                sendEventRequest(event, null, responseListener);
            }

            @Override
            public void sendEvent(Event event) {
                sendEventRequest(event, null, null);
            }

            @Override
            public void sentEventWithClientContext(Event event, IResponseListener responseListener) {
                sendEventRequest(event, clientContexts(), responseListener);
            }
        };
    }

    private void sendEventRequest(Event event,
                                  ArrayList<ClientContext> clientContexts,
                                  IResponseListener responseListener) {
        DcsRequestBody dcsRequestBody = new DcsRequestBody(event);
        dcsRequestBody.setClientContext(clientContexts);
        dcsClient.sendRequest(dcsRequestBody, responseListener);
    }

    public DcsClient getDcsClient() {
        return dcsClient;
    }

    private void createDeviceModuleFactory() {
        deviceModuleFactory = new DeviceModuleFactory(new DeviceModuleFactory.IDeviceModuleHandler() {
            @Override
            public IPlatformFactory getPlatformFactory() {
                return platformFactory;
            }

            @Override
            public DialogRequestIdHandler getDialogRequestIdHandler() {
                return dialogRequestIdHandler;
            }

            @Override
            public IMessageSender getMessageSender() {
                return messageSender;
            }

            @Override
            public BaseMultiChannelMediaPlayer getMultiChannelMediaPlayer() {
                return multiChannelMediaPlayer;
            }

            @Override
            public void addDeviceModule(BaseDeviceModule deviceModule) {
                DcsFramework.this.addDeviceModule(deviceModule);
            }

            @Override
            public DcsResponseDispatcher getResponseDispatcher() {
                return dcsResponseDispatcher;
            }
        });
    }

    private void addDeviceModule(BaseDeviceModule deviceModule) {
        dispatchDeviceModules.put(deviceModule.getNameSpace(), deviceModule);
    }

    private SystemDeviceModule getSystemDeviceModule() {
        return deviceModuleFactory.getSystemDeviceModule();
    }
}