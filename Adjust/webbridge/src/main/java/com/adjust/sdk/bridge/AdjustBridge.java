package com.adjust.sdk.bridge;

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
    public static void onResume() {
        AdjustBridge.getDefaultInstance().onResume();
    }

    @JavascriptInterface
    public static void onPause() {
        AdjustBridge.getDefaultInstance().onPause();
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
    public static boolean isEnabled() {
        return AdjustBridge.getDefaultInstance().isEnabled();
    }

    @JavascriptInterface
    public static void appWillOpenUrl(String url) {
        AdjustBridge.getDefaultInstance().appWillOpenUrl(url);
    }

    @JavascriptInterface
    public static void setReferrer(String referrer) {
        AdjustBridge.getDefaultInstance().setReferrer(referrer);
    }

    @JavascriptInterface
    public static void setOfflineMode(String isOffline) {
        AdjustBridge.getDefaultInstance().setOfflineMode(isOffline);
    }

    @JavascriptInterface
    public static void sendFirstPackages() {
        AdjustBridge.getDefaultInstance().sendFirstPackages();
    }

    @JavascriptInterface
    public static void addSessionCallbackParameter(String key, String value) {
        AdjustBridge.getDefaultInstance().addSessionCallbackParameter(key, value);
    }

    @JavascriptInterface
    public static void addSessionPartnerParameter(String key, String value) {
        AdjustBridge.getDefaultInstance().addSessionPartnerParameter(key, value);
    }

    @JavascriptInterface
    public static void removeSessionCallbackParameter(String key) {
        AdjustBridge.getDefaultInstance().removeSessionCallbackParameter(key);
    }

    @JavascriptInterface
    public static void removeSessionPartnerParameter(String key) {
        AdjustBridge.getDefaultInstance().removeSessionPartnerParameter(key);
    }

    @JavascriptInterface
    public static void resetSessionCallbackParameters() {
        AdjustBridge.getDefaultInstance().resetSessionCallbackParameters();
    }

    @JavascriptInterface
    public static void resetSessionPartnerParameters() {
        AdjustBridge.getDefaultInstance().resetSessionPartnerParameters();
    }

    @JavascriptInterface
    public static void setPushToken(String pushToken) {
        AdjustBridge.getDefaultInstance().setPushToken(pushToken);
    }

    @JavascriptInterface
    public static void getGoogleAdId(String callback) {
        AdjustBridge.getDefaultInstance().getGoogleAdId(callback);
    }

    @JavascriptInterface
    public static String getAmazonAdId() {
        return AdjustBridge.getDefaultInstance().getAmazonAdId();
    }

    @JavascriptInterface
    public static String getAdid() {
        return AdjustBridge.getDefaultInstance().getAdid();
    }

    @JavascriptInterface
    public static void getAttribution(String callback) {
        AdjustBridge.getDefaultInstance().getAttribution(callback);
    }

    @JavascriptInterface
    public static void setTestOptions(String testOptions) {
        AdjustBridge.getDefaultInstance().setTestOptions(testOptions);
    }

    public static void setWebView(WebView webView) {
        AdjustBridge.getDefaultInstance().setWebView(webView);
    }

    public static void setApplicationContext(Application application) {
        AdjustBridge.getDefaultInstance().setApplicationContext(application);
    }
}
