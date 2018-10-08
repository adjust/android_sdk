package com.adjust.sdk.imei;

import android.content.Context;

import com.adjust.sdk.ILogger;

import java.util.HashMap;
import java.util.Map;

import static com.adjust.sdk.imei.TelephonyIdsUtil.injectIMEI;

public class Util {
    public static Map<String, String> getIMEIparameters(Context context, ILogger logger) {
        Map<String, String> parameters = new HashMap<String, String>();

        injectIMEI(parameters, context, logger);

        return parameters;
    }
}
