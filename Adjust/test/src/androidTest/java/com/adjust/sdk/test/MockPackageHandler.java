package com.adjust.sdk.test;

import android.content.Context;

import com.adjust.sdk.ActivityPackage;
import com.adjust.sdk.IActivityHandler;
import com.adjust.sdk.IPackageHandler;
import com.adjust.sdk.ResponseDataTasks;

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
        queue = new ArrayList<ActivityPackage>();
    }

    @Override
    public void init(IActivityHandler activityHandler, Context context, boolean startPaused) {
        testLogger.test(prefix + "init, startPaused: " + startPaused);
        this.activityHandler = activityHandler;
        this.context = context;
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
    public void sendNextPackage(ResponseDataTasks responseDataTasks) {
        testLogger.test(prefix + "sendNextPackage, " + responseDataTasks);
    }

    @Override
    public void closeFirstPackage() {
        testLogger.test(prefix + "closeFirstPackage");
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
