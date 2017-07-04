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
package com.baidu.duer.dcs.devicemodule.audioplayer;

import com.baidu.duer.dcs.devicemodule.audioplayer.message.ClearQueuePayload;
import com.baidu.duer.dcs.devicemodule.audioplayer.message.PlayPayload;
import com.baidu.duer.dcs.devicemodule.audioplayer.message.PlaybackStatePayload;
import com.baidu.duer.dcs.devicemodule.audioplayer.message.StopPayload;
import com.baidu.duer.dcs.devicemodule.audioplayer.report.AudioPlayStateReport;
import com.baidu.duer.dcs.devicemodule.audioplayer.report.AudioPlayerProgressReporter;
import com.baidu.duer.dcs.devicemodule.audioplayer.report.AudioPlayerTimer;
import com.baidu.duer.dcs.devicemodule.system.HandleDirectiveException;
import com.baidu.duer.dcs.framework.BaseDeviceModule;
import com.baidu.duer.dcs.framework.IMessageSender;
import com.baidu.duer.dcs.framework.message.ClientContext;
import com.baidu.duer.dcs.framework.message.Directive;
import com.baidu.duer.dcs.framework.message.Header;
import com.baidu.duer.dcs.systeminterface.IMediaPlayer;
import com.baidu.duer.dcs.util.LogUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 音乐播放的端能力实现，处理指令：Play，Stop，ClearQueue
 * <p>
 * Created by guxiuzhong@baidu.com on 2017/5/31.
 */
public class AudioPlayerDeviceModule extends BaseDeviceModule {
    private static final String TAG = AudioPlayerDeviceModule.class.getSimpleName();
    // 播放列表，先进先出
    private LinkedList<PlayPayload.Stream> playQueue = new LinkedList<>();
    // 当前stream的token
    private String latestStreamToken = "";
    // 当前manager的播放器
    private IMediaPlayer mediaPlayer;
    // 播放上报
    private AudioPlayStateReport audioPlayStateReport;
    // 开始时的缓冲时间
    private long bufferingStartMilliseconds;
    // 结束时的缓冲时间
    private long bufferingEndMilliseconds;
    // 用来统计状态的时间间隔offsetInMilliseconds
    private AudioPlayerTimer timer;
    // 播放状态的上报eg：暂停，完成等
    private AudioPlayerProgressReporter progressReporter;
    // 回调接口
    private List<IMediaPlayer.IMediaPlayerListener> audioPlayerListeners;

    public AudioPlayerDeviceModule(IMediaPlayer mediaPlayer,
                                   IMessageSender messageSender) {
        super(ApiConstants.NAMESPACE, messageSender);
        this.mediaPlayer = mediaPlayer;
        this.mediaPlayer.addMediaPlayerListener(mediaPlayerListener);
        this.audioPlayStateReport = new AudioPlayStateReport(getNameSpace(),
                messageSender,
                audioPlayStateReportListener);
        this.timer = new AudioPlayerTimer();
        this.progressReporter = new AudioPlayerProgressReporter(
                new ProgressReportDelayEventRunnable(audioPlayStateReport),
                new ProgressReportIntervalEventRunnable(audioPlayStateReport), timer);
        this.audioPlayerListeners = Collections.synchronizedList(
                new ArrayList<IMediaPlayer.IMediaPlayerListener>());
    }

    @Override
    public ClientContext clientContext() {
        String namespace = ApiConstants.NAMESPACE;
        String name = ApiConstants.Events.PlaybackState.NAME;
        Header header = new Header(namespace, name);
        PlaybackStatePayload payload = new PlaybackStatePayload(latestStreamToken,
                mediaPlayer.getCurrentPosition(),
                audioPlayStateReport.getState().name());
        return new ClientContext(header, payload);
    }

    @Override
    public void handleDirective(Directive directive) throws HandleDirectiveException {
        LogUtil.d(TAG, "dcs-play-directive:" + directive.rawMessage);
        String directiveName = directive.getName();
        LogUtil.d(TAG, "dcs-play-directiveName:" + directiveName);
        if (ApiConstants.Directives.Play.NAME.equals(directiveName)) {
            handlePlay((PlayPayload) directive.getPayload());
        } else if (ApiConstants.Directives.Stop.NAME.equals(directiveName)) {
            handleStop((StopPayload) directive.getPayload());
        } else if (ApiConstants.Directives.ClearQueue.NAME.equals(directiveName)) {
            handleClearQueue((ClearQueuePayload) directive.getPayload());
        } else {
            String message = "audioPlayer cannot handle the directive";
            throw (new HandleDirectiveException(
                    HandleDirectiveException.ExceptionType.UNSUPPORTED_OPERATION, message));
        }
    }

