package com.adjust.sdk.nonplay;

import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;

import com.adjust.sdk.ILogger;
import com.adjust.sdk.Logger;
import com.adjust.sdk.PackageBuilder;

import java.util.HashMap;
import java.util.Map;

import static com.adjust.sdk.nonplay.MacAddressUtil.injectMacAddress;
import static com.adjust.sdk.nonplay.TelephonyIdsUtil.injectIMEI;

public class Util {
    public static Map<String, String> getNonPlayParameters(Context context, ILogger logger) {
        Map<String, String> parameters = new HashMap<String, String>();

        injectIMEI(parameters, context, logger);
        injectAndroidId(parameters, context);
        injectMacAddress(parameters, context);

        return parameters;
    }

    private static void injectAndroidId(Map<String, String> parameters, Context context) {
        if (!AdjustNonPlay.isAndroidIdToBeRead) {
            return;
        }
        String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        PackageBuilder.addString(parameters, "android_id", androidId);
    }
}
