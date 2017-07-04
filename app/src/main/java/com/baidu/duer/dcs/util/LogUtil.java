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

import android.util.Log;

/**
 * Log日志输出信息
 * <p>
 * Created by zhangyan42@baidu.com on 2017/5/24.
 */
public class LogUtil {
    // 是否开始debug模式，日志输出
    public static boolean DEBUG = true;
    private static final String APPNAME = "DCS-";
    private static boolean LOGV_ON = DEBUG;
    private static boolean LOGD_ON = DEBUG;
    private static boolean LOGI_ON = DEBUG;
    private static boolean LOGW_ON = DEBUG;
    private static boolean LOGE_ON = DEBUG;
    // 是否log写文件
    private static boolean isWriteFile = true;

    /**
     * 设置debug 开关
     *
     * @param isDebug debug开关，true为开，false为关
     */
    public static void setDEBUG(boolean isDebug) {
        DEBUG = isDebug;
        LOGV_ON = true & DEBUG;
        LOGD_ON = true & DEBUG;
        LOGI_ON = true & DEBUG;
        LOGW_ON = true & DEBUG;
        LOGE_ON = true & DEBUG;
    }

    public static boolean getDEBUG() {
        return DEBUG;
    }

    /**
     * 记录相应的log信息v
     *
     * @param tag log tag 信息
     * @param msg log msg 信息
     */
    public static void v(String tag, String msg) {
        if (LOGV_ON) {
            tag = APPNAME + tag;
            Log.v(tag, msg);
            writeLog("V", tag, msg, null);
        }
    }

    public static void v(Class<?> c, String msg) {
        if (LOGV_ON) {
            String tag = APPNAME + c.getSimpleName();
            Log.v(tag, msg);
            writeLog("V", tag, msg, null);
        }
    }

    public static void d(Class<?> c, String msg) {
        if (LOGD_ON) {
            String tag = APPNAME + c.getSimpleName();
            Log.d(tag, msg);
            writeLog("D", tag, msg, null);
        }
    }

    /**
     * 记录相应的log信息d
     *
     * @param tag log tag 信息
     * @param msg log msg 信息
     */
    public static void d(String tag, String msg) {
        if (LOGD_ON) {
            tag = APPNAME + tag;
            Log.d(tag, msg);
            writeLog("D", tag, msg, null);
        }
    }

    public static void d(String tag, String msg, Throwable tr) {
        if (LOGE_ON) {
            tag = APPNAME + tag;
            Log.d(tag, msg, tr);
            writeLog("D", tag, msg, tr);
        }
    }

    /**
     * 记录相应的log信息i
     *
     * @param tag log tag 信息
     * @param msg log msg 信息
     */
    public static void i(String tag, String msg) {
        if (LOGI_ON) {
            tag = APPNAME + tag;
            Log.i(tag, msg);
            writeLog("I", tag, msg, null);
        }
    }

    public static void i(Class<?> c, String msg) {
        if (LOGI_ON) {
            String tag = APPNAME + c.getSimpleName();
            Log.i(tag, msg);
            writeLog("I", tag, msg, null);
        }
    }

    public static void w(Class<?> c, String msg) {
        if (LOGW_ON) {
            String tag = APPNAME + c.getSimpleName();
            Log.w(tag, msg);
            writeLog("W", tag, msg, null);
        }
    }

    public static void w(Class<?> c, String msg, Throwable tr) {
        if (LOGW_ON) {
            String tag = APPNAME + c.getSimpleName();
            Log.w(tag, msg, tr);
            writeLog("W", tag, msg, tr);
        }
    }

    /**
     * 记录相应的log信息w
     *
     * @param tag log tag 信息
     * @param msg log msg 信息
     */
    public static void w(String tag, String msg) {
        if (LOGW_ON) {
            tag = APPNAME + tag;
            Log.w(tag, msg);
            writeLog("W", tag, msg, null);
        }
    }

    public static void w(String tag, String msg, Throwable tr) {
        if (LOGW_ON) {
            tag = APPNAME + tag;
            Log.w(tag, msg, tr);
            writeLog("W", tag, msg, tr);
        }
    }

    /**
     * 记录相应的log信息e
     *
     * @param tag log tag 信息
     * @param msg log msg 信息
     */
    public static void e(String tag, String msg) {
        if (LOGE_ON) {
            tag = APPNAME + tag;
            Log.e(tag, msg);
            writeLog("E", tag, msg, null);
        }
    }

    public static void e(Class<?> c, String msg) {
        if (LOGE_ON) {
            String tag = APPNAME + c.getSimpleName();
            Log.e(tag, msg);
            writeLog("E", tag, msg, null);
        }
    }

    public static void e(String tag, String msg, Throwable tr) {
        if (LOGE_ON) {
            tag = APPNAME + tag;
            Log.e(tag, msg, tr);
            writeLog("E", tag, msg, tr);
        }
    }

    public static void e(Class<?> c, String msg, Throwable tr) {
        if (LOGE_ON) {
            String tag = APPNAME + c.getSimpleName();
            Log.e(tag, msg, tr);
            writeLog("E", tag, msg, tr);
        }
    }

    public static void setIsWriteFile(boolean isWriteFile) {
        LogUtil.isWriteFile = isWriteFile;
    }

    private static void writeLog(String logLev, String tag, String msg, Throwable tr) {
        if (isWriteFile) {
            try {
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer
                        .append(CommonUtil.getCurrentTime())
                        .append("\n")
                        .append(tag)
                        .append("-")
                        .append(msg);
                if (tr != null) {
                    stringBuffer.append(Log.getStackTraceString(tr));
                }
                stringBuffer.append("\n");
                // 写文件
                FileUtil.appendStrToFile(stringBuffer.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}