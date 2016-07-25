package com.adjust.sdk.bridge;

import android.net.Uri;
import android.webkit.WebView;
import android.app.Application;
import android.webkit.JavascriptInterface;

/**
 * Created by uerceg on 10/06/16.
 */
public class AdjustBridge {
    private static AdjustBridgeInstance defaultInstance;

    public static synchronized AdjustBridgeInstance getDefaultInstance() {
        if (defaultInstance == null) {
            defaultInstance = new AdjustBridgeInstance();
        }

        return defaultInstance;
    }

    @JavascriptInterface
    public static void onCreate(String adjustConfigString) {
        AdjustBridge.getDefaultInstance().onCreate(adjustConfigString);
    }

    @JavascriptInterface
    public static void trackEvent(String adjustEventString) {
        AdjustBridge.getDefaultInstance().trackEvent(adjustEventString);
    }

    @JavascriptInterface
    public static void setOfflineMode(String isOffline) {
        AdjustBridge.getDefaultInstance().setOfflineMode(isOffline);
    }

    @JavascriptInterface
    public static void setEnabled(String isEnabled) {
        AdjustBridge.getDefaultInstance().setEnabled(isEnabled);
    }

    @JavascriptInterface
    public static void isEnabled(String callback) {
        AdjustBridge.getDefaultInstance().isEnabled(callback);
    }

    @JavascriptInterface
    public static void getGoogleAdId(String callback) {
        AdjustBridge.getDefaultInstance().getGoogleAdId(callback);
    }

    @JavascriptInterface
    public static void setAttributionCallback(String callback) {
        AdjustBridge.getDefaultInstance().setAttributionCallback(callback);
    }

    @JavascriptInterface
    public static void setSessionSuccessCallback(String callback) {
        AdjustBridge.getDefaultInstance().setSessionSuccessCallback(callback);
    }

    @JavascriptInterface
    public static void setSessionFailureCallback(String callback) {
        AdjustBridge.getDefaultInstance().setSessionFailureCallback(callback);
    }

    @JavascriptInterface
    public static void setEventSuccessCallback(String callback) {
        AdjustBridge.getDefaultInstance().setEventSuccessCallback(callback);
    }

    @JavascriptInterface
    public static void setEventFailureCallback(String callback) {
        AdjustBridge.getDefaultInstance().setEventFailureCallback(callback);
    }

    @JavascriptInterface
    public static void setDeferredDeeplinkCallback(String callback) {
        AdjustBridge.getDefaultInstance().setDeferredDeeplinkCallback(callback);
    }

    public static void setWebView(WebView webView) {
        AdjustBridge.getDefaultInstance().setWebView(webView);
    }

    public static void setApplicationContext(Application application) {
        AdjustBridge.getDefaultInstance().setApplicationContext(application);
    }

    public static void deeplinkReceived(Uri deeplink) {
        AdjustBridge.getDefaultInstance().deeplinkReceived(deeplink);
    }
}