    /**
     * 处理播放指令（Play）
     *
     * @param payload payload
     */
    private void handlePlay(PlayPayload payload) {
        PlayPayload.AudioItem item = payload.audioItem;
        if (payload.playBehavior == PlayPayload.PlayBehavior.REPLACE_ALL) {
            clearAll();
        } else if (payload.playBehavior == PlayPayload.PlayBehavior.REPLACE_ENQUEUED) {
            clearEnqueued();
        }
        final PlayPayload.Stream stream = item.stream;
        String streamUrl = stream.url;
        String streamId = stream.token;
        long offset = stream.offsetInMilliseconds;
        LogUtil.i(TAG, "URL:" + streamUrl);
        LogUtil.i(TAG, "StreamId:" + streamId);
        LogUtil.i(TAG, "Offset:" + offset);
        add(stream);
    }

    /**
     * 处理停止指令（Stop）
     *
     * @param payload payload
     */
    private void handleStop(StopPayload payload) {
        stop();
    }

    /**
     * 处理清空队列指令（Stop）
     *
     * @param clearQueuePayload clearQueuePayload
     */
    private void handleClearQueue(ClearQueuePayload clearQueuePayload) {
        // 清除播放列表，并停止当前播放的音频（如果有）
        if (clearQueuePayload.clearBehavior == ClearQueuePayload.ClearBehavior.CLEAR_ALL) {
            audioPlayStateReport.clearQueueAll();
            clearAll();
        } else if (clearQueuePayload.clearBehavior == ClearQueuePayload.ClearBehavior.CLEAR_ENQUEUED) {
            // 清除播放列表，但不影响当前播放
            audioPlayStateReport.clearQueueEnqueued();
            clearEnqueued();
        }
    }

    private void add(PlayPayload.Stream stream) {
        String expectedPreviousToken = stream.expectedPreviousToken;
        boolean startPlaying = playQueue.isEmpty();
        if (expectedPreviousToken == null || latestStreamToken.isEmpty()
                || latestStreamToken.equals(expectedPreviousToken)) {
            playQueue.add(stream);
        }
        LogUtil.d(TAG, " coming  playQueue size :" + playQueue.size());
        if (startPlaying) {
            startPlay();
        }
    }

    /**
     * 开始播放音乐
     */
    private void startPlay() {
        if (playQueue.isEmpty()) {
            LogUtil.d(TAG, "startPlay-playQueue isEmpty ！！");
            return;
        }
        PlayPayload.Stream currentStream = playQueue.peek();
        if (currentStream == null) {
            return;
        }
        latestStreamToken = currentStream.token;
        String url = currentStream.url;
        // 从哪个位置开始播放
        long offset = currentStream.offsetInMilliseconds;
        // 判断是否是流类型还是URL类型
        if (currentStream.hasAttachedContent()) {
            mediaPlayer.play(new IMediaPlayer.MediaResource(currentStream.getAttachedContent()));
        } else {
            mediaPlayer.play(new IMediaPlayer.MediaResource(url));
        }
        mediaPlayer.seekTo((int) offset);
    }

