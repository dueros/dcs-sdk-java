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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * iso8601string 时间格式转换
 * <p>
 * Created by zhoujianliang01@baidu.com on 2017/6/5.
 */
public class DateFormatterUtil {
    private static final String TAG = "ISO8601DateFormatter";
    private static final DateFormat DATE_FORMAT_1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.CHINESE);
    private static final DateFormat DATE_FORMAT_2 = new SimpleDateFormat("yyyy-MM-dd'T'HHmmssZ", Locale.CHINESE);
    private static final String UTC_PLUS = "+";
    private static final String UTC_MINUS = "-";

    /**
     * 将iso8601string格式字符串时间转为Date
     *
     * @param iso8601string iso8601格式的时间字符串
     * @return Date Date类型表示的结果
     * @throws ParseException ParseException
     */
    public static Date toDate(String iso8601string) throws ParseException {
        iso8601string = iso8601string.trim();
        if (iso8601string.toUpperCase().indexOf("Z") > 0) {
            iso8601string = iso8601string.toUpperCase().replace("Z", "+0000");
        } else if (((iso8601string.indexOf(UTC_PLUS)) > 0)) {
            iso8601string = replaceColon(iso8601string, iso8601string.indexOf(UTC_PLUS));
            iso8601string = appendZeros(iso8601string, iso8601string.indexOf(UTC_PLUS), UTC_PLUS);
        } else if (((iso8601string.indexOf(UTC_MINUS)) > 0)) {
            iso8601string = replaceColon(iso8601string, iso8601string.indexOf(UTC_MINUS));
            iso8601string = appendZeros(iso8601string, iso8601string.indexOf(UTC_MINUS), UTC_MINUS);
        }
        LogUtil.d(TAG, "iso8601string:" + iso8601string);
        Date date;
        if (iso8601string.contains(":")) {
            date = DATE_FORMAT_1.parse(iso8601string);
        } else {
            date = DATE_FORMAT_2.parse(iso8601string);
        }
        return date;
    }

    public static String toISO8601String(Date date) {
        return DATE_FORMAT_1.format(date);
    }

    private static String replaceColon(String sourceStr, int offsetIndex) {
        if (sourceStr.substring(offsetIndex).contains(":")) {
            return sourceStr.substring(0, offsetIndex) + sourceStr.substring(offsetIndex).replace(":", "");
        }
        return sourceStr;
    }

    private static String appendZeros(String sourceStr, int offsetIndex, String offsetChar) {
        if ((sourceStr.length() - 1) - sourceStr.indexOf(offsetChar, offsetIndex) <= 2) {
            return sourceStr + "00";
        }
        return sourceStr;
    }
}