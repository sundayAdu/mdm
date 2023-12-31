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

import org.wso2.iot.agent.BuildConfig;

import static org.wso2.iot.agent.proxy.utils.Constants.Authenticator.AUTHENTICATOR_IN_USE;
import static org.wso2.iot.agent.proxy.utils.Constants.Authenticator.MUTUAL_SSL_AUTHENTICATOR;

/**
 * This class holds all the constants used throughout the application.
 */
public class Constants {

	public static final boolean IS_CLOUD = "release".equalsIgnoreCase(BuildConfig.BUILD_TYPE.trim());
	public static final boolean DEBUG_MODE_ENABLED = BuildConfig.DEBUG_MODE_ENABLED;
	public static final boolean DISCLAIMER_ENABLED = BuildConfig.DISCLAIMER_ENABLED;
	public static final boolean SYSTEM_APP_ENABLED = BuildConfig.SYSTEM_APP_ENABLED;
	public static final boolean AUTO_ENROLLMENT_BACKGROUND_SERVICE_ENABLED =
			BuildConfig.AUTO_ENROLLMENT_BACKGROUND_SERVICE_ENABLED;
	public static final boolean ALLOW_SYSTEM_APPS_IN_APPS_LIST_RESPONSE =
			BuildConfig.ALLOW_SYSTEM_APPS_IN_APPS_LIST_RESPONSE;

	// Set DEFAULT_OWNERSHIP to null if no overriding is needed. Other possible values are,
	// OWNERSHIP_BYOD or OWNERSHIP_COPE. If you are using the mutual SSL authentication
	// This value must be set to a value other than null.
	public static final String DEFAULT_OWNERSHIP = BuildConfig.DEFAULT_OWNERSHIP;
	// This is set to override the server host name retrieving screen. If overriding is not
	// needed, set this to null.
	public static final String DEFAULT_HOST = BuildConfig.DEFAULT_HOST;
	public static final String APP_MANAGER_HOST = BuildConfig.APP_MANAGER_HOST;
	public static final String CLOUD_MANAGER = BuildConfig.CLOUD_MANAGER;
	public static final String SIGN_UP_URL = BuildConfig.SIGN_UP_URL;
	public static final String SYSTEM_SERVICE_PACKAGE = BuildConfig.SYSTEM_SERVICE_PACKAGE;
	public static final String AGENT_PACKAGE = BuildConfig.APPLICATION_ID;
	public static final String CATALOG_APP_PACKAGE_NAME = BuildConfig.CATALOG_APP_PACKAGE_NAME;
	public static final int FIRMWARE_UPGRADE_RETRY_COUNT = BuildConfig.FIRMWARE_UPGRADE_RETRY_COUNT;
	public static final float SERVER_API_VERSION = BuildConfig.SERVER_API_VERSION;
	public static final String DATE_FORMAT = "MM-dd-yyyy hh:mm a";
	public static final String SYSTEM_APP_SERVICE_START_ACTION = "org.wso2.iot.system.service.START_SERVICE";
	public static final String SYSTEM_APP_BROADCAST_ACTION = "org.wso2.iot.system.service.MESSAGE_PROCESSED";
	public static final String LOCATION_UPDATE_BROADCAST_ACTION = "org.ws2.iot.agent.LOCATION_UPDATE";
	public static final String SYNC_BROADCAST_ACTION = "org.ws2.iot.agent.SERVER_SYNC";
	public static final String AGENT_UPDATED_BROADCAST_ACTION = "org.ws2.iot.agent.APPLICATION_UPDATED";
	public static final String OWNERSHIP_BYOD = "BYOD";
	public static final String PENDING_APP_INSTALLATIONS = "PENDING_APP_INSTALLATIONS";
	public static final String PENDING_APP_UNINSTALLATIONS = "PENDING_APP_UNINSTALLATIONS";
	public static final String NOTIFIER_LOCAL = "LOCAL";
	public static final String NOTIFIER_FCM = "FCM";
	public static final String FCM_REG_ID = "fcm_reg_id";
	public static final String SERVER_PROTOCOL = BuildConfig.SERVER_PROTOCOL;
	public static final String API_SERVER_PORT = BuildConfig.API_SERVER_PORT;
	public static final int APP_DOWNLOAD_TIMEOUT = 30 * 60 * 1000;
	public static final int APP_INSTALL_TIMEOUT = 10 * 60 * 1000;
	public static final int APP_UNINSTALL_TIMEOUT = 10 * 60 * 1000;
	//Should be grater than FIRMWARE_DOWNLOAD_TIMEOUT in the system service.
	public static final int FIRMWARE_DOWNLOAD_OPERATION_TIMEOUT = 6 * 60 * 1000;
	//Is user consent required for file upload from device to server
	public static final boolean REQUIRE_CONSENT_FOR_FILE_UPLOAD = BuildConfig.REQUIRE_CONSENT_FOR_FILE_UPLOAD;
	public static final int MAX_TOKEN_FAILURE_ATTEMPTS = 10;

