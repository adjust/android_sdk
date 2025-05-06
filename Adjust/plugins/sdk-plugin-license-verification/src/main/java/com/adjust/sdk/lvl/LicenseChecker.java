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
    private ILicensingService mService;
    private boolean mBound ;

    public LicenseChecker(Context context, LicenseRawCallback callback) {
        this.mContext = context.getApplicationContext();
        this.mCallback = callback;
    }

    public void checkAccess() {
        if (mBound) return;

        Log.d(TAG, "License check starts 1");
        Intent serviceIntent = new Intent("com.android.vending.licensing.ILicensingService.BIND");
        serviceIntent.setPackage(GOOGLE_PLAY_PACKAGE);
        boolean isBind  = mContext.bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        Log.d(TAG, "License check bindService = " + isBind);
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "License check starts 3");

            mService = ILicensingService.Stub.asInterface(service);
            mBound = true;

            try {
                String packageName = mContext.getPackageName();
//                int versionCode = mContext.getPackageManager()
//                        .getPackageInfo(packageName, 0).versionCode;
                Log.d(TAG, "License check starts");

                mService.checkLicense(1, packageName, new ResultListener());

            } catch (Exception e) {
                Log.e(TAG, "License check failed", e);
                mCallback.onError(-1);
            }
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
            Log.d(TAG, "Received license response");
            if (signedData != null && signature != null) {
                mCallback.onLicenseDataReceived(signedData, signature,responseCode);
            } else {
                mCallback.onError(responseCode);
            }
        }
    }
}
