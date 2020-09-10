package com.adjust.sdk.oaid;

import android.content.Context;

import com.adjust.sdk.Reflection;

public class AdjustOaid {
    static boolean isOaidToBeRead = false;
    static boolean isMsaSdkAvailable = false;

    public static void readOaid() {
        isOaidToBeRead = true;

        isMsaSdkAvailable =
                Reflection.forName("com.bun.miitmdid.core.MdidSdkHelper") != null;
    }

    public static void readOaid(Context base) {
        readOaid();
    }

    public static void doNotReadOaid() {
        isOaidToBeRead = false;
    }
}
