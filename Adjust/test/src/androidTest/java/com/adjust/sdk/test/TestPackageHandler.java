package com.adjust.sdk.test;

import android.content.Context;
import android.os.SystemClock;
import android.test.ActivityInstrumentationTestCase2;

import com.adjust.sdk.ActivityKind;
import com.adjust.sdk.ActivityPackage;
import com.adjust.sdk.AdjustFactory;
import com.adjust.sdk.PackageHandler;

/**
 * Created by pfms on 30/01/15.
 */
public class TestPackageHandler extends ActivityInstrumentationTestCase2<UnitTestActivity> {
    private MockLogger mockLogger;
    private MockActivityHandler mockActivityHandler;
    protected MockRequestHandler mockRequestHandler;
    private AssertUtil assertUtil;
    private UnitTestActivity activity;
    private Context context;


    public TestPackageHandler() {
        super(UnitTestActivity.class);
    }

    public TestPackageHandler(Class<UnitTestActivity> activityClass) {
        super(activityClass);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mockLogger = new MockLogger();
        mockActivityHandler = new MockActivityHandler(mockLogger);
        mockRequestHandler = new MockRequestHandler(mockLogger);

        assertUtil = new AssertUtil(mockLogger);

        AdjustFactory.setLogger(mockLogger);
        AdjustFactory.setRequestHandler(mockRequestHandler);

        activity = getActivity();
        context = activity.getApplicationContext();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        AdjustFactory.setRequestHandler(null);
        AdjustFactory.setLogger(null);
    }

    public void testAddPackage() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestPackageHandler testAddPackage");

        PackageHandler packageHandler = startPackageHandler();

        ActivityPackage firstClickPackage = createClickPackage("FirstPackage");

        packageHandler.addPackage(firstClickPackage);

        SystemClock.sleep(1000);

        addPackageTests(1, "clickFirstPackage");

        PackageHandler secondPackageHandler = addSecondPackageTest(null);

        ActivityPackage secondClickPackage = createClickPackage("ThirdPackage");

        secondPackageHandler.addPackage(secondClickPackage);

        SystemClock.sleep(1000);

        addPackageTests(3, "clickThirdPackage");

        // send the first click package/ first package
        secondPackageHandler.sendFirstPackage();
        SystemClock.sleep(1000);

        assertUtil.test("RequestHandler sendPackage, clickFirstPackage");

        // send the second click package/ third package
        secondPackageHandler.sendNextPackage(null);
        SystemClock.sleep(1000);

        assertUtil.test("RequestHandler sendPackage, clickThirdPackage");

        // send the unknow package/ second package
        secondPackageHandler.sendNextPackage(null);
        SystemClock.sleep(1000);

