package com.adjust.sdk.samsung;

import android.content.Context;

import com.adjust.sdk.ILogger;
import com.adjust.sdk.ReferrerDetails;

public class Util {
    public synchronized static ReferrerDetails getSamsungInstallReferrerDetails(Context context, ILogger logger) {
        if (!AdjustSamsungReferrer.shouldReadSamsungReferrer) {
            return null;
        }

        com.samsung.android.sdk.sinstallreferrer.api.ReferrerDetails referrerDetails =
                SamsungReferrerClient.getReferrer(context, logger, 2000);
        if (referrerDetails == null) {
            return null;
        }

        ReferrerDetails returnReferrerDetails = new ReferrerDetails(referrerDetails.getInstallReferrer(),
                referrerDetails.getReferrerClickTimestampSeconds(),
                referrerDetails.getInstallBeginTimestampSeconds());

        AdjustSamsungReferrer.onSamsungInstallReferrerReadListener.onInstallReferrerRead(returnReferrerDetails, "samsung");
        return returnReferrerDetails;
    }
}
