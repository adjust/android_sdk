package com.adjust.sdk;

public interface OnGooglePlayInstallReferrerReadListener {
    void onInstallReferrerRead(GooglePlayInstallReferrerDetails referrerDetails);
    void onFail(String message);

}
