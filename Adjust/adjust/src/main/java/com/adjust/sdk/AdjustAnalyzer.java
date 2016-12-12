package com.adjust.sdk;

import android.os.*;
import android.util.*;

import org.json.*;

import java.io.*;
import java.net.*;
import java.util.*;

import static android.content.ContentValues.TAG;
import static com.adjust.sdk.AdjustFactory.getActivityHandler;
import static com.adjust.sdk.Constants.STATE_ALLOW_SUPPRESS_LOG_LEVEL;
import static com.adjust.sdk.Constants.STATE_APP_TOKEN;
import static com.adjust.sdk.Constants.STATE_BACKGROUND_ENABLED;
import static com.adjust.sdk.Constants.STATE_DEFAULT_TRACKER;
import static com.adjust.sdk.Constants.STATE_DELAY_START;
import static com.adjust.sdk.Constants.STATE_ENVIRONMENT;
import static com.adjust.sdk.Constants.STATE_IS_ATTRIBUTION_CALLBACK_IMPLEMENNTED;
import static com.adjust.sdk.Constants.STATE_IS_DEFERRED_DEEPLINK_CALLBACK_IMPLEMENTED;
import static com.adjust.sdk.Constants.STATE_IS_EVENT_TRACKING_FAILED_CALLBACK_IMPLEMENTED;
import static com.adjust.sdk.Constants.STATE_IS_EVENT_TRACKING_SUCCEEDED_CALLBACK_IMPLEMENTED;
import static com.adjust.sdk.Constants.STATE_IS_SESSION_TRACKING_FAILED_CALLBACK_IMPLEMENTED;
import static com.adjust.sdk.Constants.STATE_IS_SESSION_TRACKING_SUCCEEDED_CALLBACK_IMPLEMENTED;
import static com.adjust.sdk.Constants.STATE_PROCESS_NAME;
import static com.adjust.sdk.Constants.STATE_REFERRER;
import static com.adjust.sdk.Constants.STATE_SDK_PREFIX;
import static com.adjust.sdk.Constants.STATE_USER_AGENT;

/**
 * Created by ab on 01/12/2016.
 */

public final class AdjustAnalyzer {
    private static Socket analyzerSocket = null;
    private static SavedAdjustConfigState savedAdjustConfigState = null;

    public static class SavedAdjustConfigState implements IStateable {
        public SavedAdjustConfigState(AdjustConfig adjustConfig) {
            appToken = adjustConfig.appToken;
            environment = adjustConfig.environment;
            processName = adjustConfig.processName;
            sdkPrefix = adjustConfig.sdkPrefix;
            eventBufferingEnabled = adjustConfig.eventBufferingEnabled;
            defaultTracker = adjustConfig.defaultTracker;
            referrer = adjustConfig.referrer;
            referrerClickTime = adjustConfig.referrerClickTime;
            deviceKnown = adjustConfig.deviceKnown;
            isOnAttributionChangedListenerSet = adjustConfig.onAttributionChangedListener != null;
            isOnEventTrackingSucceededListenerSet = adjustConfig.onEventTrackingSucceededListener != null;
            isOnEventTrackingFailedListenerSet = adjustConfig.onEventTrackingFailedListener != null;
            isOnSessionTrackingSucceededListenerSet = adjustConfig.onSessionTrackingSucceededListener != null;
            isOnSessionTrackingFailedListenerSet = adjustConfig.onSessionTrackingFailedListener != null;
            isOnDeeplinkResponseListenerSet = adjustConfig.onDeeplinkResponseListener != null;
            sendInBackground = adjustConfig.sendInBackground;
            delayStart = adjustConfig.delayStart;
            allowSuppressLogLevel = adjustConfig.allowSuppressLogLevel;
            userAgent = adjustConfig.userAgent;
        }

        private String appToken;
        private String environment;
        private String processName;
        private String sdkPrefix;
        private boolean eventBufferingEnabled;
        private String defaultTracker;
        private String referrer;
        private long referrerClickTime;
        private Boolean deviceKnown;
        private boolean isOnAttributionChangedListenerSet;
        private boolean isOnEventTrackingSucceededListenerSet;
        private boolean isOnEventTrackingFailedListenerSet;
        private boolean isOnSessionTrackingSucceededListenerSet;
        private boolean isOnSessionTrackingFailedListenerSet;
        private boolean isOnDeeplinkResponseListenerSet;
        private boolean sendInBackground;
        private Double delayStart;
        private boolean allowSuppressLogLevel;
        private String userAgent;

