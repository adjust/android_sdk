package com.adjust.sdk.meta;

import com.adjust.sdk.Util;

public class MetaInstallReferrerDetails {
    public String installReferrer;     // The referrer URL of the installed package.
    public long actualTimestampInSec;  // The actual timestamp in seconds.
    public boolean isClick;            // Whether its click or not.

    MetaInstallReferrerDetails(String installReferrer, long actualTimestampInSec, boolean isClick) {
        this.installReferrer = installReferrer;
        this.actualTimestampInSec = actualTimestampInSec;
        this.isClick = isClick;
    }

    public String toString() {
        return Util.formatString(
                " installReferrer : %s" +
                        " actualTimestampInSec : %d" +
                        " isClick : %b",
                installReferrer,
                actualTimestampInSec,
                isClick);
    }}
