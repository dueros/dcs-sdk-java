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
package com.baidu.duer.dcs.util;

import android.os.Environment;
import android.text.TextUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * FileUtil
 * <p>
 * Created by guxiuzhong@baidu.com on 2017/5/31.
 */
public class FileUtil {
    public static final String TEMP_POSTFIX = ".download";
    private static final String LOG_FILE = "LogAll.txt";
    private static final String APP_DIR = "/DCS";
    private static final String SPEAK = APP_DIR + "/Speak";
    private static final String ALERT = APP_DIR + "/Alert";
    private static final String ALARM_FILE = "alarms.json";

    private static String getSpeakDirPath() {
        String dirPath = "";
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + SPEAK;
            File dir = new File(dirPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
        }
        return dirPath;
    }

    private static String getAlertDirPath() {
        String dirPath = "";
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + ALERT;
            File dir = new File(dirPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
        }
        return dirPath;
    }

    public static File getSpeakFile() {
        String dirPath = getSpeakDirPath();
        if (TextUtils.isEmpty(dirPath)) {
            return null;
        }
        return new File(dirPath,
                "dcs_" + System.currentTimeMillis() + ".mp3" + TEMP_POSTFIX);
    }

    public static File getAlarmFile() {
        String dirPath = getAlertDirPath();
        if (TextUtils.isEmpty(dirPath)) {
            return null;
        }
        return new File(dirPath, ALARM_FILE);
    }

    public static String getLogFilePath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath()
                + APP_DIR + File.separator + LOG_FILE;
    }

    /**
     * 日志追加文件
     *
     * @param content 追加的内容
     */
    public static void appendStrToFile(String content) {
        File file = new File(getLogFilePath());
        if (!file.isFile()) {
            file.delete();
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(file, true)));
            out.write(content);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}