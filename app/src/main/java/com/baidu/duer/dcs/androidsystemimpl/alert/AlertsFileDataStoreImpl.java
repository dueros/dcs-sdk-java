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
package com.baidu.duer.dcs.androidsystemimpl.alert;

import android.os.Handler;

import com.baidu.duer.dcs.devicemodule.alerts.message.Alert;
import com.baidu.duer.dcs.systeminterface.IAlertsDataStore;
import com.baidu.duer.dcs.util.CommonUtil;
import com.baidu.duer.dcs.util.FileUtil;
import com.baidu.duer.dcs.util.LogUtil;
import com.baidu.duer.dcs.util.ObjectMapperUtil;

import org.codehaus.jackson.map.ObjectReader;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.type.TypeReference;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 持久化alert数据到文件-android中实现
 * <p>
 * Created by guxiuzhong@baidu.com on 2017/5/18.
 */
public class AlertsFileDataStoreImpl implements IAlertsDataStore {
    private static final String TAG = AlertsFileDataStoreImpl.class.getSimpleName();
    private static final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private Handler mHandler = new Handler();

    public AlertsFileDataStoreImpl() {
    }

    @Override
    public void readFromDisk(final IAlertsDataStore.ReadResultListener listener) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                FileReader fis;
                BufferedReader br = null;
                ObjectReader reader = ObjectMapperUtil.instance()
                        .getObjectReader()
                        .withType(new TypeReference<List<Alert>>() {
                        });
                try {
                    File file = FileUtil.getAlarmFile();
                    if (file == null) {
                        postReadFailed(listener, "create file failed file is null");
                        return;
                    }
                    fis = new FileReader(file);
                    br = new BufferedReader(fis);
                    List<Alert> alerts = reader.readValue(br);
                    postReadSucceed(listener, alerts);
                } catch (final FileNotFoundException e) {
                    postReadFailed(listener, "Failed to load alerts from disk.");
                } catch (IOException e) {
                    postReadFailed(listener, e.getMessage());
                } finally {
                    CommonUtil.closeQuietly(br);
                }
            }
        });
    }

    private void postReadFailed(final IAlertsDataStore.ReadResultListener listener, final String errorMessage) {
        if (listener != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onFailed(errorMessage);
                }
            });
        }
    }

    private void postReadSucceed(final IAlertsDataStore.ReadResultListener listener, final List<Alert> alerts) {
        if (listener != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onSucceed(alerts);
                }
            });
        }
    }

    @Override
    public void writeToDisk(final List<Alert> alerts, final IAlertsDataStore.WriteResultListener listener) {
        mExecutor.execute(new Runnable() {

            @Override
            public void run() {
                ObjectWriter writer = ObjectMapperUtil.instance().getObjectWriter();
                PrintWriter out = null;
                try {
                    File file = FileUtil.getAlarmFile();
                    if (file == null) {
                        postWriteFailed(listener, "create file failed file is null ");
                        return;
                    }
                    out = new PrintWriter(file);
                    out.print(writer.writeValueAsString(alerts));
                    out.flush();
                    LogUtil.e(TAG, "start postWriteSucceed");
                    postWriteSucceed(listener);
                } catch (IOException e) {
                    LogUtil.e(TAG, "Failed to write to disk", e);
                    postWriteFailed(listener, "Failed to write to disk,error:" + e.getMessage());
                } finally {
                    CommonUtil.closeQuietly(out);
                }
            }
        });
    }

    private void postWriteFailed(final IAlertsDataStore.WriteResultListener listener, final String errorMessage) {
        if (listener != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onFailed(errorMessage);
                }
            });
        }
    }

    private void postWriteSucceed(final IAlertsDataStore.WriteResultListener listener) {
        if (listener != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onSucceed();
                }
            });
        }
    }
}
