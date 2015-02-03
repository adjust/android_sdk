package com.adjust.sdk.test;

import android.net.Uri;

import com.adjust.sdk.ActivityPackage;
import com.adjust.sdk.Attribution;
import com.adjust.sdk.Event;
import com.adjust.sdk.IActivityHandler;

import org.json.JSONObject;

/**
 * Created by pfms on 09/01/15.
 */
public class MockActivityHandler implements IActivityHandler {
    private MockLogger testLogger;
    private String prefix = "ActivityHandler ";
    public boolean updated;

    public MockActivityHandler(MockLogger testLogger) {
        this.testLogger = testLogger;
    }
    @Override
    public void trackSubsessionStart() {
        testLogger.test(prefix +  "trackSubsessionStart");
    }

    @Override
    public void trackSubsessionEnd() {
        testLogger.test(prefix +  "trackSubsessionEnd");
    }

    @Override
    public void trackEvent(Event event) {
        testLogger.test(prefix +  "trackEvent, " + event);
    }

    @Override
    public void finishedTrackingActivity(JSONObject jsonResponse) {
        testLogger.test(prefix +  "finishedTrackingActivity, " + jsonResponse);
    }

    @Override
    public void setEnabled(boolean enabled) {
        testLogger.test(prefix +  "setEnabled, " + enabled);
    }

    @Override
    public boolean isEnabled() {
        testLogger.test(prefix +  "isEnabled");
        return false;
    }

    @Override
    public void readOpenUrl(Uri url) {
        testLogger.test(prefix +  "readOpenUrl, " + url);
    }

    @Override
    public boolean updateAttribution(Attribution attribution) {
        testLogger.test(prefix +  "updateAttribution, " + attribution);
        return updated;
    }

    @Override
    public void launchAttributionDelegate() {
        testLogger.test(prefix +  "launchAttributionDelegate");
    }

    @Override
    public void setReferrer(String referrer) {
        testLogger.test(prefix +  "setReferrer, " + referrer);
    }

    @Override
    public void setOfflineMode(boolean enabled) {
        testLogger.test(prefix +  "setOfflineMode, " + enabled);
    }

    @Override
    public void setAskingAttribution(boolean askingAttribution) {
        testLogger.test(prefix +  "setAskingAttribution, " + askingAttribution);
    }

    @Override
    public ActivityPackage getAttributionPackage(){
        testLogger.test(prefix +  "getAttributionPackage");
        return null;
    }

}
