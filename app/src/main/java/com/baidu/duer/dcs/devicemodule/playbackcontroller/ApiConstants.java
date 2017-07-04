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
package com.baidu.duer.dcs.devicemodule.playbackcontroller;

/**
 * 定义了表示PlaybackController模块的namespace、name，以及其事件、指令的name
 * <p>
 * Created by wuruisheng on 2017/6/1.
 */
public class ApiConstants {
    public static final String NAMESPACE = "ai.dueros.device_interface.playback_controller";
    public static final String NAME = "PlaybackControllerInterface";

    public static final class Events {
        public static final class NextCommandIssued {
            public static final String NAME = NextCommandIssued.class.getSimpleName();
        }

        public static final class PreviousCommandIssued {
            public static final String NAME = PreviousCommandIssued.class.getSimpleName();
        }

        public static final class PlayCommandIssued {
            public static final String NAME = PlayCommandIssued.class.getSimpleName();
        }

        public static final class PauseCommandIssued {
            public static final String NAME = PauseCommandIssued.class.getSimpleName();
        }
    }
}
