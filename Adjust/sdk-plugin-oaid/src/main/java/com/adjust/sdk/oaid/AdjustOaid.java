package com.adjust.sdk.oaid;

import android.content.Context;
import android.util.Log;

import com.bun.miitmdid.core.MdidSdkHelper;

public class AdjustOaid {
    static boolean isOaidToBeRead = false;
    static boolean isMsaSdkAvailable = false;

    public static void readOaid() {
        isOaidToBeRead = true;
    }

    public static void readOaid(Context context) {
        readOaid();

        try {
            System.loadLibrary("msaoaidsec");
            String certificate = Util.readCertFromAssetFile(context);
            isMsaSdkAvailable = MdidSdkHelper.InitCert(context, certificate);
        } catch (Throwable t) {
            isMsaSdkAvailable = false;
            Log.d("Adjust", "Error during msa sdk initialization " + t.getMessage());
        }
    }

    public static void doNotReadOaid() {
        isOaidToBeRead = false;
    }
}
