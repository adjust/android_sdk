package com.adjust.sdk.nonplay;

public class AdjustNonPlay {
    static boolean isIMEItoBeRead = true;
    static boolean isAndroidIdToBeRead = true;
    static boolean isMacAddressToBeRead = true;

    public static void readIMEI(boolean isIMEItoBeRead) {
        AdjustNonPlay.isIMEItoBeRead = isIMEItoBeRead;
    }

    public static void readAndroidIs(boolean isAndroidIdToBeRead) {
        AdjustNonPlay.isAndroidIdToBeRead = isAndroidIdToBeRead;
    }

    public static void readMacAddress(boolean isMacAddressToBeRead) {
        AdjustNonPlay.isMacAddressToBeRead = isMacAddressToBeRead;
    }
}
