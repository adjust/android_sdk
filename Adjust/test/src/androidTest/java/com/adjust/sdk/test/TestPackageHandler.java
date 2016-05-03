package com.adjust.sdk.test;

import android.content.Context;
import android.os.SystemClock;
import android.test.ActivityInstrumentationTestCase2;

import com.adjust.sdk.ActivityKind;
import com.adjust.sdk.ActivityPackage;
import com.adjust.sdk.AdjustFactory;
import com.adjust.sdk.BackoffStrategy;
import com.adjust.sdk.Constants;
import com.adjust.sdk.PackageHandler;
import com.adjust.sdk.ResponseData;
import com.adjust.sdk.UnknownResponseData;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        assertUtil.test("RequestHandler sendPackage, activityPackage clickFirstPackage");
        assertUtil.test("RequestHandler sendPackage, queueSize 2");

        // send the second click package/ third package
        secondPackageHandler.sendNextPackage(null);
        SystemClock.sleep(1000);

        assertUtil.test("RequestHandler sendPackage, activityPackage unknownSecondPackage");
        assertUtil.test("RequestHandler sendPackage, queueSize 1");

        // send the unknow package/ second package
        secondPackageHandler.sendNextPackage(null);
        SystemClock.sleep(1000);

        assertUtil.test("RequestHandler sendPackage, activityPackage clickThirdPackage");
        assertUtil.test("RequestHandler sendPackage, queueSize 0");
    }

    public void testSendFirst() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestPackageHandler testSendFirst");

        PackageHandler packageHandler = startPackageHandler();

        packageHandler.sendFirstPackage();
        SystemClock.sleep(1000);

        sendFirstTests(SendFirstState.EMPTY_QUEUE, null, null);

        addAndSendFirstPackageTest(packageHandler);

        // try to send when it is still sending
        packageHandler.sendFirstPackage();
        SystemClock.sleep(1000);

        sendFirstTests(SendFirstState.IS_SENDING, null, null);

        // try to send paused
        packageHandler.pauseSending();
        packageHandler.sendFirstPackage();
        SystemClock.sleep(1000);

        sendFirstTests(SendFirstState.PAUSED, null, null);

        // unpause, it's still sending
        packageHandler.resumeSending();
        packageHandler.sendFirstPackage();
        SystemClock.sleep(1000);

        sendFirstTests(SendFirstState.IS_SENDING, null, null);

        // verify that both paused and isSending are reset with a new session
        PackageHandler secondSessionPackageHandler = new PackageHandler(mockActivityHandler, context, true);

        secondSessionPackageHandler.sendFirstPackage();
        SystemClock.sleep(1000);

        // send the package to request handler
        sendFirstTests(SendFirstState.SEND, "unknownFirstPackage", 0);
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

        sendFirstTests(SendFirstState.IS_SENDING, null, null);

        // add a second package
        addSecondPackageTest(packageHandler);

        //send next package
        packageHandler.sendNextPackage(null);
        SystemClock.sleep(2000);

        assertUtil.debug("Package handler wrote 1 packages");

        // try to send the second package
        sendFirstTests(SendFirstState.SEND, "unknownSecondPackage", 0);
    }

    public void testCloseFirstPackage() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestPackageHandler testCloseFirstPackage");
        AdjustFactory.setPackageHandlerBackoffStrategy(BackoffStrategy.NO_WAIT);

        PackageHandler packageHandler = startPackageHandler();

        addAndSendFirstPackageTest(packageHandler);

        // try to send when it is still sending
        packageHandler.sendFirstPackage();
        SystemClock.sleep(1000);

        sendFirstTests(SendFirstState.IS_SENDING, null, null);

        //send next package
        ActivityPackage activityPackage = new ActivityPackage(ActivityKind.UNKNOWN);
        UnknownResponseData unknownResponseData = (UnknownResponseData) ResponseData.buildResponseData(activityPackage);
        packageHandler.closeFirstPackage(unknownResponseData, null);
        SystemClock.sleep(2000);
        assertUtil.verbose("Package handler can send");
        assertUtil.test("ActivityHandler finishedTrackingActivity, message:null timestamp:null json:null");

        assertUtil.notInDebug("Package handler wrote");

        // tries to send the next package after sleeping
        sendFirstTests(SendFirstState.SEND, "unknownFirstPackage", 0);
    }

    public void testBackoffJitter() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestPackageHandler testBackoffJitter");

        AdjustFactory.setPackageHandlerBackoffStrategy(BackoffStrategy.TEST_WAIT);

        PackageHandler packageHandler = startPackageHandler();

        ActivityPackage activityPackage = new ActivityPackage(ActivityKind.UNKNOWN);
        UnknownResponseData unknownResponseData = (UnknownResponseData) ResponseData.buildResponseData(activityPackage);
        Pattern pattern = Pattern.compile("Sleeping for (\\d+\\.\\d) seconds before retrying the (\\d+) time");

        // 1st
        packageHandler.closeFirstPackage(unknownResponseData, activityPackage);
        SystemClock.sleep(1500);

        String matchingString = assertUtil.verbose("Sleeping for ");
        // Sleeping for 0.1 seconds before retrying the 1 time

        checkSleeping(pattern, matchingString, 0.1, 0.2, 1, 0.5, 1);

        // 2nd
        packageHandler.closeFirstPackage(unknownResponseData, activityPackage);
        SystemClock.sleep(1500);

        matchingString = assertUtil.verbose("Sleeping for ");

        checkSleeping(pattern, matchingString, 0.2, 0.4, 1, 0.5, 2);

        // 3rd
        packageHandler.closeFirstPackage(unknownResponseData, activityPackage);
        SystemClock.sleep(1500);

        matchingString = assertUtil.verbose("Sleeping for ");

        checkSleeping(pattern, matchingString, 0.4, 0.8, 1, 0.5, 3);

        // 4th
        packageHandler.closeFirstPackage(unknownResponseData, activityPackage);
        SystemClock.sleep(1500);

        matchingString = assertUtil.verbose("Sleeping for ");

        checkSleeping(pattern, matchingString, 0.8, 1.6, 1, 0.5, 4);

        // 5th
        packageHandler.closeFirstPackage(unknownResponseData, activityPackage);
        SystemClock.sleep(1500);

        matchingString = assertUtil.verbose("Sleeping for ");

        checkSleeping(pattern, matchingString, 1.6, 3.2, 1, 0.5, 5);

        // 6th
        packageHandler.closeFirstPackage(unknownResponseData, activityPackage);
        SystemClock.sleep(1500);

        matchingString = assertUtil.verbose("Sleeping for ");

        checkSleeping(pattern, matchingString, 6.4, 12.8, 1, 0.5, 6);
   }

    private void checkSleeping(Pattern pattern,
                               String sleepingLog,
                               double minRange,
                               double maxRange,
                               long maxCeiling,
                               double minCeiling,
                               int numberRetries)
    {
        Matcher matcher = pattern.matcher(sleepingLog);

        if (!matcher.matches()) {
            assertUtil.fail();
        }
        Double sleepingTime = Double.valueOf(matcher.group(1));

        boolean failsCeiling = sleepingTime > maxCeiling;
        assertUtil.isFalse(failsCeiling);

        if (maxRange < maxCeiling) {
            boolean failsMinRange = sleepingTime < minRange;
            assertUtil.isFalse(failsMinRange);
        } else {
            boolean failsMinRange = sleepingTime < minCeiling ;
            assertUtil.isFalse(failsMinRange);
        }

        if (maxRange < maxCeiling) {
            boolean failsMaxRange = sleepingTime > maxRange;
            assertUtil.isFalse(failsMaxRange);
        } else {
            boolean failsMaxRange = sleepingTime > maxCeiling;
            assertUtil.isFalse(failsMaxRange);
        }

        Integer retryTime = Integer.valueOf(matcher.group(2));
        assertUtil.isEqual(numberRetries, retryTime);
    }

    private PackageHandler startPackageHandler() {
        // delete package queue for fresh start
        deletePackageQueue();

        PackageHandler packageHandler = new PackageHandler(mockActivityHandler, context, true);

        SystemClock.sleep(1000);

        assertUtil.debug("Package queue file not found");

        return packageHandler;
    }

    private PackageHandler addSecondPackageTest(PackageHandler packageHandler) {
        if (packageHandler == null) {
            packageHandler = new PackageHandler(mockActivityHandler, context, true);

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

        sendFirstTests(SendFirstState.SEND, "unknownFirstPackage", 0);
    }

    private enum SendFirstState {
        EMPTY_QUEUE, PAUSED, IS_SENDING, SEND
    }

    private void sendFirstTests(SendFirstState sendFirstState, String packageString, Integer queueSize) {
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
            assertUtil.test("RequestHandler sendPackage, activityPackage " + packageString);
            assertUtil.test("RequestHandler sendPackage, queueSize " + queueSize);
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
