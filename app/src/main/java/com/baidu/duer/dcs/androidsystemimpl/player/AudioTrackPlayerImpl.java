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
package com.baidu.duer.dcs.androidsystemimpl.player;

import android.app.Service;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.baidu.duer.dcs.framework.DcsStream;
import com.baidu.duer.dcs.systeminterface.IMediaPlayer;
import com.baidu.duer.dcs.util.SystemServiceManager;


import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * AudioTrack播放器-播放pcm数据
 * <p>
 * Created by guxiuzhong@baidu.com on 2017/8/1.
 */
public class AudioTrackPlayerImpl implements IMediaPlayer {
    private static final String TAG = AudioTrackPlayerImpl.class.getSimpleName();
    private static final String KEY_SP_VOLUME = "currentVolume";
    private static final String KEY_SP_MUTE = "isMute";
    private static final int AUDIO_FORMAT_PCM8K = 8000;
    private static final int AUDIO_FORMAT_PCM16K = 16000;
    private int mChannelConfig = AudioFormat.CHANNEL_OUT_MONO;
    private int mStreamType = AudioManager.STREAM_MUSIC;
    private int iCurrentQueueAudioFormat = -1;
    private AudioTrack mAudioTrack;
    private PlayState mCurrentState = PlayState.IDLE;
    // 默认音量80%
    private float currentVolume = 0.8f;
    private boolean isMute;
    private List<IMediaPlayerListener> mediaPlayerListeners;
    private TelephonyManager telephonyManager;
    private boolean isActive;
    private Handler handlerMain = new Handler();
    private int minBufferSize;
    private Context context = SystemServiceManager.getAppContext();
    private long currentPlayTimeMs;

