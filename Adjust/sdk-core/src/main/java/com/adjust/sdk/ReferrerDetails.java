package com.adjust.sdk;

class ReferrerDetails {
    public String installReferrer;
    public long referrerClickTimestampSeconds;
    public long installBeginTimestampSeconds;
    public long referrerClickServerTimestampSeconds;
    public long installBeginServerTimestampSeconds;
    public String installVersion;
    public Boolean googlePlayInstant;

    public ReferrerDetails(final String installReferrer,
                           final long referrerClickTimestampSeconds,
                           final long installBeginTimestampSeconds,
                           final long referrerClickServerTimestampSeconds,
                           final long installBeginServerTimestampSeconds,
                           final String installVersion,
                           final Boolean googlePlayInstant) {

        this.installReferrer = installReferrer;
        this.referrerClickTimestampSeconds = referrerClickTimestampSeconds;
        this.installBeginTimestampSeconds = installBeginTimestampSeconds;
        this.referrerClickServerTimestampSeconds = referrerClickServerTimestampSeconds;
        this.installBeginServerTimestampSeconds = installBeginServerTimestampSeconds;
        this.installVersion = installVersion;
        this.googlePlayInstant = googlePlayInstant;
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
                null);

    }

}
