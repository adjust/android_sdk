package com.adjust.sdk;

import android.content.Context;

/**
 * Created by nonelse on 08.01.2018
 */

public class AdjustTestOptions {
    public Context context;
    public String baseUrl;
    public String basePath;
    public Boolean useTestConnectionOptions;
    public Long timerIntervalInMilliseconds;
    public Long timerStartInMilliseconds;
    public Long sessionIntervalInMilliseconds;
    public Long subsessionIntervalInMilliseconds;
    public Boolean teardown;
    public Boolean tryInstallReferrer;
}
