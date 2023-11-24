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

import org.wso2.iot.agent.proxy.beans.CredentialInfo;
import org.wso2.iot.agent.proxy.beans.Token;
import org.wso2.iot.agent.proxy.interfaces.APIAccessCallBack;
import org.wso2.iot.agent.proxy.interfaces.CallBack;
import org.wso2.iot.agent.proxy.interfaces.TokenCallBack;
import org.wso2.iot.agent.proxy.utils.Constants;
import org.wso2.iot.agent.proxy.utils.ServerUtilities;

import java.util.Date;

/**
 * This class handles identity proxy library initialization and token validation.
 */
public class IdentityProxy implements CallBack {

    public static String clientID;
    public static String clientSecret;
    private static String TAG = "IdentityProxy";
    private static Token token = null;
    private static IdentityProxy identityProxy = new IdentityProxy();
    private Context context;
    private static String accessTokenURL;
    private APIAccessCallBack apiAccessCallBack;
    private TokenCallBack tokenCallBack;
    private int requestCode = 0;
    private static volatile boolean IS_TOKEN_RENEWING = false;

    private IdentityProxy() {
    }

    public static synchronized IdentityProxy getInstance() {
        return identityProxy;
    }

    public int getRequestCode() {
        return requestCode;
    }

    public synchronized void setRequestCode(int requestCode) {
        this.requestCode = requestCode;
    }

    public String getAccessTokenURL() {
        return accessTokenURL;
    }

    public void setAccessTokenURL(String accessTokenURL) {
        IdentityProxy.accessTokenURL = accessTokenURL;
    }

    @Override
    public void receiveAccessToken(String status, String message, Token token) {
        IS_TOKEN_RENEWING = false;
        if (Constants.DEBUG_ENABLED && token != null) {
            Log.d(TAG, "Receive Access Token: " + token.getAccessToken());
        }
        IdentityProxy.token = token;
        apiAccessCallBack.onAPIAccessReceive(status);
    }

    @Override
    public void receiveNewAccessToken(String status, String message, Token token) {
        IS_TOKEN_RENEWING = false;
        if (token != null) {
            if (Constants.DEBUG_ENABLED) {
                Log.d(TAG, "Using Access Token: " + token.getAccessToken());
            }
            IdentityProxy.token = token;
        } else {
            Log.w(TAG, "Token is not renewed. Status: " + status);
        }
        tokenCallBack.onReceiveTokenResult(token, status, message);
    }

    /**
     * Initializing the IDP plugin and obrtaining the access token.
     *
     * @param info              - Includes token end point and Oauth app credentials.
     * @param apiAccessCallBack - Callback when API access happens.
     * @param context        - Application context.
     */
    public void init(CredentialInfo info, APIAccessCallBack apiAccessCallBack, Context context) {
        if (Constants.DEBUG_ENABLED) {
            Log.d(TAG, "init");
        }
        IdentityProxy.clientID = info.getClientID();
        IdentityProxy.clientSecret = info.getClientSecret();
        this.apiAccessCallBack = apiAccessCallBack;
        //To avoid having multiple contexts from different threads
        if (this.context == null) {
            this.context = context;
        }
        SharedPreferences mainPref = context.getSharedPreferences(Constants.APPLICATION_PACKAGE,
                Context.MODE_PRIVATE);
        Editor editor = mainPref.edit();
        editor.putString(Constants.CLIENT_ID, clientID);
        editor.putString(Constants.CLIENT_SECRET, clientSecret);
        editor.putString(Constants.TOKEN_ENDPOINT, info.getTokenEndPoint());
        editor.apply();
        setAccessTokenURL(info.getTokenEndPoint());
        long lastTokenRenewalAt = mainPref.getLong(Constants.LAST_TOKEN_RENEWAL, 0);
        if (!IS_TOKEN_RENEWING
                || lastTokenRenewalAt + Constants.HttpClient.DEFAULT_TOKEN_TIME_OUT * 2 < System.currentTimeMillis()) {
            IS_TOKEN_RENEWING = true;
            editor = mainPref.edit();
            editor.putLong(Constants.LAST_TOKEN_RENEWAL, System.currentTimeMillis());
            editor.commit(); //Need to make sure pref is committed
            AccessTokenHandler accessTokenHandler = new AccessTokenHandler(info, this);
            accessTokenHandler.obtainAccessToken();
        }
    }

