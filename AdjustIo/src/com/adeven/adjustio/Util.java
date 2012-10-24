//
//  Util.java
//  AdjustIo
//
//  Created by Christian Wellenbrock on 11.10.12.
//  Copyright (c) 2012 adeven. All rights reserved.
//

package com.adeven.adjustio;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;

public class Util {
    
    private static final String BASEURL = "http://app.adjust.io";
    private static final String CLIENTSDK = "android1.0";
    
    public static String getBase64EncodedParameters(Map<String, String> parameters) {
        if (parameters == null) {
            return null;
        }

        JSONObject jsonObject = new JSONObject(parameters);
        String jsonString = jsonObject.toString();
        String encoded = Base64.encodeToString(jsonString.getBytes(), Base64.DEFAULT);
        return encoded;
    }
    
    public static StringEntity getEntityEncodedParameters(String... parameters) throws UnsupportedEncodingException {
        List<NameValuePair> pairs = new ArrayList<NameValuePair>(2);
        for (int i = 0; i+1 < parameters.length; i += 2) {
            String key = parameters[i];
            String value = parameters[i+1];
            if (value != null) {
                pairs.add(new BasicNameValuePair(key, value));
            }
        }
        StringEntity entity = new UrlEncodedFormEntity(pairs);
        return entity;
    }

    public static HttpClient getHttpClient(String userAgent) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpParams params = httpClient.getParams(); 
        params.setParameter(CoreProtocolPNames.USER_AGENT, userAgent);
        return httpClient;
    }

    public static HttpPost getPostRequest(String path) {
        String url = BASEURL + path;
        HttpPost request = new HttpPost(url);

        String language = Locale.getDefault().getLanguage();
        request.addHeader("Accept-Language", language);
        request.addHeader("Client-SDK", CLIENTSDK);

        return request;
    }
    
    protected static String getUserAgent(Application app) {
        Resources resources = app.getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();
        Locale locale = configuration.locale;
        int screenLayout = configuration.screenLayout;

        StringBuilder builder = new StringBuilder(app.getPackageName());
        builder.append(" " + Util.getAppVersion(app));
        builder.append(" " + Util.getDeviceType(screenLayout));
        builder.append(" " + Build.DEVICE.replaceAll(" ", ""));
        builder.append(" " + "android");
        builder.append(" " + Build.VERSION.SDK_INT);
        builder.append(" " + locale.getLanguage());
        builder.append(" " + locale.getCountry());
        builder.append(" " + Util.getScreenSize(screenLayout));
        builder.append(" " + Util.getScreenFormat(screenLayout));
        builder.append(" " + Util.getScreenDensity(displayMetrics));
        builder.append(" " + displayMetrics.widthPixels);
        builder.append(" " + displayMetrics.heightPixels);

        String userAgent = builder.toString();
        return userAgent;
    }

    protected static String getMacAddress(Application app) {
        WifiManager wifiManager = (WifiManager) app.getSystemService(Context.WIFI_SERVICE); // TODO: only works on device
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String address = wifiInfo.getMacAddress();
        
        if (address == null) {
            address = "andremul";
        } else {
            address = address.replace(":", "");
        }
        
        Log.d("aoeu", address);
        return address;
    }

    private static String getAppVersion(Application app) {
        try {
            PackageManager packageManager = app.getPackageManager();
            String name = app.getPackageName();
            PackageInfo info = packageManager.getPackageInfo(name, 0);
            String versionName = info.versionName;
            return versionName;
        } catch (NameNotFoundException e) {
            return "unknown";
        }
    }

    private static String getDeviceType(int screenLayout) {
        int screenSize = screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        
        switch (screenSize) {
        case Configuration.SCREENLAYOUT_SIZE_SMALL:
        case Configuration.SCREENLAYOUT_SIZE_NORMAL:
            return "phone";
        case Configuration.SCREENLAYOUT_SIZE_LARGE:
        case 4:
            return "tablet";
        default:
            return "unknown";
        }
    }

    private static String getScreenSize(int screenLayout) {
        int screenSize = screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;

        switch (screenSize) {
        case Configuration.SCREENLAYOUT_SIZE_SMALL:
            return "small";
        case Configuration.SCREENLAYOUT_SIZE_NORMAL:
            return "normal";
        case Configuration.SCREENLAYOUT_SIZE_LARGE:
            return "large";
        case 4:
            return "xlarge";
        default:
            return "unknown";
        }
    }

    private static String getScreenFormat(int screenLayout) {
        int screenFormat = screenLayout & Configuration.SCREENLAYOUT_LONG_MASK;

        switch (screenFormat) {
        case Configuration.SCREENLAYOUT_LONG_YES:
            return "long";
        case Configuration.SCREENLAYOUT_LONG_NO:
            return "normal";
        default:
            return "unknown";
        }
    }
    
    private static String getScreenDensity(DisplayMetrics displayMetrics) {
        int density = displayMetrics.densityDpi;
        int low = (DisplayMetrics.DENSITY_MEDIUM + DisplayMetrics.DENSITY_LOW) / 2;
        int high = (DisplayMetrics.DENSITY_MEDIUM + DisplayMetrics.DENSITY_HIGH) / 2;
        
        if (density == 0) {
            return "unknown";
        } else if (density < low) {
            return "low";
        } else if (density > high) {
            return "high";
        } else {
            return "medium";
        }
    }
}
