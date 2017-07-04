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
package com.baidu.duer.dcs.devicemodule.audioplayer.report;

import com.baidu.duer.dcs.devicemodule.audioplayer.message.PlayPayload;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 当服务器返回Play指令有progressReportDelayInMilliseconds，
 * <p>
 * ProgressReportDelayElapsed和ProgressReportIntervalElapsed事件的上报
 * <p>
 * Created by guxiuzhong@baidu.com on 2017/5/22.
 */
public class AudioPlayerProgressReporter {
    private final ScheduledExecutorService eventScheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> progressReportDelayFuture;
    private ScheduledFuture<?> progressReportIntervalFuture;
    private final Runnable progressReportDelayRunnable;
    private final Runnable progressReportIntervalRunnable;
    private final AudioPlayerTimer audioPlayerTimer;
    private long progressReportDelay;
    private long progressReportInterval;
    private boolean isSetup;

    public AudioPlayerProgressReporter(Runnable progressReportDelayRunnable,
                                       Runnable progressReportIntervalRunnable,
                                       AudioPlayerTimer audioPlayerTimer) {
        if (progressReportDelayRunnable == null
                || progressReportIntervalRunnable == null
                || audioPlayerTimer == null) {
            throw new IllegalArgumentException("All arguments must be provided.");
        }
        this.progressReportDelayRunnable = progressReportDelayRunnable;
        this.progressReportIntervalRunnable = progressReportIntervalRunnable;
        this.audioPlayerTimer = audioPlayerTimer;
        this.isSetup = false;
    }

    public synchronized void setup(PlayPayload.ProgressReport progressReport) {
        if (progressReport == null) {
            String errorMessage = "ProgressReport must not be null.";
            throw new IllegalArgumentException(errorMessage);
        }
        if (isSetup) {
            String errorMessage = "AudioPlayerProgressReporter has already been setup. "
                    + "Please disable it before setting it up again.";
            throw new IllegalStateException(errorMessage);
        }

        cancelEvents();
        progressReportDelay = progressReport.progressReportDelayInMilliseconds;
        progressReportInterval = progressReport.progressReportIntervalInMilliseconds;
        isSetup = true;
    }

    public synchronized void disable() {
        isSetup = false;
        cancelEvents();
        progressReportDelay = 0;
        progressReportInterval = 0;
    }

    public synchronized void start() {
        cancelEvents();

        if (!isSetup) {
            String errorMessage = "AudioPlayerProgressReporter cannot be started "
                    + "because it has not been setup yet.";
            throw new IllegalStateException(errorMessage);
        }

        long currentOffsetIntoTrack = audioPlayerTimer.getOffsetInMilliseconds();

        long timeUntilDelayReport = progressReportDelay - currentOffsetIntoTrack;
        if (timeUntilDelayReport > 0) {
            scheduleDelayEvent(timeUntilDelayReport);
        }

        long timeUntilIntervalReport = progressReportInterval == 0 ? 0 :
                progressReportInterval - (currentOffsetIntoTrack % progressReportInterval);
        if (timeUntilIntervalReport > 0) {
            scheduleIntervalEvent(timeUntilIntervalReport, progressReportInterval);
        }
    }

    public synchronized void stop() {
        cancelEvents();
    }

    public synchronized boolean isSetup() {
        return isSetup;
    }

    private void scheduleDelayEvent(long delay) {
        progressReportDelayFuture = eventScheduler.schedule(progressReportDelayRunnable, delay,
                TimeUnit.MILLISECONDS);
    }

    private void scheduleIntervalEvent(long delay, long interval) {
        progressReportIntervalFuture = eventScheduler.scheduleAtFixedRate(
                progressReportIntervalRunnable, delay, interval, TimeUnit.MILLISECONDS);
    }

    private void cancelEvents() {
        if (progressReportDelayFuture != null && !progressReportDelayFuture.isDone()) {
            progressReportDelayFuture.cancel(false);
        }
        if (progressReportIntervalFuture != null && !progressReportIntervalFuture.isDone()) {
            progressReportIntervalFuture.cancel(false);
        }
    }
}
