package com.adjust.sdk.nonplay;

import android.content.Context;

import com.adjust.sdk.ILogger;

import java.util.HashMap;
import java.util.Map;

import static com.adjust.sdk.nonplay.TelephonyIdsUtil.injectIMEI;

public class Util {
    public static Map<String, String> getNonPlayParameters(Context context, ILogger logger) {
        Map<String, String> parameters = new HashMap<String, String>();

        injectIMEI(parameters, context, logger);

        return parameters;
    }
}
