/*
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
/**
 * 唤醒调用底层so库实现-jni
 * <p>
 * Created by guxiuzhong@baidu.com on 2017/6/22.
 */
#include <jni.h>
#include "Interface.h"
#include <android/log.h>

#define LOG_TAG "wakeup"
#define LOG_D(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)


extern "C"
JNIEXPORT jint JNICALL
Java_com_baidu_duer_dcs_androidsystemimpl_wakeup_WakeUpNative_wakeUpInitial(JNIEnv *env,
                                                                            jobject instance,
                                                                            jstring wakeUpWd_,
                                                                            jstring sFile_,
                                                                            jint mode) {
    const char *wakeUpWd = env->GetStringUTFChars(wakeUpWd_, 0);
    const char *sFile = env->GetStringUTFChars(sFile_, 0);

    int _mode = mode;
    // 打开log
    // SetLogLevel(5);
    LOG_D("WakeUpInitial wakeUpWd=%s", wakeUpWd);
    LOG_D("WakeUpInitial sFile_=%s", sFile);
    int ret = WakeUpInitial(wakeUpWd, sFile, _mode);
    LOG_D("WakeUpInitial ret=%d", ret);

    env->ReleaseStringUTFChars(wakeUpWd_, wakeUpWd);
    env->ReleaseStringUTFChars(sFile_, sFile);

    return ret;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_baidu_duer_dcs_androidsystemimpl_wakeup_WakeUpNative_wakeUpReset(JNIEnv *env,
                                                                          jobject instance) {
    return WakeUpReset();
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_baidu_duer_dcs_androidsystemimpl_wakeup_WakeUpNative_wakeUpDecode(JNIEnv *env,
                                                                           jobject instance,
                                                                           jshortArray data_,
                                                                           jint dataLen,
                                                                           jstring senArr_,
                                                                           jint expectNum,
                                                                           jint wakeWord_frame_len,
                                                                           jboolean is_confidence,
                                                                           jint voice_offset,
                                                                           jboolean bEd) {


    jboolean isCopy = 0;
    short *data_invoke = env->GetShortArrayElements(data_, &isCopy);

    // 写入文件
    // FILE *file = fopen("/storage/emulated/0/222.pcm", "ab+");
    // fwrite(data_invoke, sizeof(short) * dataLen, 1, file);
    // fclose(file);

    LOG_D("WakeUpDecode data_ GetArrayLength=%d", env->GetArrayLength(data_));

    int dataLen_invoke = dataLen;
    // 因为so库里面最大为2560
    char *senArr = new char[2560];
    int expectNum_invoke = expectNum;
    int wakeWord_frame_len_invoke = wakeWord_frame_len;
    bool is_confidence_invoke = is_confidence;
    int voice_offset_invoke = voice_offset;
    bool bEd_invoke = bEd;

    LOG_D("WakeUpDecode dataLen_invoke=%d", dataLen_invoke);
    LOG_D("WakeUpDecode expectNum_invoke=%d", expectNum_invoke);
    LOG_D("WakeUpDecode wakeWord_frame_len_invoke=%d", wakeWord_frame_len_invoke);
    LOG_D("WakeUpDecode is_confidence_invoke=%d", is_confidence_invoke);
    LOG_D("WakeUpDecode bEd_invoke=%d", bEd_invoke);


    int ret = WakeUpDecode(data_invoke,
                           dataLen_invoke,
                           &senArr,
                           expectNum_invoke,
                           wakeWord_frame_len_invoke,
                           is_confidence_invoke,
                           voice_offset_invoke,
                           bEd_invoke
    );

    LOG_D("WakeUpDecode ret=%d", ret);
    LOG_D("WakeUpDecode senArr=%s", senArr);
    LOG_D("WakeUpDecode wakeWord_frame_len_invoke=%d", wakeWord_frame_len_invoke);
    LOG_D("WakeUpDecode is_confidence_invoke=%d", is_confidence_invoke);
    LOG_D("WakeUpDecode voice_offset_invoke=%d", voice_offset_invoke);

    // 释放内存资源
    env->ReleaseShortArrayElements(data_, data_invoke, 0);
    env->ReleaseStringUTFChars(senArr_, senArr);
    return ret;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_baidu_duer_dcs_androidsystemimpl_wakeup_WakeUpNative_wakeUpFree(JNIEnv *env,
                                                                         jobject instance) {
    LOG_D("WakeUpFree");
    return WakeUpFree();
}