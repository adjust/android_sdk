package com.adjust.sdk.webbridge;

import android.net.Uri;
import android.webkit.WebView;

import com.adjust.sdk.AdjustAttribution;
import com.adjust.sdk.AdjustEventFailure;
import com.adjust.sdk.AdjustEventSuccess;
import com.adjust.sdk.AdjustFactory;
import com.adjust.sdk.AdjustRemoteTrigger;
import com.adjust.sdk.AdjustSessionFailure;
import com.adjust.sdk.AdjustSessionSuccess;
import com.adjust.sdk.AdjustThirdPartySharingResult;
import com.adjust.sdk.ILogger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by uerceg on 22/07/16.
 */
public class AdjustBridgeUtil {
    private static final String NATIVE_CALLBACK_NAME = "Adjust._nativeCallback";
    private static final String SET_BRIDGE_TOKEN_CALLBACK_NAME = "Adjust._setBridgeToken";

    public static void sendDeeplinkToWebView(final WebView webView, final Uri deeplink) {
        // If web view is initialised, trigger adjust_deeplink method which user should override.
        // In this method, the content of the deeplink will be delivered.
        if (webView != null) {
            webView.post(new Runnable() {
                @Override
                public void run() {
                    String deeplinkValue = deeplink == null ? "null" : JSONObject.quote(deeplink.toString());
                    String command = "if (typeof adjust_deeplink === 'function') { adjust_deeplink(" + deeplinkValue + "); }";
                    webView.evaluateJavascript(command, null);
                }
            });
        }
    }

    public static String fieldToString(Object field) {
        if (field == null) {
            return null;
        }

        String fieldString = field.toString();
        if (fieldString.equals("null")) {
            return null;
        }

        return fieldString;
    }

    public static Boolean fieldToBoolean(Object field) {
        if (field == null) {
            return null;
        }

        String fieldString = field.toString();
        if (fieldString.equalsIgnoreCase("true")) {
            return true;
        }
        if (fieldString.equalsIgnoreCase("false")) {
            return false;
        }

        return null;
    }

    public static Double fieldToDouble(Object field) {
        if (field == null) {
            return null;
        }

        String fieldString = field.toString();
        try {
            return Double.parseDouble(fieldString);
        } catch (Exception e) {
            return null;
        }
    }

    public static Long fieldToLong(Object field) {
        if (field == null) {
            return null;
        }

        String fieldString = field.toString();
        try {
            return Long.parseLong(fieldString);
        } catch (Exception e) {
            return null;
        }
    }

    public static Integer fieldToInteger(Object field) {
        if (field == null) {
            return null;
        }

        String fieldString = field.toString();
        try {
            return Integer.parseInt(fieldString);
        } catch (Exception e) {
            return null;
        }
    }

    public static void execAdidCallbackCommand(final WebView webView, final String commandName, final String adid) {
        execNativeCallback(webView, commandName, adid == null ? "null" : JSONObject.quote(adid));
    }

