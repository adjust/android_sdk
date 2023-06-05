package com.adjust.sdk;

public class AdjustPurchaseVerificationResult {
    private final String verificationStatus;
    private final int code;
    private final String message;

    public AdjustPurchaseVerificationResult(final String verificationStatus,
                                            final int code,
                                            final String message) {
        this.verificationStatus = verificationStatus;
        this.code = code;
        this.message = message;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
