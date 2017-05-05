package com.adjust.sdk;

/**
 * Created by pfms on 31/03/16.
 */
public interface ISdkClickHandler {
    void init(IActivityHandler activityHandler, boolean startsSending);
    void pauseSending();
    void resumeSending();
    void sendSdkClick(ActivityPackage sdkClick);
    void teardown();
}
