package com.adjust.sdk.google.lvl;

import com.adjust.sdk.ILogger;

public class LicenseResponseHandler {
    private final LicenseRawCallback callback;
    private final ILogger logger;
    private final int maxRetries;
    // Server response codes.
    private static final int LICENSED = 0x0;
    private static final int NOT_LICENSED = 0x1;
    private static final int LICENSED_OLD_KEY = 0x2;
    private static final int ERROR_NOT_MARKET_MANAGED = 0x3;
    private static final int ERROR_SERVER_FAILURE = 0x4;
    private static final int ERROR_OVER_QUOTA = 0x5;

    private static final int ERROR_CONTACTING_SERVER = 0x101;
    private static final int ERROR_INVALID_PACKAGE_NAME = 0x102;
    private static final int ERROR_NON_MATCHING_UID = 0x103;

    public LicenseResponseHandler(LicenseRawCallback callback, ILogger logger, int maxRetries) {
        this.callback = callback;
        this.logger = logger;
        this.maxRetries = maxRetries;
    }

    public boolean handleResponse(int responseCode, String signedData, String signature, int retryCount) {
        switch (responseCode) {
            case NOT_LICENSED: // NOT_LICENSED
            case ERROR_INVALID_PACKAGE_NAME: // INVALID_PACKAGE_NAME
            case ERROR_NON_MATCHING_UID: // NON_MATCHING_UID
            case ERROR_NOT_MARKET_MANAGED: // NOT_MARKET_MANAGED
                logger.warn("LVL license check failed: " + responseCode);
                callback.onError(responseCode);
                return false;

            case ERROR_CONTACTING_SERVER: // CONTACTING_SERVER_ERROR
            case ERROR_SERVER_FAILURE: // SERVER_FAILURE
            case ERROR_OVER_QUOTA: // refusing to talk to this device, over quota
                if (retryCount < maxRetries) {
                    logger.warn("LVL retry attempt [" + (retryCount + 1) + "]: ", responseCode);
                    return true; // signal to retry
                } else {
                    logger.error("LVL retry attempt [" + retryCount + "] failed after max retries: ", responseCode);
                    callback.onError(responseCode);
                    return false;
                }
            case LICENSED: // LICENSED
            case LICENSED_OLD_KEY: // LICENSED_OLD_KEY
                if (signedData != null && signature != null) {
                    callback.onLicenseDataReceived(responseCode, signedData, signature);
                } else {
                    logger.error("LVL missing signed data or signature");
                    callback.onError(responseCode);
                }
                return false;
            default:
                logger.error("LVL unexpected response code: ", responseCode);
                callback.onError(responseCode);
                return false;
        }
    }
}
