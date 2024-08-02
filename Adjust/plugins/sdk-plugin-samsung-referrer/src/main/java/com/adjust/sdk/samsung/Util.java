package com.adjust.sdk.samsung;

import android.content.Context;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.ILogger;
import com.adjust.sdk.ReferrerDetails;

public class Util {
    public synchronized static ReferrerDetails getSamsungInstallReferrerDetails(Context context, ILogger logger) {
        if (!AdjustSamsungReferrer.shouldReadSamsungReferrer) {
            return null;
        }

        logger.info("getSamsungInstallReferrerDetails invoked");

        SamsungInstallReferrerResult referrerResult =
                SamsungReferrerClient.getReferrer(context, logger, 2000);
        if (referrerResult == null) {
            return null;
        }
        SamsungInstallReferrerDetails samsungInstallReferrerDetails = referrerResult.samsungInstallReferrerDetails;
        if (samsungInstallReferrerDetails == null) {
            return null;
        }
        return new ReferrerDetails(samsungInstallReferrerDetails.installReferrer,
                samsungInstallReferrerDetails.referrerClickTimestampSeconds,
                samsungInstallReferrerDetails.installBeginTimestampSeconds,
                -1,
                -1,
                null,
                null,
                null);
    }
}
