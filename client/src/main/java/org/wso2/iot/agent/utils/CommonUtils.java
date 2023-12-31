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
package org.wso2.iot.agent.utils;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.bouncycastle.jce.provider.asymmetric.ec.KeyPairGenerator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.iot.agent.AndroidAgentException;
import org.wso2.iot.agent.R;
import org.wso2.iot.agent.api.ApplicationManager;
import org.wso2.iot.agent.beans.AppRestriction;
import org.wso2.iot.agent.beans.Operation;
import org.wso2.iot.agent.beans.ServerConfig;
import org.wso2.iot.agent.beans.UnregisterProfile;
import org.wso2.iot.agent.events.listeners.DeviceCertCreationListener;
import org.wso2.iot.agent.proxy.APIController;
import org.wso2.iot.agent.proxy.beans.EndPointInfo;
import org.wso2.iot.agent.proxy.interfaces.APIResultCallBack;
import org.wso2.iot.agent.proxy.utils.Constants.HTTP_METHODS;
import org.wso2.iot.agent.services.AgentDeviceAdminReceiver;
import org.wso2.iot.agent.services.DynamicClientManager;
import org.wso2.iot.agent.services.LocationUpdateReceiver;
import org.wso2.iot.agent.services.PolicyOperationsMapper;
import org.wso2.iot.agent.services.PolicyRevokeHandler;
import org.wso2.iot.agent.services.ResultPayload;
import org.wso2.iot.agent.services.SystemServiceResponseReceiver;
import org.wso2.iot.agent.services.location.LocationService;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.security.auth.x500.X500Principal;

/**
 * This class represents all the common functions used throughout the application.
 */
public class CommonUtils {

	public static String TAG = CommonUtils.class.getSimpleName();
	private static final String PROTOCOL_HTTPS = "https://";
	private static final String PROTOCOL_HTTP = "http://";
	private static final String COLON = ":";
	/**
	 * Calls the secured API.
	 * @param context           -The Activity which calls an API..
	 * @param endpoint          -The API endpoint.
	 * @param methodType        -The method type.
	 * @param apiResultCallBack -The API result call back object.
	 * @param requestCode       -The request code.
	 */
	public static void callSecuredAPI(Context context, String endpoint, HTTP_METHODS methodType,
									  String requestParams,
									  APIResultCallBack apiResultCallBack, int requestCode) {

		EndPointInfo apiUtilities = new EndPointInfo();
		ServerConfig utils = new ServerConfig();
		apiUtilities.setEndPoint(endpoint);
		apiUtilities.setHttpMethod(methodType);
		if (requestParams != null) {
			apiUtilities.setRequestParams(requestParams);
		}

		if (endpoint.contains(Constants.NOTIFICATION_ENDPOINT)) {
			apiUtilities.setIsJSONArrayRequest(true);
		}

		APIController apiController;

		if (org.wso2.iot.agent.proxy.utils.Constants.Authenticator.AUTHENTICATOR_IN_USE.
				equals(org.wso2.iot.agent.proxy.utils.Constants.Authenticator.
						       MUTUAL_SSL_AUTHENTICATOR)) {
			apiController = new APIController();
			apiController.securedNetworkCall(apiResultCallBack, requestCode, apiUtilities, context);
		} else {
			String clientKey = Preference.getString(context, Constants.CLIENT_ID);
			String clientSecret = Preference.getString(context, Constants.CLIENT_SECRET);
			if (utils.getHostFromPreferences(context) != null
			    && !utils.getHostFromPreferences(context).isEmpty() &&
			    clientKey != null && !clientKey.isEmpty() && !clientSecret.isEmpty()) {
				apiController = new APIController(clientKey, clientSecret);
				apiController.invokeAPI(apiUtilities, apiResultCallBack, requestCode,
				                        context.getApplicationContext());
			}
		}

	}

