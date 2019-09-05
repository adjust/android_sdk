package com.adjust.sdk.oaid;

import android.content.Context;

public class AdjustOaid {
    static boolean isOaidToBeRead = false;

    public static void readOaid(final Context context) {
        if (context != null) {
            AdjustOaid.isOaidToBeRead = true;
            OpenDeviceIdUtil.connect(context.getApplicationContext());
        }
    }

    public static void doNotReadOaid(Context context) {
        AdjustOaid.isOaidToBeRead = false;
        if (context != null) {
            OpenDeviceIdUtil.disconnect(context.getApplicationContext());
        }
    }
}
