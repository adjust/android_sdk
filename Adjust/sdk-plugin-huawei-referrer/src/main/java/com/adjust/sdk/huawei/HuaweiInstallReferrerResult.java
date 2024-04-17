package com.adjust.sdk.huawei;

public class HuaweiInstallReferrerResult {
    public String error;
    public HuaweiInstallReferrerDetails huaweiInstallReferrerDetails;

    HuaweiInstallReferrerResult(HuaweiInstallReferrerDetails huaweiInstallReferrerDetails) {
        this.huaweiInstallReferrerDetails = huaweiInstallReferrerDetails;
    }

    public HuaweiInstallReferrerResult(String error) {
        this.error = error;
    }
}