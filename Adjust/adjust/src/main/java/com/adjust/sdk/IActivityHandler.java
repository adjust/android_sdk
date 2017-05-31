package com.adjust.sdk;

import android.net.Uri;

/**
 * Created by pfms on 15/12/14.
 */
public interface IActivityHandler {
    void init(AdjustConfig config);

    void onResume();

    void onPause();

    void trackEvent(AdjustEvent event);

    void finishedTrackingActivity(ResponseData responseData);

    void setEnabled(boolean enabled);

    boolean isEnabled();

    void readOpenUrl(Uri url, long clickTime);

    boolean updateAttributionI(AdjustAttribution attribution);

    void launchEventResponseTasks(EventResponseData eventResponseData);

    void launchSessionResponseTasks(SessionResponseData sessionResponseData);

    void launchSdkClickResponseTasks(SdkClickResponseData sdkClickResponseData);

    void launchAttributionResponseTasks(AttributionResponseData attributionResponseData);

    void sendReferrer(String referrer, long clickTime);

    void setOfflineMode(boolean enabled);

    void setAskingAttribution(boolean askingAttribution);

    void sendFirstPackages();

    void addSessionCallbackParameter(String key, String value);

    void addSessionPartnerParameter(String key, String value);

    void removeSessionCallbackParameter(String key);

    void removeSessionPartnerParameter(String key);

    void resetSessionCallbackParameters();

    void resetSessionPartnerParameters();

    void teardown(boolean deleteState);

    void setPushToken(String token);

    String getAdid();

    AdjustAttribution getAttribution();

    String getBasePath();
}
