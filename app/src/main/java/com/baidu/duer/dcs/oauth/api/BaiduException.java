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
package com.baidu.duer.dcs.oauth.api;

/**
 * 封装了oauth授权和api请求的异常信息
 * <p>
 * Created by zhangyan42@baidu.com on 2017/5/24.
 */
public class BaiduException extends Exception {
    private static final long serialVersionUID = -8309515227501598366L;
    private String errorCode;
    private String errorMsg;

    public BaiduException() {
        super();
    }

    public BaiduException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public BaiduException(String detailMessage) {
        super(detailMessage);
    }

    public BaiduException(Throwable throwable) {
        super(throwable);
    }

    public BaiduException(String errorCode, String errorDesp) {
        this.errorCode = errorCode;
        this.errorMsg = errorDesp;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorDesp() {
        return errorMsg;
    }

    public void setErrorDesp(String errorDesp) {
        this.errorMsg = errorDesp;
    }

    @Override
    public String toString() {
        return "BaiduException [errorCode=" + errorCode + ", errorDesp=" + errorMsg + "]";
    }
}