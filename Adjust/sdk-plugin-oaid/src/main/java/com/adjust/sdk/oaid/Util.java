package com.adjust.sdk.oaid;

import android.content.Context;
import android.util.Log;

import com.adjust.sdk.ILogger;
import com.adjust.sdk.PackageBuilder;
import com.adjust.sdk.oaid.OpenDeviceIdentifierClient.Info;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class Util {
    public synchronized static Map<String, String> getOaidParameters(Context context, ILogger logger) {
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
        for (int attempt = 1; attempt <= 3; attempt += 1) {
            Info oaidInfo = OpenDeviceIdentifierClient.getOaidInfo(context, logger, 3000 * attempt);
            if (oaidInfo != null) {
                Map<String, String> parameters = new HashMap<String, String>();
                PackageBuilder.addString(parameters, "oaid", oaidInfo.getOaid());
                PackageBuilder.addBoolean(parameters, "oaid_tracking_enabled", !oaidInfo.isOaidTrackLimited());
                PackageBuilder.addString(parameters, "oaid_src", "hms");
                PackageBuilder.addLong(parameters, "oaid_attempt", attempt);
                return parameters;
            }
        }
        logger.debug("Fail to read the OAID using HMS");
        return null;
    }

    private static Map<String, String> getOaidParametersUsingMSA(Context context, ILogger logger) {
        if (!AdjustOaid.isMsaSdkAvailable) {
            return null;
        }

        for (int attempt = 1; attempt <= 3; attempt += 1) {
            String oaid = MsaSdkClient.getOaid(context, logger, 3000 * attempt);
            if (oaid != null && !oaid.isEmpty()) {
                Map<String, String> parameters = new HashMap<String, String>();
                PackageBuilder.addString(parameters, "oaid", oaid);
                PackageBuilder.addString(parameters, "oaid_src", "msa");
                PackageBuilder.addLong(parameters, "oaid_attempt", attempt);
                return parameters;
            }
        }

        logger.debug("Fail to read the OAID using MSA");
        return null;
    }

    public static String readCertFromAssetFile(Context context) {
        try {
            String assetFileName = context.getPackageName() + ".cert.pem";
            InputStream is = context.getAssets().open(assetFileName);
            BufferedReader in = new BufferedReader(new InputStreamReader(is));
            StringBuilder builder= new StringBuilder();
            String line;
            while ((line = in.readLine()) != null){
                builder.append(line);
                builder.append('\n');
            }
            return builder.toString();
        } catch (Exception e) {
            Log.e("Adjust", "readCertFromAssetFile failed");
            return "";
        }
    }
}
