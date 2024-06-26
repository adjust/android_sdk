package com.adjust.sdk.xiaomi;

public class XiaomiInstallReferrerResult {

    public String error;
    public XiaomiInstallReferrerDetails xiaomiInstallReferrerDetails;

    XiaomiInstallReferrerResult(XiaomiInstallReferrerDetails xiaomiInstallReferrerDetails) {
        this.xiaomiInstallReferrerDetails = xiaomiInstallReferrerDetails;
    }

    public XiaomiInstallReferrerResult(String error) {
        this.error = error;
    }
}
