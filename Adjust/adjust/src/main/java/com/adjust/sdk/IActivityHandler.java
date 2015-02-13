package com.adjust.sdk;

import android.net.Uri;

import org.json.JSONObject;

/**
 * Created by pfms on 15/12/14.
 */
public interface IActivityHandler {
    public void trackSubsessionStart();

    public void trackSubsessionEnd();

    public void trackEvent(Event event);

    public void finishedTrackingActivity(JSONObject jsonResponse);

    public void setEnabled(boolean enabled);

    public boolean isEnabled();

    public void readOpenUrl(Uri url, long clickTime);

    public void updateAttribution(Attribution attribution);

    public void setReferrer(String referrer, long clickTime);

    public void setOfflineMode(boolean enabled);

    public void setAskingAttribution(boolean askingAttribution);

    public ActivityPackage getAttributionPackage();
}
