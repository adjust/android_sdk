package com.adjust.sdk;

public class ReferrerDetails {
    public String installReferrer;                     // The referrer URL of the installed package.
    public long referrerClickTimestampSeconds;         // The client-side timestamp, when the referrer click happened.
    public long installBeginTimestampSeconds;          // The client-side timestamp, when app installation began.
    public long referrerClickTimestampServerSeconds;   // The server-side timestamp, when the referrer click happened.
    public long installBeginTimestampServerSeconds;    // The server-side timestamp, when app installation began.
    public String installVersion;                      // The app's version at the time when the app was first installed.
    public Boolean googlePlayInstant;                  // Indicates whether app's instant experience was launched within the past 7 days.
    public Boolean isClick;                            // Indicates click (true) or impression (false)

    public ReferrerDetails(final String installReferrer,
                           final long referrerClickTimestampSeconds,
                           final long installBeginTimestampSeconds,
                           final long referrerClickTimestampServerSeconds,
                           final long installBeginTimestampServerSeconds,
                           final String installVersion,
                           final Boolean googlePlayInstant,
                           final Boolean isClick) {

        this.installReferrer = installReferrer;
        this.referrerClickTimestampSeconds = referrerClickTimestampSeconds;
        this.installBeginTimestampSeconds = installBeginTimestampSeconds;
        this.referrerClickTimestampServerSeconds = referrerClickTimestampServerSeconds;
        this.installBeginTimestampServerSeconds = installBeginTimestampServerSeconds;
        this.installVersion = installVersion;
        this.googlePlayInstant = googlePlayInstant;
        this.isClick = isClick;
    }

    public ReferrerDetails(final String installReferrer,
                           final long referrerClickTimestampSeconds,
                           final long installBeginTimestampSeconds) {

        this(installReferrer,
                referrerClickTimestampSeconds,
                installBeginTimestampSeconds,
                -1,
                -1,
                null,
                null,
                null);

    }

    public ReferrerDetails(final String installReferrer,
                           final long referrerClickTimestampSeconds,
                           final Boolean isClick) {

        this(installReferrer,
                referrerClickTimestampSeconds,
                -1,
                -1,
                -1,
                null,
                null,
                isClick);

    }

    public String toString() {
        return Util.formatString(
                " installReferrer : %s" +
                " referrerClickTimestampSeconds : %d" +
                " installBeginTimestampSeconds : %d" +
                " referrerClickTimestampServerSeconds : %d" +
                " installBeginTimestampServerSeconds : %d" +
                " installVersion : %s" +
                " googlePlayInstant : %s" +
                " isClick: %s",
                installReferrer,
                referrerClickTimestampSeconds,
                installBeginTimestampSeconds,
                referrerClickTimestampServerSeconds,
                installBeginTimestampServerSeconds,
                installVersion,
                googlePlayInstant,
                isClick);
    }
}
