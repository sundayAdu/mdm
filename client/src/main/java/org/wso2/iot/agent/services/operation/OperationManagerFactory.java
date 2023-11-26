/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.iot.agent.services.operation;

import android.annotation.TargetApi;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.os.Build;
import org.wso2.iot.agent.api.DeviceInfo;
import org.wso2.iot.agent.utils.Constants;

/**
 * This class produce the matching Operation Manager according to the Device Configurations.
 */
public class OperationManagerFactory {

    private Context context;
    private DevicePolicyManager manager;

    public OperationManagerFactory(Context context) {
        this.context = context;
        this.manager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
    }

    public OperationManager getOperationManager() {
        if (manager.isProfileOwnerApp(Constants.AGENT_PACKAGE)) {
            return new OperationManagerWorkProfile(context);
        } else {
            return new OperationManagerBYOD(context);
        }
    }

}
