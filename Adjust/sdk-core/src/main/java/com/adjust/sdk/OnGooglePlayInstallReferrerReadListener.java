package com.adjust.sdk;

public interface OnGooglePlayInstallReferrerReadListener {
    void onInstallReferrerRead(ReferrerDetails referrerDetails);
    void onFailure(String message);

}
