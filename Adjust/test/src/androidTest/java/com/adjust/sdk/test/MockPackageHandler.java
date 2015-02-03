package com.adjust.sdk.test;

import com.adjust.sdk.ActivityPackage;
import com.adjust.sdk.IActivityHandler;
import com.adjust.sdk.IPackageHandler;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MockPackageHandler implements IPackageHandler {
    private MockLogger testLogger;
    private String prefix = "PackageHandler ";
    public IActivityHandler activityHandler;
    public JSONObject jsonResponse;
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
        /*
        if (activityHandler != null) {
            activityHandler.finishedTrackingActivity(jsonResponse);
        }
        */
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
        testLogger.test(prefix +  "getFailureMessage");
        return "Mock Failure Message.";
    }

    @Override
    public void finishedTrackingActivity(JSONObject jsonResponse) {
        testLogger.test(prefix +  "finishedTrackingActivity, " + jsonResponse);
    }

    @Override
    public void sendClickPackage(ActivityPackage clickPackage) {
        testLogger.test(prefix +  "sendClickPackage");
        queue.add(clickPackage);
    }
}
