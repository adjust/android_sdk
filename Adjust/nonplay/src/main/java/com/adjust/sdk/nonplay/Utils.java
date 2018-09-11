package com.adjust.sdk.nonplay;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.adjust.sdk.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.adjust.sdk.nonplay.TelephonyIdsUtil.injectIMEI;

public class Utils {
    public static Map<String, String> getNonPlayParameters(Context context, Logger logger) {
        Map<String, String> parameters = new HashMap<String, String>();

        injectIMEI(parameters, context, logger);

        return parameters;
    }

    static void addStringToMap(Map<String, String> parameters, String key, String value) {
        if (TextUtils.isEmpty(value)) {
            return;
        }

        parameters.put(key, value);
    }
}
