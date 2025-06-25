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


    private final Context mContext;
    private final LicenseRawCallback mCallback;
    private final ILogger logger;
    private final long installTimeStamp;
    private ILicensingService mService;
    private boolean mBound;

    private static final int MAX_RETRIES = 3;
    private int retryCount = 0;

    public LicenseChecker(Context context, LicenseRawCallback callback, ILogger logger, long installTimeStamp) {
        this.mContext = context;
        this.mCallback = callback;
        this.logger = logger;
        this.installTimeStamp = installTimeStamp;
    }

    public synchronized void checkAccess() {
        if (mBound) return;
        logger.debug("License check starts 1");
        Intent serviceIntent = new Intent("com.android.vending.licensing.ILicensingService");
        serviceIntent.setPackage(GOOGLE_PLAY_PACKAGE);
        boolean isBind = mContext.bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        logger.debug("License check bindService = " + isBind);
    }

    private void executeLicenseCheck() {
        try {
            logger.debug("License check connected" +", retryCount = " + retryCount);
            String packageName = mContext.getPackageName();
            long nonce = generateNonce(installTimeStamp);

            logger.debug("License installTimeStamp = " + installTimeStamp);
            logger.debug("License check nonce = " + nonce);
            logger.debug("License check attempt #" + (retryCount + 1));

            mService.checkLicense(nonce, packageName, new ResultListener());

        } catch (Exception e) {
            logger.error("License check failed", e);
            mCallback.onError(-1);
        }
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName name, IBinder service) {

            mService = ILicensingService.Stub.asInterface(service);
            mBound = true;

            retryCount = 0; // Reset retry count on successful connection
            executeLicenseCheck();
        }


        public void onServiceDisconnected(ComponentName name) {
            logger.debug("License check disconnected");
            mService = null;
            mBound = false;
        }
    };

    public void onDestroy() {
        logger.debug("License check onDestroy");
        if (mBound) {
            mContext.unbindService(mServiceConnection);
            mBound = false;
        }
    }

    private class ResultListener extends com.android.vending.licensing.ILicenseResultListener.Stub {
        @Override
        public void verifyLicense(int responseCode, String signedData, String signature) throws RemoteException {
            logger.debug( "Received license response, code: " + responseCode);

            switch (responseCode) {
                case NOT_LICENSED: // NOT_LICENSED
                case ERROR_INVALID_PACKAGE_NAME: // INVALID_PACKAGE_NAME
                case ERROR_NON_MATCHING_UID: // NON_MATCHING_UID
                case ERROR_NOT_MARKET_MANAGED: // NOT_MARKET_MANAGED
                    logger.warn("License check failed: " + responseCode);
                    mCallback.onError(responseCode);
                    return;

                case ERROR_CONTACTING_SERVER: // CONTACTING_SERVER_ERROR
                case ERROR_SERVER_FAILURE: // SERVER_FAILURE
                case ERROR_OVER_QUOTA: // refusing to talk to this device, over quota
                    if (++retryCount < MAX_RETRIES) {
                        logger.warn("Retry attempt [%d] for response code [%d]" , retryCount, responseCode);
                        executeLicenseCheck();
                    } else {
                        logger.error("License check failed after max retries");
                        mCallback.onError(responseCode);
                    }
                    break;

                case LICENSED: // LICENSED
                case LICENSED_OLD_KEY: // LICENSED_OLD_KEY
                default:
                    if (signedData != null && signature != null) {
                        mCallback.onLicenseDataReceived(responseCode, signedData, signature);
                    } else {
                        logger.error("missing signed data or signature");
                        mCallback.onError(responseCode);
                    }
            }
        }
    }


    public static long generateNonce(String deviceId, long installTimestamp) {
        // Hash the device ID (ADID, UUID, etc.)
        int deviceHash = deviceId != null ? deviceId.hashCode() : 0;

        // Reduce timestamp (e.g., divide by 60 to get minutes)
        long reducedTimestamp = (installTimestamp / 1000) % (1L << 24);  // 24 bits

        // Pack into a long: [deviceHash: 32 bits][timestamp: 24 bits][spare: 8 bits]
        long nonce = 0;
        nonce |= (deviceHash & 0xFFFFFFFFL) << 32;
        nonce |= (reducedTimestamp & 0xFFFFFFL) << 8;
        nonce |= 0x01; // Version or flag, optional

        return nonce;
    }

    public static long generateNonce(long installTimestamp) {
        // Reduce timestamp (e.g., seconds since epoch / 60 to reduce size)
        long reducedTimestamp = (installTimestamp / 1000) % (1L << 24); // 24 bits

        // Pack into a long: [reserved: 32 bits][timestamp: 24 bits][version/flags: 8 bits]
        long nonce = 0;
        nonce |= (reducedTimestamp & 0xFFFFFFL) << 8;  // bits 8–31
        nonce |= 0x01; // version in the lowest 8 bits

        return nonce;
    }
}
