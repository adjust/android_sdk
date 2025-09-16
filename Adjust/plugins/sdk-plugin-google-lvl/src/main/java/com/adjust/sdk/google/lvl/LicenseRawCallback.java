package com.adjust.sdk.google.lvl;

public interface LicenseRawCallback {
    void onLicenseDataReceived(int responseCode, String signedData, String signature);
    void onError(int errorCode);
}
