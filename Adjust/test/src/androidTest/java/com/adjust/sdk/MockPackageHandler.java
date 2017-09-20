package com.adjust.sdk;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class MockPackageHandler implements IPackageHandler {
    private MockLogger testLogger;
    private String prefix = "PackageHandler ";
    IActivityHandler activityHandler;
    List<ActivityPackage> queue;
    Context context;

    public MockPackageHandler(MockLogger testLogger) {
        this.testLogger = testLogger;
    }

    @Override
    public void init(IActivityHandler activityHandler, Context context, boolean startsSending) {
        testLogger.test(prefix + "init, startsSending: " + startsSending);
        this.activityHandler = activityHandler;
        this.context = context;
        this.queue = new ArrayList<ActivityPackage>();
    }

    @Override
    public void addPackage(ActivityPackage pack) {
        testLogger.test(prefix + "addPackage");
        queue.add(pack);
    }

    @Override
    public void sendFirstPackage() {
        testLogger.test(prefix + "sendFirstPackage");
        /*
        if (activityHandler != null) {
            activityHandler.finishedTrackingActivity(jsonResponse);
        }
        */
    }

    @Override
    public void sendNextPackage(ResponseData responseData) {
        testLogger.test(prefix + "sendNextPackage, " + responseData);
    }

    @Override
    public void closeFirstPackage(ResponseData responseData, ActivityPackage activityPackage) {
        testLogger.test(prefix + "closeFirstPackage, responseData" + responseData);
        testLogger.test(prefix + "closeFirstPackage, activityPackage" + activityPackage);
    }

    @Override
    public void pauseSending() {
        testLogger.test(prefix + "pauseSending");
    }

    @Override
    public void resumeSending() {
        testLogger.test(prefix + "resumeSending");
    }

    @Override
    public void updatePackages(SessionParameters sessionParameters) {
        testLogger.test(prefix + "updatePackages, sessionParameters" + sessionParameters);
    }

    @Override
    public String getBasePath() {
        return null;
    }

    @Override
    public void teardown(boolean deleteState) {
        testLogger.test(prefix + "teardown deleteState, " + deleteState);
    }
}
