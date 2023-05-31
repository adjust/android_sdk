package com.adjust.sdk;

public class AdjustPurchaseVerificationResult {
    private String verificationStatus;
    private int code;
    private String message;

    public AdjustPurchaseVerificationResult(final String verificationStatus,
                                            final int code,
                                            final String message) {
        this.verificationStatus = verificationStatus;
        this.code = code;
        this.message = message;
    }
}
