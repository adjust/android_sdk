package com.adjust.sdk.samsung;

public interface OnSamsungInstallReferrerReadListener {
    void onSamsungInstallReferrerRead(SamsungInstallReferrerDetails referrerDetails);
    void onFail(String message);
}