    private IMediaPlayer.IMediaPlayerListener mediaPlayerListener = new IMediaPlayer.SimpleMediaPlayerListener() {
        // 是否处于暂停
        private boolean isPause;
        // 是否第一次到达了100
        private boolean stutterFinished;
        // 是否处于上报ProgressReport事件中
        private boolean progressReporting;
        // 是否处于缓冲中
        private boolean bufferUnderRunInProgress;

        @Override
        public void onInit() {
            super.onInit();
            LogUtil.d(TAG, "onInit");
            isPause = false;
            stutterFinished = false;
            progressReporting = false;
            bufferUnderRunInProgress = false;
        }

        @Override
        public void onPrepared() {
            super.onPrepared();
            LogUtil.d(TAG, "onPrepared");
            fireOnPrepared();
        }

        @Override
        public void onPlaying() {
            super.onPlaying();
            LogUtil.d(TAG, "onPlaying");
            // 暂停后继续播放
            if (isPause) {
                isPause = false;
                audioPlayStateReport.playbackResumed();
            } else {
                // 第一次播放
                PlayPayload.Stream stream = playQueue.peek();
                if (stream == null) {
                    return;
                }
                long offset = stream.offsetInMilliseconds;
                LogUtil.d(TAG, "onPlaying---Duration----：" + mediaPlayer.getDuration());
                timer.reset(offset, mediaPlayer.getDuration());

                // 上报PlaybackStarted事件
                audioPlayStateReport.playbackStarted();
            }
            startTimerAndProgressReporter();
            fireOnPlaying();
        }

        @Override
        public void onPaused() {
            LogUtil.d(TAG, "onPaused");
            stopTimerAndProgressReporter();
            isPause = true;
            audioPlayStateReport.playbackPaused();
            fireOnPaused();
        }

        @Override
        public void onStopped() {
            super.onStopped();
            stopTimerAndProgressReporter();
            audioPlayStateReport.playbackStopped();
            fireOnStopped();
        }

        @Override
        public void onCompletion() {
            LogUtil.d(TAG, "onCompletion");
            stopTimerAndProgressReporter();
            playQueue.poll();
            audioPlayStateReport.playbackFinished();
            audioPlayStateReport.playbackNearlyFinished();
            if (!playQueue.isEmpty()) {
                startPlay();
            }
            fireOnCompletion();
        }

        @Override
        public void onRelease() {
            LogUtil.d(TAG, "onError");
            stopTimerAndProgressReporter();
            fireOnRelease();
        }

        @Override
        public void onError(String error, IMediaPlayer.ErrorType errorType) {
            LogUtil.d(TAG, "onError");
            playQueue.clear();
            audioPlayStateReport.playbackFailed(errorType);
            stopTimerAndProgressReporter();
            fireOnError(error, errorType);
        }

        @Override
        public void onBufferingUpdate(int percent) {
            LogUtil.d(TAG, "onBufferingUpdate：" + percent);
            fireOnBufferingUpdate(percent);
            PlayPayload.Stream stream = playQueue.peek();
            if (stream == null) {
                return;
            }
            // Play指令有progressReportDelayInMilliseconds
            if (!progressReporting && stream.getProgressReportRequired()) {
                LogUtil.d(TAG, "onBufferingUpdate：" + percent);
                progressReporting = true;
                progressReporter.disable();
                progressReporter.setup(stream.progressReport);
                long offset = stream.offsetInMilliseconds;
                timer.reset(offset, mediaPlayer.getDuration());
                startTimerAndProgressReporter();
            }

            // 已经缓冲完成了
            if (stutterFinished) {
                return;
            }
            // 开始缓冲
            if (!bufferUnderRunInProgress) {
                LogUtil.d(TAG, "==playbackStutterStarted");
                bufferUnderRunInProgress = true;
                bufferingStartMilliseconds = System.currentTimeMillis();
                audioPlayStateReport.playbackStutterStarted();
            }
            // 缓冲完毕后上报playbackStutterFinished
            if (percent >= 100) {
                stutterFinished = true;
                bufferingEndMilliseconds = System.currentTimeMillis();
                audioPlayStateReport.playbackStutterFinished();
            }
        }
    };

    private void startTimerAndProgressReporter() {
        timer.start();
        if (progressReporter.isSetup()) {
            progressReporter.start();
        }
    }

    private void stopTimerAndProgressReporter() {
        timer.stop();
        progressReporter.stop();
    }

    private void clearAll() {
        stop();
        playQueue.clear();
    }

    private void clearEnqueued() {
        PlayPayload.Stream top = playQueue.poll();
        playQueue.clear();
        if (top != null) {
            playQueue.add(top);
        }
    }

    private void stop() {
        if (!playQueue.isEmpty() && isPlayingOrPaused()) {
            stopTimerAndProgressReporter();
            // 要把播放策略中的对应的那一条删除了
            mediaPlayer.stop();
        }
    }

    private boolean isPlaying() {
        return (audioPlayStateReport.getState() == AudioPlayStateReport.AudioPlayerState.PLAYING
                || audioPlayStateReport.getState() == AudioPlayStateReport.AudioPlayerState.PAUSED
                || audioPlayStateReport.getState() == AudioPlayStateReport.AudioPlayerState.BUFFER_UNDERRUN);
    }

