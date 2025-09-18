package com.adjust.sdk;

public class LicenseData {
    private final String signedData;
    private final String signature;
    private final int responseCode;

    public LicenseData(String signedData, String signature, int responseCode) {
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

    public boolean isValid() {

        if (this.signedData == null || this.signature == null) {
            return false;
        }

        if (this.signedData.isEmpty() || this.signature.isEmpty()){
            return false;
        }
        return true;
    }
}
