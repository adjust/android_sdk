package com.adjust.sdk.oaid;

public class AdjustOaid {
    static boolean isOaidToBeRead = false;

    public static void readOaid() {
        AdjustOaid.isOaidToBeRead = true;
    }

    public static void doNotReadOaid() {
        AdjustOaid.isOaidToBeRead = false;
    }
}
