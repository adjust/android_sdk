package com.adjust.sdk.imei;

import android.content.Context;

import com.adjust.sdk.ILogger;

import java.util.HashMap;
import java.util.Map;

import static com.adjust.sdk.imei.TelephonyIdsUtil.injectImei;

public class Util {
    public static Map<String, String> getImeiParameters(Context context, ILogger logger) {
        Map<String, String> parameters = new HashMap<String, String>();
        injectImei(parameters, context, logger);
        return parameters;
    }
}
