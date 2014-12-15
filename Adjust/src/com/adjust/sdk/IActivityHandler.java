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
    public void setEnabled(Boolean enabled);
    public boolean isEnabled();
    public void readOpenUrl(Uri url);
    public void updateAttribution(Attribution attribution);
    public void launchAttributionDelegate();
    public void setReferrer(String referrer);
    public void setOfflineMode(boolean enabled);

}