	// This is used to skip the license
	public static final boolean SKIP_LICENSE = BuildConfig.SKIP_LICENSE;
	public static final boolean HIDE_LOGIN_UI = BuildConfig.HIDE_LOGIN_UI;
	public static final boolean HIDE_UNREGISTER_BUTTON = BuildConfig.HIDE_UNREGISTER_BUTTON;
	public static final boolean SKIP_WORK_PROFILE_CREATION = BuildConfig.SKIP_WORK_PROFILE_CREATION;
	public static final boolean HIDE_ERROR_DIALOG = BuildConfig.HIDE_ERROR_DIALOG;
	public static final boolean COSU_SECRET_EXIT = BuildConfig.COSU_SECRET_EXIT;
	public static final String KIOSK_APP_PACKAGE_NAME ="kioskAppPackageName";
	public static final String SERVER_APP_ENDPOINT = AUTHENTICATOR_IN_USE.equals(MUTUAL_SSL_AUTHENTICATOR) ?
			"/api/device-mgt/android/mssl/v" + SERVER_API_VERSION + "/"
			: "/api/device-mgt/android/v" + SERVER_API_VERSION + "/";
	public static final String LICENSE_ENDPOINT = SERVER_APP_ENDPOINT + "configuration/license";
	public static final String REGISTER_ENDPOINT = SERVER_APP_ENDPOINT + "devices/";
	public static final String CONFIGURATION_ENDPOINT = SERVER_APP_ENDPOINT + "configuration/";
	public static final String SCEP_ENDPOINT = "/api/scep-mgt/v" + SERVER_API_VERSION +
												"/certificates/signcsr";
	public static final String OAUTH_ENDPOINT = "/token";
	public static final String DEVICE_ENDPOINT = SERVER_APP_ENDPOINT + "devices/";
	public static final String IS_REGISTERED_ENDPOINT = "/status";
	public static final String UNREGISTER_ENDPOINT =  REGISTER_ENDPOINT;
	public static final String DEVICES_ENDPOINT = SERVER_APP_ENDPOINT + "devices/";
	public static final String NOTIFICATION_ENDPOINT  = "/pending-operations";
	public static final String GOOGLE_PLAY_APP_URI = "market://details?id=";
	public final static String API_APPLICATION_CONTEXT =
			"/api-application-registration";
	public final static String API_APPLICATION_REGISTRATION_CONTEXT = API_APPLICATION_CONTEXT +
			"/register";
	public final static String API_APPLICATION_UNREGISTRATION_CONTEXT = API_APPLICATION_CONTEXT +
			"/unregister";
	public final static String API_APPLICATION_NAME_PREFIX = "cdmf_android_";
	public static final String APP_LIST_ENDPOINT = "/api/appm/publisher/v1.1/apps/mobileapp?field-filter=all";
	public static final String APP_DOWNLOAD_ENDPOINT = "/store/api/mobileapp/getfile";
	public static final String ACTION_RESPONSE = "org.wso2.iot.agent.MESSAGE_PROCESSED";
	public static final String EVENT_ENDPOINT = SERVER_APP_ENDPOINT + "events/publish";
	public static final String EULA_TITLE = BuildConfig.EULA_TITLE;
	public static final String STATUS_KEY = "status";
	public static final String USERNAME = "username";
	public static final String STATUS = "status";
	public static final String RESPONSE = "response";
	public static final String OPERATION_CODE = "operation";
	public static final String DEVICE_TYPE = "deviceType";
	public static final String CLIENT_ID = "client_id";
	public static final String CLIENT_SECRET = "client_secret";
	public static final String CLIENT_NAME = "client_name";
	public static final String USER_AGENT = "Mozilla/5.0 ( compatible ), Android";
	public static final String ADMIN_MESSAGE = "message";
	public static final String IS_LOCKED = "lock";
	public static final String LOCK_MESSAGE = "lockMessage";
	public static final String OPERATION_ID = "operationId";
	public static final String IS_HARD_LOCK_ENABLED = "isHardLockEnabled";
	public static final String USERNAME_PATTERN = "[user]";
	public static final String EMM_DB = "emm_db";
	public static final String TOKEN_EXPIRED = "token_expired";
	public static final String TOKEN_FAILURE_ATTEMPTS = "token_failures";
	public static final String PERMISSION_MISSING = "permission_missing";
	public static final String LOCATION_DISABLED = "location_disabled";
	public static final int SIGN_IN_NOTIFICATION_ID = 0;
	public static final int PERMISSION_MISSING_NOTIFICATION_ID = 1;
	public static final int LOCATION_DISABLED_NOTIFICATION_ID = 2;
	public static final String NOTIFIRE_FREQUENCY_VALUE_KEY = "value";
	/**
	 * Device certificates.
	 */
	public static final boolean ENABLE_DEVICE_CERTIFICATE_GENERATION = true;
	public static final String DEVICE_CERTIFCATE_NAME = "device-cert.p12";
	public static final String DEVICE_CERTIFCATE_ALIAS = "wso2carbon";
	public static final String DEVICE_CERTIFCATE_PASSWORD = "wso2carbon";
	public static final String DEVICE_KEY_TYPE = "RSA";
	public static final String DEVICE_KEY_ALGO = "SHA256withRSA";
	public static final String DEVICE_CSR_INFO = "CN=WSO2 Device";
	public static final String APP_LOCK_SERVICE = "AppLockService";
	/**
	 * Request codes.
	 */
	public static final int REGISTER_REQUEST_CODE = 300;
	public static final int IS_REGISTERED_REQUEST_CODE = 301;
	public static final int DYNAMIC_CLIENT_REGISTER_REQUEST_CODE = 302;
	public static final int SENDER_ID_REQUEST_CODE = 303;
	public static final int LICENSE_REQUEST_CODE = 304;
	public static final int UNREGISTER_REQUEST_CODE = 305;
	public static final int NOTIFICATION_REQUEST_CODE = 306;
	public static final int DEVICE_INFO_REQUEST_CODE = 307;
	public static final int FCM_REGISTRATION_ID_SEND_CODE = 308;
	public static final int POLICY_REQUEST_CODE = 309;
	public static final int CONFIGURATION_REQUEST_CODE = 310;
	public static final int AUTHENTICATION_REQUEST_CODE = 311;
	public static final int EVENT_REQUEST_CODE = 312;
	public static final int APP_LIST_REQUEST_CODE = 313;
	public static final int DYNAMIC_CLIENT_UNREGISTER_REQUEST_CODE = 314;
	public static final int SCEP_REQUEST_CODE = 300;
	public static final int DO_NOT_DISTURB_REQUEST_CODE = 315;
	/**
	 * Tag used on log messages.
	 */
	public static final String TAG = "WSO2IoTAgent";
	public static final String MIME_TYPE = "text/html";
	public static final String ENCODING_METHOD = "utf-8";
	public static final int DEFAULT_REPEAT_COUNT = 0;
	public static final int NOTIFIER_CHECK = 2;
	public static final int DEFAULT_REQUEST_CODE = 0;
	public static final boolean ASK_TO_ENABLE_LOCATION = BuildConfig.ASK_TO_ENABLE_LOCATION;
	public static final boolean LOCATION_PUBLISHING_ENABLED = BuildConfig.LOCATION_PUBLISHING_ENABLED;
	public static final boolean WIFI_SCANNING_ENABLED = BuildConfig.WIFI_SCANNING_ENABLED;
	public static final int DEFAULT_INTERVAL = BuildConfig.DEFAULT_INTERVAL;
	public static final int DEFAULT_FCM_INTERVAL = BuildConfig.DEFAULT_FCM_INTERVAL;
	public static final int DEFAULT_START_INTERVAL = BuildConfig.DEFAULT_START_TIME;
	public static final boolean FCM_FALLBACK_PULL_ENABLED = BuildConfig.FCM_FALLBACK_PULL_ENABLED;

