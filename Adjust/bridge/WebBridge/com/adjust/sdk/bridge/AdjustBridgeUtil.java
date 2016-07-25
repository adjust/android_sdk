package com.adjust.sdk.bridge;

import android.net.Uri;
import android.webkit.WebView;

import com.adjust.sdk.AdjustAttribution;
import com.adjust.sdk.AdjustEventFailure;
import com.adjust.sdk.AdjustEventSuccess;
import com.adjust.sdk.AdjustSessionFailure;
import com.adjust.sdk.AdjustSessionSuccess;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by uerceg on 22/07/16.
 */
public class AdjustBridgeUtil {
    public static void sendDeeplinkToWebView(final WebView webView, final Uri deeplink) {
        // If web view is initialised, trigger adjust_deeplink method which user should override.
        // In this method, the content of the deeplink will be delivered.
        if (webView != null) {
            webView.post(new Runnable() {
                @Override
                public void run() {
                    String command = "javascript:adjust_deeplink('" + deeplink.toString() + "');";
                    webView.loadUrl(command);
                }
            });
        }
    }

    public static boolean isFieldValid(Object field) {
        if (field != null) {
            if (!field.toString().equals("") && !field.toString().equals("null")) {
                return true;
            }
        }

        return false;
    }

    public static void execAttributionCallbackCommand(final WebView webView, final String commandName, final AdjustAttribution attribution) {
        if (webView == null) {
            return;
        }

        webView.post(new Runnable() {
            @Override
            public void run() {
                JSONObject jsonAttribution = new JSONObject();

                try {
                    jsonAttribution.put("trackerName", attribution.trackerName == null ? JSONObject.NULL : attribution.trackerName);
                    jsonAttribution.put("trackerToken", attribution.trackerToken == null ? JSONObject.NULL : attribution.trackerToken);
                    jsonAttribution.put("campaign", attribution.campaign == null ? JSONObject.NULL : attribution.campaign);
                    jsonAttribution.put("network", attribution.network == null ? JSONObject.NULL : attribution.network);
                    jsonAttribution.put("creative", attribution.creative == null ? JSONObject.NULL : attribution.creative);
                    jsonAttribution.put("adgroup", attribution.adgroup == null ? JSONObject.NULL : attribution.adgroup);
                    jsonAttribution.put("clickLabel", attribution.clickLabel == null ? JSONObject.NULL : attribution.clickLabel);

                    String command = "javascript:" + commandName + "(" + jsonAttribution.toString() + ");";

                    webView.loadUrl(command);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void execSessionSuccessCallbackCommand(final WebView webView, final String commandName, final AdjustSessionSuccess sessionSuccess) {
        if (webView == null) {
            return;
        }

        webView.post(new Runnable() {
            @Override
            public void run() {
                JSONObject jsonSessionSuccess = new JSONObject();

                try {
                    jsonSessionSuccess.put("message", sessionSuccess.message == null ? JSONObject.NULL : sessionSuccess.message);
                    jsonSessionSuccess.put("adid", sessionSuccess.adid == null ? JSONObject.NULL : sessionSuccess.adid);
                    jsonSessionSuccess.put("timestamp", sessionSuccess.timestamp == null ? JSONObject.NULL : sessionSuccess.timestamp);
                    jsonSessionSuccess.put("jsonResponse", sessionSuccess.jsonResponse == null ? JSONObject.NULL : sessionSuccess.jsonResponse);

                    String command = "javascript:" + commandName + "(" + jsonSessionSuccess.toString() + ");";

                    webView.loadUrl(command);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void execSessionFailureCallbackCommand(final WebView webView, final String commandName, final AdjustSessionFailure sessionFailure) {
        if (webView == null) {
            return;
        }

        webView.post(new Runnable() {
            @Override
            public void run() {
                JSONObject jsonSessionFailure = new JSONObject();

                try {
                    jsonSessionFailure.put("message", sessionFailure.message == null ? JSONObject.NULL : sessionFailure.message);
                    jsonSessionFailure.put("adid", sessionFailure.adid == null ? JSONObject.NULL : sessionFailure.adid);
                    jsonSessionFailure.put("timestamp", sessionFailure.timestamp == null ? JSONObject.NULL : sessionFailure.timestamp);
                    jsonSessionFailure.put("willRetry", sessionFailure.willRetry ? String.valueOf(true) : String.valueOf(false));
                    jsonSessionFailure.put("jsonResponse", sessionFailure.jsonResponse == null ? JSONObject.NULL : sessionFailure.jsonResponse);

                    String command = "javascript:" + commandName + "(" + jsonSessionFailure.toString() + ");";

                    webView.loadUrl(command);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void execEventSuccessCallbackCommand(final WebView webView, final String commandName, final AdjustEventSuccess eventSuccess) {
        if (webView == null) {
            return;
        }

        webView.post(new Runnable() {
            @Override
            public void run() {
                JSONObject jsonEventSuccess = new JSONObject();

                try {
                    jsonEventSuccess.put("eventToken", eventSuccess.eventToken == null ? JSONObject.NULL : eventSuccess.eventToken);
                    jsonEventSuccess.put("message", eventSuccess.message == null ? JSONObject.NULL : eventSuccess.message);
                    jsonEventSuccess.put("adid", eventSuccess.adid == null ? JSONObject.NULL : eventSuccess.adid);
                    jsonEventSuccess.put("timestamp", eventSuccess.timestamp == null ? JSONObject.NULL : eventSuccess.timestamp);
                    jsonEventSuccess.put("jsonResponse", eventSuccess.jsonResponse == null ? JSONObject.NULL : eventSuccess.jsonResponse);

                    String command = "javascript:" + commandName + "(" + jsonEventSuccess.toString() + ");";

                    webView.loadUrl(command);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void execEventFailureCallbackCommand(final WebView webView, final String commandName, final AdjustEventFailure eventFailure) {
        if (webView == null) {
            return;
        }

        webView.post(new Runnable() {
            @Override
            public void run() {
                JSONObject jsonEventFailure = new JSONObject();

                try {
                    jsonEventFailure.put("eventToken", eventFailure.eventToken == null ? JSONObject.NULL : eventFailure.eventToken);
                    jsonEventFailure.put("message", eventFailure.message == null ? JSONObject.NULL : eventFailure.message);
                    jsonEventFailure.put("adid", eventFailure.adid == null ? JSONObject.NULL : eventFailure.adid);
                    jsonEventFailure.put("timestamp", eventFailure.timestamp == null ? JSONObject.NULL : eventFailure.timestamp);
                    jsonEventFailure.put("willRetry", eventFailure.willRetry ? String.valueOf(true) : String.valueOf(false));
                    jsonEventFailure.put("jsonResponse", eventFailure.jsonResponse == null ? JSONObject.NULL : eventFailure.jsonResponse);

                    String command = "javascript:" + commandName + "(" + jsonEventFailure.toString() + ");";

                    webView.loadUrl(command);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void execDeferredDeeplinkCallbackCommand(final WebView webView, final String commandName, final String deeplink) {
        if (webView == null) {
            return;
        }

        webView.post(new Runnable() {
            @Override
            public void run() {
                String command = "javascript:" + commandName + "('" + deeplink + "');";
                webView.loadUrl(command);
            }
        });
    }

    public static void execIsEnabledCallbackCommand(final WebView webView, final String commandName, final String isEnabled) {
        if (webView == null) {
            return;
        }

        webView.post(new Runnable() {
            @Override
            public void run() {
                String command = "javascript:" + commandName + "(" + isEnabled + ");";
                webView.loadUrl(command);
            }
        });
    }

    public static void execGetGoogleAdIdCallbackCommand(final WebView webView, final String commandName, final String gpsAdid) {
        if (webView == null) {
            return;
        }

        webView.post(new Runnable() {
            @Override
            public void run() {
                String command = "javascript:" + commandName + "(" + gpsAdid + ");";
                webView.loadUrl(command);
            }
        });
    }

    public static String[] jsonArrayToArray(JSONArray jsonArray) throws JSONException {
        if (jsonArray != null) {
            String[] array = new String[jsonArray.length()];

            for (int i = 0; i < jsonArray.length(); i++) {
                array[i] = jsonArray.get(i).toString();
            }

            return array;
        }

        return null;
    }
}
