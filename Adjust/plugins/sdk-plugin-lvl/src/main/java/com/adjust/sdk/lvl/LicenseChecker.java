package com.adjust.sdk.lvl;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.adjust.sdk.ILogger;
import com.android.vending.licensing.ILicensingService;

public class LicenseChecker {
    private static final String GOOGLE_PLAY_PACKAGE = "com.android.vending";

    private final Context mContext;
    private final LicenseRawCallback mCallback;
    private final ILogger logger;
    private final long timestamp;

    private ILicensingService mService;
    private boolean mBound;
    private int retryCount = 0;
    private static final int MAX_RETRIES = 3;

    public LicenseChecker(Context context, LicenseRawCallback callback, ILogger logger, long timestamp) {
        this.mContext = context;
        this.mCallback = callback;
        this.logger = logger;
        this.timestamp = timestamp;
    }

    public synchronized void checkAccess() {
        if (mBound) {
            return;
        }
        logger.debug("[LicenseVerification] License check starts");

        Intent serviceIntent = new Intent("com.android.vending.licensing.ILicensingService");
        serviceIntent.setPackage(GOOGLE_PLAY_PACKAGE);
        boolean isBind = mContext.bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        logger.debug("[LicenseVerification] bindService result: " + isBind);
    }

    public void onDestroy() {
        logger.debug("[LicenseVerification] LicenseChecker destroyed");
        if (mBound) {
            mContext.unbindService(mServiceConnection);
            mBound = false;
        }
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            logger.debug("[LicenseVerification] Service connected");
            mService = ILicensingService.Stub.asInterface(service);
            mBound = true;
            retryCount = 0;
            executeLicenseCheck();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            logger.debug("[LicenseVerification] Service disconnected");
            mService = null;
            mBound = false;
        }
    };

    private void executeLicenseCheck() {
        try {
            String packageName = mContext.getPackageName();
            long nonce = generateNonce(timestamp);
            logger.debug("[LicenseVerification] Generated nonce: " + nonce);

            mService.checkLicense(nonce, packageName, new ResultListener());
        } catch (Exception e) {
            logger.error("[LicenseVerification] License check failed", e);
            mCallback.onError(-1);
        }
    }

    private class ResultListener extends com.android.vending.licensing.ILicenseResultListener.Stub {
        @Override
        public void verifyLicense(int responseCode, String signedData, String signature) throws RemoteException {
            logger.debug("[LicenseVerification] Received license response: " + responseCode);

            LicenseResponseHandler handler = new LicenseResponseHandler(mCallback, logger, MAX_RETRIES);
            boolean shouldRetry = handler.handleResponse(responseCode, signedData, signature, retryCount);
            if (shouldRetry) {
                retryCount++;
                logger.debug("[LicenseVerification] Retrying license check... Attempt " + retryCount);
                executeLicenseCheck();
            } else {
                onDestroy();
            }
        }
    }

    /**
     * Generates a 64-bit nonce with:
     * - 56-bit reduced timestamp (in seconds) stored in bits 8–63
     * - 8-bit reserved field in bits 0–7 (currently using 0x01 as version marker)
     */
    public static long generateNonce(long timestamp) {
        // Mask to ensure we only keep the lowest 56 bits
        long MASK_56_BITS = 0x00FFFFFFFFFFFFFFL;
        long reducedTimestamp = timestamp & MASK_56_BITS;

        // Shift timestamp to occupy bits 8–63, reserve LSB for flags/version
        return (reducedTimestamp << 8) | 0x01;
    }

}
