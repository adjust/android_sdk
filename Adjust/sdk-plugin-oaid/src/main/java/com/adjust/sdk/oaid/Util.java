package com.adjust.sdk.oaid;

import android.content.Context;

import com.adjust.sdk.ILogger;

import java.util.HashMap;
import java.util.Map;

import static com.adjust.sdk.oaid.OpenDeviceIdUtil.injectOaid;

public class Util {
    public static Map<String, String> getOaidParameters(Context context, ILogger logger) {
        Map<String, String> parameters = new HashMap<String, String>();
        injectOaid(parameters, context, logger);
        return parameters;
    }
}
