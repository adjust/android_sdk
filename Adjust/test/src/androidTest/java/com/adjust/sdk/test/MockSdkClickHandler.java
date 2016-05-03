package com.adjust.sdk.test;

import com.adjust.sdk.ActivityPackage;
import com.adjust.sdk.ISdkClickHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pfms on 01/04/16.
 */
public class MockSdkClickHandler implements ISdkClickHandler {
    private MockLogger testLogger;
    private String prefix = "SdkClickHandler ";
    List<ActivityPackage> queue;

    public MockSdkClickHandler(MockLogger testLogger) {
        this.testLogger = testLogger;
        queue = new ArrayList<ActivityPackage>();
    }
    @Override
    public void init(boolean startsSending) {
        testLogger.test(prefix + "init, startsSending: " + startsSending);
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
    public void sendSdkClick(ActivityPackage sdkClick) {
        testLogger.test(prefix + "sendSdkClick");
        queue.add(sdkClick);
    }
}
