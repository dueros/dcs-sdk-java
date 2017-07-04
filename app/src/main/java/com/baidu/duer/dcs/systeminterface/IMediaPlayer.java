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
package com.baidu.duer.dcs.systeminterface;

import java.io.InputStream;

/**
 * 播放器接口
 * <p>
 * Created by guxiuzhong@baidu.com on 2017/5/31.
 */
public interface IMediaPlayer {
    /**
     * 播放状态
     */
    enum PlayState {
        ERROR(-1),          // 播放错误
        IDLE(0),            // 播放未开始
        PREPARING(1),       // 播放准备中
        PREPARED(2),        // 播放准备就绪
        PLAYING(3),         // 正在播放
        PAUSED(4),          // 暂停播放
        STOPPED(5),         // 停止状态
        COMPLETED(6);       // 播放完成

        private int state;

        PlayState(int state) {
            this.state = state;
        }

        public int getState() {
            return state;
        }
    }

    /**
     * 获取当前的播放状态
     *
     * @return PlayState
     */
    PlayState getPlayState();

    /**
     * 播放
     *
     * @param mediaResource mediaResource
     */
    void play(MediaResource mediaResource);

    /**
     * 暂停
     */
    void pause();

    /**
     * 停止播放
     */
    void stop();

    /**
     * 调用pause暂停后重新播放
     */
    void resume();

    /**
     * 从milliseconds位置开始播放
     *
     * @param milliseconds 毫秒
     */
    void seekTo(int milliseconds);

    /**
     * 释放，销毁播放器
     */
    void release();

    /**
     * 设置音量 volume：0-1
     *
     * @param volume 音量（0-1之间的浮点数）
     */
    void setVolume(float volume);

    float getVolume();

    /**
     * 设置静音
     *
     * @param mute 静音开关
     */
    void setMute(boolean mute);

    boolean getMute();

    /**
     * 获取当前的播放位置
     *
     * @return 当前的播放位置
     */
    long getCurrentPosition();

    /**
     * 获取当前音频文件／流的总时长，单位:ms
     *
     * @return ms
     */
    long getDuration();

    /**
     * 获取当前缓冲到多少 0-100f
     *
     * @return 0-100f
     */
    float getBufferPercentage();

    /**
     * 添加播放器状态回调
     *
     * @param listener listener
     */
    void addMediaPlayerListener(IMediaPlayerListener listener);

    void removeMediaPlayerListener(IMediaPlayerListener listener);

    /**
     * 设置播放通道是否处在活跃状态，比如：当用户在语音请求时，对话通道进入活跃状态
     *
     * @param isActive 是否处在活跃状态
     */
    void setActive(boolean isActive);

    boolean isActive();

    /**
     * 播放器播放状态回调接口
     */
    interface IMediaPlayerListener {

        /**
         * 调用完play方法后回调此方法
         */
        void onInit();

        /**
         * 播放器准备完成后回调
         */
        void onPrepared();

        /**
         * 播放器销毁后回调
         */
        void onRelease();

        /**
         * 正在播放时回调
         */
        void onPlaying();

        /**
         * 暂停后回调
         */
        void onPaused();

        void onStopped();

        /**
         * 播放完成后回调
         */
        void onCompletion();

        /**
         * 播放出错
         */
        void onError(String error, ErrorType errorType);

        /**
         * 播放器缓冲回调
         *
         * @param percent 缓冲的进度 0-100
         */
        void onBufferingUpdate(int percent);

        /**
         * 开始缓冲时回调
         */
        void onBufferingStart();

        /**
         * 结束缓冲时回调
         */
        void onBufferingEnd();

    }

    /**
     * 播放错误类型信息
     */
    enum ErrorType {
        MEDIA_ERROR_UNKNOWN("An unknown error occurred"),
        MEDIA_ERROR_INVALID_REQUEST(
                "The server recognized the request as being malformed "
                        + "(bad request, unauthorized, forbidden, not found, etc)"),
        MEDIA_ERROR_SERVICE_UNAVAILABLE("The device was unavailable to reach the service"),
        MEDIA_ERROR_INTERNAL_SERVER_ERROR(
                "The server accepted the request, but was unable to process it as expected"),
        MEDIA_ERROR_INTERNAL_DEVICE_ERROR("There was an internal error on the device");

        private final String message;

        ErrorType(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * 内部空实现的IMediaPlayerListener，如果你只关心onCompletion,可以用这个
     */
    class SimpleMediaPlayerListener implements IMediaPlayerListener {
        @Override
        public void onInit() {

        }

        @Override
        public void onPrepared() {

        }

        @Override
        public void onRelease() {

        }

        @Override
        public void onPlaying() {

        }

        @Override
        public void onPaused() {

        }

        @Override
        public void onStopped() {

        }

        @Override
        public void onCompletion() {

        }

        @Override
        public void onError(String error, ErrorType errorType) {

        }

        @Override
        public void onBufferingUpdate(int percent) {

        }

        @Override
        public void onBufferingStart() {

        }

        @Override
        public void onBufferingEnd() {

        }
    }

    /**
     * 需要播放的音频类型
     */
    final class MediaResource {
        public InputStream stream;  // 流类型
        public String url;          // url类型
        public boolean isStream;    // 是否值流类型

        public MediaResource(InputStream stream) {
            this.stream = stream;
            this.isStream = true;
        }

        public MediaResource(String url) {
            this.url = url;
            this.isStream = false;
        }

        @Override
        public String toString() {
            if (isStream) {
                return "stream";
            } else {
                return url;
            }
        }
    }
}