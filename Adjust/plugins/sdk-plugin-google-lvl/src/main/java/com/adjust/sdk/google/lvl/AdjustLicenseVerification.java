package com.adjust.sdk.google.lvl;

import android.content.Context;

import com.adjust.sdk.ILogger;
import com.adjust.sdk.LicenseData;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class AdjustLicenseVerification {

    public static LicenseData fetchLicenseDate(Context context, ILogger logger, long timestamp) {
        try {
            BlockingQueue<LicenseData> licenseHolder = new LinkedBlockingQueue<LicenseData>(1);
            LicenseChecker checker = new LicenseChecker(context, new LicenseRawCallback() {
                @Override
                public void onLicenseDataReceived(int responseCode, String signedData, String signature) {
                    licenseHolder.offer(new LicenseData(signedData, signature, responseCode));
                }

                @Override
                public void onError(int errorCode) {
                    licenseHolder.offer(new LicenseData(null, null, errorCode));
                }
            }, logger, timestamp);
            checker.checkAccess();
            return licenseHolder.poll(3000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.error("LVL License checker interrupted: ", e.getMessage());
            return null;
        }
    }
}
