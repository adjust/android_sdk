package com.adjust.sdk;

public interface InstallReferrerReadListener {
    void onInstallReferrerRead(ReferrerDetails referrerDetails, String referrerApi);
    void onFail(String message);

}
