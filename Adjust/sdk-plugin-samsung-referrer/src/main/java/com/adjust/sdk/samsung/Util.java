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

        SamsungInstallReferrerResult referrerDetails =
                SamsungReferrerClient.getReferrer(context, logger, 2000);
        if (referrerDetails == null) {
            return null;
        }
        SamsungInstallReferrerDetails samsungInstallReferrerDetails = referrerDetails.samsungInstallReferrerDetails;

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
