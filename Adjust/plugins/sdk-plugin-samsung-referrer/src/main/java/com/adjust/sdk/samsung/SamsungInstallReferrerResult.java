package com.adjust.sdk.samsung;

public class SamsungInstallReferrerResult {
    public String error;
    public SamsungInstallReferrerDetails samsungInstallReferrerDetails;

    SamsungInstallReferrerResult(SamsungInstallReferrerDetails samsungInstallReferrerDetails) {
        this.samsungInstallReferrerDetails = samsungInstallReferrerDetails;
    }

    public SamsungInstallReferrerResult(String error) {
        this.error = error;
    }
}
