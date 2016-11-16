package com.adjust.sdk.plugin;

import android.content.Context;
import android.provider.Settings.Secure;

public final class AndroidIdUtil {
    private AndroidIdUtil() {

    }

    public static String getAndroidId(final Context context) {
        return Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
    }
}
