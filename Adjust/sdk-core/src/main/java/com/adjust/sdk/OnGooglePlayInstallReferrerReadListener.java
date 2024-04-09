package com.adjust.sdk;

public interface OnGooglePlayInstallReferrerReadListener {
    void onInstallReferrerRead(ReferrerDetails referrerDetails, String referrerApi);
    void onFail(String message);

}
