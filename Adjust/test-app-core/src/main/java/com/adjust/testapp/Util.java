package com.adjust.testapp;

import android.util.Log;

import com.adjust.sdk.network.UtilNetworking;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by nonelse on 22.02.2018
 */
public class Util {
    static Boolean strictParseStringToBoolean(String value) {
        if (value == null) {
            return null;
        }
        if (value.equalsIgnoreCase("true")) {
            return true;
        }
        if (value.equalsIgnoreCase("false")) {
            return false;
        }

        return null;
    }

    static UtilNetworking.IConnectionOptions testConnectionOptions() {
        return new UtilNetworking.IConnectionOptions() {
            @Override
            public void applyConnectionOptions(final HttpsURLConnection connection,
                                               final String clientSdk)
            {
                UtilNetworking.IConnectionOptions defaultConnectionOption =
                        UtilNetworking.createDefaultConnectionOptions();
                defaultConnectionOption.applyConnectionOptions(connection, clientSdk);
                try {
                    SSLContext sc = SSLContext.getInstance("TLS");
                    sc.init(null, new TrustManager[]{
                            new X509TrustManager() {
                                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                                    Log.d("TestApp","getAcceptedIssuers");
                                    return null;
                                }
                                public void checkClientTrusted(
                                        X509Certificate[] certs, String authType) {
                                    Log.d("TestApp","checkClientTrusted ");
                                }
                                public void checkServerTrusted(
                                        X509Certificate[] certs, String authType) throws CertificateException {
                                    Log.d("TestApp","checkServerTrusted ");

                                    String serverThumbprint = "7BCFF44099A35BC093BB48C5A6B9A516CDFDA0D1";
                                    X509Certificate certificate = certs[0];

                                    MessageDigest md = null;
                                    try {
                                        md = MessageDigest.getInstance("SHA1");
                                        byte[] publicKey = md.digest(certificate.getEncoded());
                                        String hexString = byte2HexFormatted(publicKey);

                                        if (!hexString.equalsIgnoreCase(serverThumbprint)) {
                                            throw new CertificateException();
                                        }
                                    } catch (NoSuchAlgorithmException e) {
                                        Log.e("TestApp","testingMode error " + e.getMessage());
                                    } catch (CertificateEncodingException e) {
                                        Log.e("TestApp","testingMode error " + e.getMessage());
                                    }
                                }
                            }
                    }, new java.security.SecureRandom());
                    connection.setSSLSocketFactory(sc.getSocketFactory());

                    connection.setHostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            Log.d("TestApp","verify hostname ");
                            return true;
                        }
                    });
                } catch (Exception e) {
                    Log.e("TestApp","testingMode error " + e.getMessage());
                }
            }
        };
    }

    private static String byte2HexFormatted(byte[] arr) {
        StringBuilder str = new StringBuilder(arr.length * 2);

        for (int i = 0; i < arr.length; i++) {
            String h = Integer.toHexString(arr[i]);
            int l = h.length();

            if (l == 1) {
                h = "0" + h;
            }

            if (l > 2) {
                h = h.substring(l - 2, l);
            }

            str.append(h.toUpperCase());

            // if (i < (arr.length - 1)) str.append(':');
        }
        return str.toString();
    }
}
