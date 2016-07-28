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

    boolean updateAttribution(AdjustAttribution attribution);

    void launchEventResponseTasks(EventResponseData eventResponseData);

    void launchSessionResponseTasks(SessionResponseData sessionResponseData);

    void launchAttributionResponseTasks(AttributionResponseData attributionResponseData);

    void sendReferrer(String referrer, long clickTime);

    void setOfflineMode(boolean enabled);

    void setAskingAttribution(boolean askingAttribution);

    void sendFirstPackages();
}
