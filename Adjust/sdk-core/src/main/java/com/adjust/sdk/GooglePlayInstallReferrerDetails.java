package com.adjust.sdk;

public class GooglePlayInstallReferrerDetails {
    public String installReferrer;                     // The referrer URL of the installed package.
    public long referrerClickTimestampSeconds;         // The client-side timestamp, when the referrer click happened.
    public long installBeginTimestampSeconds;          // The client-side timestamp, when app installation began.
    public long referrerClickTimestampServerSeconds;   // The server-side timestamp, when the referrer click happened.
    public long installBeginTimestampServerSeconds;    // The server-side timestamp, when app installation began.
    public String installVersion;                      // The app's version at the time when the app was first installed.
    public Boolean googlePlayInstant;                  // Indicates whether app's instant experience was launched within the past 7 days.

    GooglePlayInstallReferrerDetails(ReferrerDetails referrerDetails){
        if (referrerDetails == null){
            return;
        }
        this.installReferrer = referrerDetails.installReferrer;
        this.referrerClickTimestampSeconds = referrerDetails.referrerClickTimestampSeconds;
        this.installBeginTimestampSeconds = referrerDetails.installBeginTimestampSeconds;
        this.referrerClickTimestampServerSeconds = referrerDetails.referrerClickTimestampServerSeconds;
        this.installBeginTimestampServerSeconds = referrerDetails.installBeginTimestampServerSeconds;
        this.installVersion = referrerDetails.installVersion;
        this.googlePlayInstant = referrerDetails.googlePlayInstant;
    }

    public String toString() {
        return Util.formatString(
                " installReferrer : %s" +
                        " referrerClickTimestampSeconds : %d" +
                        " installBeginTimestampSeconds : %d" +
                        " referrerClickTimestampServerSeconds : %d" +
                        " installBeginTimestampServerSeconds : %d" +
                        " installVersion : %s" +
                        " googlePlayInstant : %s",
                installReferrer,
                referrerClickTimestampSeconds,
                installBeginTimestampSeconds,
                referrerClickTimestampServerSeconds,
                installBeginTimestampServerSeconds,
                installVersion,
                googlePlayInstant);
    }
}
