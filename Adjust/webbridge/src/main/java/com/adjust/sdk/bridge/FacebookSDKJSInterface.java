package com.adjust.sdk.bridge;
/**
 * Taken and adapted from
 * https://github.com/facebook/facebook-android-sdk/blob/8cb6f95df8d2763f67e136eb7b2a66d9ddfc5157/facebook-core/src/main/java/com/facebook/appevents/FacebookSDKJSInterface.java
 *
 * Copyright (c) 2014-present, Facebook, Inc. All rights reserved.
 *
 * You are hereby granted a non-exclusive, worldwide, royalty-free license to use,
 * copy, modify, and distribute this software in source code or binary form for use
 * in connection with the web services and APIs provided by Facebook.
 *
 * As with any software that integrates with the Facebook platform, your use of
 * this software is subject to the Facebook Developer Principles and Policies
 * [http://developers.facebook.com/policy/]. This copyright notice shall be
 * included in all copies or substantial portions of the software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.webkit.JavascriptInterface;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustEvent;
import com.adjust.sdk.AdjustFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class FacebookSDKJSInterface {
    private static final String PROTOCOL = "fbmq-0.1";
    private static final String PARAMETER_FBSDK_PIXEL_REFERRAL = "_fb_pixel_referral_id";
    private static final String APPLICATION_ID_PROPERTY = "com.facebook.sdk.ApplicationId";
    private String fbPixelDefaultEventToken;
    private Map<String, String> fbPixelMapping;

    public FacebookSDKJSInterface() {
        fbPixelMapping = new HashMap<String, String>();
    }

    private static Map<String, String> jsonStringToMap(String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            Map<String, String> stringMap = new HashMap<String, String>(jsonObject.length());
            Iterator iter = jsonObject.keys();
            while (iter.hasNext()) {
                String key = (String) iter.next();
                String value = jsonObject.getString(key);
                stringMap.put(key, value);
            }
            return stringMap;
        } catch (JSONException ignored) {
            return new HashMap<String, String>();
        }
    }

    public static String getApplicationId(Context context) {
        String applicationId = null;
        ApplicationInfo ai = null;
        try {
            ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            AdjustFactory.getLogger().error("Error loading fb ApplicationInfo: %s", e.getMessage());
            return null;
        }

        Object appId = ai.metaData.get(APPLICATION_ID_PROPERTY);
        if (appId instanceof String) {
            String appIdString = (String) appId;
            if (appIdString.toLowerCase(Locale.ROOT).startsWith("fb")) {
                applicationId = appIdString.substring(2);
            } else {
                applicationId = appIdString;
            }
        } else if (appId instanceof Integer) {
            AdjustFactory.getLogger().error("App Ids cannot be directly placed in the manifest." +
                    "They must be prefixed by 'fb' or be placed in the string resource file.");
        } else {
            AdjustFactory.getLogger().error("App Ids is not a string or integer");
        }
        return applicationId;
    }

    public void setDefaultEventToken(String fbPixelDefaultEventToken) {
        this.fbPixelDefaultEventToken = fbPixelDefaultEventToken;
    }

    public void addFbPixelEventTokenMapping(String key, String value) {
        this.fbPixelMapping.put(key, value);
    }

    @JavascriptInterface
    public void sendEvent(String pixelId, String event_name, String jsonString) {
        if (pixelId == null) {
            AdjustFactory.getLogger().error("Can't bridge an event without a referral Pixel ID. " +
                "Check your webview Pixel configuration");
            return;
        }

        String eventToken = fbPixelMapping.get(event_name);
        if (eventToken == null) {
            AdjustFactory.getLogger().debug("No mapping found for the fb pixel event %s, trying to fall back to the default event token", event_name);
            eventToken = this.fbPixelDefaultEventToken;
        }

        if (eventToken == null) {
            AdjustFactory.getLogger().warn("There is not a default event token configured or a mapping found for event named: '%s'. It won't be tracked as an adjust event", event_name);
            return;
        }
        AdjustEvent fbPixelEvent = new AdjustEvent(eventToken);
        if (!fbPixelEvent.isValid()) {
            return;
        }

        Map<String, String> stringMap = jsonStringToMap(jsonString);
        stringMap.put(PARAMETER_FBSDK_PIXEL_REFERRAL, pixelId);
        // stringMap.put("_eventName", event_name);

        AdjustFactory.getLogger().debug("FB pixel event received, eventName: %s, payload: %s", event_name, stringMap);

        for (Map.Entry<String,String> entry : stringMap.entrySet() ) {
            String key = entry.getKey();
            fbPixelEvent.addPartnerParameter(key, entry.getValue());
        }

        Adjust.trackEvent(fbPixelEvent);
    }

    @JavascriptInterface
    public String getProtocol() {
        return PROTOCOL;
    }
}
