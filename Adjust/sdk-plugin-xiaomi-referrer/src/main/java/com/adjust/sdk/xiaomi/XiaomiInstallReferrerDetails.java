package com.adjust.sdk.xiaomi;

import com.miui.referrer.api.GetAppsReferrerDetails;

public class XiaomiInstallReferrerDetails {

    public String installReferrer;                     // The referrer URL of the installed package.
    public long referrerClickTimestampSeconds;         // The client-side timestamp, when the referrer click happened.
    public long installBeginTimestampSeconds;          // The client-side timestamp, when app installation began.
    public long installBeginTimestampServerSeconds;    // The server-side timestamp, when app installation began.
    public long referrerClickTimestampServerSeconds;  // The server-side timestamp, when the referrer click happened.
    public String installVersion;                      // The app's version at the time when the app was first installed.

    public XiaomiInstallReferrerDetails(GetAppsReferrerDetails getAppsReferrerDetails) {
        this.installReferrer = getAppsReferrerDetails.getInstallReferrer();
        this.referrerClickTimestampSeconds = getAppsReferrerDetails.getReferrerClickTimestampSeconds();
        this.installBeginTimestampSeconds = getAppsReferrerDetails.getInstallBeginTimestampSeconds();
        this.installBeginTimestampServerSeconds = getAppsReferrerDetails.getInstallBeginTimestampServerSeconds();
        this.referrerClickTimestampServerSeconds = getAppsReferrerDetails.getReferrerClickTimestampServerSeconds();
        this.installVersion = getAppsReferrerDetails.getInstallVersion();
    }

}
