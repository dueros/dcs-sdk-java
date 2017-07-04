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
package com.baidu.duer.dcs.devicemodule.alerts;

/**
 * 定义了表示Alerts模块的namespace、name，以及其事件、指令的name
 * <p>
 * Created by wuruisheng on 2017/6/1.
 */
public class ApiConstants {
    public static final String NAMESPACE = "ai.dueros.device_interface.alerts";
    public static final String NAME = "AlertsInterface";

    public static final class Events {
        public static final class SetAlertSucceeded {
            public static final String NAME = SetAlertSucceeded.class.getSimpleName();
        }

        public static final class SetAlertFailed {
            public static final String NAME = SetAlertFailed.class.getSimpleName();
        }

        public static final class DeleteAlertSucceeded {
            public static final String NAME = DeleteAlertSucceeded.class.getSimpleName();
        }

        public static final class DeleteAlertFailed {
            public static final String NAME = DeleteAlertFailed.class.getSimpleName();
        }

        public static final class AlertStarted {
            public static final String NAME = AlertStarted.class.getSimpleName();
        }

        public static final class AlertStopped {
            public static final String NAME = AlertStopped.class.getSimpleName();
        }

        public static final class AlertsState {
            public static final String NAME = AlertsState.class.getSimpleName();
        }

        public static final class AlertEnteredForeground {
            public static final String NAME = AlertEnteredForeground.class.getSimpleName();
        }

        public static final class AlertEnteredBackground {
            public static final String NAME = AlertEnteredBackground.class.getSimpleName();
        }
    }

    public static final class Directives {
        public static final class SetAlert {
            public static final String NAME = SetAlert.class.getSimpleName();
        }

        public static final class DeleteAlert {
            public static final String NAME = DeleteAlert.class.getSimpleName();
        }
    }
}
