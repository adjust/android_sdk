package com.adjust.sdk;

import android.net.Uri;

/**
 * Created by pfms on 15/12/14.
 */
public interface IActivityHandler {
    public void init(AdjustConfig config);

    public void trackSubsessionStart();

    public void trackSubsessionEnd();

    public void trackEvent(AdjustEvent event);

    public void finishedTrackingActivity(ResponseData responseData);

    public void setEnabled(boolean enabled);

    public boolean isEnabled();

    public void readOpenUrl(Uri url, long clickTime);

    public void launchResponseTasks(ResponseData responseData);

    public void sendReferrer(String referrer, long clickTime);

    public void setOfflineMode(boolean enabled);

    public void setAskingAttribution(boolean askingAttribution);

    public ActivityPackage getAttributionPackage();
}
