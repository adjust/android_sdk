package com.adjust.sdk.test;

import com.adjust.sdk.ActivityPackage;
import com.adjust.sdk.IPackageHandler;
import com.adjust.sdk.IRequestHandler;

public class MockRequestHandler implements IRequestHandler {
    private MockLogger testLogger;
    private String prefix = "RequestHandler ";

    public MockRequestHandler(MockLogger testLogger) {
        this.testLogger = testLogger;
    }

    @Override
    public void sendPackage(ActivityPackage pack) {
        testLogger.test(prefix +  "sendPackage, " + pack);

        /*
        // respond successfully to the package handler
        if (packageHandler != null && !errorNextSend) {
            packageHandler.sendNextPackage();
        }

        if (packageHandler != null && errorNextSend) {
            testLogger.test(packageHandler.getFailureMessage());
            packageHandler.closeFirstPackage();
        }
        */
    }

    @Override
    public void sendClickPackage(ActivityPackage clickPackage) {
        testLogger.test(prefix +  "sendClickPackage, " + clickPackage);
    }
}