	/**
	 * Generates keys, CSR and certificates for the devices.
	 * @param context - Application context.
	 * @param listener - DeviceCertCreationListener which provide device .
	 */
	public static void generateDeviceCertificate(final Context context, final DeviceCertCreationListener listener) throws AndroidAgentException{

		if(context.getFileStreamPath(Constants.DEVICE_CERTIFCATE_NAME).exists()){
			try {
				listener.onDeviceCertCreated(new BufferedInputStream(context.openFileInput(Constants.DEVICE_CERTIFCATE_NAME)));
			} catch (FileNotFoundException e) {
				Log.e(TAG, e.getMessage());
			}
		}else{

			try {
				ServerConfig utils = new ServerConfig();
				final KeyPair deviceKeyPair= KeyPairGenerator.getInstance(Constants.DEVICE_KEY_TYPE).generateKeyPair();
				X500Principal subject = new X500Principal(Constants.DEVICE_CSR_INFO);
				PKCS10CertificationRequest csr = new PKCS10CertificationRequest
						(Constants.DEVICE_KEY_ALGO, subject, deviceKeyPair.getPublic(), null, deviceKeyPair.getPrivate()
						);


				EndPointInfo endPointInfo = new EndPointInfo();
				endPointInfo.setHttpMethod(org.wso2.iot.agent.proxy.utils.Constants.HTTP_METHODS.POST);
				endPointInfo.setEndPoint(utils.getAPIServerURL(context) + Constants.SCEP_ENDPOINT);
				endPointInfo.setRequestParams(Base64.encodeToString(csr.getEncoded(), Base64.DEFAULT));

				new APIController().invokeAPI(endPointInfo,new APIResultCallBack() {
					@Override
					public void onReceiveAPIResult(Map<String, String> result, int requestCode) {
						try {
							CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
							InputStream in = new ByteArrayInputStream(Base64.decode(result.get("response"), Base64.DEFAULT));
							X509Certificate cert = (X509Certificate)certFactory.generateCertificate(in);
							ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
							KeyStore keyStore = KeyStore.getInstance("PKCS12");
							keyStore.load(null);
							keyStore.setKeyEntry(Constants.DEVICE_CERTIFCATE_ALIAS, (Key)deviceKeyPair.getPrivate(),
									Constants.DEVICE_CERTIFCATE_PASSWORD.toCharArray(),
									new java.security.cert.Certificate[]{cert});
							keyStore.store(byteArrayOutputStream, Constants.DEVICE_CERTIFCATE_PASSWORD.toCharArray());
							FileOutputStream outputStream = context.openFileOutput(Constants.DEVICE_CERTIFCATE_NAME, Context.MODE_PRIVATE);
							outputStream.write(byteArrayOutputStream.toByteArray());
							byteArrayOutputStream.close();
							outputStream.close();
							try {
								listener.onDeviceCertCreated(new BufferedInputStream(context.openFileInput(Constants.DEVICE_CERTIFCATE_NAME)));
							} catch (FileNotFoundException e) {
								Log.e(TAG, e.getMessage());
							}
						} catch (CertificateException | KeyStoreException | NoSuchAlgorithmException | IOException e) {
							Log.e(TAG, e.getMessage(), e);
						}
					}
				},Constants.SCEP_REQUEST_CODE,context, true);

			} catch (NoSuchAlgorithmException e) {
				throw new AndroidAgentException("No algorithm for key generation", e);
			} catch (SignatureException e) {
				throw new AndroidAgentException("Invalid Signature", e);
			} catch (NoSuchProviderException e) {
				throw new AndroidAgentException("Invalid provider", e);
			} catch (InvalidKeyException e) {
				throw new AndroidAgentException("Invalid key", e);
			}
		}
	}

	/**
	 * Clear application data.
	 * @param context - Application context.
	 */
	public static void clearAppData(Context context) throws AndroidAgentException {
		try {
			revokePolicy(context);
		} catch (SecurityException e) {
			throw new AndroidAgentException("Error occurred while revoking policy", e);
		} finally {
			Preference.clearPreferences(context);
			context.deleteDatabase(Constants.EMM_DB);
		}
		Toast.makeText(context, R.string.toast_message_disenroll, Toast.LENGTH_LONG).show();
	}

	/**
	 * Returns network availability status.
	 * @param context - Application context.
	 * @return - Network availability status.
	 */
	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivityManager =
				(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectivityManager.getActiveNetworkInfo();
		return (info != null && info.isConnected());
	}

