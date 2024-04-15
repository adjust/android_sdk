package com.adjust.sdk.vivo;

import com.adjust.sdk.ReferrerDetails;
import com.adjust.sdk.Util;

public class VivoInstallReferrerDetails {

    public String installReferrer;                     // The referrer URL of the installed package.
    public long referrerClickTimestampSeconds;         // The client-side timestamp, when the referrer click happened.
    public long installBeginTimestampSeconds;          // The client-side timestamp, when app installation began.
    public String installVersion;                      // The app's version at the time when the app was first installed.

    VivoInstallReferrerDetails(ReferrerDetails referrerDetails){
        if (referrerDetails == null){
            return;
        }
        this.installReferrer = referrerDetails.installReferrer;
        this.referrerClickTimestampSeconds = referrerDetails.referrerClickTimestampSeconds;
        this.installBeginTimestampSeconds = referrerDetails.installBeginTimestampSeconds;
        this.installVersion = referrerDetails.installVersion;
    }

    public String toString() {
        return Util.formatString(
                " installReferrer : %s" +
                        " referrerClickTimestampSeconds : %d" +
                        " installBeginTimestampSeconds : %d" +
                        " installVersion : %s",
                installReferrer,
                referrerClickTimestampSeconds,
                installBeginTimestampSeconds,
                installVersion);
    }
}
