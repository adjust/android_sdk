package com.adjust.sdk.test;

import android.net.Uri;

import com.adjust.sdk.ActivityPackage;
import com.adjust.sdk.AdjustAttribution;
import com.adjust.sdk.AdjustConfig;
import com.adjust.sdk.AdjustEvent;
import com.adjust.sdk.IActivityHandler;

import org.json.JSONObject;

/**
 * Created by pfms on 09/01/15.
 */
public class MockActivityHandler implements IActivityHandler {
    private MockLogger testLogger;
    private String prefix = "ActivityHandler ";
    boolean updated;
    AdjustConfig config;


    public MockActivityHandler(MockLogger testLogger) {
        this.testLogger = testLogger;
    }

    @Override
    public void init(AdjustConfig config) {
        testLogger.test(prefix + "init");
        this.config = config;
    }

    @Override
    public void trackSubsessionStart() {
        testLogger.test(prefix + "trackSubsessionStart");
    }

    @Override
    public void trackSubsessionEnd() {
        testLogger.test(prefix + "trackSubsessionEnd");
    }

    @Override
    public void trackEvent(AdjustEvent event) {
        testLogger.test(prefix + "trackEvent, " + event);
    }

    @Override
    public void finishedTrackingActivity(JSONObject jsonResponse) {
        testLogger.test(prefix + "finishedTrackingActivity, " + jsonResponse);
    }

    @Override
    public void setEnabled(boolean enabled) {
        testLogger.test(prefix + "setEnabled, " + enabled);
    }

    @Override
    public boolean isEnabled() {
        testLogger.test(prefix + "isEnabled");
        return false;
    }

    @Override
    public void readOpenUrl(Uri url, long clickTime) {
        testLogger.test(prefix + "readOpenUrl, " + url + ". ClickTime, " + clickTime);
    }

    @Override
    public boolean tryUpdateAttribution(AdjustAttribution attribution) {
        testLogger.test(prefix + "tryUpdateAttribution, " + attribution);
        return false;
    }

    @Override
    public void sendReferrer(String referrer, long clickTime) {
        testLogger.test(prefix + "sendReferrer, " + referrer + ". ClickTime, " + clickTime);
    }

    @Override
    public void setOfflineMode(boolean enabled) {
        testLogger.test(prefix + "setOfflineMode, " + enabled);
    }

    @Override
    public void setAskingAttribution(boolean askingAttribution) {
        testLogger.test(prefix + "setAskingAttribution, " + askingAttribution);
    }

    @Override
    public ActivityPackage getAttributionPackage() {
        testLogger.test(prefix + "getAttributionPackage");
        return null;
    }

}
