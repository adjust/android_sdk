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
            Info oaidInfo = OpenDeviceIdentifierClient.getOaidInfo(context, logger, 1000);
            if (oaidInfo != null) {
                Map<String, String> parameters = new HashMap<String, String>();
                PackageBuilder.addString(parameters, "oaid", oaidInfo.getOaid());
                PackageBuilder.addBoolean(parameters, "oaid_tracking_enabled", oaidInfo.isOaidTrackLimited());
                return parameters;
            }
        }

        return null;
    }
}
