/*
* This project is licensed under the MIT License.
* However, this project includes code from the following third-party component(s):
*       - ILicenseResultListener.aidl , ILicensingService.aidl - Licensed under
*       the Apache License, Version 2.0
*       http://www.apache.org/licenses/LICENSE-2.0
* The Apache 2.0 license and any required NOTICE file are included in the
* com.android.vending.licensing directory.
*/

package com.adjust.sdk.lvl;

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