	/**
	 * Log publishers
	 */
	public final class LogPublisher {
		public static final String DAS_PUBLISHER = "DAS_PULISHER";
		public static final String SPLUNK_PUBLISHER = "SPLUNK_PUBLISHER";
		public static final String LOG_PUBLISHER_IN_USE = BuildConfig.LOG_PUBLISHER_IN_USE;
		public static final String LOG_LEVEL = BuildConfig.LOG_LEVEL;
		public static final int NUMBER_OF_LOG_LINES = BuildConfig.NUMBER_OF_LOG_LINES;

		private LogPublisher() {
			throw new AssertionError();
		}
	}

	/**
	 * Splunk configurations
	 */
	public final class SplunkConfigs {
		public static final String API_KEY = BuildConfig.SPLUNK_API_KEY;
		public static final String TYPE_HTTP = "HTTP";
		public static final String TYPE_MINT = "MINT";
		public static final String DATA_COLLECTOR_TYPE = BuildConfig.SPLUNK_DATA_COLLECTOR_TYPE;
		public static final String HEC_TOKEN = BuildConfig.HEC_TOKEN;
		public static final String HEC_MINT_ENDPOINT_URL = BuildConfig.HEC_MINT_ENDPOINT_URL;

		private SplunkConfigs() {
			throw new AssertionError();
		}
	}

