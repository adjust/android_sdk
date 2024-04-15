package com.adjust.sdk.huawei;

import com.adjust.sdk.ReferrerDetails;
import com.adjust.sdk.Util;

public class HuaweiInstallReferrerDetails {
    public String installReferrer;                     // The referrer URL of the installed package.
    public long referrerClickTimestampSeconds;         // The client-side timestamp, when the referrer click happened.
    public long installBeginTimestampSeconds;          // The client-side timestamp, when app installation began.

    HuaweiInstallReferrerDetails(ReferrerDetails referrerDetails){
        if (referrerDetails == null){
            return;
        }
        this.installReferrer = referrerDetails.installReferrer;
        this.referrerClickTimestampSeconds = referrerDetails.referrerClickTimestampSeconds;
        this.installBeginTimestampSeconds = referrerDetails.installBeginTimestampSeconds;;
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
