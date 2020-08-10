package com.adjust.sdk;

import java.util.ArrayList;
import java.util.List;

public class UrlFactory {

    private static List<String> baseUrls = null;
    private static List<String> gdprUrls = null;
    private static List<String> subscriptionUrls = null;
    
    public static List<String> getBaseUrls() {
        if (baseUrls == null) {
            baseUrls = new ArrayList<String>();
            baseUrls.add(AdjustFactory.getBaseUrl());
            baseUrls.addAll(AdjustFactory.getFallbackBaseUrls());

            for (String ip : AdjustFactory.getFallbackIps()) {
                baseUrls.add(getUrlForIp(ip));
            }
        }

        return baseUrls;
    }

    public static List<String> getGdprUrls() {
        if (gdprUrls == null) {
            gdprUrls = new ArrayList<String>();
            gdprUrls.add(AdjustFactory.getGdprUrl());
            gdprUrls.addAll(AdjustFactory.getFallbackGdprUrls());

            for (String ip : AdjustFactory.getFallbackIps()) {
                gdprUrls.add(getUrlForIp(ip));
            }
        }

        return gdprUrls;
    }

    public static List<String> getSubscriptionUrls() {
        if (subscriptionUrls == null) {
            subscriptionUrls = new ArrayList<String>();
            subscriptionUrls.add(AdjustFactory.getSubscriptionUrl());
            subscriptionUrls.addAll(AdjustFactory.getFallbackSubscriptionUrls());

            for (String ip : AdjustFactory.getFallbackIps()) {
                subscriptionUrls.add(getUrlForIp(ip));
            }
        }

        return subscriptionUrls;
    }

    public synchronized static void prioritiseBaseUrl(String baseUrl) {
        if (baseUrls.indexOf(baseUrl) != 0) {
            baseUrls.remove(baseUrl);
            baseUrls.add(0, baseUrl);
        }
    }

    public synchronized static void prioritiseGdprUrl(String gdprUrl) {
        if (gdprUrls.indexOf(gdprUrl) != 0) {
            gdprUrls.remove(gdprUrl);
            gdprUrls.add(0, gdprUrl);
        }
    }

    public synchronized static void prioritiseSubscriptionUrl(String subscriptionUrl) {
        if (subscriptionUrls.indexOf(subscriptionUrl) != 0) {
            subscriptionUrls.remove(subscriptionUrl);
            subscriptionUrls.add(0, subscriptionUrl);
        }
    }

    public static boolean matchesFallbackIp(String host) {
        return AdjustFactory.getFallbackIps().contains(host);
    }

    private static String getUrlForIp(String ip) {
        return "https://" + ip;
    }
}