	/**
	 * Status codes
	 */
	public final class Code {
		public static final String SUCCESS = "200";
		public static final String PENDING = "300";
		public static final String FAILURE = "400";

		private Code() {
			throw new AssertionError();
		}
	}

	/**
	 * Sub Status codes
	 */
	public final class Status {
		public static final String SUCCESSFUL = "200";
		public static final String CREATED = "201";
		public static final String ACCEPT = "202";
		public static final String AUTHENTICATION_FAILED = "400";
		public static final String UNAUTHORIZED = "401";
		public static final String INTERNAL_SERVER_ERROR = "500";

		private Status() {
			throw new AssertionError();
		}
	}

	/**
	 * Operation IDs
	 */
	public final class Operation {
		public static final String FILE_DOWNLOAD = "FILE_UPLOAD_TO_THE_DEVICE";
		public static final String FILE_UPLOAD = "FILE_DOWNLOAD_FROM_THE_DEVICE";
		public static final String DEVICE_LOCK = "DEVICE_LOCK";
		public static final String DEVICE_UNLOCK = "DEVICE_UNLOCK";
		public static final String DEVICE_LOCATION = "DEVICE_LOCATION";
		public static final String WIFI = "WIFI";
		public static final String CAMERA = "CAMERA";
		public static final String EMAIL = "EMAIL";
		public static final String DEVICE_MUTE = "DEVICE_MUTE";
		public static final String PASSWORD_POLICY = "PASSCODE_POLICY";
		public static final String DEVICE_INFO = "DEVICE_INFO";
		public static final String ENTERPRISE_WIPE = "ENTERPRISE_WIPE";
		public static final String CLEAR_PASSWORD = "CLEAR_PASSWORD";
		public static final String WIPE_DATA = "WIPE_DATA";
		public static final String APPLICATION_LIST = "APPLICATION_LIST";
		public static final String CHANGE_LOCK_CODE = "CHANGE_LOCK_CODE";
		public static final String INSTALL_APPLICATION = "INSTALL_APPLICATION";
		public static final String UNINSTALL_APPLICATION = "UNINSTALL_APPLICATION";
		public static final String UPDATE_APPLICATION = "UPDATE_APPLICATION";
		public static final String BLACKLIST_APPLICATIONS = "BLACKLIST_APPLICATIONS";
		public static final String ENCRYPT_STORAGE = "ENCRYPT_STORAGE";
		public static final String DEVICE_RING = "DEVICE_RING";
		public static final String PASSCODE_POLICY = "PASSCODE_POLICY";
		public static final String NOTIFICATION = "NOTIFICATION";
		public static final String INSTALL_APPLICATION_BUNDLE = "INSTALL_APPLICATION_BUNDLE";
		public static final String WEBCLIP = "WEBCLIP";
		public static final String INSTALL_GOOGLE_APP = "INSTALL_GOOGLE_APP";
		public static final String POLICY_BUNDLE = "POLICY_BUNDLE";
		public static final String POLICY_MONITOR = "MONITOR";
		public static final String POLICY_REVOKE = "POLICY_REVOKE";
		public static final String DISENROLL = "DISENROLL";
		public static final String UPGRADE_FIRMWARE = "UPGRADE_FIRMWARE";
		public static final String REBOOT = "REBOOT";
		public static final String VPN = "VPN";
		public static final String EXECUTE_SHELL_COMMAND = "SHELL_COMMAND";
		public static final String DISALLOW_ADJUST_VOLUME = "DISALLOW_ADJUST_VOLUME";
		public static final String DISALLOW_CONFIG_BLUETOOTH = "DISALLOW_CONFIG_BLUETOOTH";
		public static final String DISALLOW_CONFIG_CELL_BROADCASTS =
				"DISALLOW_CONFIG_CELL_BROADCASTS";
		public static final String DISALLOW_CONFIG_CREDENTIALS = "DISALLOW_CONFIG_CREDENTIALS";
		public static final String DISALLOW_CONFIG_MOBILE_NETWORKS =
				"DISALLOW_CONFIG_MOBILE_NETWORKS";
		public static final String DISALLOW_CONFIG_TETHERING = "DISALLOW_CONFIG_TETHERING";
		public static final String DISALLOW_CONFIG_VPN = "DISALLOW_CONFIG_VPN";
		public static final String DISALLOW_CONFIG_WIFI = "DISALLOW_CONFIG_WIFI";
		public static final String DISALLOW_APPS_CONTROL = "DISALLOW_APPS_CONTROL";
		public static final String DISALLOW_CREATE_WINDOWS = "DISALLOW_CREATE_WINDOWS";
		public static final String DISALLOW_CROSS_PROFILE_COPY_PASTE
				= "DISALLOW_CROSS_PROFILE_COPY_PASTE";
		public static final String DISALLOW_DEBUGGING_FEATURES = "DISALLOW_DEBUGGING_FEATURES";
		public static final String DISALLOW_FACTORY_RESET = "DISALLOW_FACTORY_RESET";
		public static final String DISALLOW_ADD_USER = "DISALLOW_ADD_USER";
		public static final String DISALLOW_INSTALL_APPS = "DISALLOW_INSTALL_APPS";
		public static final String DISALLOW_INSTALL_UNKNOWN_SOURCES
				= "DISALLOW_INSTALL_UNKNOWN_SOURCES";
		public static final String DISALLOW_MODIFY_ACCOUNTS = "DISALLOW_MODIFY_ACCOUNTS";
		public static final String DISALLOW_MOUNT_PHYSICAL_MEDIA = "DISALLOW_MOUNT_PHYSICAL_MEDIA";
		public static final String DISALLOW_NETWORK_RESET = "DISALLOW_NETWORK_RESET";
		public static final String DISALLOW_OUTGOING_BEAM = "DISALLOW_OUTGOING_BEAM";
		public static final String DISALLOW_OUTGOING_CALLS = "DISALLOW_OUTGOING_CALLS";
		public static final String DISALLOW_REMOVE_USER = "DISALLOW_REMOVE_USER";
		public static final String DISALLOW_SAFE_BOOT = "DISALLOW_SAFE_BOOT";
		public static final String DISALLOW_SHARE_LOCATION = "DISALLOW_SHARE_LOCATION";
		public static final String DISALLOW_SMS = "DISALLOW_SMS";
		public static final String DISALLOW_UNINSTALL_APPS = "DISALLOW_UNINSTALL_APPS";
		public static final String DISALLOW_UNMUTE_MICROPHONE = "DISALLOW_UNMUTE_MICROPHONE";
		public static final String DISALLOW_USB_FILE_TRANSFER = "DISALLOW_USB_FILE_TRANSFER";
		public static final String ALLOW_PARENT_PROFILE_APP_LINKING
				= "ALLOW_PARENT_PROFILE_APP_LINKING";
		public static final String ENSURE_VERIFY_APPS = "ENSURE_VERIFY_APPS";
		public static final String AUTO_TIME = "AUTO_TIME";
		public static final String ENABLE_ADMIN = "ENABLE_ADMIN";
		public static final String SET_SCREEN_CAPTURE_DISABLED = "SET_SCREEN_CAPTURE_DISABLED";
		public static final String SET_STATUS_BAR_DISABLED = "SET_STATUS_BAR_DISABLED";
		public static final String SILENT_INSTALL_APPLICATION = "SILENT_INSTALL_APPLICATION";
		public static final String SILENT_UNINSTALL_APPLICATION = "SILENT_UNINSTALL_APPLICATION";
		public static final String APP_RESTRICTION = "APP-RESTRICTION";
		public static final String WORK_PROFILE= "WORK_PROFILE";
		public static final String GET_APPLICATION_LIST = "GET_APP_LIST";
		public static final String UNINSTALL_WEBCLIP = "UNINSTALL_WEBCLIP";
		public static final String GET_APP_DOWNLOAD_PROGRESS = "APP_DOWNLOAD_PROGRESS";
		public static final String GET_ENROLLMENT_STATUS = "ENROLLMENT_STATUS";
		public static final String GET_FIRMWARE_UPGRADE_PACKAGE_STATUS = "FIRMWARE_UPGRADE_PACKAGE_STATUS";
		public static final String GET_FIRMWARE_UPGRADE_DOWNLOAD_PROGRESS = "FIRMWARE_UPGRADE_DOWNLOAD_PROGRESS";
		public static final String FAILED_FIRMWARE_UPGRADE_NOTIFICATION = "FAILED_FIRMWARE_UPGRADE_NOTIFICATION";
		public static final String FIRMWARE_UPGRADE_COMPLETE = "FIRMWARE_UPGRADE_COMPLETE";
		public static final String FIRMWARE_IMAGE_DOWNLOADING = "FIRMWARE_IMAGE_DOWNLOADING";
		public static final String FIRMWARE_UPGRADE_FAILURE = "FIRMWARE_UPGRADE_FAILURE";
		public static final String FIRMWARE_INSTALLATION_CANCELED = "FIRMWARE_INSTALLATION_CANCELED";
		public static final String GET_FIRMWARE_BUILD_DATE = "FIRMWARE_BUILD_DATE";
		public static final String LOGCAT = "LOGCAT";
		public static final String FIRMWARE_UPGRADE_AUTOMATIC_RETRY = "FIRMWARE_UPGRADE_AUTOMATIC_RETRY";
		public static final String SYSTEM_UPDATE_POLICY = "SYSTEM_UPDATE_POLICY";
		public static final String RUNTIME_PERMISSION_POLICY = "RUNTIME_PERMISSION_POLICY";
		public static final String COSU_PROFILE_POLICY = "COSU_PROFILE";
		public static final String ENABLE_LOCK ="ENABLE_LOCK";
        public static final String TRIGGER_HEARTBEAT = "TRIGGER_HEARTBEAT";
		public static final String NOTIFIER_FREQUENCY = "NOTIFIER_FREQUENCY";

