package com.adjust.sdk.test;

import com.adjust.sdk.ActivityPackage;
import com.adjust.sdk.IPackageHandler;
import com.adjust.sdk.IRequestHandler;

public class MockRequestHandler implements IRequestHandler {

    private MockLogger testLogger;
    private String prefix = "RequestHandler ";
    private IPackageHandler packageHandler;
    private Boolean errorNextSend;

    public MockRequestHandler(MockLogger testLogger) {
        this.testLogger = testLogger;
        this.errorNextSend = false;
    }

    @Override
    public void sendPackage(ActivityPackage pack) {
        testLogger.test(prefix +  "sendPackage");

        // respond successfully to the package handler
        if (packageHandler != null && !errorNextSend) {
            packageHandler.sendNextPackage();
        }

        if (packageHandler != null && errorNextSend) {
            testLogger.test(packageHandler.getFailureMessage());
            packageHandler.closeFirstPackage();
        }
    }

    public void setPackageHandler(IPackageHandler packageHandler) {
        this.packageHandler = packageHandler;
    }

    public void setErrorNextSend(Boolean errorNextSend) {
        this.errorNextSend = errorNextSend;
    }

}
