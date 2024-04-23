package com.adjust.sdk.meta;

public class MetaInstallReferrerResult {
    public MetaInstallReferrerDetails metaInstallReferrerDetails;
    public String error;

    MetaInstallReferrerResult(MetaInstallReferrerDetails metaInstallReferrerDetails) {
        this.metaInstallReferrerDetails = metaInstallReferrerDetails;
    }

    public MetaInstallReferrerResult(String error) {
        this.error = error;
    }
}