    public void requestToken(Context context, TokenCallBack tokenCallBack, String clientID,
                             String clientSecret) {
        this.context = context;
        this.tokenCallBack = tokenCallBack;
        IdentityProxy.clientID = clientID;
        IdentityProxy.clientSecret = clientSecret;
        if (Constants.DEBUG_ENABLED) {
            Log.d(TAG, "requestToken called.");
            if(IdentityProxy.clientID == null || IdentityProxy.clientSecret == null) {
                Log.d(TAG, "Client credentials are null.");
            }
        }
        if (token == null) {
            if (Constants.DEBUG_ENABLED) {
                Log.d(TAG, "token is null.");
            }
            validateStoredToken();
        } else {
            boolean isExpired = ServerUtilities.isExpired(token.getExpiresOn());
            if (Constants.DEBUG_ENABLED) {
                Log.d(TAG, "token is expired "+ isExpired);
                Log.d(TAG, "token expiry "+ token.getExpiresOn().toString());
            }
            if (!isExpired) {
                synchronized(this){
                    IdentityProxy.getInstance().receiveNewAccessToken(Constants.REQUEST_SUCCESSFUL,
                            "success", token);
                }
            } else {
                validateStoredToken();
            }
        }
    }

    private void validateStoredToken() {
        if (Constants.DEBUG_ENABLED) {
            Log.d(TAG, "validateStoredToken.");
        }
        SharedPreferences mainPref = context.getSharedPreferences(Constants.APPLICATION_PACKAGE,
                Context.MODE_PRIVATE);
        String refreshToken = mainPref.getString(Constants.REFRESH_TOKEN, null);
        String accessToken = mainPref.getString(Constants.ACCESS_TOKEN, null);
        long expiresOn = mainPref.getLong(Constants.EXPIRE_TIME, 0);
        String endPoint = mainPref.getString(Constants.TOKEN_ENDPOINT, null);
        setAccessTokenURL(endPoint);

        if (refreshToken != null) {
            if (Constants.DEBUG_ENABLED) {
                Log.d(TAG, "refreshToken is not empty.");
            }
            token = new Token();
            token.setExpiresOn(new Date(expiresOn));
            token.setRefreshToken(refreshToken);
            token.setAccessToken(accessToken);
            boolean isExpired = ServerUtilities.isExpired(token.getExpiresOn());
            if (!isExpired) {
                if (Constants.DEBUG_ENABLED) {
                    Log.d(TAG, "stored token is not expired.");
                }
                synchronized(this){
                    IdentityProxy.getInstance().receiveNewAccessToken(Constants.REQUEST_SUCCESSFUL,
                            Constants.SUCCESS_RESPONSE,
                            token);
                }
            } else {
                if (Constants.DEBUG_ENABLED) {
                    Log.d(TAG, "stored token is expired, refreshing");
                }
                refreshToken();
            }
        } else {
            if (Constants.DEBUG_ENABLED) {
                Log.d(TAG, "refreshToken is empty.");
            }
            synchronized(this){
                IdentityProxy.getInstance().receiveNewAccessToken(Constants.ACCESS_FAILURE,
                        Constants.FAILURE_RESPONSE, token);
            }
        }
    }

    @SuppressLint("ApplySharedPref")
    private void refreshToken() {
        SharedPreferences mainPref = context.getSharedPreferences(Constants.APPLICATION_PACKAGE,
                Context.MODE_PRIVATE);
        long lastTokenRenewalAt = mainPref.getLong(Constants.LAST_TOKEN_RENEWAL, 0);
        if (!IS_TOKEN_RENEWING
                || lastTokenRenewalAt + Constants.HttpClient.DEFAULT_TOKEN_TIME_OUT * 2 < System.currentTimeMillis()) {
            IS_TOKEN_RENEWING = true;
            Editor editor = mainPref.edit();
            editor.putLong(Constants.LAST_TOKEN_RENEWAL, System.currentTimeMillis());
            editor.commit(); //Need to make sure pref is committed
            RefreshTokenHandler refreshTokenHandler = new RefreshTokenHandler(token);
            refreshTokenHandler.obtainNewAccessToken();
        }
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

}