		private Operation() {
			throw new AssertionError();
		}
	}

	/**
	 * File transfer uploadFile/downloadFile specific constants.
	 */
	public final class FileTransfer {
		public static final String FILE_URL = "fileURL";
		public static final String USER_NAME = "userName";
		public static final String PASSWORD = "ftpPassword";
		public static final String FILE_LOCATION = "fileLocation";
		public static final String FTP = "ftp";
		public static final String SFTP = "sftp";
		public static final String HTTP = "http";
	}

	/**
	 *  Runtime Permission Policy specific constants
	 */
	public final class RuntimePermissionPolicy {
		public static final String PACKAGE_NAME = "packageName";
		public static final String PERMISSION_NAME = "permissionName";
		public static final String PERMISSION_TYPE = "permissionType";
		public static final String ALL_PERMISSIONS = "*";
		public static final String PERMITTED_APP_DATA = "permittedAppData";

	}

	/**
	 *  Device specific constants
	 */
	public final class Device {
		public static final String SERIAL = "SERIAL";
		public static final String IMEI = "IMEI";
		public static final String MAC = "MAC";
		public static final String IMSI = "IMSI";
		public static final String MODEL = "DEVICE_MODEL";
		public static final String VENDOR = "VENDOR";
		public static final String OS = "OS_VERSION";
		public static final String OS_BUILD_DATE = "OS_BUILD_DATE";
		public static final String NAME = "DEVICE_NAME";
		public static final String BATTERY_LEVEL = "BATTERY_LEVEL";
		public static final String ENCRYPTION_STATUS = "ENCRYPTION_ENABLED";
		public static final String PASSCODE_STATUS = "PASSCODE_ENABLED";
		public static final String MOBILE_DEVICE_LATITUDE = "LATITUDE";
		public static final String MOBILE_DEVICE_LONGITUDE = "LONGITUDE";
		public static final String MEMORY_INFO_INTERNAL_TOTAL = "INTERNAL_TOTAL_MEMORY";
		public static final String MEMORY_INFO_EXTERNAL_TOTAL = "EXTERNAL_TOTAL_MEMORY";
		public static final String MEMORY_INFO_INTERNAL_AVAILABLE = "INTERNAL_AVAILABLE_MEMORY";
		public static final String MEMORY_INFO_EXTERNAL_AVAILABLE = "EXTERNAL_AVAILABLE_MEMORY";
		public static final String NETWORK_OPERATOR = "OPERATOR";
		public static final String INFO = "DEVICE_INFO";
		public static final String FCM_TOKEN = "FCM_TOKEN";
		public static final String WIFI_SSID = "WIFI_SSID";
		public static final String WIFI_SIGNAL_STRENGTH = "WIFI_SIGNAL_STRENGTH";
		public static final String NETWORK_INFO = "NETWORK_INFO";
		public static final String WIFI_SCAN_RESULT = "WIFI_SCAN_RESULT";
		public static final String CONNECTION_TYPE = "CONNECTION_TYPE";
		public static final String MOBILE_CONNECTION_TYPE = "MOBILE_CONNECTION_TYPE";
		public static final String MOBILE_SIGNAL_STRENGTH = "MOBILE_SIGNAL_STRENGTH";
		public static final String CPU_INFO = "CPU_INFO";
		public static final String RAM_INFO = "RAM_INFO";
		public static final String TOTAL_MEMORY = "TOTAL_MEMORY";
		public static final String LOW_MEMORY = "LOW_MEMORY";
		public static final String THRESHOLD = "THRESHOLD";
		public static final String AVAILABLE_MEMORY = "AVAILABLE_MEMORY";
		public static final String BATTERY_INFO = "BATTERY_INFO";
		public static final String SCALE = "SCALE";
		public static final String BATTERY_VOLTAGE = "BATTERY_VOLTAGE";
		public static final String HEALTH = "HEALTH";
		public static final String STATUS = "STATUS";
		public static final String PLUGGED = "PLUGGED";
		public static final String USS = "USS";
		public static final String PHONE_NUMBER = "PHONE_NUMBER";

