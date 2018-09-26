package com.adjust.sdk.nonplay;

public class AdjustNonPlay {
    static boolean isIMEItoBeRead = true;

    public static void readIMEI(boolean isIMEItoBeRead) {
        AdjustNonPlay.isIMEItoBeRead = isIMEItoBeRead;
    }
}
