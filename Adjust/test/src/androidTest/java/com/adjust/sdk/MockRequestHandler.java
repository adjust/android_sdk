package com.adjust.sdk;

public class MockRequestHandler implements IRequestHandler {
    private MockLogger testLogger;
    private String prefix = "RequestHandler ";
    IPackageHandler packageHandler;

    public MockRequestHandler(MockLogger testLogger) {
        this.testLogger = testLogger;
    }

    @Override
    public void init(IPackageHandler packageHandler) {
        testLogger.test(prefix + "init");
        this.packageHandler = packageHandler;
    }

    @Override
    public void sendPackage(ActivityPackage activityPackage, int queueSize) {
        testLogger.test(prefix + "sendPackage, activityPackage " + activityPackage);
        testLogger.test(prefix + "sendPackage, queueSize " + queueSize);

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
    public void teardown() {
        testLogger.test(prefix + "teardown");
    }
}
