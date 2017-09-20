package com.adjust.sdk;

import android.content.Context;
import android.net.Uri;


/**
 * Created by pfms on 09/01/15.
 */
public class MockActivityHandler implements IActivityHandler {
    private MockLogger testLogger;
    private String prefix = "ActivityHandler ";
    private AdjustConfig config;
    private ResponseData lastResponseData;

    public MockActivityHandler(MockLogger testLogger) {
        this.testLogger = testLogger;
    }

    @Override
    public void init(AdjustConfig config) {
        testLogger.test(prefix + "init");
        this.config = config;
    }

    @Override
    public void onResume() {
        testLogger.test(prefix + "onResume");
    }

    @Override
    public void onPause() {
        testLogger.test(prefix + "onPause");
    }

    @Override
    public void trackEvent(AdjustEvent event) {
        testLogger.test(prefix + "trackEvent, " + event);
    }

    @Override
    public void finishedTrackingActivity(ResponseData responseData) {
        testLogger.test(prefix + "finishedTrackingActivity, " + responseData);
        this.lastResponseData = responseData;
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
    public boolean updateAttributionI(AdjustAttribution attribution) {
        testLogger.test(prefix + "updateAttributionI, " + attribution);
        return false;
    }

    @Override
    public void launchEventResponseTasks(EventResponseData eventResponseData) {
        testLogger.test(prefix + "launchEventResponseTasks, " + eventResponseData);
        this.lastResponseData = eventResponseData;
    }

    @Override
    public void launchSessionResponseTasks(SessionResponseData sessionResponseData) {
        testLogger.test(prefix + "launchSessionResponseTasks, " + sessionResponseData);
        this.lastResponseData = sessionResponseData;
    }

    @Override
    public void launchSdkClickResponseTasks(SdkClickResponseData sdkClickResponseData) {
        testLogger.test(prefix + "launchSdkClickResponseTasks, " + sdkClickResponseData);
        this.lastResponseData = sdkClickResponseData;
    }

    @Override
    public void launchAttributionResponseTasks(AttributionResponseData attributionResponseData) {
        testLogger.test(prefix + "launchAttributionResponseTasks, " + attributionResponseData);
        this.lastResponseData = attributionResponseData;
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
    public void sendFirstPackages() {
        testLogger.test(prefix + "sendFirstPackages");
    }

    @Override
    public void addSessionCallbackParameter(String key, String value) {
        testLogger.test(prefix + "addSessionCallbackParameter key, " + key + ", value, " + value);
    }

    @Override
    public void addSessionPartnerParameter(String key, String value) {
        testLogger.test(prefix + "addSessionPartnerParameter key, " + key + ", value, " + value);
    }

    @Override
    public void removeSessionCallbackParameter(String key) {
        testLogger.test(prefix + "removeSessionCallbackParameter, " + key);
    }

    @Override
    public void removeSessionPartnerParameter(String key) {
        testLogger.test(prefix + "removeSessionPartnerParameter, " + key);
    }

    @Override
    public void resetSessionCallbackParameters() {
        testLogger.test(prefix + "resetSessionCallbackParameters");
    }

    @Override
    public void resetSessionPartnerParameters() {
        testLogger.test(prefix + "resetSessionPartnerParameters");
    }

    @Override
    public void teardown(boolean deleteState) {
        testLogger.test(prefix + "teardown deleteState, " + deleteState);
    }

    @Override
    public void setPushToken(String token, boolean preSaved) {

    }

    @Override
    public Context getContext() {
        return null;
    }

    @Override
    public String getAdid() {
        return null;
    }

    @Override
    public AdjustAttribution getAttribution() {
        return null;
    }

    @Override
    public String getBasePath() {
        return null;
    }

    public void setPushToken(String token) {
        testLogger.test(prefix + "setPushToken token, " + token);
    }
}
