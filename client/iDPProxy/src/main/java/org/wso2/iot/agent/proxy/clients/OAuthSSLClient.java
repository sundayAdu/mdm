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

package org.wso2.iot.agent.proxy.clients;

import android.content.Context;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;

import org.wso2.iot.agent.proxy.IDPTokenManagerException;
import org.wso2.iot.agent.proxy.IdentityProxy;
import org.wso2.iot.agent.proxy.R;
import org.wso2.iot.agent.proxy.utils.Constants;
import org.wso2.iot.agent.proxy.utils.StreamHandlerUtil;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Map;

public class OAuthSSLClient implements CommunicationClient {
    private static final String TAG = OAuthSSLClient.class.getName();

    @Override
    public RequestQueue getHttpClient() throws IDPTokenManagerException {
        RequestQueue client = null;
        InputStream inStream = null;
        try {
            Context context = IdentityProxy.getInstance().getContext();
            if (context == null) {
                String message = "Context is not available";
                Log.e(TAG, message);
                throw new IDPTokenManagerException(message);
            }
            if (Constants.SERVER_PROTOCOL.equalsIgnoreCase("https://")) {
                KeyStore localTrustStore = KeyStore.getInstance("BKS");
                if (Constants.TRUSTSTORE_LOCATION != null) {
                    inStream = new FileInputStream(new File(Constants.TRUSTSTORE_LOCATION));
                } else {
                    inStream = IdentityProxy.getInstance().getContext().getResources().
                            openRawResource(R.raw.truststore);
                }
                localTrustStore.load(inStream, Constants.TRUSTSTORE_PASSWORD.toCharArray());
                if(localTrustStore.size() > 0) { // Handling self-signed server SSL
                    String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
                    TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
                    tmf.init(localTrustStore);


                    SSLContext sslContext = SSLContext.getInstance("TLS");
                    sslContext.init(null, tmf.getTrustManagers(), null);
                    final SSLSocketFactory socketFactory = sslContext.getSocketFactory();
                    HurlStack hurlStack = new HurlStack() {
                        @Override
                        protected HttpURLConnection createConnection(URL url) throws IOException {
                            if (Constants.DEBUG_ENABLED) {
                                Log.d(TAG, "url: " + url);
                            }
                            if ("https".equalsIgnoreCase(url.getProtocol())) {
                                if (Constants.DEBUG_ENABLED) {
                                    Log.d(TAG, "Creating https URL connection");
                                }
                                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) super.createConnection(url);
                                httpsURLConnection.setSSLSocketFactory(socketFactory);
                                httpsURLConnection.setHostnameVerifier(getHostnameVerifier());
                                return httpsURLConnection;
                            } else {
                                if (Constants.DEBUG_ENABLED) {
                                    Log.d(TAG, "Creating http URL connection");
                                }
                                return super.createConnection(url);
                            }
                        }
                    };
                    client = Volley.newRequestQueue(context, hurlStack);
                } else { //Handling valid(non-self-signed) SSL (Server must present the full
                         // certificate chain.)
                    client = Volley.newRequestQueue(context);
                }
            } else {
                client = Volley.newRequestQueue(context);
            }

        } catch (KeyStoreException e) {
            String errorMsg = "Error occurred while accessing keystore.";
            Log.e(TAG, errorMsg);
            throw new IDPTokenManagerException(errorMsg, e);
        } catch (CertificateException e) {
            String errorMsg = "Error occurred while loading certificate.";
            Log.e(TAG, errorMsg);
            throw new IDPTokenManagerException(errorMsg, e);
        } catch (NoSuchAlgorithmException e) {
            String errorMsg = "Error occurred due to mismatch of defined algorithm.";
            Log.e(TAG, errorMsg);
            throw new IDPTokenManagerException(errorMsg, e);
        } catch (KeyManagementException e) {
            String errorMsg = "Error occurred while accessing keystore.";
            Log.e(TAG, errorMsg);
            throw new IDPTokenManagerException(errorMsg, e);
        } catch (IOException e) {
            String errorMsg = "Error occurred while loading trust store.";
            Log.e(TAG, errorMsg);
            throw new IDPTokenManagerException(errorMsg, e);
        } finally {
            StreamHandlerUtil.closeInputStream(inStream, TAG);
        }
        return client;
    }

    private HostnameVerifier getHostnameVerifier() {
        return new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
//                HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();
//                return hv.verify(hostname, session);
                Log.i(TAG, "Approving certificate for " + hostname);
                return true;
            }
        };
    }

    //TODO: Move oauth specific bits in Agent source to proxy.
    @Override
    public void addAdditionalHeader(Map<String, String> headers) {

    }
}
