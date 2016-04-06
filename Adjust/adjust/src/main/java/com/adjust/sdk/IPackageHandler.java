package com.adjust.sdk;

import android.content.Context;

import java.util.Map;

public interface IPackageHandler {
    void init(IActivityHandler activityHandler, Context context, boolean startsSending);

    void addPackage(ActivityPackage activityPackage);

    void sendFirstPackage();

    void sendNextPackage(ResponseData responseData);

    void closeFirstPackage(ResponseData responseData, ActivityPackage activityPackage);

    void pauseSending();

    void resumeSending();

    void updateQueue(Map<String, String> sessionCallbackParameters, Map<String, String> sessionPartnerParameters);
}
