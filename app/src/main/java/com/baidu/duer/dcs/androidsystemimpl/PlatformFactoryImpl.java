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
package com.baidu.duer.dcs.androidsystemimpl;

import android.content.Context;
import android.os.Looper;

import com.baidu.duer.dcs.androidsystemimpl.alert.AlertsFileDataStoreImpl;
import com.baidu.duer.dcs.androidsystemimpl.audioinput.AudioVoiceInputImpl;
import com.baidu.duer.dcs.androidsystemimpl.playbackcontroller.IPlaybackControllerImpl;
import com.baidu.duer.dcs.androidsystemimpl.player.AudioTrackPlayerImpl;
import com.baidu.duer.dcs.androidsystemimpl.player.MediaPlayerImpl;
import com.baidu.duer.dcs.androidsystemimpl.wakeup.WakeUpImpl;
import com.baidu.duer.dcs.systeminterface.IAlertsDataStore;
import com.baidu.duer.dcs.systeminterface.IAudioInput;
import com.baidu.duer.dcs.systeminterface.IAudioRecord;
import com.baidu.duer.dcs.systeminterface.IHandler;
import com.baidu.duer.dcs.systeminterface.IMediaPlayer;
import com.baidu.duer.dcs.systeminterface.IPlatformFactory;
import com.baidu.duer.dcs.systeminterface.IPlaybackController;
import com.baidu.duer.dcs.systeminterface.IWakeUp;
import com.baidu.duer.dcs.systeminterface.IWebView;

import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by wuruisheng on 2017/6/7.
 */
public class PlatformFactoryImpl implements IPlatformFactory {
    private IHandler mainHandler;
    private IAudioInput voiceInput;
    private IWebView webView;
    private IPlaybackController playback;
    private Context context;
    private IAudioRecord audioRecord;
    private LinkedBlockingDeque<byte[]> linkedBlockingDeque = new LinkedBlockingDeque<>();

    public PlatformFactoryImpl(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public IHandler createHandler() {
        return new HandlerImpl();
    }

    @Override
    public IHandler getMainHandler() {
        if (mainHandler == null) {
            mainHandler = new HandlerImpl(Looper.getMainLooper());
        }

        return mainHandler;
    }

    @Override
    public IAudioRecord getAudioRecord() {
        if (audioRecord == null) {
            audioRecord = new AudioRecordThread(linkedBlockingDeque);
        }
        return audioRecord;
    }

    @Override
    public IWakeUp getWakeUp() {
        return new WakeUpImpl(context, linkedBlockingDeque);
    }

    @Override
    public IAudioInput getVoiceInput() {
        if (voiceInput == null) {
            voiceInput = new AudioVoiceInputImpl(linkedBlockingDeque);
        }

        return voiceInput;
    }

    @Override
    public IMediaPlayer createMediaPlayer() {
        return new MediaPlayerImpl();
    }

    @Override
    public IMediaPlayer createAudioTrackPlayer() {
        return new AudioTrackPlayerImpl();
    }

    public IAlertsDataStore createAlertsDataStore() {
        return new AlertsFileDataStoreImpl();
    }

    @Override
    public IWebView getWebView() {
        return webView;
    }

    @Override
    public IPlaybackController getPlayback() {
        if (playback == null) {
            playback = new IPlaybackControllerImpl();
        }

        return playback;
    }

    public void setWebView(IWebView webView) {
        this.webView = webView;
    }
}