package com.adjust.sdk;

import android.content.Context;

public interface IPackageHandler {
    void init(IActivityHandler activityHandler, Context context, boolean startsSending);

    void addPackage(ActivityPackage activityPackage);

    void sendFirstPackage();

    void sendNextPackage(ResponseData responseData);

    void closeFirstPackage(ResponseData responseData, ActivityPackage activityPackage);

    void pauseSending();

    void resumeSending();

    void updatePackages(SessionParameters sessionParameters);

    String getBasePath();

    void teardown(boolean deleteState);
}
