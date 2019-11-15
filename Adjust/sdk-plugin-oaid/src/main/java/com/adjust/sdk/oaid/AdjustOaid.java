package com.adjust.sdk.oaid;

import android.content.Context;
import android.util.Log;

import com.bun.miitmdid.core.JLibrary;

public class AdjustOaid {
    static boolean isOaidToBeRead = false;

    public static void readOaid(Context base) {
        AdjustOaid.isOaidToBeRead = true;

        try {
            JLibrary.InitEntry(base);
        } catch (NoClassDefFoundError ex) {
            Log.d("Adjust", "Couldn't find msa sdk " + ex.getMessage());
        }
    }

    public static void doNotReadOaid() {
        AdjustOaid.isOaidToBeRead = false;
    }
}
