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

import com.baidu.duer.dcs.systeminterface.IMediaPlayer;
import com.baidu.duer.dcs.systeminterface.IPlatformFactory;
import com.baidu.duer.dcs.util.LogUtil;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 1.用途：控制各个DeviceModule的播放策略-基类，主要控制的是{@link com.baidu.duer.dcs.devicemodule.alerts.AlertsDeviceModule}
 * and {@link com.baidu.duer.dcs.devicemodule.voiceoutput.VoiceOutputDeviceModule}
 * and {@link com.baidu.duer.dcs.devicemodule.audioplayer.AudioPlayerDeviceModule}
 * <p>
 * 2.如果你需要实现自己的策略需要复写handlePlay方法，该sample中提供了一种暂停策略，即
 * {@link com.baidu.duer.dcs.framework.PauseStrategyMultiChannelMediaPlayer}
 * <p>
 * Created by guxiuzhong@baidu.com on 2017/5/31.
 */
public abstract class BaseMultiChannelMediaPlayer {
    // 注册的mediaPlayer
    protected Map<String, ChannelMediaPlayerInfo> mediaPlayers = new ConcurrentHashMap<>();
    private IPlatformFactory factory;

    public BaseMultiChannelMediaPlayer(IPlatformFactory factory) {
        this.factory = factory;
    }

    /**
     * 添加一个频道并创建一个mediaPlayer
     *
     * @param channelName 频道的名字
     * @param priority    频道的优先级
     * @return IMediaPlayer 用于处理端能力的播放
     */
    public IMediaPlayer addNewChannel(IMediaPlayer mediaPlayer, String channelName, int priority) {
        ChannelMediaPlayerInfo info = new ChannelMediaPlayerInfo();
        info.channelName = channelName;
        info.priority = priority;
        info.mediaPlayer = mediaPlayer;
        mediaPlayers.put(channelName, info);

        return new ChannelMediaPlayer(info);
    }

    /**
     * 处理对话频道（speak）, 闹钟／提醒频道(alert)，音乐频道（audio）的播放策略，
     * 比如: 当播放一个低优先级的指令内容时，来了一个高优先级的播放指令是否需要暂停／降低 低优先级频道对应的mediaPlayer
     *
     * @param channelName   频道的名字（即通过addNewChannel方法指定的）
     * @param mediaResource 媒体资源
     */
    protected abstract void handlePlay(String channelName, IMediaPlayer.MediaResource mediaResource);

    protected int getPriorityByChannelName(String channelName) {
        if (mediaPlayers.containsKey(channelName)) {
            ChannelMediaPlayerInfo info = mediaPlayers.get(channelName);
            return info.priority;
        }
        return UNKNOWN_PRIORITY;
    }

    /**
     * 根据频道的名字获取对应的播放器
     *
     * @param channelName channelName 频道的名字
     * @return IMediaPlayer
     */
    protected IMediaPlayer getMediaPlayer(String channelName) {
        if (channelName != null && channelName.length() > 0) {
            if (mediaPlayers.containsKey(channelName)) {
                return mediaPlayers.get(channelName).mediaPlayer;
            }
        }
        return null;
    }

    private static final String TAG = BaseMultiChannelMediaPlayer.class.getSimpleName();
    protected static final int UNKNOWN_PRIORITY = -1;
    // 按优先级保存的需要播放的数据
    protected Map<Integer, ChannelMediaPlayerInfo> currentPlayMap = new TreeMap<>(
            new Comparator<Integer>() {
                public int compare(Integer obj1, Integer obj2) {
                    // 降序排序
                    return obj2 - obj1;
                }
            });

    /**
     * 播放信息
     */
    protected static class ChannelMediaPlayerInfo {
        // 播放信息
        IMediaPlayer.MediaResource mediaResource;
        // 频道名字
        String channelName;
        // 频道优先级
        int priority;
        // 播放器
        IMediaPlayer mediaPlayer;
    }

    /**
     * 获取控制音量，静音等操作的实例
     *
     * @return ISpeakerController
     */
    public ISpeakerController getSpeakerController() {
        return new SpeakerControllerImpl();
    }

