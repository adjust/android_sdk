package com.adjust.sdk.oaid;

import android.content.Context;
import android.util.Log;

import com.bun.miitmdid.core.JLibrary;

public class AdjustOaid {
    static boolean isOaidToBeRead = false;
    static boolean isMsaSdkAvailable = false;

    public static void readOaid() {
        AdjustOaid.isOaidToBeRead = true;
    }

    public static void readOaid(Context base) {
        readOaid();

        try {
            JLibrary.InitEntry(base);
            isMsaSdkAvailable = true;
        } catch (Throwable t) {
            isMsaSdkAvailable = false;
            Log.d("Adjust", "Error during msa sdk initialization " + t.getMessage());
        }
    }

    public static void doNotReadOaid() {
        AdjustOaid.isOaidToBeRead = false;
    }
}