    public static void execAttributionCallbackCommand(final WebView webView, final String commandName, final AdjustAttribution attribution) {
        if (webView == null) {
            return;
        }
        if (commandName == null) {
            return;
        }
        try {
            if (attribution != null) {
                JSONObject jsonAttribution = new JSONObject();
                jsonAttribution.put("trackerName", attribution.trackerName == null ? JSONObject.NULL : attribution.trackerName);
                jsonAttribution.put("trackerToken", attribution.trackerToken == null ? JSONObject.NULL : attribution.trackerToken);
                jsonAttribution.put("campaign", attribution.campaign == null ? JSONObject.NULL : attribution.campaign);
                jsonAttribution.put("network", attribution.network == null ? JSONObject.NULL : attribution.network);
                jsonAttribution.put("creative", attribution.creative == null ? JSONObject.NULL : attribution.creative);
                jsonAttribution.put("adgroup", attribution.adgroup == null ? JSONObject.NULL : attribution.adgroup);
                jsonAttribution.put("clickLabel", attribution.clickLabel == null ? JSONObject.NULL : attribution.clickLabel);
                jsonAttribution.put("costType", attribution.costType == null ? JSONObject.NULL : attribution.costType);
                jsonAttribution.put("costAmount", attribution.costAmount == null || attribution.costAmount.isNaN() ? 0 : attribution.costAmount);
                jsonAttribution.put("costCurrency", attribution.costCurrency == null ? JSONObject.NULL : attribution.costCurrency);
                jsonAttribution.put("fbInstallReferrer", attribution.fbInstallReferrer == null ? JSONObject.NULL : attribution.fbInstallReferrer);
                jsonAttribution.put("jsonResponse", attribution.jsonResponse == null ? JSONObject.NULL : new JSONObject(attribution.jsonResponse));

                execNativeCallback(webView, commandName, jsonAttribution.toString());
            } else {
                execNativeCallback(webView, commandName, "null");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void execThirdPartySharingSettingsCallbackCommand(final WebView webView, final String commandName, final AdjustThirdPartySharingResult result) {
        if (webView == null) {
            return;
        }
        if (commandName == null) {
            return;
        }
        try {
            if (result != null) {
                JSONObject jsonAdjustThirdPartySharingResult = new JSONObject();
                jsonAdjustThirdPartySharingResult.put("thirdPartySharingSettingsJson", result.getThirdPartySharingSettingsJson() == null ? JSONObject.NULL : result.getThirdPartySharingSettingsJson());
                execNativeCallback(webView, commandName, jsonAdjustThirdPartySharingResult.toString());
            } else {
                execNativeCallback(webView, commandName, "null");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void execSessionSuccessCallbackCommand(final WebView webView, final String commandName, final AdjustSessionSuccess sessionSuccess) {
        if (webView == null) {
            return;
        }
        if (sessionSuccess == null) {
            return;
        }
        JSONObject jsonSessionSuccess = new JSONObject();
        try {
            jsonSessionSuccess.put("message", sessionSuccess.message == null ? JSONObject.NULL : sessionSuccess.message);
            jsonSessionSuccess.put("adid", sessionSuccess.adid == null ? JSONObject.NULL : sessionSuccess.adid);
            jsonSessionSuccess.put("timestamp", sessionSuccess.timestamp == null ? JSONObject.NULL : sessionSuccess.timestamp);
            jsonSessionSuccess.put("jsonResponse", sessionSuccess.jsonResponse == null ? JSONObject.NULL : sessionSuccess.jsonResponse);
            execNativeCallback(webView, commandName, jsonSessionSuccess.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void execSessionFailureCallbackCommand(final WebView webView, final String commandName, final AdjustSessionFailure sessionFailure) {
        if (webView == null) {
            return;
        }
        if (sessionFailure == null) {
            return;
        }
        JSONObject jsonSessionFailure = new JSONObject();
        try {
            jsonSessionFailure.put("message", sessionFailure.message == null ? JSONObject.NULL : sessionFailure.message);
            jsonSessionFailure.put("adid", sessionFailure.adid == null ? JSONObject.NULL : sessionFailure.adid);
            jsonSessionFailure.put("timestamp", sessionFailure.timestamp == null ? JSONObject.NULL : sessionFailure.timestamp);
            jsonSessionFailure.put("willRetry", sessionFailure.willRetry ? String.valueOf(true) : String.valueOf(false));
            jsonSessionFailure.put("jsonResponse", sessionFailure.jsonResponse == null ? JSONObject.NULL : sessionFailure.jsonResponse);
            execNativeCallback(webView, commandName, jsonSessionFailure.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void execEventSuccessCallbackCommand(final WebView webView, final String commandName, final AdjustEventSuccess eventSuccess) {
        if (webView == null) {
            return;
        }
        if (eventSuccess == null) {
            return;
        }
        JSONObject jsonEventSuccess = new JSONObject();
        try {
            jsonEventSuccess.put("eventToken", eventSuccess.eventToken == null ? JSONObject.NULL : eventSuccess.eventToken);
            jsonEventSuccess.put("message", eventSuccess.message == null ? JSONObject.NULL : eventSuccess.message);
            jsonEventSuccess.put("adid", eventSuccess.adid == null ? JSONObject.NULL : eventSuccess.adid);
            jsonEventSuccess.put("timestamp", eventSuccess.timestamp == null ? JSONObject.NULL : eventSuccess.timestamp);
            jsonEventSuccess.put("callbackId", eventSuccess.callbackId == null ? JSONObject.NULL : eventSuccess.callbackId);
            jsonEventSuccess.put("jsonResponse", eventSuccess.jsonResponse == null ? JSONObject.NULL : eventSuccess.jsonResponse);
            execNativeCallback(webView, commandName, jsonEventSuccess.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void execEventFailureCallbackCommand(final WebView webView, final String commandName, final AdjustEventFailure eventFailure) {
        if (webView == null) {
            return;
        }
        if (eventFailure == null) {
            return;
        }
        JSONObject jsonEventFailure = new JSONObject();
        try {
            jsonEventFailure.put("eventToken", eventFailure.eventToken == null ? JSONObject.NULL : eventFailure.eventToken);
            jsonEventFailure.put("message", eventFailure.message == null ? JSONObject.NULL : eventFailure.message);
            jsonEventFailure.put("adid", eventFailure.adid == null ? JSONObject.NULL : eventFailure.adid);
            jsonEventFailure.put("timestamp", eventFailure.timestamp == null ? JSONObject.NULL : eventFailure.timestamp);
            jsonEventFailure.put("willRetry", eventFailure.willRetry ? String.valueOf(true) : String.valueOf(false));
            jsonEventFailure.put("callbackId", eventFailure.callbackId == null ? JSONObject.NULL : eventFailure.callbackId);
            jsonEventFailure.put("jsonResponse", eventFailure.jsonResponse == null ? JSONObject.NULL : eventFailure.jsonResponse);
            execNativeCallback(webView, commandName, jsonEventFailure.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void execRemoteTriggerCallbackCommand(final WebView webView, final String commandName, final AdjustRemoteTrigger remoteTrigger) {
        if (webView == null) {
            return;
        }
        if (remoteTrigger == null) {
            return;
        }

        JSONObject jsonRemoteTrigger = new JSONObject();
        try {
            jsonRemoteTrigger.put("label", remoteTrigger.getLabel() == null ? JSONObject.NULL : remoteTrigger.getLabel());
            jsonRemoteTrigger.put("payload", remoteTrigger.getPayload() == null ? JSONObject.NULL : remoteTrigger.getPayload());

            execNativeCallback(webView, commandName, jsonRemoteTrigger.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void execSingleValueCallback(final WebView webView, final String commandName, final String value) {
        execNativeCallback(webView, commandName, value == null ? "null" : JSONObject.quote(value));
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

    public static ILogger getLogger() {
        return AdjustFactory.getLogger();
    }

    public static void sendBridgeTokenToWebView(final WebView webView, final String token) {
        if (webView == null) {
            return;
        }
        if (token == null) {
            return;
        }

        webView.post(new Runnable() {
            @Override
            public void run() {
                String command = "if (window.Adjust && typeof " + SET_BRIDGE_TOKEN_CALLBACK_NAME + " === 'function') { "
                        + SET_BRIDGE_TOKEN_CALLBACK_NAME + "(" + JSONObject.quote(token) + "); }";
                webView.evaluateJavascript(command, null);
            }
        });
    }

    private static void execNativeCallback(final WebView webView, final String commandName, final String payload) {
        if (webView == null) {
            return;
        }
        if (commandName == null) {
            return;
        }

        webView.post(new Runnable() {
            @Override
            public void run() {
                String command = "if (window.Adjust && typeof " + NATIVE_CALLBACK_NAME + " === 'function') { "
                        + NATIVE_CALLBACK_NAME + "(" + JSONObject.quote(commandName) + ", " + payload + "); }";
                webView.evaluateJavascript(command, null);
            }
        });
    }
}
