package com.adjust.sdk.play;

import android.content.Context;

import com.adjust.sdk.ILogger;
import com.adjust.sdk.PackageBuilder;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Util {
    public static Map<String, String> getPlayParameters(Context context, ILogger logger) {
        Map<String, String> parameters = new HashMap<String, String>();

        injectPlayInfo(parameters, context, logger);

        return parameters;
    }

    private static void injectPlayInfo(Map<String, String> parameters, Context context, ILogger logger) {
        AdvertisingIdClient.Info advertisingIdInfo = null;
        try {
            advertisingIdInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
        } catch (IOException e) {
            logger.error("IOException when trying to get Advertising Id Info: %s", e.getMessage());
        } catch (GooglePlayServicesNotAvailableException e) {
            logger.error("GooglePlayServicesNotAvailableException when trying to get Advertising Id Info: %s", e.getMessage());
        } catch (GooglePlayServicesRepairableException e) {
            logger.error("GooglePlayServicesRepairableException when trying to get Advertising Id Info: %s", e.getMessage());
        }
        if (advertisingIdInfo == null) {
            return;
        }

        String id = advertisingIdInfo.getId();
        boolean limitAdTrackingEnabled = advertisingIdInfo.isLimitAdTrackingEnabled();

        PackageBuilder.addString(parameters, "gps_adid", id);
        PackageBuilder.addBoolean(parameters, "tracking_enabled", limitAdTrackingEnabled);
    }
}
