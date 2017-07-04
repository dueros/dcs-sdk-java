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

/**
 * 定义了表示Audio Player模块的namespace、name，以及其事件、指令的name
 * <p>
 * Created by guxiuzhong@baidu.com on 2017/6/1.
 */
public class ApiConstants {
    public static final String NAMESPACE = "ai.dueros.device_interface.audio_player";
    public static final String NAME = "AudioPlayerInterface";

    public static final class Events {
        public static final class PlaybackStarted {
            public static final String NAME = PlaybackStarted.class.getSimpleName();
        }

        public static final class PlaybackNearlyFinished {
            public static final String NAME = PlaybackNearlyFinished.class.getSimpleName();
        }

        public static final class PlaybackStutterStarted {
            public static final String NAME = PlaybackStutterStarted.class.getSimpleName();
        }

        public static final class PlaybackStutterFinished {
            public static final String NAME = PlaybackStutterFinished.class.getSimpleName();
        }

        public static final class PlaybackFinished {
            public static final String NAME = PlaybackFinished.class.getSimpleName();
        }

        public static final class PlaybackFailed {
            public static final String NAME = PlaybackFailed.class.getSimpleName();
        }

        public static final class PlaybackStopped {
            public static final String NAME = PlaybackStopped.class.getSimpleName();
        }

        public static final class PlaybackPaused {
            public static final String NAME = PlaybackPaused.class.getSimpleName();
        }

        public static final class PlaybackResumed {
            public static final String NAME = PlaybackResumed.class.getSimpleName();
        }

        public static final class PlaybackQueueCleared {
            public static final String NAME = PlaybackQueueCleared.class.getSimpleName();
        }

        public static final class ProgressReportDelayElapsed {
            public static final String NAME = ProgressReportDelayElapsed.class.getSimpleName();
        }

        public static final class ProgressReportIntervalElapsed {
            public static final String NAME = ProgressReportIntervalElapsed.class
                    .getSimpleName();
        }

        public static final class PlaybackState {
            public static final String NAME = PlaybackState.class.getSimpleName();
        }
    }

    public static final class Directives {
        public static final class Play {
            public static final String NAME = Play.class.getSimpleName();
        }

        public static final class Stop {
            public static final String NAME = Stop.class.getSimpleName();
        }

        public static final class ClearQueue {
            public static final String NAME = ClearQueue.class.getSimpleName();
        }
    }
}