    private boolean isPlayingOrPaused() {
        return isPlaying() || audioPlayStateReport.getState() == AudioPlayStateReport.AudioPlayerState.PAUSED;
    }

    @Override
    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer.removeMediaPlayerListener(mediaPlayerListener);
        }
        stopTimerAndProgressReporter();
        audioPlayerListeners.clear();
    }

    /**
     * 播放上报时需要的信息
     */
    private AudioPlayStateReport.AudioPlayStateReportListener audioPlayStateReportListener =
            new AudioPlayStateReport.AudioPlayStateReportListener() {
                @Override
                public String getCurrentStreamToken() {
                    return latestStreamToken;
                }

                @Override
                public long getCurrentOffsetInMilliseconds() {
                    return getCurrentOffsetInMillisecondsByTime();
                }

                @Override
                public long getStutterDurationInMilliseconds() {
                    // 缓冲时间ms
                    return bufferingEndMilliseconds - bufferingStartMilliseconds;
                }
            };

    private long getCurrentOffsetInMillisecondsByTime() {
        AudioPlayStateReport.AudioPlayerState playerActivity = audioPlayStateReport.getState();
        long offset;
        switch (playerActivity) {
            case PLAYING:
            case PAUSED:
            case BUFFER_UNDERRUN:
            case STOPPED:
            case FINISHED:
                offset = timer.getOffsetInMilliseconds();
                break;
            case IDLE:
            default:
                offset = 0;
        }
        LogUtil.d(TAG, "getCurrentOffsetInMilliseconds offset:" + offset);
        return offset;
    }

    private static class ProgressReportDelayEventRunnable implements Runnable {
        private final AudioPlayStateReport audioPlayStateReport;

        ProgressReportDelayEventRunnable(AudioPlayStateReport audioPlayStateReport) {
            this.audioPlayStateReport = audioPlayStateReport;
        }

        @Override
        public void run() {
            audioPlayStateReport.reportProgressDelay();
        }
    }

    private static class ProgressReportIntervalEventRunnable implements Runnable {
        private final AudioPlayStateReport audioPlayStateReport;

        ProgressReportIntervalEventRunnable(AudioPlayStateReport audioPlayStateReport) {
            this.audioPlayStateReport = audioPlayStateReport;
        }

        @Override
        public void run() {
            audioPlayStateReport.reportProgressInterval();
        }
    }

    private void fireOnPrepared() {
        for (IMediaPlayer.IMediaPlayerListener listener : audioPlayerListeners) {
            listener.onPrepared();
        }
    }

    private void fireOnRelease() {
        for (IMediaPlayer.IMediaPlayerListener listener : audioPlayerListeners) {
            listener.onRelease();
        }
    }

    private void fireOnPlaying() {
        for (IMediaPlayer.IMediaPlayerListener listener : audioPlayerListeners) {
            listener.onPlaying();
        }
    }

    private void fireOnPaused() {
        for (IMediaPlayer.IMediaPlayerListener listener : audioPlayerListeners) {
            listener.onPaused();
        }
    }

    private void fireOnStopped() {
        for (IMediaPlayer.IMediaPlayerListener listener : audioPlayerListeners) {
            listener.onStopped();
        }
    }

    private void fireOnCompletion() {
        for (IMediaPlayer.IMediaPlayerListener listener : audioPlayerListeners) {
            listener.onCompletion();
        }
    }

    private void fireOnError(String error, IMediaPlayer.ErrorType errorType) {
        for (IMediaPlayer.IMediaPlayerListener listener : audioPlayerListeners) {
            listener.onError(error, errorType);
        }
    }

    private void fireOnBufferingUpdate(int percent) {
        for (IMediaPlayer.IMediaPlayerListener listener : audioPlayerListeners) {
            listener.onBufferingUpdate(percent);
        }
    }

    private void fireBufferingStart() {
        for (IMediaPlayer.IMediaPlayerListener listener : audioPlayerListeners) {
            listener.onBufferingStart();
        }
    }

    private void fireBufferingEnd() {
        for (IMediaPlayer.IMediaPlayerListener listener : audioPlayerListeners) {
            listener.onBufferingEnd();
        }
    }

    public void addAudioPlayListener(IMediaPlayer.IMediaPlayerListener listener) {
        audioPlayerListeners.add(listener);
    }
}
