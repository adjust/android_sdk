package com.adjust.test_options;

import android.util.Log;

import com.adjust.sdk.AdjustFactory;
import com.adjust.sdk.network.UtilNetworking;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class TestConnectionOptions {
    public static void setTestConnectionOptions() {
        AdjustFactory.setConnectionOptions(new UtilNetworking.IConnectionOptions() {
            @Override
            public void applyConnectionOptions(HttpsURLConnection connection, String clientSdk) {
                UtilNetworking.IConnectionOptions defaultConnectionOption =
                        UtilNetworking.createDefaultConnectionOptions();
                defaultConnectionOption.applyConnectionOptions(connection, clientSdk);

                try {
                    TLSSocketFactory tlsSocketFactory =
                            new TLSSocketFactory(
                                    TestConnectionOptions.generateTrustAllCerts(),
                                    new java.security.SecureRandom());
                    connection.setSSLSocketFactory(tlsSocketFactory);
                    connection.setHostnameVerifier(
                            TestConnectionOptions.generateHostnameVerifier());
                } catch (Exception e) {
                    Log.e("TestOptions","connectionOptions error " + e.getMessage());
                }
            }
        });
    }

    private static TrustManager[] generateTrustAllCerts() {
        return new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        Log.d("TestApp", "getAcceptedIssuers");
                        return null;
                    }

                    public void checkClientTrusted(final java.security.cert.X509Certificate[] certs,
                                                   final String authType)
                    {
                        Log.d("TestApp", "checkClientTrusted");
                    }

                    public void checkServerTrusted(final java.security.cert.X509Certificate[] certs,
                                                   final String authType)
                    {
                        Log.d("TestApp", "checkServerTrusted");
                    }
                }
        };
    }

    private static HostnameVerifier generateHostnameVerifier() {
        return new HostnameVerifier() {
            @Override
            public boolean verify(final String hostname, final SSLSession session) {
                Log.d("TestApp", "HostnameVerifier verify");
                return true;
            }
        };
    }

}
