package com.adjust.sdk.lvl;

import android.content.Context;

import com.adjust.sdk.ILogger;
import com.adjust.sdk.LicenseRequiredData;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class AdjustLicenseVerification {

    public static LicenseRequiredData fetchLicenseDate(Context context, ILogger logger,String gpsAdid, long installTimeStamp) {
        try {
            logger.info("start fetch license data");
            BlockingQueue<LicenseRequiredData> licenseHolder = new LinkedBlockingQueue<LicenseRequiredData>(1);
            LicenseChecker checker = new LicenseChecker(context, new LicenseRawCallback() {
                @Override
                public void onLicenseDataReceived(int responseCode,String signedData, String signature) {
                    logger.info("license data received from server with code: " + responseCode );
                    licenseHolder.offer(new LicenseRequiredData(signedData, signature, responseCode));
                }

                @Override
                public void onError(int errorCode) {
                    logger.error("license error: " + errorCode );
                    licenseHolder.offer(new LicenseRequiredData(null, null, errorCode));
                }
            }, gpsAdid, installTimeStamp);
            logger.info("license check access");
            checker.checkAccess();
            return licenseHolder.poll(10000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.error("license checker interrupted: ", e.getMessage());
            return null;
        }
    }
}
