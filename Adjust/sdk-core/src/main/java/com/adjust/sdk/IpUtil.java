package com.adjust.sdk;

import java.util.Arrays;
import java.util.Random;

public class IpUtil {

    private static final String[] ips = {
            "185.151.204.6",
            "185.151.204.7",
            "185.151.204.8",
            "185.151.204.9",
            "185.151.204.10",
            "185.151.204.11",
            "185.151.204.12",
            "185.151.204.13",
            "185.151.204.14",
            "185.151.204.15" };

    public static String getIpUrl() {
        int min = 0;
        int max = ips.length-1;

        Random random = new Random();
        int ipIndex = random.nextInt((max - min) + 1) + min;

        return "https://" + ips[ipIndex];
    }

    public static boolean containsIp(String host) {
        return Arrays.asList(ips).contains(host);
    }

}
