package com.adjust.sdk.lvl;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.android.vending.licensing.ILicensingService;

public class LicenseChecker {
    private static final String TAG = "LicenseChecker";
    private static final String GOOGLE_PLAY_PACKAGE = "com.android.vending";

    private final Context mContext;
    private final LicenseRawCallback mCallback;
    private String gpsAdid;
    private long installTimeStamp;
    private ILicensingService mService;
    private boolean mBound;

    private static final int MAX_RETRIES = 3;
    private int retryCount = 0;

    public LicenseChecker(Context context, LicenseRawCallback callback, String gpsAdid, long installTimeStamp) {
        this.mContext = context;
        this.mCallback = callback;
        this.gpsAdid = gpsAdid;
        this.installTimeStamp = installTimeStamp;
    }

    public synchronized void checkAccess() {
        if (mBound) return;
        Log.d(TAG, "License check starts 1");
        Intent serviceIntent = new Intent("com.android.vending.licensing.ILicensingService");
        serviceIntent.setPackage(GOOGLE_PLAY_PACKAGE);
        boolean isBind = mContext.bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        Log.d(TAG, "License check bindService = " + isBind);
    }

    private void executeLicenseCheck() {
        try {
            Log.d(TAG, "License check connected" +", retryCount = " + retryCount);
            String packageName = mContext.getPackageName();
            long nonce = generateNonce(gpsAdid, installTimeStamp);

            Log.d(TAG, "License gpsAdid = " + gpsAdid);
            Log.d(TAG, "License installTimeStamp = " + installTimeStamp);
            Log.d(TAG, "License check nonce = " + nonce);
            Log.d(TAG, "License check attempt #" + (retryCount + 1));

            mService.checkLicense(nonce, packageName, new ResultListener());

        } catch (Exception e) {
            Log.e(TAG, "License check failed", e);
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
            Log.d(TAG, "License check disconnected");
            mService = null;
            mBound = false;
        }
    };

    public void onDestroy() {
        Log.d(TAG, "License check onDestroy");
        if (mBound) {
            mContext.unbindService(mServiceConnection);
            mBound = false;
        }
    }

    private class ResultListener extends com.android.vending.licensing.ILicenseResultListener.Stub {
        @Override
        public void verifyLicense(int responseCode, String signedData, String signature) throws RemoteException {
            Log.d(TAG, "Received license response, code: " + responseCode);

            switch (responseCode) {
                case 0: // LICENSED
                case 2: // LICENSED_OLD_KEY
                    retryCount = 0;
                    if (signedData != null && signature != null) {
                        mCallback.onLicenseDataReceived(responseCode, signedData, signature);
                    } else {
                        Log.e(TAG, "Valid response code, but missing signed data or signature");
                        mCallback.onError(responseCode);
                    }
                    break;

                case 1: // NOT_LICENSED
                case 6: // INVALID_PACKAGE_NAME
                case 7: // NON_MATCHING_UID
                    retryCount = 0;
                    Log.w(TAG, "License check failed: " + responseCode);
                    mCallback.onError(responseCode);
                    break;

                case 3: // SERVER_FAILURE
                case 4: // RETRY
                case 5: // CONTACTING_SERVER_ERROR
                    if (++retryCount < MAX_RETRIES) {
                        Log.w(TAG, "Temporary license error, retrying... Attempt " + retryCount);
                        executeLicenseCheck();
                    } else {
                        Log.e(TAG, "License check failed after max retries");
                        mCallback.onError(responseCode);
                    }
                    break;

                default:
                    retryCount = 0;
                    Log.e(TAG, "Unexpected license response code: " + responseCode);
                    mCallback.onError(responseCode);
                    break;
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
}
