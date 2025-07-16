package com.adjust.sdk.lvl;

import android.content.Context;

import com.adjust.sdk.ILogger;
import com.adjust.sdk.LicenseRequiredData;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class AdjustLicenseVerification {

    public static LicenseRequiredData fetchLicenseDate(Context context, ILogger logger, long installTimestamp) {
        try {
            BlockingQueue<LicenseRequiredData> licenseHolder = new LinkedBlockingQueue<LicenseRequiredData>(1);
            LicenseChecker checker = new LicenseChecker(context, new LicenseRawCallback() {
                @Override
                public void onLicenseDataReceived(int responseCode,String signedData, String signature) {
                    licenseHolder.offer(new LicenseRequiredData(signedData, signature, responseCode));
                }

                @Override
                public void onError(int errorCode) {
                    licenseHolder.offer(new LicenseRequiredData(null, null, errorCode));
                }
            }, logger, installTimeStamp);
            checker.checkAccess();
            return licenseHolder.poll(3000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.error("[LicenseVerification] License checker interrupted: ", e.getMessage());
            return null;
        }
    }
}
