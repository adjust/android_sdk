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
}