		private Device() {
			throw new AssertionError();
		}
	}

	// sqlite database related tables
	public final class NotificationTable {
		public static final String NAME = "notification";
		public static final String ID = "id";
		public static final String MESSAGE_TITLE = "messageTitle";
		public static final String MESSAGE_TEXT = "messageText";
		public static final String RECEIVED_TIME = "received_time";
		public static final String RESPONSE_TIME = "response_time";
		public static final String STATUS = "status";

		private NotificationTable() {
			throw new AssertionError();
		}
	}

	public final class Location {
		public static final String GEO_ENDPOINT = "http://nominatim.openstreetmap.org/reverse";
		public static final String RESULT_FORMAT = "format=json";
		public static final String LONGITUDE = "lon";
		public static final String LATITUDE = "lat";
		public static final String ACCEPT_LANGUAGE = "accept-language";
		public static final String LANGUAGE_CODE = "en-us";
		public static final String ADDRESS = "address";
		public static final String CITY = "city";
		public static final String TOWN = "town";
		public static final String COUNTRY = "country";
		public static final String ZIP = "postcode";
		public static final String STREET1 = "road";
		public static final String STREET2 = "suburb";
		public static final String STATE = "state";
		public static final String LOCATION = "location";

		private Location() {
			throw new AssertionError();
		}
	}

