package com.adjust.sdk.imei;

public class AdjustImei {
    static boolean isImeiToBeRead = false;

    public static void readImei() {
        AdjustImei.isImeiToBeRead = true;
    }

    public static void doNotReadImei() {
        AdjustImei.isImeiToBeRead = false;
    }
}
