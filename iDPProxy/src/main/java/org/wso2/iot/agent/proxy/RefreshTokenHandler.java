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
package org.wso2.iot.agent.proxy;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.iot.agent.proxy.beans.Token;
import org.wso2.iot.agent.proxy.utils.Constants;
import org.wso2.iot.agent.proxy.utils.ServerUtilities;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * This class handles the entire functionality of OAuth token expiration and
 * refresh process.
 */

public class RefreshTokenHandler {
	private static final String TAG = "RefreshTokenHandler";
	private static final String SCOPE_LABEL = "scope";
	private static final String COLON = ":";
	private Token token;

	public RefreshTokenHandler(Token token) {
		this.token = token;
	}

	public void obtainNewAccessToken() {
		if(Constants.DEBUG_ENABLED) {
			Log.d(TAG, "Renewing tokens.");
		}
		RequestQueue queue =  null;
		try {
			queue = ServerUtilities.getCertifiedHttpClient();
		} catch (IDPTokenManagerException e) {
			Log.e(TAG, "Failed to retrieve HTTP client", e);
			return;
		}

		StringRequest request = new StringRequest(Request.Method.POST,
				IdentityProxy.getInstance().getAccessTokenURL(),
				new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						if (Constants.DEBUG_ENABLED) {
							Log.d(TAG, "Token renewal response: " + response);
						}
					}
				},
				new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						Log.e(TAG, "Error in token renewal. " + error.toString());
						Log.w(TAG, "Refresh token used: " + token.getRefreshToken());
						if (error.networkResponse != null) {
							Log.w(TAG, error.toString() + " Status code: " + error.networkResponse.statusCode);
							processTokenResponse(String.valueOf(error.networkResponse.statusCode),
									new String(error.networkResponse.data));
						}
					}
				})
		{
			@Override
			protected Response<String> parseNetworkResponse(NetworkResponse response) {
				processTokenResponse(String.valueOf(response.statusCode), new String(response.data));
				return super.parseNetworkResponse(response);
			}

			@Override
			protected Map<String, String> getParams() throws AuthFailureError {
				Map<String, String> requestParams = new HashMap<>();
				requestParams.put(Constants.GRANT_TYPE, Constants.REFRESH_TOKEN);
				requestParams.put(Constants.REFRESH_TOKEN, token.getRefreshToken());
				if(token.getRefreshToken() == null) {
					Log.w(TAG, "Refresh token is null.");
				}
				return requestParams;
			}

			@Override
			public Map<String, String> getHeaders() throws AuthFailureError {
				byte[] credentials = Base64.encodeBase64((IdentityProxy.clientID + COLON +
				                                          IdentityProxy.clientSecret).getBytes());
				String encodedCredentials = new String(credentials);
				Map<String, String> headers = new HashMap<>();

				String authorizationString = Constants.AUTHORIZATION_MODE + encodedCredentials;
				headers.put(Constants.AUTHORIZATION_HEADER, authorizationString);
				headers.put(Constants.CONTENT_TYPE_HEADER, Constants.DEFAULT_CONTENT_TYPE);
				return headers;
			}
		};
		request.setRetryPolicy(new DefaultRetryPolicy(
				Constants.HttpClient.DEFAULT_TOKEN_TIME_OUT,
				Constants.HttpClient.DEFAULT_TOKEN_RETRY_COUNT,
				DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
		queue.add(request);
	}

	/**
	 * Processing token response from the server.
	 *
	 * @param responseCode - HTTP Response code.
	 * @param result       - Service result.
	 */
	@SuppressLint("SimpleDateFormat")
	private void processTokenResponse(String responseCode, String result) {
		String refreshToken;
		String accessToken;
		int timeToExpireSecond;
		IdentityProxy identityProxy = IdentityProxy.getInstance();
		try {
			if (Constants.REQUEST_SUCCESSFUL.equals(responseCode)) {
				JSONObject response = new JSONObject(result);
				accessToken = response.getString(Constants.ACCESS_TOKEN);
				refreshToken = response.getString(Constants.REFRESH_TOKEN);
				timeToExpireSecond = Integer.parseInt(response.getString(Constants.EXPIRE_LABEL));
				Token token = new Token();
				long expiresOn = new Date().getTime()
						+ (timeToExpireSecond - (100 - Constants.HttpClient.TOKEN_VALIDITY_PERCENTAGE) * timeToExpireSecond / 100) * 1000;
				token.setExpiresOn(new Date(expiresOn));
				token.setRefreshToken(refreshToken);
				token.setAccessToken(accessToken);
				token.setExpired(false);

				SharedPreferences mainPref = IdentityProxy.getInstance().getContext().
						getSharedPreferences(Constants.APPLICATION_PACKAGE, Context.MODE_PRIVATE);
				Editor editor = mainPref.edit();
				editor.putString(Constants.ACCESS_TOKEN, accessToken);
				editor.putString(Constants.REFRESH_TOKEN, refreshToken);
				editor.putLong(Constants.EXPIRE_TIME, expiresOn);
				editor.apply();

				if (Constants.DEBUG_ENABLED) {
					Log.d(TAG, "Token expires on:" + token.getExpiresOn().toString());
				}

				identityProxy
						.receiveNewAccessToken(responseCode, Constants.SUCCESS_RESPONSE, token);
			} else if (Constants.ACCESS_FAILURE.equals(responseCode)){
				identityProxy.receiveNewAccessToken(responseCode, Constants.REFRESH_TOKEN_EXPIRED, null);
			} else if (responseCode != null) {
				String errorDescription = Constants.ERROR_LABEL;
				if (result != null) {
					JSONObject responseBody = new JSONObject(result);
					errorDescription = responseBody.getString(Constants.ERROR_DESCRIPTION_LABEL);
				}
				identityProxy.receiveNewAccessToken(responseCode, errorDescription, null);
			} else {
				identityProxy.receiveNewAccessToken(Constants.CLIENT_ERROR, Constants.ERROR_LABEL, null);
			}
		} catch (JSONException e) {
			identityProxy.receiveNewAccessToken(responseCode, e.getMessage(), null);
			Log.e(TAG, "Invalid JSON. " + e);
		}
	}
}
