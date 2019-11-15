package com.adjust.sdk.oaid;

import android.content.Context;

import com.adjust.sdk.ILogger;
import com.adjust.sdk.PackageBuilder;
import com.adjust.sdk.oaid.OpenDeviceIdentifierClient.Info;

import java.util.HashMap;
import java.util.Map;

public class Util {
    public static Map<String, String> getOaidParameters(Context context, ILogger logger) {

        if (AdjustOaid.isOaidToBeRead) {

            // IMPORTANT: current logic is to first try reading the oaid with hms (huawei mobile service) approach
            // which has the capability to return both oaid and limit tracking status
            // And as a fallback, use the msa sdk approach, which currently only gives the oaid

            Info oaidInfo = OpenDeviceIdentifierClient.getOaidInfo(context, logger, 5000);
            if (oaidInfo != null) {
                Map<String, String> parameters = new HashMap<String, String>();
                PackageBuilder.addString(parameters, "oaid", oaidInfo.getOaid());
                PackageBuilder.addBoolean(parameters, "oaid_tracking_enabled", !oaidInfo.isOaidTrackLimited());
                return parameters;
            }

            logger.debug("Fail to read the OAID using hms, now try reading it using msa");

            String oaid = MsaSdkClient.getOaid(context, logger, 1000);
            if (oaid != null) {
                Map<String, String> parameters = new HashMap<String, String>();
                PackageBuilder.addString(parameters, "oaid", oaid);
                return parameters;
            }

            logger.error("Fail to read the OAID completely");

        }

        return null;
    }
}
