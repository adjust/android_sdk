package com.adjust.sdk.oaid;

import android.content.Context;

import com.adjust.sdk.ILogger;
import com.adjust.sdk.PackageBuilder;
import com.adjust.sdk.oaid.OpenDeviceIdentifierClient.Info;

import java.util.HashMap;
import java.util.Map;

public class Util {
    public static Map<String, String> getOaidParameters(Context context, ILogger logger) {
        if (!AdjustOaid.isOaidToBeRead) {
            return null;
        }

        Map<String, String> oaidParameters;

        // IMPORTANT:
        // if manufacturer is huawei then try reading the oaid with hms (huawei mobile service)
        // approach first, as it can read both oaid and limit tracking status
        // otherwise use the msa sdk which only gives the oaid currently

        if (isManufacturerHuawei(logger)) {
            oaidParameters = getOaidParametersUsingHMS(context, logger);
            if (oaidParameters != null) {
                return oaidParameters;
            }

            return getOaidParametersUsingMSA(context, logger);
        } else {
            oaidParameters = getOaidParametersUsingMSA(context, logger);
            if (oaidParameters != null) {
                return oaidParameters;
            }

            return getOaidParametersUsingHMS(context, logger);
        }
    }

    private static boolean isManufacturerHuawei(ILogger logger) {
        try {
            String manufacturer = android.os.Build.MANUFACTURER;
            if (manufacturer != null && manufacturer.equalsIgnoreCase("huawei")) {
                return true;
            }
        } catch (Exception e) {
            logger.debug("Manufacturer not available");
        }
        return false;
    }

    private static Map<String, String> getOaidParametersUsingHMS(Context context, ILogger logger) {
        Info oaidInfo = OpenDeviceIdentifierClient.getOaidInfo(context, logger, 1000);
        if (oaidInfo != null) {
            Map<String, String> parameters = new HashMap<String, String>();
            PackageBuilder.addString(parameters, "oaid", oaidInfo.getOaid());
            PackageBuilder.addBoolean(parameters, "oaid_tracking_enabled", !oaidInfo.isOaidTrackLimited());
            return parameters;
        }
        logger.debug("Fail to read the OAID using HMS");
        return null;
    }

    private static Map<String, String> getOaidParametersUsingMSA(Context context, ILogger logger) {
        if (!AdjustOaid.isMsaSdkAvailable) {
            return null;
        }

        String oaid = MsaSdkClient.getOaid(context, logger, 1000);
        if (oaid != null && !oaid.isEmpty()) {
            Map<String, String> parameters = new HashMap<String, String>();
            PackageBuilder.addString(parameters, "oaid", oaid);
            return parameters;
        }

        logger.debug("Fail to read the OAID using MSA");
        return null;
    }
}
