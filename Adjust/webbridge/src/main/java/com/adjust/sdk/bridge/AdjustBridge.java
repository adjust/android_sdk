package com.adjust.sdk.bridge;

import android.webkit.WebView;
import android.app.Application;

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

    public static void setWebView(WebView webView) {
        AdjustBridge.getDefaultInstance().setWebView(webView);
    }

    public static void setApplicationContext(Application application) {
        AdjustBridge.getDefaultInstance().setApplicationContext(application);
    }
}