	public final class LocationInfo {
		public static final String CITY = "city";
		public static final String COUNTRY = "country";
		public static final String ZIP = "zip";
		public static final String STREET1 = "street1";
		public static final String STREET2 = "street2";
		public static final String STATE = "state";
		public static final String LONGITUDE = "longitude";
		public static final String LATITUDE = "latitude";
		public static final String TIME_STAMP = "timeStamp";

		private LocationInfo() {
			throw new AssertionError();
		}
	}

	public final class EventListeners {
		public static final boolean EVENT_LISTENING_ENABLED = BuildConfig.EVENT_LISTENING_ENABLED;
		public static final boolean APPLICATION_STATE_LISTENER =
				BuildConfig.APPLICATION_STATE_LISTENER;
		public static final String APPLICATION_STATE = "APPLICATION_STATE";
		public static final boolean RUNTIME_STATE_LISTENER = BuildConfig.RUNTIME_STATE_LISTENER;
		public static final String RUNTIME_STATE = "RUNTIME_STATE";
		public static final long DEFAULT_START_TIME = BuildConfig.DEFAULT_START_TIME;
		public static final long DEFAULT_INTERVAL = BuildConfig.DEFAULT_INTERVAL;
		public static final int DEFAULT_LISTENER_CODE = BuildConfig.DEFAULT_LISTENER_CODE;
		public static final String REQUEST_CODE = "requestCode";
		public static final String LOCATION_EVENT_TYPE = "location";

		private EventListeners() {
			throw new AssertionError();
		}
	}

	public final class WorkProfile {
		public static final String MESSAGE_FOR_UNINSTALLING_AGENT =
				"When the work-profile is created, you can uninstall Agent in Personal Profile.";
		public static final String MESSAGE_DEVICE_PROVISIONING_NOT_ENABLED =
				"Device provisioning is not enabled. Stopping.";
		public static final String PROVISIONING_DONE = "Provisioning Done.";
		public static final String PROVISIONING_FAILED = "Provisioning Failed.";
	}

