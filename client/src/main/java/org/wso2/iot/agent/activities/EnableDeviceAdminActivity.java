/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.iot.agent.activities;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.wso2.iot.agent.R;
import org.wso2.iot.agent.services.AgentDeviceAdminReceiver;
import org.wso2.iot.agent.utils.CommonUtils;

/**
 * Activity which handles device administrator activation for enrollment service.
 */
public class EnableDeviceAdminActivity extends Activity {

	private static final int ACTIVATION_REQUEST = 47;
	private ComponentName cdmDeviceAdmin;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		cdmDeviceAdmin = new ComponentName(this, AgentDeviceAdminReceiver.class);
		EnableDeviceAdminActivity.this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Intent deviceAdminIntent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
				deviceAdminIntent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, cdmDeviceAdmin);
				deviceAdminIntent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
				                           getResources().getString(R.string.device_admin_enable_alert));
				startActivityForResult(deviceAdminIntent, ACTIVATION_REQUEST);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ACTIVATION_REQUEST) {
			if (resultCode == Activity.RESULT_OK) {
				CommonUtils.callSystemApp(getApplicationContext(), null, null, null);
				Log.i("onActivityResult", "Administration enabled!");
			} else {
				Log.i("onActivityResult", "Administration enable FAILED!");
			}
			finish();
		}
	}

}