        assertUtil.test("RequestHandler sendPackage, unknownSecondPackage");
    }

    public void testSendFirst() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestPackageHandler testSendFirst");

        PackageHandler packageHandler = startPackageHandler();

        packageHandler.sendFirstPackage();
        SystemClock.sleep(1000);

        sendFirstTests(SendFirstState.EMPTY_QUEUE, null);

        addAndSendFirstPackageTest(packageHandler);

        // try to send when it is still sending
        packageHandler.sendFirstPackage();
        SystemClock.sleep(1000);

        sendFirstTests(SendFirstState.IS_SENDING, null);

        // try to send paused
        packageHandler.pauseSending();
        packageHandler.sendFirstPackage();
        SystemClock.sleep(1000);

        sendFirstTests(SendFirstState.PAUSED, null);

        // unpause, it's still sending
        packageHandler.resumeSending();
        packageHandler.sendFirstPackage();
        SystemClock.sleep(1000);

        sendFirstTests(SendFirstState.IS_SENDING, null);

        // verify that both paused and isSending are reset with a new session
        PackageHandler secondSessionPackageHandler = new PackageHandler(mockActivityHandler, context, false);

        secondSessionPackageHandler.sendFirstPackage();
        SystemClock.sleep(1000);

        // send the package to request handler
        sendFirstTests(SendFirstState.SEND, "unknownFirstPackage");
    }

    public void testSendNext() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestPackageHandler testSendNext");

        // init package handler
        PackageHandler packageHandler = startPackageHandler();

        // add and send the first package
        addAndSendFirstPackageTest(packageHandler);

        // try to send when it is still sending
        packageHandler.sendFirstPackage();
        SystemClock.sleep(1000);

        sendFirstTests(SendFirstState.IS_SENDING, null);

        // add a second package
        addSecondPackageTest(packageHandler);

        //send next package
        packageHandler.sendNextPackage(null);
        SystemClock.sleep(2000);

        assertUtil.debug("Package handler wrote 1 packages");

        // try to send the second package
        sendFirstTests(SendFirstState.SEND, "unknownSecondPackage");
    }

    public void testCloseFirstPackage() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestPackageHandler testCloseFirstPackage");

        PackageHandler packageHandler = startPackageHandler();

        addAndSendFirstPackageTest(packageHandler);

        // try to send when it is still sending
        packageHandler.sendFirstPackage();
        SystemClock.sleep(1000);

        sendFirstTests(SendFirstState.IS_SENDING, null);

        //send next package
        //packageHandler.closeFirstPackage(null);
        SystemClock.sleep(2000);

        assertUtil.notInDebug("Package handler wrote");

        packageHandler.sendFirstPackage();
        SystemClock.sleep(2000);

        // try to send the first package again
        sendFirstTests(SendFirstState.SEND, "unknownFirstPackage");
    }

    public void testCalls() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestPackageHandler testCalls");

        PackageHandler packageHandler = startPackageHandler();

        // TODO test "will retry later"

        assertUtil.test("ActivityHandler finishedTrackingActivity, null");
    }

    private PackageHandler startPackageHandler() {
        // delete package queue for fresh start
        deletePackageQueue();

        PackageHandler packageHandler = new PackageHandler(mockActivityHandler, context, false);

        SystemClock.sleep(1000);

        assertUtil.verbose("Package queue file not found");

        return packageHandler;
    }

    private PackageHandler addSecondPackageTest(PackageHandler packageHandler) {
        if (packageHandler == null) {
            packageHandler = new PackageHandler(mockActivityHandler, context, false);

            SystemClock.sleep(1000);

            // check that it can read the previously saved package
            assertUtil.debug("Package handler read 1 packages");
        }

        ActivityPackage secondActivityPackage = createUnknowPackage("SecondPackage");

        packageHandler.addPackage(secondActivityPackage);

        SystemClock.sleep(1000);

        addPackageTests(2, "unknownSecondPackage");

        return packageHandler;
    }

    private void addPackageTests(int packageNumber, String packageString) {
        assertUtil.debug("Added package " + packageNumber + " (" + packageString + ")");

        assertUtil.debug("Package handler wrote " + packageNumber + " packages");
    }

    private void addAndSendFirstPackageTest(PackageHandler packageHandler) {
        // add a package
        ActivityPackage activityPackage = createUnknowPackage("FirstPackage");

        // send the first package
        packageHandler.addPackage(activityPackage);

        packageHandler.sendFirstPackage();
        SystemClock.sleep(2000);

        addPackageTests(1, "unknownFirstPackage");

        sendFirstTests(SendFirstState.SEND, "unknownFirstPackage");
    }

    private enum SendFirstState {
        EMPTY_QUEUE, PAUSED, IS_SENDING, SEND
    }

    private void sendFirstTests(SendFirstState sendFirstState, String packageString) {
        if (sendFirstState == SendFirstState.PAUSED) {
            assertUtil.debug("Package handler is paused");
        } else {
            assertUtil.notInDebug("Package handler is paused");
        }

        if (sendFirstState == SendFirstState.IS_SENDING) {
            assertUtil.verbose("Package handler is already sending");
        } else {
            assertUtil.notInVerbose("Package handler is already sending");
        }

        if (sendFirstState == SendFirstState.SEND) {
            assertUtil.test("RequestHandler sendPackage, " + packageString);
        } else {
            assertUtil.notInTest("RequestHandler sendPackage");
        }
    }

    private void deletePackageQueue() {
        boolean packageQueueDeleted = PackageHandler.deletePackageQueue(context);

        mockLogger.test("Was PackageQueue deleted? " + packageQueueDeleted);
    }

    private ActivityPackage createUnknowPackage(String suffix) {
        ActivityPackage activityPackage = new ActivityPackage(ActivityKind.UNKNOWN);
        activityPackage.setSuffix(suffix);

        return activityPackage;
    }

    private ActivityPackage createClickPackage(String suffix) {
        ActivityPackage activityPackage = new ActivityPackage(ActivityKind.CLICK);
        activityPackage.setSuffix(suffix);

        return activityPackage;
    }
}