        @Override
        public Map<String, Object> getState() {
            Map<String, Object> data = new HashMap<>();
            data.put(STATE_DEFAULT_TRACKER, defaultTracker);
            data.put(STATE_IS_ATTRIBUTION_CALLBACK_IMPLEMENNTED, isOnAttributionChangedListenerSet);
            data.put(STATE_IS_EVENT_TRACKING_SUCCEEDED_CALLBACK_IMPLEMENTED, isOnEventTrackingSucceededListenerSet);
            data.put(STATE_IS_EVENT_TRACKING_FAILED_CALLBACK_IMPLEMENTED, isOnEventTrackingFailedListenerSet);
            data.put(STATE_IS_SESSION_TRACKING_SUCCEEDED_CALLBACK_IMPLEMENTED, isOnSessionTrackingSucceededListenerSet);
            data.put(STATE_IS_SESSION_TRACKING_FAILED_CALLBACK_IMPLEMENTED, isOnSessionTrackingFailedListenerSet);
            data.put(STATE_IS_DEFERRED_DEEPLINK_CALLBACK_IMPLEMENTED, isOnDeeplinkResponseListenerSet);
            data.put(STATE_ALLOW_SUPPRESS_LOG_LEVEL, allowSuppressLogLevel);
            data.put(STATE_USER_AGENT, userAgent);
            data.put(STATE_APP_TOKEN, appToken);
            data.put(STATE_ENVIRONMENT, environment);
            data.put(STATE_PROCESS_NAME, processName);
            data.put(STATE_BACKGROUND_ENABLED, sendInBackground);
            data.put(STATE_SDK_PREFIX, sdkPrefix);
            data.put(STATE_REFERRER, referrer);
            data.put(STATE_DELAY_START, delayStart);

            return data;
        }
    }

    private AdjustAnalyzer() {

    }

    public static void connectToAnalyzer(String host, String port, AdjustConfig adjustConfig) {
        if (analyzerSocket != null) {
            AdjustFactory.getLogger().warn("Analyzer socket is already open");
            return;
        }

        if (adjustConfig == null) {
            AdjustFactory.getLogger().warn("adjustConfig is null");
            return;
        }

        //Save the state of Adjust Config
        savedAdjustConfigState = new SavedAdjustConfigState(adjustConfig);

        new AsyncTask<String, Void, Void>() {
            @Override
            protected Void doInBackground(String... params) {
                try {
                    analyzerSocket = new Socket(
                            params[0],
                            Integer.parseInt(params[1]));

                    Log.e(TAG, "doInBackground: analyzerSocket: " + analyzerSocket);
                } catch (IOException e) {
                    Log.e(TAG, "connectToAnalyzer: " + e.getLocalizedMessage());
                }
                return null;
            }
        }.execute(host, port);
    }

    public static void shutdownAnalyzer() {
        if (analyzerSocket == null) {
            AdjustFactory.getLogger().warn("Analyzer socket is null");
            return;
        }

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    analyzerSocket.close();
                } catch (IOException e) {
                    Log.e(TAG, "connectToAnalyzer: " + e.getLocalizedMessage());
                }
                return null;
            }
        }.execute();
    }

    public static void reportState(String callsite) {
        if (analyzerSocket == null) {
            AdjustFactory.getLogger().warn("Analyzer socket is null");
            return;
        }

        Map<String, Object> map = new TreeMap<>();
        map.put("call_site", callsite);
        map.putAll(savedAdjustConfigState.getState());

        ActivityHandler activityHandler = (ActivityHandler) getActivityHandler();
        if (activityHandler == null) {
            AdjustFactory.getLogger().error("activity handler is null. Couldn't report");
            return;
        }
        map.putAll(activityHandler.getState());


        String json = new JSONObject(map).toString();

        new AsyncTask<String, Void, Void>() {
            @Override
            protected Void doInBackground(String... params) {
                try {
                    OutputStream out = analyzerSocket.getOutputStream();
                    out.write((params[0] + '\n').getBytes());
//                InputStream in = socket.getInputStream();
//                byte buf[] = new byte[1024];
//                int nbytes;
//                while ((nbytes = in.read(buf)) != -1) {
//                    sb.append(new String(buf, 0, nbytes));
//                }
                } catch (IOException ignored) {
                    Log.e(TAG, "doInBackground: " + ignored.getLocalizedMessage());
                }

                return null;
            }
        }.execute(json);
    }

}
