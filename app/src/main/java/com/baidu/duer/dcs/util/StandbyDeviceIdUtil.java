/*
 * *
 * Copyright (c) 2017 Baidu, Inc. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.baidu.duer.dcs.util;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.io.File;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Created by denglin03 on 2018/1/22.
 */

public class StandbyDeviceIdUtil {
    private static final String DEF_MAC = "02:00:00:00:00:00";
    private static final String DEVICE_KEY = "com.baidu.dcs";
    private static final String FILE_PATH = getSDPath()
            + File.separator + "baidu" + File.separator
            + "dueros" + File.separator;
    private static final String FILE_NAME = FILE_PATH + "DUER.CUID";

    /**
     * @param context
     * @return String cuid
     **/
    public static String getStandbyDeviceId(Context context) {
        String standbyDeviceId = (String) PreferenceUtil.get(context, DEVICE_KEY, "");
        if (TextUtils.isEmpty(standbyDeviceId)) {
            standbyDeviceId = getSystemSettingValue(context, DEVICE_KEY);
            if (TextUtils.isEmpty(standbyDeviceId)) {
                standbyDeviceId = FileUtil.getFileToString(FILE_NAME);
                if (TextUtils.isEmpty(standbyDeviceId)) {
                    standbyDeviceId = MD5Util.md5((getAndroidId(context) + getImei(context)
                            + getWlanId(context)))
                            + getUUID();
                    /** 只有在最后一步才能存储，防止用户关闭读写权限，和系统数据写入权限，频繁写入照成bug **/
                    PreferenceUtil.put(context, DEVICE_KEY, standbyDeviceId);
                    tryPutSystemSettingValue(context, DEVICE_KEY, standbyDeviceId);
                    FileUtil.storeStrToFile(FILE_NAME, standbyDeviceId);
                }
            }
        }
        return standbyDeviceId;
    }

    public static String getSDPath() {
        String sdDir;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(Environment.MEDIA_MOUNTED);
        // 判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory().toString();
            // 获取跟SD卡目录
        } else {
            sdDir = "/data/data/" + SystemServiceManager.getAppContext().getPackageName();
        }
        return sdDir;
    }

    /**
     * @param context
     * @param key
     * @return String CUID
     **/
    private static String getSystemSettingValue(Context context, String key) {
        try {
            return Settings.System.getString(context.getContentResolver(), key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param context
     * @return String android_id
     **/
    private static String getAndroidId(Context context) {
        String androidId = "def_android_id";
        try {
            // 获取android_id
            androidId = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return androidId;
    }

    /**
     * android.permission.READ_PHONE_STATE
     *
     * @param context
     * @return String IMEI
     **/
    private static String getImei(Context context) {
        String szImei = "def_imei";
        try {
            TelephonyManager telephonyMgr = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            szImei = telephonyMgr.getDeviceId();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return szImei;
    }

    /**
     * android.permission.ACCESS_WIFI_STATE
     *
     * @param context
     * @return String IMEI
     **/
    private static String getWlanId(Context context) {
        String wlanMac = DEF_MAC;
        try {
            WifiManager wm = (WifiManager) context.getSystemService(context.getApplicationContext().WIFI_SERVICE);
            wlanMac = wm.getConnectionInfo().getMacAddress();
            if (DEF_MAC.equals(wlanMac)) {
                wlanMac = getMacId();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return wlanMac;
    }

    private static String getMacId() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) {
                    continue;
                }

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:", b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return DEF_MAC;
    }

    /**
     * @return String UUID
     **/
    private static String getUUID() {
        String uuid = UUID.randomUUID().toString();
        try {
            // 获取UUID并转化为String对象
            uuid = uuid.replace("-", "").toUpperCase();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return uuid;
    }

    /**
     * android.permission.WRITE_SETTINGS.
     * android 6.0之后不能设置此方法
     *
     * @param context
     * @param key
     * @param value
     * @return boolean saveSuccess
     **/
    private static boolean tryPutSystemSettingValue(Context context, String key, String value) {
        // 有了权限，具体的动作
        try {
            return Settings.System.putString(context.getContentResolver(), key, value);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }
}