    public AudioTrackPlayerImpl() {
        // init
        initAudioTrack(AUDIO_FORMAT_PCM8K, 1);
        // 读取音量和静音的数据
        currentVolume = (float) MediaPlayerPreferenceUtil.get(context,
                KEY_SP_VOLUME, 0.8f);
        isMute = (boolean) MediaPlayerPreferenceUtil.get(context,
                KEY_SP_MUTE, false);
        // LinkedList
        mediaPlayerListeners = Collections.synchronizedList(new LinkedList<IMediaPlayerListener>());
        // 来电监听
        telephonyManager = (TelephonyManager)
                context.getSystemService(Service.TELEPHONY_SERVICE);
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    private PhoneStateListener phoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            switch (state) {
                // 电话挂断
                case TelephonyManager.CALL_STATE_IDLE:
                    resume();
                    break;
                // 等待接电话
                case TelephonyManager.CALL_STATE_RINGING:
                    pause();
                    break;
                // 通话中
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    break;
                default:
                    break;
            }
        }
    };

    private void initAudioTrack(int sampleRate, int channels) {
        if (sampleRate <= 0) {
            sampleRate = AUDIO_FORMAT_PCM8K;
        }
        if (channels <= 0) {
            channels = 1;
        }
        if (channels == 1) {
            mChannelConfig = AudioFormat.CHANNEL_OUT_MONO;
        } else if (channels == 2) {
            mChannelConfig = AudioFormat.CHANNEL_OUT_STEREO;
        }
        if (iCurrentQueueAudioFormat == sampleRate) {
            if (mAudioTrack == null) {
                mAudioTrack = createAudioTrack(iCurrentQueueAudioFormat);
            }
        } else {
            Log.d(TAG, "Decoder-initAudioTrack-sampleRate=" + sampleRate);
            Log.d(TAG, "Decoder-initAudioTrack-channels=" + channels);
            mAudioTrack = createAudioTrack(sampleRate);
            iCurrentQueueAudioFormat = sampleRate;
        }
    }

    private AudioTrack createAudioTrack(int sampleRate) {
        int encoding = AudioFormat.ENCODING_PCM_16BIT;
        // 得到一个满足最小要求的缓冲区的大小
        int minBufferSize = getMinBufferSize(sampleRate, mChannelConfig, encoding);
        Log.d(TAG, "Decoder-AudioTrack-minBufferSize=" + minBufferSize);
        AudioTrack audioTrack =
                new AudioTrack(mStreamType,
                        sampleRate,
                        mChannelConfig,
                        encoding,
                        minBufferSize,
                        AudioTrack.MODE_STREAM);
        audioTrack.play();
        return audioTrack;
    }

    private int getMinBufferSize(int sampleRate, int channelConfig, int audioFormat) {
        minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat);
        // 解决异常IllegalArgumentException: Invalid audio buffer size
        int channelCount = 1;
        switch (channelConfig) {
            // AudioFormat.CHANNEL_CONFIGURATION_DEFAULT
            case AudioFormat.CHANNEL_OUT_DEFAULT:
            case AudioFormat.CHANNEL_OUT_MONO:
            case AudioFormat.CHANNEL_CONFIGURATION_MONO:
                channelCount = 1;
                break;
            case AudioFormat.CHANNEL_OUT_STEREO:
            case AudioFormat.CHANNEL_CONFIGURATION_STEREO:
                channelCount = 2;
                break;
            default:
                channelCount = Integer.bitCount(channelConfig);
        }
        // 判断minBufferSize是否在范围内，如果不在设定默认值为1152
        int frameSizeInBytes = channelCount * (audioFormat == AudioFormat.ENCODING_PCM_8BIT ? 1 : 2);
        if ((minBufferSize % frameSizeInBytes != 0) || (minBufferSize < 1)) {
            minBufferSize = 1152;
        }
        return minBufferSize;
    }


    @Override
    public PlayState getPlayState() {
        return mCurrentState;
    }

    private void play(DcsStream dcsStream) {
        if (mCurrentState != PlayState.PLAYING) {
            mCurrentState = PlayState.PLAYING;
            firePlaying();
        }
        Log.i(TAG, "Decoder-START WriteWorkThread");
        writeWorkThread = new WriteWorkThread(dcsStream);
        writeWorkThread.start();
    }

    @Override
    public void play(MediaResource mediaResource) {
        fireOnInit();
        prepared();
        play(mediaResource.dcsStream);
    }

    private void prepared() {
        mCurrentState = PlayState.PREPARED;
        fireOnPrepared();
        //  一开始就说话让它静音了
        if (isMute) {
            mAudioTrack.setStereoVolume(0, 0);
        } else {
            setVolume(currentVolume);
        }
    }

    private WriteWorkThread writeWorkThread;

    private final class WriteWorkThread extends Thread {
        private DcsStream dcsStream;
        private LinkedBlockingDeque<byte[]> deque;
        private volatile boolean isStop;
        private int needWriteTotal;

        public WriteWorkThread(DcsStream dcsStream) {
            this.dcsStream = dcsStream;
            this.deque = this.dcsStream.dataQueue;
            // 需要再次调用play方法
            mAudioTrack.play();
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
            Log.i("Decoder", "minBufferSize=" + minBufferSize);
            needWriteTotal = minBufferSize + 400;
        }

        public void stopWrite() {
            isStop = true;
            this.deque.clear();
            this.interrupt();
        }

        @Override
        public void run() {
            super.run();
            while (!isStop && !dcsStream.isFinished()) {
                try {
                    byte[] writeBytes = deque.take();
                    initAudioTrack(dcsStream.sampleRate, dcsStream.channels);
                    if (writeBytes != null && writeBytes.length > 0) {
                        // writeBytes.length always 1152 ( mp3 frame )
                        if (needWriteTotal <= 0) {
                            mAudioTrack.write(writeBytes, 0, writeBytes.length);
                        } else {
                            int ret = needWriteTotal - writeBytes.length;
                            if (ret <= 0) {
                                long start1 = System.currentTimeMillis();
                                byte[] buffer1 = new byte[needWriteTotal];
                                System.arraycopy(writeBytes, 0, buffer1, 0, buffer1.length);
                                int bytesWritten = mAudioTrack.write(buffer1, 0, buffer1.length);
                                long end1 = System.currentTimeMillis();
                                Log.i(TAG, "Decoder-write  if," + bytesWritten + "," + (end1 - start1));
                                Log.i("Decoder", "满足有声音:" + System.currentTimeMillis());

                                byte[] buffer2 = new byte[writeBytes.length - buffer1.length];
                                System.arraycopy(writeBytes, buffer1.length, buffer2, 0, buffer2.length);
                                mAudioTrack.write(buffer2, 0, buffer2.length);
                            } else {
                                long start1 = System.currentTimeMillis();
                                int bytesWritten = mAudioTrack.write(writeBytes, 0, writeBytes.length);
                                long end1 = System.currentTimeMillis();
                                Log.i(TAG, "Decoder-write else ," + bytesWritten + "," + (end1 - start1));
                            }
                            needWriteTotal -= writeBytes.length;
                        }
                    }
                } catch (Exception e) {
                    Log.d(TAG, "Decoder-WriteWorkThread Exception.", e);
                    break;
                }
            }
            Log.d(TAG, "Decoder-bytesWritten finished.");
            handlerMain.post(new Runnable() {
                @Override
                public void run() {
                    // 只有播放完成，非打断状态才会回调OnCompletion
                    if (mCurrentState == PlayState.PLAYING) {
                        mCurrentState = PlayState.COMPLETED;
                        fireOnCompletion();
                    } else {
                        mCurrentState = PlayState.COMPLETED;
                    }
                }
            });
        }
    }

    @Override
    public void pause() {
        if (mCurrentState == PlayState.PLAYING
                || mCurrentState == PlayState.PREPARED
                || mCurrentState == PlayState.PREPARING) {
            mAudioTrack.pause();
            mCurrentState = PlayState.PAUSED;
            fireOnPaused();
        }
    }

    private void getAudioTrackCurrentPosition() {
        if (mCurrentState == PlayState.COMPLETED || mCurrentState == PlayState.IDLE) {
            currentPlayTimeMs = 0;
            return;
        }
        int currentFrame = mAudioTrack.getPlaybackHeadPosition();
        Log.d(TAG, "currentFrame=" + currentFrame);
        float playTime = currentFrame * 1.0f / mAudioTrack.getPlaybackRate();
        Log.d(TAG, "playTime=" + playTime);
        currentPlayTimeMs = (long) (1000 * playTime);
    }

    @Override
    public void stop() {
        getAudioTrackCurrentPosition();
        mCurrentState = PlayState.STOPPED;
        if (writeWorkThread != null) {
            writeWorkThread.stopWrite();
        }
        try {
            Log.d(TAG, "stop-PlayState:" + mAudioTrack.getPlayState());
            if (mAudioTrack != null && mAudioTrack.getPlayState() != AudioTrack.STATE_UNINITIALIZED) {
                mAudioTrack.pause();
                mAudioTrack.flush();
                Log.d(TAG, "stop-ok");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "stop()", e);
        }
        fireStopped();
    }

    @Override
    public void resume() {
        if (mCurrentState == PlayState.PAUSED) {
            mAudioTrack.play();
            mCurrentState = PlayState.PLAYING;
            firePlaying();
        }
    }

    @Override
    public void seekTo(int milliseconds) {
        throw new RuntimeException("unSupport seekTo.");
    }

    @Override
    public void release() {
        mCurrentState = PlayState.IDLE;
        if (writeWorkThread != null) {
            writeWorkThread.stopWrite();
        }
        try {
            Log.d(TAG, "release-PlayState:" + mAudioTrack.getPlayState());
            if (mAudioTrack != null && mAudioTrack.getPlayState() != AudioTrack.STATE_UNINITIALIZED) {
                mAudioTrack.pause();
                mAudioTrack.flush();
                mAudioTrack.stop();
                mAudioTrack.release();
                Log.d(TAG, "release-ok");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "release()", e);
        }
        fireOnRelease();
        mediaPlayerListeners.clear();
        handlerMain.removeCallbacksAndMessages(null);
    }

    /**
     * 设置音量
     *
     * @param volume 0.0 -1.0
     */
    @Override
    public void setVolume(float volume) {
        // 设置音量就不再静音了，比如：说了调衡音量等操作
        isMute = false;
        currentVolume = volume;
        if (mAudioTrack != null) {
            mAudioTrack.setStereoVolume(volume, volume);
        }
        // 保存数据
        MediaPlayerPreferenceUtil.put(context,
                KEY_SP_VOLUME, currentVolume);
        MediaPlayerPreferenceUtil.put(context,
                KEY_SP_MUTE, isMute);
    }

    @Override
    public float getVolume() {
        return currentVolume;
    }

    @Override
    public void setMute(boolean mute) {
        isMute = mute;
        if (mAudioTrack != null) {
            if (mute) {
                mAudioTrack.setStereoVolume(0, 0);
            } else {
                mAudioTrack.setStereoVolume(currentVolume, currentVolume);
            }
        }
        // 保存数据
        MediaPlayerPreferenceUtil.put(context,
                KEY_SP_MUTE, isMute);
    }

    @Override
    public boolean getMute() {
        return isMute;
    }

    @Override
    public long getCurrentPosition() {
        return currentPlayTimeMs;
    }

    @Override
    public long getDuration() {
        return 0;
    }

    @Override
    public float getBufferPercentage() {
        return 0;
    }

    @Override
    public void addMediaPlayerListener(IMediaPlayerListener listener) {
        if (!mediaPlayerListeners.contains(listener)) {
            mediaPlayerListeners.add(listener);
        }
    }

    @Override
    public void removeMediaPlayerListener(IMediaPlayerListener listener) {
        if (mediaPlayerListeners.contains(listener)) {
            mediaPlayerListeners.remove(listener);
        }
    }

    @Override
    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    private void fireOnInit() {
        for (IMediaPlayerListener listener : mediaPlayerListeners) {
            if (listener != null) {
                listener.onInit();
            }
        }
    }

    private void fireOnPrepared() {
        for (IMediaPlayerListener listener : mediaPlayerListeners) {
            if (listener != null) {
                listener.onPrepared();
            }
        }
    }

    private void fireOnPaused() {
        for (IMediaPlayerListener listener : mediaPlayerListeners) {
            if (listener != null) {
                listener.onPaused();
            }
        }
    }

    private void fireStopped() {
        for (IMediaPlayerListener listener : mediaPlayerListeners) {
            if (listener != null) {
                listener.onStopped();
            }
        }
    }

    private void fireOnRelease() {
        for (IMediaPlayerListener listener : mediaPlayerListeners) {
            if (listener != null) {
                listener.onRelease();
            }
        }
    }

    private void firePlaying() {
        for (IMediaPlayerListener listener : mediaPlayerListeners) {
            if (listener != null) {
                listener.onPlaying();
            }
        }
    }

    private void fireOnCompletion() {
        for (IMediaPlayerListener listener : mediaPlayerListeners) {
            if (listener != null) {
                listener.onCompletion();
            }
        }
    }
}