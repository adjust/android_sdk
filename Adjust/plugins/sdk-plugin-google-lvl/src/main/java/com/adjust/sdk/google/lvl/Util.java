package com.adjust.sdk.google.lvl;

import android.content.Context;

import com.adjust.sdk.ILogger;
import com.adjust.sdk.LicenseData;

public class Util {

    public synchronized static LicenseData getLicenseRequiredData(Context context, ILogger logger, long timestamp) {
        return AdjustLicenseVerification.fetchLicenseDate(context, logger, timestamp);
    }
}
