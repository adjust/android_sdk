package com.adjust.sdk.test;

import com.adjust.sdk.ActivityPackage;
import com.adjust.sdk.IActivityHandler;
import com.adjust.sdk.IAttributionHandler;
import com.adjust.sdk.ResponseDataTasks;

/**
 * Created by pfms on 09/01/15.
 */
public class MockAttributionHandler implements IAttributionHandler {
    private MockLogger testLogger;
    private String prefix = "AttributionHandler ";
    IActivityHandler activityHandler;
    ResponseDataTasks lastResponseDataTasks;
    ActivityPackage attributionPackage;

    public MockAttributionHandler(MockLogger testLogger) {
        this.testLogger = testLogger;
    }

    @Override
    public void init(IActivityHandler activityHandler,
                     ActivityPackage attributionPackage,
                     boolean startPaused,
                     boolean hasListener) {
        testLogger.test(prefix + "init, startPaused: " + startPaused +
                ", hasListener: " + hasListener);
        this.activityHandler = activityHandler;
        this.attributionPackage = attributionPackage;
    }

    @Override
    public void getAttribution() {
        testLogger.test(prefix + "getAttribution");
    }

    @Override
    public void checkResponse(ResponseDataTasks responseDataTasks) {
        testLogger.test(prefix + "checkResponse");

        this.lastResponseDataTasks = responseDataTasks;
    }

    @Override
    public void pauseSending() {
        testLogger.test(prefix + "pauseSending");
    }

    @Override
    public void resumeSending() {
        testLogger.test(prefix + "resumeSending");
    }
}
