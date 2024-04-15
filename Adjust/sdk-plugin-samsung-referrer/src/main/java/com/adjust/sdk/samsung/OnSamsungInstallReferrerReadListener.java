package com.adjust.sdk.samsung;

public interface OnSamsungInstallReferrerReadListener {
    void onInstallReferrerRead(SamsungInstallReferrerDetails referrerDetails);
    void onFail(String message);
}
