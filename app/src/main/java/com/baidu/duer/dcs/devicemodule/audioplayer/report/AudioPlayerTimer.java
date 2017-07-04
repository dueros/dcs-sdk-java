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

import java.util.concurrent.TimeUnit;

/**
 * audio 播放统计OffsetInMilliseconds
 * <p>
 * Created by guxiuzhong@baidu.com on 2017/5/22.
 */
public class AudioPlayerTimer {
    private long startNano;
    private long elapsedTimeMs;
    private long totalStreamLength;
    private boolean isPlaying = false;

    public synchronized void start() {
        startNano = System.nanoTime();
        isPlaying = true;
    }

    public synchronized void stop() {
        if (isPlaying) {
            elapsedTimeMs += getCurrentOffsetInMilliseconds();
            isPlaying = false;
        }
    }

    public synchronized long getOffsetInMilliseconds() {
        long offset = elapsedTimeMs + (isPlaying ? getCurrentOffsetInMilliseconds() : 0);
        if (totalStreamLength > 0) {
            offset = Math.min(totalStreamLength, offset);
        }
        return offset;
    }

    public void reset() {
        reset(0);
    }

    public void reset(long startPosition) {
        reset(startPosition, -1);
    }

    public synchronized void reset(long startPosition, long maxPosition) {
        elapsedTimeMs = startPosition;
        isPlaying = false;
        startNano = System.nanoTime();
        totalStreamLength = maxPosition;
    }

    private long getCurrentOffsetInMilliseconds() {
        return TimeUnit.MILLISECONDS.convert(System.nanoTime() - startNano, TimeUnit.NANOSECONDS);
    }
}
