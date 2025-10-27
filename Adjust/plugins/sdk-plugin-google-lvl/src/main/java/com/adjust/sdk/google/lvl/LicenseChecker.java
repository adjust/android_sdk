package com.adjust.sdk.google.lvl;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

import com.adjust.sdk.ILogger;

public class LicenseChecker {
    private static final String GOOGLE_PLAY_PACKAGE = "com.android.vending";
    private static final String LICENSING_SERVICE_DESCRIPTOR = "com.android.vending.licensing.ILicensingService";
    private static final String RESULT_LISTENER_DESCRIPTOR = "com.android.vending.licensing.ILicenseResultListener";
    private static final int TRANSACTION_CHECK_LICENSE = IBinder.FIRST_CALL_TRANSACTION;

    private final Context mContext;
    private final LicenseRawCallback mCallback;
    private final ILogger logger;
    private final long timestamp;
    private final ResultListenerBinder resultListenerBinder = new ResultListenerBinder();

    private IBinder mServiceBinder;
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
        logger.debug("LVL License check starts");

        Intent serviceIntent = new Intent("com.android.vending.licensing.ILicensingService");
        serviceIntent.setPackage(GOOGLE_PLAY_PACKAGE);
        boolean isBind = mContext.bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        logger.debug("LVL bindService result: " + isBind);
    }

    public void onDestroy() {
        logger.debug("LVL license checker destroyed");
        if (mBound) {
            mContext.unbindService(mServiceConnection);
            mBound = false;
        }
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            logger.debug("LVL service connected");
            mServiceBinder = service;
            mBound = true;
            retryCount = 0;
            executeLicenseCheck();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            logger.debug("LVL service disconnected");
            mServiceBinder = null;
            mBound = false;
        }
    };

    private void executeLicenseCheck() {
        try {
            if (mServiceBinder == null) {
                logger.error("LVL binder unavailable for license check");
                mCallback.onError(-1);
                return;
            }

            String packageName = mContext.getPackageName();
            long nonce = generateNonce(timestamp);
            logger.debug("LVL generated nonce: " + nonce);

            Parcel data = Parcel.obtain();
            try {
                data.writeInterfaceToken(LICENSING_SERVICE_DESCRIPTOR);
                data.writeLong(nonce);
                data.writeString(packageName);
                data.writeStrongBinder(resultListenerBinder);
                boolean transacted = mServiceBinder.transact(
                        TRANSACTION_CHECK_LICENSE,
                        data,
                        null,
                        IBinder.FLAG_ONEWAY
                );
                logger.debug("LVL binder transact sent (code " + TRANSACTION_CHECK_LICENSE + "): " + transacted);
                if (!transacted) {
                    logger.error("LVL binder transact failed to enqueue");
                    mCallback.onError(-1);
                }
            } finally {
                data.recycle();
            }
        } catch (RemoteException e) {
            logger.error("LVL remote exception during license check: ", e);
            mCallback.onError(-1);
        } catch (Exception e) {
            logger.error("LVL license check failed: ", e);
            mCallback.onError(-1);
        }
    }

    private class ResultListenerBinder extends Binder implements IInterface {
        ResultListenerBinder() {
            attachInterface(this, RESULT_LISTENER_DESCRIPTOR);
        }

        @Override
        public IBinder asBinder() {
            return this;
        }

        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == IBinder.INTERFACE_TRANSACTION) {
                if (reply != null) {
                    reply.writeString(RESULT_LISTENER_DESCRIPTOR);
                }
                return true;
            }

            if (code == IBinder.FIRST_CALL_TRANSACTION) {
                try {
                    data.enforceInterface(RESULT_LISTENER_DESCRIPTOR);
                    int responseCode = data.readInt();
                    String signedData = data.readString();
                    String signature = data.readString();

                    logger.debug("LVL received license response: " + responseCode);

                    LicenseResponseHandler handler = new LicenseResponseHandler(mCallback, logger, MAX_RETRIES);
                    boolean shouldRetry = handler.handleResponse(responseCode, signedData, signature, retryCount);
                    if (shouldRetry) {
                        retryCount++;
                        logger.debug("LVL retrying license check... Attempt: " + retryCount);
                        executeLicenseCheck();
                    } else {
                        onDestroy();
                    }
                    return true;
                } catch (Exception ex) {
                    logger.error("LVL failed to process license response: ", ex);
                    mCallback.onError(-1);
                    return true;
                }
            }

            return super.onTransact(code, data, reply, flags);
        }
    }

    /**
     * Generates a 64-bit nonce with:
     * - 56-bit reduced timestamp (in milliseconds) stored in bits 8–63
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