	/**
	 * Convert given object to json formatted string.
	 * @param obj Object to be converted.
	 * @return Json formatted string.
	 * @throws AndroidAgentException
	 */
	public static String toJSON (Object obj) throws AndroidAgentException {
		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.writeValueAsString(obj);
		} catch (JsonMappingException e) {
			throw new AndroidAgentException("Error occurred while mapping class to json", e);
		} catch (JsonGenerationException e) {
			throw new AndroidAgentException("Error occurred while generating json", e);
		} catch (IOException e) {
			throw new AndroidAgentException("Error occurred while reading the stream", e);
		}
	}

	/**
	 * This method is used to initiate the oauth client app unregister process.
	 *
	 * @param context Application context
	 * @throws AndroidAgentException
	 */
	public static void unRegisterClientApp(Context context, APIResultCallBack apiCallBack) throws AndroidAgentException {
		String serverIP = Constants.DEFAULT_HOST;
		String prefIP = Preference.getString(context.getApplicationContext(), Constants.PreferenceFlag.IP);
		if (prefIP != null) {
			serverIP = prefIP;
		}
		if (serverIP != null && !serverIP.isEmpty()) {
			String applicationName = Preference.getString(context, Constants.CLIENT_NAME);
			String consumerKey = Preference.getString(context, Constants.CLIENT_ID);
			String userId = Preference.getString(context, Constants.USERNAME);

			if (applicationName != null && !applicationName.isEmpty() &&
			    consumerKey != null && !consumerKey.isEmpty() &&
			    userId != null && !userId.isEmpty()) {

				UnregisterProfile profile = new UnregisterProfile();
				profile.setApplicationName(applicationName);
				profile.setConsumerKey(consumerKey);
				profile.setUserId(userId);

				ServerConfig utils = new ServerConfig();
				utils.setServerIP(serverIP);

				DynamicClientManager dynamicClientManager = new DynamicClientManager();
				boolean isUnregistered = dynamicClientManager.unregisterClient(profile, utils, context, apiCallBack);

				if (!isUnregistered) {
					Log.e(TAG, "Error occurred while removing the OAuth client app");
				}
			} else {
				Log.e(TAG, "Client credential is not available");
			}
		} else {
			Log.e(TAG, "There is no valid IP to contact the server");
		}
	}

	/**
	 * Disable admin privileges.
	 * @param context - Application context.
	 */
	public static void disableAdmin(Context context) {
		DevicePolicyManager devicePolicyManager;
		ComponentName demoDeviceAdmin;
		devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
		demoDeviceAdmin = new ComponentName(context, AgentDeviceAdminReceiver.class);
		devicePolicyManager.removeActiveAdmin(demoDeviceAdmin);
	}

	/**
	 * Revoke currently enforced policy.
	 * @param context - Application context.
	 */
	public static void revokePolicy(Context context) throws AndroidAgentException {
		String payload = Preference.getString(context, Constants.PreferenceFlag.APPLIED_POLICY);
		PolicyOperationsMapper operationsMapper = new PolicyOperationsMapper();
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

		PolicyRevokeHandler revokeHandler = new PolicyRevokeHandler(context);

		try {
			if(payload != null) {
				List<org.wso2.iot.agent.beans.Operation> operations = mapper.readValue(
						payload,
						mapper.getTypeFactory().constructCollectionType(List.class,
						                                                org.wso2.iot.agent.beans.Operation.class));
				for (org.wso2.iot.agent.beans.Operation op : operations) {
					op = operationsMapper.getOperation(op);
					revokeHandler.revokeExistingPolicy(op);
				}
				Preference.putString(context, Constants.PreferenceFlag.APPLIED_POLICY, null);
			}
		} catch (IOException e) {
			throw new AndroidAgentException("Error occurred while parsing stream", e);
		}
	}

	/**
	 * Clear client credentials.
	 * @param context - Application context.
	 */
	public static void clearClientCredentials(Context context) {
		Preference.removePreference(context, Constants.CLIENT_ID);
		Preference.removePreference(context, Constants.CLIENT_SECRET);
	}

	/**
	 * Call EMM system app in COPE mode.
	 * @param context - Application context.
	 * @param operation - Operation code.
	 * @param command - Shell command to be executed.
	 * @param appUri - App package/APK URI when an app operation executed.
	 */
	public static void callSystemApp(Context context, String operation, String command, String appUri) {
		if(Constants.SYSTEM_APP_ENABLED) {
			Intent intent =  new Intent(Constants.SYSTEM_APP_SERVICE_START_ACTION);
			Intent explicitIntent = createExplicitFromImplicitIntent(context, intent);
			if (explicitIntent != null) {
				intent = explicitIntent;
			}
			intent.putExtra(Constants.OPERATION_CODE, operation);
			intent.setPackage(Constants.AGENT_PACKAGE);

			if (appUri != null) {
				intent.putExtra("appUri", appUri);
			}

			if (command != null) {
				if (Constants.Operation.UPGRADE_FIRMWARE.equals(operation)) {
					try {
						JSONObject upgradeData = new JSONObject(command);
						if (upgradeData.isNull(context.getResources()
								.getString(R.string.firmware_upgrade_automatic_retry)) && Preference.hasPreferenceKey(context, context
								.getResources().getString(R.string.is_automatic_firmware_upgrade))) {
							boolean isFirmwareUpgradeAutoRetry = Preference.getBoolean(context, context
									.getResources().getString(R.string.is_automatic_firmware_upgrade));
							upgradeData.put(context.getResources()
									.getString(R.string.firmware_upgrade_automatic_retry), isFirmwareUpgradeAutoRetry);
							command = upgradeData.toString();
							Log.d(TAG, "Updated payload: " + command);
						} else if (!upgradeData.isNull(context.getResources()
								.getString(R.string.firmware_upgrade_automatic_retry))){
							Preference.putBoolean(context, context.getResources()
									.getString(R.string.is_automatic_firmware_upgrade), upgradeData.getBoolean(context.getResources()
									.getString(R.string.firmware_upgrade_automatic_retry)));
						} else {
							upgradeData.put(context.getResources()
									.getString(R.string.firmware_upgrade_automatic_retry), false);
							Preference.putBoolean(context, context.getResources()
									.getString(R.string.is_automatic_firmware_upgrade), false);
							Log.d(TAG, "Updated payload: " + command);
						}
					} catch (JSONException e) {
						Log.e(TAG, "Could not parse Firmware upgrade operation", e);
					}
					if (Constants.DEBUG_MODE_ENABLED) {
						Log.d(TAG, "Firmware Upgrade Operation Id that is sent to the system app : " + Preference.getInt
								(context, "firmwareOperationId"));
					}
					intent.putExtra("operationId", Preference.getInt(context, "firmwareOperationId"));
				}
				intent.putExtra("command", command);
			}
			context.startService(intent);
		} else {
			Log.e(TAG, "System app not enabled.");
		}
	}

	public static void callSystemAppInit(Context context) {
		if(Constants.SYSTEM_APP_ENABLED) {
			Intent intent =  new Intent(Constants.SYSTEM_APP_SERVICE_START_ACTION);
			Intent explicitIntent = createExplicitFromImplicitIntent(context, intent);
			if (explicitIntent != null) {
				intent = explicitIntent;
			}
			context.startService(intent);
		} else {
			Log.e(TAG, "System app not enabled.");
		}
	}

	public static Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {
		//Retrieve all services that can match the given intent
		PackageManager pm = context.getPackageManager();
		List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);

		//Make sure only one match was found
		if (resolveInfo == null || resolveInfo.size() != 1) {
			return null;
		}

		//Get component info and create ComponentName
		ResolveInfo serviceInfo = resolveInfo.get(0);
		String packageName = serviceInfo.serviceInfo.packageName;
		String className = serviceInfo.serviceInfo.name;
		ComponentName component = new ComponentName(packageName, className);

		//Create a new intent. Use the old one for extras and such reuse
		Intent explicitIntent = new Intent(implicitIntent);

		//Set the component to be explicit
		explicitIntent.setComponent(component);

		return explicitIntent;
	}

	public static void registerSystemAppReceiver(Context context) {
		IntentFilter filter = new IntentFilter(Constants.SYSTEM_APP_BROADCAST_ACTION);
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		SystemServiceResponseReceiver receiver = new SystemServiceResponseReceiver();
		context.registerReceiver(receiver, filter);
	}

	/**
	 *	Get installed package list of applications in device
	 * @param context Context object
	 * @return list of installed app packages
	 */
	public static List<String> getInstalledAppPackages(Context context) {
		ApplicationManager applicationManager = new ApplicationManager(context.getApplicationContext());
		List<ApplicationInfo> installedApplications = applicationManager.getInstalledApplications();
		List<String> installedAppPackages = new ArrayList<>();
		for(ApplicationInfo appInfo : installedApplications) {
			installedAppPackages.add(appInfo.packageName);
		}
		return installedAppPackages;
	}

	/**
	 *	Get installed package list of applications in device by user
	 * @param context Context object
	 * @return list of installed app packages
	 */
	public static List<String> getInstalledAppPackagesByUser(Context context) {
		return new ApplicationManager(context.getApplicationContext()).getInstalledAppsByUser();
	}

	/**
	 * Get package list of applications that are not system apps
	 *
	 * @param context Context object
	 * @return list of package list that are not system apps
	 */
	public static List<String> getAppsOfUser(Context context) {
		return new ApplicationManager(context.getApplicationContext()).getAppsOfUser();
	}

	public static AppRestriction getAppRestrictionTypeAndList(Operation operation,
	                                                          ResultPayload resultBuilder,
	                                                          Resources resources)
			throws AndroidAgentException {
		AppRestriction appRestriction = new AppRestriction();
		JSONArray permittedApps = null;
		try {
			JSONObject payload = new JSONObject(operation.getPayLoad().toString());
			appRestriction.setRestrictionType(
					(String) payload.get(Constants.AppRestriction.RESTRICTION_TYPE));
			if (!payload.isNull(Constants.AppRestriction.RESTRICTED_APPLICATIONS)) {
				permittedApps = payload.getJSONArray(Constants.AppRestriction.RESTRICTED_APPLICATIONS);
			}
		} catch (JSONException e) {
			if (resources != null && resultBuilder != null) {
				operation.setStatus(resources.getString(R.string.operation_value_error));
				resultBuilder.build(operation);
			}
			throw new AndroidAgentException("Invalid JSON format.", e);
		}

		List<String> restrictedPackages = new ArrayList<>();

		if (permittedApps != null) {
			for (int i = 0; i < permittedApps.length(); i++) {
				try {
					restrictedPackages.add((String) ((JSONObject) permittedApps.get(i))
							.get(Constants.AppRuntimePermission.PACKAGE_NAME));
				} catch (JSONException e) {
					if (resources != null && resultBuilder != null) {
						operation.setStatus(resources.getString(R.string.operation_value_error));
						resultBuilder.build(operation);
					}
					throw new AndroidAgentException("Invalid JSON format", e);
				}
			}
		}

		appRestriction.setRestrictedList(restrictedPackages);
		return appRestriction;
	}

	public static AppRestriction getAppRuntimePermissionTypeAndList(Operation operation,
															  ResultPayload resultBuilder,
															  Resources resources)
			throws AndroidAgentException {
		AppRestriction appRestriction = new AppRestriction();
		JSONArray restrictedApps = null;
		try {
			JSONObject payload = new JSONObject(operation.getPayLoad().toString());
			appRestriction.setRestrictionType(
					(String) payload.get(Constants.AppRuntimePermission.PERMISSION_TYPE));
			if (!payload.isNull(Constants.AppRuntimePermission.PERMITTED_APPS)) {
				restrictedApps = payload.getJSONArray(Constants.AppRuntimePermission.PERMITTED_APPS);
			}
		} catch (JSONException e) {
			if (resources != null && resultBuilder != null) {
				operation.setStatus(resources.getString(R.string.operation_value_error));
				resultBuilder.build(operation);
			}
			throw new AndroidAgentException("Invalid JSON format.", e);
		}

		List<String> restrictedPackages = new ArrayList<>();

		if (restrictedApps != null) {
			for (int i = 0; i < restrictedApps.length(); i++) {
				try {
					restrictedPackages.add((String) ((JSONObject) restrictedApps.get(i))
							.get(Constants.AppRestriction.PACKAGE_NAME));
				} catch (JSONException e) {
					if (resources != null && resultBuilder != null) {
						operation.setStatus(resources.getString(R.string.operation_value_error));
						resultBuilder.build(operation);
					}
					throw new AndroidAgentException("Invalid JSON format", e);
				}
			}
		}

		appRestriction.setRestrictedList(restrictedPackages);
		return appRestriction;
	}

	public static void saveHostDeatils(Context context, String host){
		if (host.contains(PROTOCOL_HTTP)) {
			String hostWithPort = host.substring(PROTOCOL_HTTP.length(), host.length());
			Preference.putString(context.getApplicationContext(), Constants.PreferenceFlag.IP,
			                     getHostFromUrl(hostWithPort));
			Preference.putString(context.getApplicationContext(), Constants.PreferenceFlag.PROTOCOL, PROTOCOL_HTTP);
			Preference.putString(context.getApplicationContext(), Constants.PreferenceFlag.PORT,
			                     getPortFromUrl(hostWithPort, PROTOCOL_HTTP));
		} else if (host.contains(PROTOCOL_HTTPS)) {
			String hostWithPort = host.substring(PROTOCOL_HTTPS.length(), host.length());
			Preference.putString(context.getApplicationContext(), Constants.PreferenceFlag.IP,
			                     getHostFromUrl(hostWithPort));
			Preference.putString(context.getApplicationContext(), Constants.PreferenceFlag.PROTOCOL, PROTOCOL_HTTPS);
			Preference.putString(context.getApplicationContext(), Constants.PreferenceFlag.PORT,
			                     getPortFromUrl(hostWithPort, PROTOCOL_HTTPS));
		} else if (host.contains(COLON)) {
			Preference.putString(context.getApplicationContext(), Constants.PreferenceFlag.IP,
			                     getHostFromUrl(host));
			Preference.putString(context.getApplicationContext(), Constants.PreferenceFlag.PORT,
			                     getPortFromUrl(host, PROTOCOL_HTTP));
		} else {
			Preference.putString(context.getApplicationContext(), Constants.PreferenceFlag.IP, host);
		}
	}

	public static String getHostFromUrl (String url) {
		if (url.contains(COLON)) {
			return url.substring(0, url.indexOf(COLON));
		} else {
			return url;
		}
	}

	public static String getPortFromUrl (String url, String protocol) {
		if (url.contains(COLON)) {
			return url.substring((url.indexOf(COLON) + 1), url.length());
		} else {
			if(protocol.equals(PROTOCOL_HTTP)) {
				return String.valueOf(org.wso2.iot.agent.proxy.utils.Constants.HTTP);
			} else {
				return String.valueOf(org.wso2.iot.agent.proxy.utils.Constants.HTTPS);
			}
		}
	}

	public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	public static Location getLocation(Context context) {
		Intent serviceIntent = new Intent(context, LocationService.class);
		context.startService(serviceIntent);
		return LocationUpdateReceiver.getLocation();
	}

	public static void displayNotification(Context context, int icon, String title, String message, Class<?> sourceActivityClass, String tag, int id){
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
		mBuilder.setSmallIcon(icon);
		mBuilder.setContentTitle(title);
		mBuilder.setContentText(message);
		mBuilder.setOngoing(true);
		mBuilder.setOnlyAlertOnce(true);

		Intent resultIntent = new Intent(context, sourceActivityClass);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
		stackBuilder.addParentStack(sourceActivityClass);

		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);

		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		if (mNotificationManager != null) {
			mNotificationManager.notify(tag, id, mBuilder.build());
		} else {
			Log.w(TAG, "Unable to retrieve notification manager.");
		}
	}

	public static Date currentDate() {
		Calendar calendar = Calendar.getInstance();
		return calendar.getTime();
	}

	public static String getTimeAgo(long time, Context ctx) {

		Date curDate = currentDate();
		long now = curDate.getTime();
		if (time > now || time <= 0) {
			return null;
		}

		int dim = getTimeDistanceInMinutes(time);

		String timeAgo;

		if (dim == 0) {
			timeAgo = ctx.getResources().getString(R.string.date_util_term_less) + " " + ctx.getResources().getString(R.string.date_util_term_a) + " " + ctx.getResources().getString(R.string.date_util_unit_minute);
		} else if (dim == 1) {
			timeAgo = dim + ctx.getResources().getString(R.string.date_util_unit_minute);
		} else if (dim >= 2 && dim <= 44) {
			timeAgo = dim + " " + ctx.getResources().getString(R.string.date_util_unit_minutes);
		} else if (dim >= 45 && dim <= 89) {
			timeAgo = ctx.getResources().getString(R.string.date_util_prefix_about) + " " + ctx.getResources().getString(R.string.date_util_term_an) + " " + ctx.getResources().getString(R.string.date_util_unit_hour);
		} else if (dim >= 90 && dim <= 1439) {
			timeAgo = ctx.getResources().getString(R.string.date_util_prefix_about) + " " + (Math.round(dim / 60)) + " " + ctx.getResources().getString(R.string.date_util_unit_hours);
		} else if (dim >= 1440 && dim <= 2519) {
			timeAgo = "1 " + ctx.getResources().getString(R.string.date_util_unit_day);
		} else if (dim >= 2520 && dim <= 43199) {
			timeAgo = (Math.round(dim / 1440)) + " " + ctx.getResources().getString(R.string.date_util_unit_days);
		} else if (dim >= 43200 && dim <= 86399) {
			timeAgo = ctx.getResources().getString(R.string.date_util_prefix_about) + " " + ctx.getResources().getString(R.string.date_util_term_a) + " " + ctx.getResources().getString(R.string.date_util_unit_month);
		} else if (dim >= 86400 && dim <= 525599) {
			timeAgo = (Math.round(dim / 43200)) + " " + ctx.getResources().getString(R.string.date_util_unit_months);
		} else if (dim >= 525600 && dim <= 655199) {
			timeAgo = ctx.getResources().getString(R.string.date_util_prefix_about) + " " + ctx.getResources().getString(R.string.date_util_term_a) + " " + ctx.getResources().getString(R.string.date_util_unit_year);
		} else if (dim >= 655200 && dim <= 914399) {
			timeAgo = ctx.getResources().getString(R.string.date_util_prefix_over) + " " + ctx.getResources().getString(R.string.date_util_term_a) + " " + ctx.getResources().getString(R.string.date_util_unit_year);
		} else if (dim >= 914400 && dim <= 1051199) {
			timeAgo = ctx.getResources().getString(R.string.date_util_prefix_almost) + " 2 " + ctx.getResources().getString(R.string.date_util_unit_years);
		} else {
			timeAgo = ctx.getResources().getString(R.string.date_util_prefix_about) + " " + (Math.round(dim / 525600)) + " " + ctx.getResources().getString(R.string.date_util_unit_years);
		}

		return timeAgo + " " + ctx.getResources().getString(R.string.date_util_suffix);
	}

	private static int getTimeDistanceInMinutes(long time) {
		long timeDistance = currentDate().getTime() - time;
		return Math.round((Math.abs(timeDistance) / 1000) / 60);
	}

	public static void allowUnknownSourcesForProfile(final Context context, final boolean isEnabled) {
		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			final DevicePolicyManager manager = (DevicePolicyManager) context
					.getSystemService(Context.DEVICE_POLICY_SERVICE);
			if (manager != null && manager.isProfileOwnerApp(context.getApplicationContext()
					.getPackageName())) {
				final ComponentName cdmAdmin = new ComponentName(context, AgentDeviceAdminReceiver.class);
				manager.setSecureSetting(cdmAdmin, Settings.Secure.INSTALL_NON_MARKET_APPS,
						isEnabled ? "1" : "0");
				Log.i(TAG, "Enable unknown sources: " + String.valueOf(isEnabled));
			}
		}
	}

}
