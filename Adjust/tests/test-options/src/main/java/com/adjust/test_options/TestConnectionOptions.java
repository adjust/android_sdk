package com.adjust.test_options;

import android.util.Log;

import com.adjust.sdk.AdjustFactory;
import com.adjust.sdk.network.UtilNetworking;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
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
                    // create modern SSLContext that supports TLS 1.2+
                    SSLContext sslContext = SSLContext.getInstance("TLS");
                    sslContext.init(
                            null,
                            TestConnectionOptions.generateTrustAllCerts(),
                            new java.security.SecureRandom());

                    // use the modern SSL socket factory instead of TLSSocketFactory
                    // this avoids the TLS 1.1 limitation
                    connection.setSSLSocketFactory(sslContext.getSocketFactory());
                    connection.setHostnameVerifier(TestConnectionOptions.generateHostnameVerifier());
                } catch (Exception e) {
                    Log.e("TestOptions","connectionOptions error " + e.getMessage());
                }
            }
        });
    }

    private static TrustManager[] generateTrustAllCerts() {
        return new TrustManager[] {
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    Log.d("TestApp", "getAcceptedIssuers");
                    return null;
                }

                public void checkClientTrusted(final java.security.cert.X509Certificate[] certs,
                                               final String authType) {
                    Log.d("TestApp", "checkClientTrusted");
                }

                public void checkServerTrusted(final java.security.cert.X509Certificate[] certs,
                                               final String authType) {
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
