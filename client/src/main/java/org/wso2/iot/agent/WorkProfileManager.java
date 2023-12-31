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
package org.wso2.iot.agent;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import org.wso2.iot.agent.api.ApplicationManager;
import org.wso2.iot.agent.utils.Constants;

public class WorkProfileManager extends Activity {

    private static final int REQUEST_PROVISION_MANAGED_PROFILE = 1;
    private static final String TAG = WorkProfileManager.class.getSimpleName();

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        provisionManagedProfile();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void provisionManagedProfile() {
        Activity activity = this;
        Intent intent =
                new Intent(android.app.admin.DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE);
        intent.putExtra(android.app.admin.
                        DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_PACKAGE_NAME,
                activity.getApplicationContext().getPackageName());
        // Once the provisioning is done, user is prompted to uninstall the agent in personal profile.
        ApplicationManager applicationManager = new ApplicationManager(this.getApplicationContext());
        try {
            applicationManager.uninstallApplication(Constants.AGENT_PACKAGE, null, null);
        } catch (AndroidAgentException e) {
            Log.e(TAG,"App uninstallation failed");
        }

        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_PROVISION_MANAGED_PROFILE);
            activity.finish();
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                Log.e(TAG,"Thread is interrupted");
            }
            Toast.makeText(this,
                    Constants.WorkProfile.MESSAGE_FOR_UNINSTALLING_AGENT,
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(activity,
                    Constants.WorkProfile.
                            MESSAGE_DEVICE_PROVISIONING_NOT_ENABLED, Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PROVISION_MANAGED_PROFILE) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, Constants.WorkProfile.PROVISIONING_DONE, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, Constants.WorkProfile.PROVISIONING_FAILED, Toast.LENGTH_SHORT).show();
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
