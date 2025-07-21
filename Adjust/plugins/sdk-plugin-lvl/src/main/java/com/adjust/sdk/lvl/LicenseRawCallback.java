package com.adjust.sdk.lvl;

public interface LicenseRawCallback {
    void onLicenseDataReceived(int responseCode, String signedData, String signature);
    void onError(int errorCode);
}
