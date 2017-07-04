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
package com.baidu.duer.dcs.devicemodule.speakcontroller;

/**
 * 定义了表示SpeakController模块的namespace、name，以及其事件、指令的name
 * <p>
 * Created by wuruisheng on 2017/6/1.
 */
public class ApiConstants {
    public static final String NAMESPACE = "ai.dueros.device_interface.speaker_controller";
    public static final String NAME = "SpeakerControllerInterface";

    public static final class Events {
        public static final class VolumeChanged {
            public static final String NAME = VolumeChanged.class.getSimpleName();
        }

        public static final class MuteChanged {
            public static final String NAME = MuteChanged.class.getSimpleName();
        }

        public static final class VolumeState {
            public static final String NAME = VolumeState.class.getSimpleName();
        }
    }

    public static final class Directives {
        public static final class SetVolume {
            public static final String NAME = SetVolume.class.getSimpleName();
        }

        public static final class AdjustVolume {
            public static final String NAME = AdjustVolume.class.getSimpleName();
        }

        public static final class SetMute {
            public static final String NAME = SetMute.class.getSimpleName();
        }
    }
}
