package com.adjust.sdk.test;

import java.util.ArrayList;
import java.util.List;

import com.adjust.sdk.ActivityPackage;
import com.adjust.sdk.IPackageHandler;
import com.adjust.sdk.ResponseData;

public class MockPackageHandler implements IPackageHandler {

    private MockLogger testLogger;
    private String prefix = "PackageHandler ";
    public List<ActivityPackage> queue;

    public MockPackageHandler(MockLogger testLogger) {
        this.testLogger = testLogger;
        queue = new ArrayList<ActivityPackage>();
    }

    @Override
    public void addPackage(ActivityPackage pack) {
        testLogger.test(prefix +  "addPackage");
        queue.add(pack);
    }

    @Override
    public void sendFirstPackage() {
        testLogger.test(prefix +  "sendFirstPackage");
    }

    @Override
    public void sendNextPackage() {
        testLogger.test(prefix +  "sendNextPackage");
    }

    @Override
    public void closeFirstPackage() {
        testLogger.test(prefix +  "closeFirstPackage");
    }

    @Override
    public void pauseSending() {
        testLogger.test(prefix +  "pauseSending");
    }

    @Override
    public void resumeSending() {
        testLogger.test(prefix +  "resumeSending");
    }

    @Override
    public String getFailureMessage() {
        testLogger.debug(prefix +  "getFailureMessage");
        return "Will retry later.";
    }

    @Override
    public boolean dropsOfflineActivities() {
        return false; // TODO: implement?
    }

    @Override
    public void finishedTrackingActivity(ActivityPackage activityPackage, ResponseData responseData) {
        // TODO: implement
    }
}