    /**
     * 暂停所有的mediaPlayer播放
     */
    private void pauseAll() {
        // 需要暂停所有的播放，有可能没有requestDialogId的指令正在speaking
        Iterator<Map.Entry<String, ChannelMediaPlayerInfo>> it = mediaPlayers.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, ChannelMediaPlayerInfo> entry = it.next();
            ChannelMediaPlayerInfo value = entry.getValue();
            value.mediaPlayer.pause();
        }
    }

    private void stop(String channelName) {
        IMediaPlayer mediaPlayer = getMediaPlayer(channelName);
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    /**
     * 设置所有频道的mediaPlayer的音量
     *
     * @param volume 音量 0-1
     */
    private void setVolumeAll(float volume) {
        Iterator<Map.Entry<String, ChannelMediaPlayerInfo>> it = mediaPlayers.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, ChannelMediaPlayerInfo> entry = it.next();
            ChannelMediaPlayerInfo info = entry.getValue();
            info.mediaPlayer.setVolume(volume);
        }
    }

    /**
     * 获取最高优先级的音量
     *
     * @return float 0-1
     */
    private float getVolumeTopPriority() {
        Iterator<Map.Entry<String, ChannelMediaPlayerInfo>> it = mediaPlayers.entrySet().iterator();
        if (it.hasNext()) {
            Map.Entry<String, ChannelMediaPlayerInfo> entry = it.next();
            ChannelMediaPlayerInfo info = entry.getValue();
            return info.mediaPlayer.getVolume();
        }
        return 0;
    }

    /**
     * 设置所有频道的mediaPlayer静音状态
     *
     * @param mute 静音状态
     */
    private void setMuteAll(boolean mute) {
        Iterator<Map.Entry<String, ChannelMediaPlayerInfo>> it = mediaPlayers.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, ChannelMediaPlayerInfo> entry = it.next();
            ChannelMediaPlayerInfo info = entry.getValue();
            info.mediaPlayer.setMute(mute);
        }
    }

    /**
     * 获取最高优先级的mediaPlayer静音状态
     *
     * @return true 静音，false 非静音
     */
    private boolean getMuteTopPriority() {
        Iterator<Map.Entry<String, ChannelMediaPlayerInfo>> it = mediaPlayers.entrySet().iterator();
        if (it.hasNext()) {
            Map.Entry<String, ChannelMediaPlayerInfo> entry = it.next();
            ChannelMediaPlayerInfo info = entry.getValue();
            return info.mediaPlayer.getMute();
        }
        return false;
    }

    protected void findToPlay() {
        LogUtil.d(TAG, "findToPlay");
        // 找到最高优先级的重新播放
        Iterator<Map.Entry<Integer, ChannelMediaPlayerInfo>> it = currentPlayMap.entrySet().iterator();
        if (!it.hasNext()) {
            return;
        }
        Map.Entry<Integer, ChannelMediaPlayerInfo> entry = it.next();
        ChannelMediaPlayerInfo info = entry.getValue();
        LogUtil.d(TAG, "findToPlay-channelName:" + info.channelName);
        if (info.mediaPlayer.getPlayState() == IMediaPlayer.PlayState.PAUSED) {
            LogUtil.d(TAG, "findToPlay-value-resume:" + info.priority);
            info.mediaPlayer.resume();
        } else {
            LogUtil.d(TAG, "findToPlay-PlayState:" + info.mediaPlayer.getPlayState());
            // 不是处于正在播放或者准备播放中或者准备完成，开始播放
            if (info.mediaPlayer.getPlayState() != IMediaPlayer.PlayState.PLAYING
                    && info.mediaPlayer.getPlayState() != IMediaPlayer.PlayState.PREPARING
                    && info.mediaPlayer.getPlayState() != IMediaPlayer.PlayState.PREPARED) {
                LogUtil.d(TAG, "findToPlay-value-play:" + info.priority);
                info.mediaPlayer.play(info.mediaResource);
            } else {
                LogUtil.d(TAG, "findToPlay-value-isPlaying-false:" + info.priority);
            }
        }
    }

    private final class ListenerProxy extends IMediaPlayer.SimpleMediaPlayerListener {
        private String channelName;

        ListenerProxy(String channelName) {
            this.channelName = channelName;
        }

        @Override
        public void onStopped() {
            super.onStopped();
            // stop 后就删除不需要播放了
            int priority = getPriorityByChannelName(channelName);
            LogUtil.d(TAG, "ListenerProxy onStopped  del:" + channelName);
            if (priority != UNKNOWN_PRIORITY) {
                currentPlayMap.remove(priority);
            }
            LogUtil.d(TAG, "ListenerProxy onStopped after del :" + currentPlayMap.size());
        }

        @Override
        public void onCompletion() {
            super.onCompletion();
            LogUtil.d(TAG, "ListenerProxy onCompletion,channelName:" + channelName);
            // 删除当前的
            int priority = BaseMultiChannelMediaPlayer.this.getPriorityByChannelName(channelName);
            if (priority != UNKNOWN_PRIORITY) {
                currentPlayMap.remove(priority);
            }
        }
    }

    /**
     * 带有优先级信息的IMediaPlayer
     */
    private final class ChannelMediaPlayer implements IMediaPlayer {
        private ChannelMediaPlayerInfo mediaPlayerInfo;
        private String channelName;
        private IMediaPlayer mediaPlayer;

        ChannelMediaPlayer(ChannelMediaPlayerInfo info) {
            this.mediaPlayerInfo = info;
            this.channelName = mediaPlayerInfo.channelName;
            this.mediaPlayer = mediaPlayerInfo.mediaPlayer;
        }

        @Override
        public void play(MediaResource mediaResource) {
            LogUtil.d(TAG, "ChannelMediaPlayer-play,will handlePlay");
            BaseMultiChannelMediaPlayer.this.handlePlay(channelName, mediaResource);
        }

        @Override
        public void pause() {
            mediaPlayer.pause();
        }

        @Override
        public void stop() {
            LogUtil.d(TAG, "ChannelMediaPlayer-stop-channelName:" + channelName);
            BaseMultiChannelMediaPlayer.this.handleStop(channelName);
        }

        @Override
        public void resume() {
            mediaPlayer.resume();
        }

        @Override
        public void seekTo(int milliseconds) {
            mediaPlayer.seekTo(milliseconds);
        }

        @Override
        public void release() {
            mediaPlayer.release();
        }

        @Override
        public PlayState getPlayState() {
            return mediaPlayer.getPlayState();
        }

        @Override
        public void setVolume(float volume) {
            mediaPlayer.setVolume(volume);
        }

        @Override
        public float getVolume() {
            return mediaPlayer.getVolume();
        }

        @Override
        public void setMute(boolean mute) {
            mediaPlayer.setMute(mute);
        }

        @Override
        public boolean getMute() {
            return mediaPlayer.getMute();
        }

        @Override
        public long getCurrentPosition() {
            return mediaPlayer.getCurrentPosition();
        }

        @Override
        public long getDuration() {
            return mediaPlayer.getDuration();
        }

        @Override
        public float getBufferPercentage() {
            return mediaPlayer.getBufferPercentage();
        }

        @Override
        public void addMediaPlayerListener(IMediaPlayerListener listener) {
            // 注意add的顺序
            // 先1后2的目的是播放完要先从等待播放map中删除对应改成播放完的那一条。
            // 1.先add wrappedListener
            IMediaPlayerListener wrappedListener =
                    new ListenerProxy(channelName);
            mediaPlayer.addMediaPlayerListener(wrappedListener);

            // 2.再addDeviceModule通知到DeviceModule
            mediaPlayer.addMediaPlayerListener(listener);
        }

        @Override
        public void removeMediaPlayerListener(IMediaPlayerListener listener) {
            mediaPlayer.removeMediaPlayerListener(listener);
        }

        @Override
        public void setActive(boolean isActive) {
            LogUtil.d(TAG, "ChannelMediaPlayer-setActive-isActive:" + isActive);
            mediaPlayer.setActive(isActive);
            // 不是处于活跃频道
            if (!isActive && mediaPlayer.getPlayState() != IMediaPlayer.PlayState.STOPPED) {
                BaseMultiChannelMediaPlayer.this.handleActive();
            }
        }

        @Override
        public boolean isActive() {
            return mediaPlayer.isActive();
        }
    }

    /**
     * 处理停止播放
     */
    private void handleStop(String channelName) {
        stop(channelName);
        pauseAll();
    }

    /**
     * 设置对话通道不活跃后的处理逻辑
     */
    private void handleActive() {
        // 找到最高优先级的开始播放
        findToPlay();
    }

    /**
     * 控制接口
     */
    public interface ISpeakerController {
        /**
         * 设置音量（0-1）
         *
         * @param volume 音量（0-1）
         */
        void setVolume(float volume);

        /**
         * 获取当前最高通道的音量
         *
         * @return 音量（0-1）
         */
        float getVolume();

        /**
         * 设置是否静音
         *
         * @param mute 是否静音
         */
        void setMute(boolean mute);

        /**
         * 获取当前最高通道的静音状态
         *
         * @return 静音状态，true 是静音
         */
        boolean getMute();
    }

    /**
     * 给speak-controller 用的，控制一些音量等操作
     */
    private final class SpeakerControllerImpl implements ISpeakerController {

        @Override
        public void setVolume(float volume) {
            BaseMultiChannelMediaPlayer.this.setVolumeAll(volume);
            findToPlay();
        }

        @Override
        public float getVolume() {
            return BaseMultiChannelMediaPlayer.this.getVolumeTopPriority();
        }

        @Override
        public void setMute(boolean mute) {
            BaseMultiChannelMediaPlayer.this.setMuteAll(mute);
            findToPlay();
        }

        @Override
        public boolean getMute() {
            return BaseMultiChannelMediaPlayer.this.getMuteTopPriority();
        }
    }
}