	public final class PreferenceFlag {
		public static final String REG_ID = "regId";
		public static final String REGISTERED = "registered";
		public static final String IP = "ip";
		public static final String DEVICE_ACTIVE = "deviceActive";
		public static final String PORT = "serverPort";
		public static final String PROTOCOL = "serverProtocol";
		public static final String APPLIED_POLICY = "appliedPolicy";
		public static final String IS_AGREED = "isAgreed";
		public static final String NOTIFIER_TYPE = "notifierType";
		public static final String FIRMWARE_UPGRADE_INITIATED_AT = "firmwareUpgradeInitiatedAt";
		public static final String DOWNLOAD_INITIATED_AT = "downloadInitiatedAt";
		public static final String INSTALLATION_INITIATED_AT = "installationInitiatedAt";
		public static final String UNINSTALLATION_INITIATED_AT = "uninstallationInitiatedAt";
		public static final String APP_INSTALLATION_LAST_STATUS = "appInstallationLastStatus";
		public static final String CURRENT_INSTALLING_APP = "installingApplication";
		public static final String LOCAL_NOTIFIER_INVOKED_PREF_KEY = "localNotificationInvoked";
		public static final String DEVICE_ID_PREFERENCE_KEY = "deviceId";
		public static final String LAST_SERVER_CALL = "lastServerCall";
		public static final String DISALLOW_UNKNOWN_SOURCES = "DISALLOW_UNKNOWN_SOURCES";

		private PreferenceFlag() {
			throw new AssertionError();
		}
	}

	public final class AppState {
		private AppState() {
			throw new AssertionError();
		}

		public static final String DOWNLOAD_STARTED = "DOWNLOAD_STARTED";
		public static final String DOWNLOAD_RETRY = "DOWNLOAD_RETRY";
		public static final String DOWNLOAD_COMPLETED = "DOWNLOAD_COMPLETED";
		public static final String DOWNLOAD_FAILED = "DOWNLOAD_FAILED";
		public static final String INSTALL_FAILED = "INSTALL_FAILED";
		public static final String INSTALLED = "INSTALLED";
		public static final String UNINSTALLED = "UNINSTALLED";
		public static final String UNINSTALL_FAILED = "UNINSTALL_FAILED";
	}

	public final class PreferenceCOSUProfile {
		public static final String FREEZE_TIME = "lockDownStartTime";
		public static final String RELEASE_TIME = "lockDownEndTime";
		public static final String ENABLE_LOCKDOWN = "false";

		private PreferenceCOSUProfile() {
			throw new AssertionError();
		}
	}

	public final class AppRestriction {
		public static final String RESTRICTION_TYPE = "restriction-type";
		public static final String RESTRICTED_APPLICATIONS = "restricted-applications";
		public static final String WHITE_LIST = "white-list";
		public static final String BLACK_LIST = "black-list";
		public static final String PACKAGE_NAME = "packageName";
		public static final String APP_LIST = "appList";
		public static final String WHITE_LIST_APPS = "whiteListApps";
		public static final String BLACK_LIST_APPS = "blackListApps";
		public static final String DISALLOWED_APPS = "disallowedApps";
		private AppRestriction() {
			throw new AssertionError();
		}
	}

	public final class AppRuntimePermission {
		public static final String PERMISSION_TYPE = "permission-type";
		public static final String PERMITTED_APPS = "permitted-applications";
		public static final String PACKAGE_NAME = "packageName";

		private AppRuntimePermission() {
			throw new AssertionError();
		}
	}

	public final class COSUProfilePolicy {
		public static final String deviceReleaseTime = "cosuProfileRestrictionStartTime";
		public static final String deviceFreezeTime = "cosuProfileRestrictionEndTime";
		private COSUProfilePolicy() {
			throw new AssertionError();
		}
	}

    public static final int APP_MONITOR_FREQUENCY = 60000;

	// Passcode Policy Keys
	public static final String POLICY_PASSWORD_MAX_FAILED_ATTEMPTS = "maxFailedAttempts";
	public static final String POLICY_PASSWORD_MIN_LENGTH = "minLength";
	public static final String POLICY_PASSWORD_PIN_HISTORY = "pinHistory";
	public static final String POLICY_PASSWORD_MIN_COMPLEX_CHARS = "minComplexChars";
	public static final String POLICY_PASSWORD_REQUIRE_ALPHANUMERIC = "requireAlphanumeric";
	public static final String POLICY_PASSWORD_ALLOW_SIMPLE = "allowSimple";
	public static final String POLICY_PASSWORD_PIN_AGE_IN_DAYS = "maxPINAgeInDays";

	// Intent Extras
	public static final String INTENT_EXTRA_TYPE = "type";
	public static final String INTENT_EXTRA_PASSWORD_SETTING = "lock_settings";
	public static final String INTENT_EXTRA_MESSAGE_TEXT = "messageText";

	//Operation Values
	public static final String OPERATION_VALUE_COMPLETED = "COMPLETED";
	public static final String OPERATION_VALUE_ERROR = "ERROR";

	public static final String NOTIFICATION_CHANNEL_ID = AGENT_PACKAGE + ".notification.channel.id";

}
