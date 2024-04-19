package com.adjust.sdk.samsung;

import com.adjust.sdk.ReferrerDetails;
import com.adjust.sdk.Util;

public class SamsungInstallReferrerDetails {
    public String installReferrer;                     // The referrer URL of the installed package.
    public long referrerClickTimestampSeconds;         // The client-side timestamp, when the referrer click happened.
    public long installBeginTimestampSeconds;          // The client-side timestamp, when app installation began.

    SamsungInstallReferrerDetails(String installReferrer, long referrerClickTimestampSeconds, long installBeginTimestampSeconds){
        this.installReferrer = installReferrer;
        this.referrerClickTimestampSeconds = referrerClickTimestampSeconds;
        this.installBeginTimestampSeconds = installBeginTimestampSeconds;
    }

    public String toString() {
        return Util.formatString(
                " installReferrer : %s" +
                        " referrerClickTimestampSeconds : %d" +
                        " installBeginTimestampSeconds : %d",
                installReferrer,
                referrerClickTimestampSeconds,
                installBeginTimestampSeconds);
    }
}
