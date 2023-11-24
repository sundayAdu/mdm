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

package org.wso2.iot.agent.proxy.utils;

import org.wso2.iot.agent.proxy.BuildConfig;

/**
 * This class holds all the constants used throughout the application.
 */
public class Constants {


	/**
	 * Authenticators
	 */
	public final class Authenticator {
		private Authenticator(){
			throw new AssertionError();
		}
		public static final String OAUTH_AUTHENTICATOR = "OAUTH_AUTHENTICATOR";
		public static final String MUTUAL_SSL_AUTHENTICATOR = "MUTUAL_SSL_AUTHENTICATOR";
		public static final String MUTUAL_AUTH_HEADER = "mutual-auth-header";
		public static final String MUTUAL_AUTH_HEADER_VALUE = "mutual-auth-enabled";
		public static final String AUTHENTICATOR_IN_USE = OAUTH_AUTHENTICATOR;
	}

	/**
	 * HTTP clients
	 */
	public final class HttpClient {
		private HttpClient(){
			throw new AssertionError();
		}
		public static final String OAUTH_HTTP_CLIENT = "OAUTH_HTTP_CLIENT";
		public static final String MUTUAL_HTTP_CLIENT = "MUTUAL_HTTP_CLIENT";
		public static final String HTTP_CLIENT_IN_USE = OAUTH_HTTP_CLIENT;
		public static final int DEFAULT_TIME_OUT = 60000;
		public static final int DEFAULT_RETRY_COUNT = 3;
		public static final int DEFAULT_TOKEN_TIME_OUT = 120000;
		public static final int DEFAULT_TOKEN_RETRY_COUNT = 0;
		//Taken will renew after tokenValidityTime * TOKEN_VALIDITY_PERCENTAGE%
		public static final int TOKEN_VALIDITY_PERCENTAGE = 90;
	}

	public static final String SERVER_PROTOCOL = BuildConfig.SERVER_PROTOCOL;
	public static final String TRUSTSTORE_PASSWORD = BuildConfig.TRUSTSTORE_PASSWORD;
	public static final String KEYSTORE_PASSWORD = BuildConfig.KEYSTORE_PASSWORD;
	public static final String TRUSTSTORE_LOCATION = BuildConfig.TRUSTSTORE_LOCATION;
	public static final String KEYSTORE_LOCATION = BuildConfig.KEYSTORE_LOCATION;
	public static final boolean DEBUG_ENABLED = BuildConfig.DEBUG_MODE_ENABLED;
	public final static int ACCESS_TOKEN_AGE = 3000;

	public final static String GRANT_TYPE = "grant_type";
	public final static String GRANT_TYPE_PASSWORD = "password";
	public final static String GRANT_TYPE_ACCESS_TOKEN = "access_token";
	public final static String AUTHORIZATION_MODE = "Basic ";
	public final static String AUTHORIZATION_HEADER = "Authorization";
	public final static String CONTENT_TYPE_HEADER = "Content-Type";
	public final static String DEFAULT_CONTENT_TYPE = "application/x-www-form-urlencoded";
	public final static String REDIRECT_URL = "redirect_uri";
	public final static String SCOPE = "scope";
//	public final static String SCOPES = "default";
	public final static String SCOPES = "default appm:read" +
			" perm:android:enroll perm:android:disenroll" +
			" perm:android:view-configuration perm:android:manage-configuration";

	public final static String OPENID = "openid";
	public static final String EXPIRE_TIME = "expires_on";
	public final static String SERVER_RESPONSE_BODY = "response";
	public final static String SERVER_RESPONSE_STATUS = "status";
	public static final String SUCCESS_RESPONSE = "success";
	public static final String FAILURE_RESPONSE = "fail";
	public final static String REFRESH_TOKEN = "refresh_token";
	public final static String ACCESS_TOKEN = "access_token";
	public final static String LAST_TOKEN_RENEWAL = "last_token_renewal";
	public final static String EXPIRE_LABEL = "expires_in";
	public static final String ERROR_LABEL = "error";
	public static final String ERROR_DESCRIPTION_LABEL = "error_description";
	public final static String APPLICATION_PACKAGE = BuildConfig.APPLICATION_PACKAGE;

	public final static String ID_TOKEN = "id_token";
	public final static String CLIENT_ID = "client_id";
	public final static String CLIENT_SECRET = "client_secret";
	public final static String TOKEN_ENDPOINT = "token_endpoint";
	public static enum HTTP_METHODS{GET, POST, DELETE, PUT};
	public static final String CLIENT_ERROR = "-1";
	public static final String INTERNAL_SERVER_ERROR = "500";
	public static final String ACCESS_FAILURE = "400";
	public static final String REQUEST_SUCCESSFUL = "200";
	public static final int HTTP = BuildConfig.API_HTTP_SERVER_PORT;
	public static final int HTTPS = BuildConfig.API_HTTPS_SERVER_PORT;
	public static final String BKS = "BKS";
	public static final String REFRESH_TOKEN_EXPIRED = "Refresh token is expired.";

	public static final int ADD_HEADER_CALLBACK = 5001;
}
