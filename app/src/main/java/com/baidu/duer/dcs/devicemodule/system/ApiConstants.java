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
package com.baidu.duer.dcs.devicemodule.system;

/**
 * 定义了表示System模块的namespace、name，以及其事件、指令、异常的name
 * <p>
 * Created by wuruisheng on 2017/6/1.
 */
public class ApiConstants {
    public static final String NAMESPACE = "ai.dueros.device_interface.system";
    public static final String NAME = "SystemInterface";

    public static final class Events {
        public static final class SynchronizeState {
            public static final String NAME = SynchronizeState.class.getSimpleName();
        }

        public static final class ExceptionEncountered {
            public static final String NAME = ExceptionEncountered.class.getSimpleName();
        }

        public static final class UserInactivityReport {
            public static final String NAME = UserInactivityReport.class.getSimpleName();
        }
    }

    public static final class Directives {
        public static final class ResetUserInactivity {
            public static final String NAME = ResetUserInactivity.class.getSimpleName();
        }

        public static final class SetEndpoint {
            public static final String NAME = SetEndpoint.class.getSimpleName();
        }

        public static final class ThrowException {
            public static final String NAME = ThrowException.class.getSimpleName();
        }
    }

    public static final class Exception {
        public static final String NAME = Exception.class.getSimpleName();
    }
}
