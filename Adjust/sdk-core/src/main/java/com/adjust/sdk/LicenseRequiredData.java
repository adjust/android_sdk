package com.adjust.sdk;

public class LicenseRequiredData {
    private final String signedData;
    private final String signature;
    private final int responseCode;

    public LicenseRequiredData(String signedData, String signature, int responseCode) {
        this.signedData = signedData;
        this.signature = signature;
        this.responseCode = responseCode;
    }

    public String getSignedData() {
        return signedData;
    }

    public String getSignature() {
        return signature;
    }

    public int getResponseCode() {
        return responseCode;
    }
}
