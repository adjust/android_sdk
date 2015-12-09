package com.adjust.sdk;

import android.content.Context;

public interface IPackageHandler {
    public void init(IActivityHandler activityHandler, Context context, boolean startPaused);

    public void addPackage(ActivityPackage pack);

    public void sendFirstPackage();

    public void sendNextPackage(ResponseDataTasks responseDataTasks);

    public void closeFirstPackage();

    public void pauseSending();

    public void resumeSending();
}
