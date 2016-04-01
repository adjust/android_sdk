package com.adjust.sdk;

import android.net.Uri;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by pfms on 04/12/14.
 */
public class AdjustInstance {

    private String referrer;
    private long referrerClickTime;
    private ActivityHandler activityHandler;
    private List<Map.Entry<String, String>> sessionCallbackParameters;
    private List<Map.Entry<String, String>> sessionPartnerParameters;

    private static ILogger getLogger() {
        return AdjustFactory.getLogger();
    }

    public void onCreate(AdjustConfig adjustConfig) {
        if (activityHandler != null) {
            getLogger().error("Adjust already initialized");
            return;
        }

        adjustConfig.referrer = this.referrer;
        adjustConfig.referrerClickTime = this.referrerClickTime;
        adjustConfig.sessionCallbackParameters = this.sessionCallbackParameters;
        adjustConfig.sessionPartnerParameters = this.sessionPartnerParameters;

        activityHandler = ActivityHandler.getInstance(adjustConfig);

        // release the complex instances
        this.sessionCallbackParameters = null;
        this.sessionPartnerParameters = null;
    }

    public void trackEvent(AdjustEvent event) {
        if (!checkActivityHandler()) return;
        activityHandler.trackEvent(event);
    }

    public void onResume() {
        if (!checkActivityHandler()) return;
        activityHandler.onResume();
    }

    public void onPause() {
        if (!checkActivityHandler()) return;
        activityHandler.onPause();
    }

    public void setEnabled(boolean enabled) {
        if (!checkActivityHandler()) return;
        activityHandler.setEnabled(enabled);
    }

    public boolean isEnabled() {
        if (!checkActivityHandler()) return false;
        return activityHandler.isEnabled();
    }

    public void appWillOpenUrl(Uri url) {
        if (!checkActivityHandler()) return;
        long clickTime = System.currentTimeMillis();
        activityHandler.readOpenUrl(url, clickTime);
    }

    public void sendReferrer(String referrer) {
        long clickTime = System.currentTimeMillis();
        // sendReferrer might be triggered before Adjust
        if (activityHandler == null) {
            // save it to inject in the config before launch
            this.referrer = referrer;
            this.referrerClickTime = clickTime;
        } else {
            activityHandler.sendReferrer(referrer, clickTime);
        }
    }

    public void setOfflineMode(boolean enabled) {
        if (!checkActivityHandler()) return;
        activityHandler.setOfflineMode(enabled);
    }

    public void addSessionCallbackParameter(String key, String value) {
        if (activityHandler == null) {
            List<Map.Entry<String, String>> sessionCallbackParameters = getSessionCallbackParameters();
            Map.Entry<String, String> entry = new AbstractMap.SimpleEntry<String, String>(key, value);
            sessionCallbackParameters.add(entry);
        } else {
            activityHandler.addSessionCallbackParameter(key, value);
        }
    }

    public void addSessionPartnerParameter(String key, String value) {
        if (activityHandler == null) {
            List<Map.Entry<String, String>> sessionPartnerParameters = getSessionPartnerParameters();
            Map.Entry<String, String> entry = new AbstractMap.SimpleEntry<String, String>(key, value);
            sessionPartnerParameters.add(entry);
        } else {
            activityHandler.addSessionPartnerParameter(key, value);
        }
    }

    public void updateSessionCallbackParameters(SessionCallbackParametersUpdater sessionCallbackParametersUpdater) {
        if (!checkActivityHandler()) return;
        activityHandler.updateSessionCallbackParameters(sessionCallbackParametersUpdater);
    }

    public void updateSessionPartnerParameters(SessionPartnerParametersUpdater sessionPartnerParametersUpdater) {
        if (!checkActivityHandler()) return;
        activityHandler.updateSessionPartnerParameters(sessionPartnerParametersUpdater);
    }

    private boolean checkActivityHandler() {
        if (activityHandler == null) {
            getLogger().error("Adjust not initialized correctly");
            return false;
        } else {
            return true;
        }
    }

    private synchronized List<Map.Entry<String, String>> getSessionCallbackParameters() {
        if (sessionCallbackParameters == null) {
            sessionCallbackParameters = new ArrayList<Map.Entry<String, String>>();
        }
        return sessionCallbackParameters;
    }

    private synchronized List<Map.Entry<String, String>> getSessionPartnerParameters() {
        if (sessionPartnerParameters == null) {
            sessionPartnerParameters = new ArrayList<Map.Entry<String, String>>();
        }
        return sessionPartnerParameters;
    }
}
