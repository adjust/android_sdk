package com.adjust.sdk.imei;

public class AdjustIMEI {
    static boolean isIMEItoBeRead = false;

    public static void readIMEI() {
        AdjustIMEI.isIMEItoBeRead = true;
    }

    public static void doNotReadIMEI() {
        AdjustIMEI.isIMEItoBeRead = false;
    }
}
