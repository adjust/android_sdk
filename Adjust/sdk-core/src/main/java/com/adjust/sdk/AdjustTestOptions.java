package com.adjust.sdk;

import android.content.Context;

/**
 * Created by nonelse on 08.01.2018
 */

public class AdjustTestOptions {
    public Context context;
    public String baseUrl;
    public String gdprUrl;
    public String subscriptionUrl;
    public String purchaseVerificationUrl;
    public String basePath;
    public String gdprPath;
    public String subscriptionPath;
    public String purchaseVerificationPath;
    public Long timerIntervalInMilliseconds;
    public Long timerStartInMilliseconds;
    public Long sessionIntervalInMilliseconds;
    public Long subsessionIntervalInMilliseconds;
    public Boolean teardown;
    public Boolean tryInstallReferrer = false;
    public Boolean noBackoffWait;
    public Boolean ignoreSystemLifecycleBootstrap = true;
    public Boolean allowUrlStrategyFallback = false;
}
