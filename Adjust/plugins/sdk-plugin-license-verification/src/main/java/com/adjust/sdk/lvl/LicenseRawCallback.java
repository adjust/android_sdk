package com.adjust.sdk.lvl;

public interface LicenseRawCallback {
    void onLicenseDataReceived(String signedData, String signature,int responseCode);
    void onError(int errorCode);
}
