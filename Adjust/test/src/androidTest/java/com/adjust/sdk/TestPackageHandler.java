package com.adjust.sdk;

import android.content.Context;
import android.os.SystemClock;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.adjust.sdk.test.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by pfms on 22/08/2016.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class TestPackageHandler {
    private MockLogger mockLogger;
    private MockActivityHandler mockActivityHandler;
    protected MockRequestHandler mockRequestHandler;
    private AssertUtil assertUtil;
    private com.adjust.sdk.test.UnitTestActivity activity;
    private Context context;

    @Rule
    public ActivityTestRule<com.adjust.sdk.test.UnitTestActivity> mActivityRule = new ActivityTestRule(com.adjust.sdk.test.UnitTestActivity.class);

    @Before
    public void setUp() {
        mockLogger = new MockLogger();
        mockActivityHandler = new MockActivityHandler(mockLogger);
        mockRequestHandler = new MockRequestHandler(mockLogger);

        assertUtil = new AssertUtil(mockLogger);

        AdjustFactory.setLogger(mockLogger);
        AdjustFactory.setRequestHandler(mockRequestHandler);

        activity = mActivityRule.getActivity();
        context = activity.getApplicationContext();
    }

    @After
    public void tearDown() {
        AdjustFactory.setRequestHandler(null);
        AdjustFactory.setLogger(null);
    }

    @Test
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

    @Test
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

    @Test
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

    @Test
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

        assertUtil.test("ActivityHandler finishedTrackingActivity, message:null timestamp:null json:null");
        assertUtil.verbose("Package handler can send");

        assertUtil.notInDebug("Package handler wrote");

        // tries to send the next package after sleeping
        sendFirstTests(SendFirstState.SEND, "unknownFirstPackage", 0);
    }

    @Test
    public void testBackoffJitter() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestPackageHandler testBackoffJitter");

        AdjustFactory.setPackageHandlerBackoffStrategy(BackoffStrategy.TEST_WAIT);

        PackageHandler packageHandler = startPackageHandler();

        ActivityPackage activityPackage = new ActivityPackage(ActivityKind.UNKNOWN);
        UnknownResponseData unknownResponseData = (UnknownResponseData) ResponseData.buildResponseData(activityPackage);
        Pattern pattern = Pattern.compile("Waiting for (\\d+\\.\\d) seconds before retrying the (\\d+) time");

        // 1st
        packageHandler.closeFirstPackage(unknownResponseData, activityPackage);
        SystemClock.sleep(1500);

        String matchingString = assertUtil.verbose("Waiting for ");
        // Waiting for 0.1 seconds before retrying the 1 time

        checkSleeping(pattern, matchingString, 0.1, 0.2, 1, 0.5, 1);

        // 2nd
        packageHandler.closeFirstPackage(unknownResponseData, activityPackage);
        SystemClock.sleep(1500);

        matchingString = assertUtil.verbose("Waiting for ");

        checkSleeping(pattern, matchingString, 0.2, 0.4, 1, 0.5, 2);

        // 3rd
        packageHandler.closeFirstPackage(unknownResponseData, activityPackage);
        SystemClock.sleep(1500);

        matchingString = assertUtil.verbose("Waiting for ");

        checkSleeping(pattern, matchingString, 0.4, 0.8, 1, 0.5, 3);

        // 4th
        packageHandler.closeFirstPackage(unknownResponseData, activityPackage);
        SystemClock.sleep(1500);

        matchingString = assertUtil.verbose("Waiting for ");

        checkSleeping(pattern, matchingString, 0.8, 1.6, 1, 0.5, 4);

        // 5th
        packageHandler.closeFirstPackage(unknownResponseData, activityPackage);
        SystemClock.sleep(1500);

        matchingString = assertUtil.verbose("Waiting for ");

        checkSleeping(pattern, matchingString, 1.6, 3.2, 1, 0.5, 5);

        // 6th
        packageHandler.closeFirstPackage(unknownResponseData, activityPackage);
        SystemClock.sleep(1500);

        matchingString = assertUtil.verbose("Waiting for ");

        checkSleeping(pattern, matchingString, 6.4, 12.8, 1, 0.5, 6);
    }

    @Test
    public void testUpdate() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestPackageHandler testUpdate");

        List<ActivityPackage> delayPackages = createDelayPackages();

        ActivityPackage firstSessionPackage = delayPackages.get(0);
        ActivityPackage firstEventPackage = delayPackages.get(1);
        ActivityPackage secondEventPackage = delayPackages.get(2);

        // create event package test
        TestActivityPackage testFirstSessionPackage = new TestActivityPackage(firstSessionPackage);

        testFirstSessionPackage.testSessionPackage(1);

        // create event package test
        TestActivityPackage testFirstEventPackage = new TestActivityPackage(firstEventPackage);

        // set event test parameters
        testFirstEventPackage.eventCount = "1";
        testFirstEventPackage.suffix = "'event1'";
        testFirstEventPackage.savedCallbackParameters = new HashMap<String,String>(1);
        testFirstEventPackage.savedCallbackParameters.put("ceFoo", "ceBar");
        testFirstEventPackage.savedPartnerParameters = new HashMap<String,String>(1);
        testFirstEventPackage.savedPartnerParameters.put("peFoo", "peBar");

        // test first event
        testFirstEventPackage.testEventPackage("event1");

        // create event package test
        TestActivityPackage testSecondEventPackage = new TestActivityPackage(secondEventPackage);

        // set event test parameters
        testSecondEventPackage.eventCount = "2";
        testSecondEventPackage.suffix = "'event2'";
        testSecondEventPackage.savedCallbackParameters = new HashMap<String,String>(1);
        testSecondEventPackage.savedCallbackParameters.put("scpKey", "ceBar");
        testSecondEventPackage.savedPartnerParameters = new HashMap<String,String>(1);
        testSecondEventPackage.savedPartnerParameters.put("sppKey", "peBar");

        // test second event
        testSecondEventPackage.testEventPackage("event2");

        //  initialize Package Handler
        IPackageHandler packageHandler = startPackageHandler();

        sendFirstTests(SendFirstState.EMPTY_QUEUE, null, null);

        packageHandler.addPackage(firstSessionPackage);
        packageHandler.addPackage(firstEventPackage);
        packageHandler.addPackage(secondEventPackage);

        SystemClock.sleep(1000);

        addPackageTests(1, "session");

        addPackageTests(2, "event'event1'");

        addPackageTests(3, "event'event2'");

        packageHandler.updatePackages(null);

        SystemClock.sleep(1000);

        assertUtil.notInDebug("Updating package handler queue");

        SessionParameters emptySessionParameters = new SessionParameters();
        packageHandler.updatePackages(emptySessionParameters);

        SystemClock.sleep(1000);

        assertUtil.debug("Updating package handler queue");

        assertUtil.verbose("Session external device id: null");
        assertUtil.verbose("Session callback parameters: null");
        assertUtil.verbose("Session partner parameters: null");

        // writes the non-updated packages
        assertUtil.debug("Wrote Package queue: [session, event'event1', event'event2']");
        assertUtil.debug("Package handler wrote 3 packages");

        SessionParameters sessionParameters = new SessionParameters();
        sessionParameters.externalDeviceId = "sedi";
        sessionParameters.callbackParameters = new HashMap<String, String>(1);
        sessionParameters.callbackParameters.put("scpKey", "scpValue");
        sessionParameters.partnerParameters = new HashMap<String, String>(1);
        sessionParameters.partnerParameters.put("sppKey", "sppValue");

        packageHandler.updatePackages(sessionParameters);

        SystemClock.sleep(1000);

        assertUtil.debug("Updating package handler queue");

        assertUtil.verbose("Session external device id: sedi");
        assertUtil.verbose("Session callback parameters: {scpKey=scpValue}");
        assertUtil.verbose("Session partner parameters: {sppKey=sppValue}");

        assertUtil.warn("Key scpKey with value scpValue from Callback parameter was replaced by value ceBar");
        assertUtil.warn("Key sppKey with value sppValue from Partner parameter was replaced by value peBar");
        assertUtil.debug("Package handler wrote 3 packages");

        testFirstSessionPackage.externalDeviceId = "sedi";
        testFirstSessionPackage.callbackParams = "{scpKey=scpValue}";
        testFirstSessionPackage.partnerParams = "{sppKey=sppValue}";
        testFirstSessionPackage.testSessionPackage(1);

        testFirstEventPackage.externalDeviceId = "sedi";
        testFirstEventPackage.callbackParams = "{scpKey=scpValue, ceFoo=ceBar}";
        testFirstEventPackage.partnerParams = "{sppKey=sppValue, peFoo=peBar}";

        testFirstEventPackage.testEventPackage("event1");

        testSecondEventPackage.externalDeviceId = "sedi";
        testSecondEventPackage.callbackParams = "{scpKey=ceBar}";
        testSecondEventPackage.partnerParams = "{sppKey=peBar}";

        testSecondEventPackage.testEventPackage("event2");
    }

    private List<ActivityPackage> createDelayPackages() {
        MockPackageHandler mockPackageHandler = new MockPackageHandler(mockLogger);
        AdjustFactory.setPackageHandler(mockPackageHandler);

        MockSdkClickHandler mockSdkClickHandler = new MockSdkClickHandler(mockLogger);
        AdjustFactory.setSdkClickHandler(mockSdkClickHandler);

        MockAttributionHandler mockAttributionHandler = new MockAttributionHandler(mockLogger);
        AdjustFactory.setAttributionHandler(mockAttributionHandler);

        AdjustFactory.setSessionInterval(-1);
        AdjustFactory.setSubsessionInterval(-1);
        AdjustFactory.setTimerInterval(-1);
        AdjustFactory.setTimerStart(-1);

        ActivityHandler.deleteActivityState(context);
        ActivityHandler.deleteAttribution(context);
        ActivityHandler.deleteSessionParameters(context);
        ActivityHandler.deleteSessionCallbackParameters(context);
        ActivityHandler.deleteSessionPartnerParameters(context);

        AdjustConfig config = new AdjustConfig(context, "123456789012", "sandbox");

        config.setDelayStart(4);

        IActivityHandler activityHandler = ActivityHandler.getInstance(config);

        activityHandler.addSessionCallbackParameter("scpKey", "scpValue");
        activityHandler.addSessionPartnerParameter("sppKey", "sppValue");

        activityHandler.onResume();

        AdjustEvent firstEvent = new AdjustEvent("event1");
        firstEvent.addCallbackParameter("ceFoo", "ceBar");
        firstEvent.addPartnerParameter("peFoo", "peBar");
        activityHandler.trackEvent(firstEvent);

        AdjustEvent secondEvent = new AdjustEvent("event2");
        secondEvent.addCallbackParameter("scpKey", "ceBar");
        secondEvent.addPartnerParameter("sppKey", "peBar");
        activityHandler.trackEvent(secondEvent);

        SystemClock.sleep(3000);

        ActivityPackage firstSessionPackage = mockPackageHandler.queue.get(0);
        ActivityPackage firstEventPackage = mockPackageHandler.queue.get(1);
        ActivityPackage secondEventPackage = mockPackageHandler.queue.get(2);

        mockLogger.reset();

        return Arrays.asList(firstSessionPackage, firstEventPackage, secondEventPackage);
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
