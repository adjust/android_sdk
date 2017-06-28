package com.adjust.sdk;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.SystemClock;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.mock.MockContext;
import android.test.suitebuilder.annotation.LargeTest;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by pfms on 08/08/2016.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class TestActivityHandler {
    protected MockLogger mockLogger;
    protected MockPackageHandler mockPackageHandler;
    protected MockAttributionHandler mockAttributionHandler;
    protected MockSdkClickHandler mockSdkClickHandler;
    protected com.adjust.sdk.test.UnitTestActivity activity;
    protected Context context;
    protected AssertUtil assertUtil;

    @Rule
    public ActivityTestRule<com.adjust.sdk.test.UnitTestActivity> mActivityRule = new ActivityTestRule(com.adjust.sdk.test.UnitTestActivity.class);

    @Before
    public void setUp() {
        mockLogger = new MockLogger();
        mockPackageHandler = new MockPackageHandler(mockLogger);
        mockAttributionHandler = new MockAttributionHandler(mockLogger);
        mockSdkClickHandler = new MockSdkClickHandler(mockLogger);
        assertUtil = new AssertUtil(mockLogger);

        AdjustFactory.setLogger(mockLogger);
        AdjustFactory.setPackageHandler(mockPackageHandler);
        AdjustFactory.setAttributionHandler(mockAttributionHandler);
        AdjustFactory.setSdkClickHandler(mockSdkClickHandler);

        //activity = launchActivity(null);
        activity = mActivityRule.getActivity();
        context = activity.getApplicationContext();

        // deleting state to simulate fresh install
        mockLogger.test("Was AdjustActivityState deleted? " + ActivityHandler.deleteActivityState(context));
        mockLogger.test("Was Attribution deleted? " + ActivityHandler.deleteAttribution(context));
        mockLogger.test("Was Session Callback Parameters deleted? " + ActivityHandler.deleteSessionCallbackParameters(context));
        mockLogger.test("Was Session Partner Parameters deleted? " + ActivityHandler.deleteSessionPartnerParameters(context));

        // check the server url
        assertUtil.isEqual(Constants.BASE_URL, "https://app.adjust.com");
    }

    @After
    public void tearDown() {
        AdjustFactory.setPackageHandler(null);
        AdjustFactory.setAttributionHandler(null);
        AdjustFactory.setSdkClickHandler(null);
        AdjustFactory.setLogger(null);
        AdjustFactory.setTimerInterval(-1);
        AdjustFactory.setTimerStart(-1);
        AdjustFactory.setSessionInterval(-1);
        AdjustFactory.setSubsessionInterval(-1);

        activity = null;
        context = null;
    }

    @Test
    public void testFirstSession() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testFirstSession");

        // create the config to start the session
        AdjustConfig config = getConfig();

        // start activity handler with config
        ActivityHandler activityHandler = startAndCheckFirstSession(config);

        // checking the default values of the first session package
        // should only have one package
        assertUtil.isEqual(1, mockPackageHandler.queue.size());

        ActivityPackage activityPackage = mockPackageHandler.queue.get(0);

        // create activity package test
        TestActivityPackage testActivityPackage = new TestActivityPackage(activityPackage);

        // set first session
        testActivityPackage.testSessionPackage(1);
    }

    @Test
    public void testEventsBuffered() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testEventsBuffered");

        // create the config to start the session
        AdjustConfig config = getConfig();

        // buffer events
        config.setEventBufferingEnabled(true);

        // set default tracker
        config.setDefaultTracker("default1234tracker");

        //  create handler and start the first session
        ActivityHandler activityHandler = getActivityHandler(config);

        SystemClock.sleep(1500);

        // test init values
        StateActivityHandlerInit initState = new StateActivityHandlerInit(activityHandler);

        initState.eventBufferingIsEnabled = true;
        initState.defaultTracker = "default1234tracker";

        StateSession stateSession = new StateSession(StateSession.SessionType.NEW_SESSION);
        stateSession.eventBufferingIsEnabled = true;

        checkInitAndStart(activityHandler, initState, stateSession);

        // create the first Event
        AdjustEvent firstEvent = new AdjustEvent("event1");

        // add callback parameters
        firstEvent.addCallbackParameter("keyCall", "valueCall");
        firstEvent.addCallbackParameter("keyCall", "valueCall2");
        firstEvent.addCallbackParameter("fooCall", "barCall");

        // add partner paramters
        firstEvent.addPartnerParameter("keyPartner", "valuePartner");
        firstEvent.addPartnerParameter("keyPartner", "valuePartner2");
        firstEvent.addPartnerParameter("fooPartner", "barPartner");

        // check that callback parameter was overwritten
        assertUtil.warn("Key keyCall was overwritten");

        // check that partner parameter was overwritten
        assertUtil.warn("Key keyPartner was overwritten");

        // add revenue
        firstEvent.setRevenue(0.001, "EUR");

        // set order id
        firstEvent.setOrderId("orderIdTest");

        // track event
        activityHandler.trackEvent(firstEvent);

        SystemClock.sleep(1500);

        StateEvent stateEvent1 = new StateEvent();
        stateEvent1.orderId = "orderIdTest";
        stateEvent1.bufferedSuffix = "(0.00100 EUR, 'event1')";
        stateEvent1.activityStateSuffix = "ec:1";

        checkEvent(stateEvent1);

        // create the second Event
        AdjustEvent secondEvent = new AdjustEvent("event2");

        // set order id
        secondEvent.setOrderId("orderIdTest");

        // track second event
        activityHandler.trackEvent(secondEvent);

        SystemClock.sleep(1500);

        StateEvent stateEvent2 = new StateEvent();
        stateEvent2.duplicatedOrderId = true;
        stateEvent2.orderId = "orderIdTest";

        checkEvent(stateEvent2);

        // create third Event
        AdjustEvent thirdEvent = new AdjustEvent("event3");

        // set order id
        thirdEvent.setOrderId("otherOrderId");

        // add empty revenue
        thirdEvent.setRevenue(0, "USD");

        // track third event
        activityHandler.trackEvent(thirdEvent);

        SystemClock.sleep(1500);

        StateEvent stateEvent3 = new StateEvent();
        stateEvent3.orderId = "otherOrderId";
        stateEvent3.bufferedSuffix = "(0.00000 USD, 'event3')";
        stateEvent3.activityStateSuffix = "ec:2";

        checkEvent(stateEvent3);

        // create a forth Event object without revenue
        AdjustEvent forthEvent = new AdjustEvent("event4");

        // track third event
        activityHandler.trackEvent(forthEvent);

        SystemClock.sleep(1500);

        StateEvent stateEvent4 = new StateEvent();
        stateEvent4.bufferedSuffix = "'event4'";
        stateEvent4.activityStateSuffix = "ec:3";

        checkEvent(stateEvent4);

        // check the number of activity packages
        // 1 session + 3 events
        assertUtil.isEqual(4, mockPackageHandler.queue.size());

        ActivityPackage firstSessionPackage = mockPackageHandler.queue.get(0);

        // create activity package test
        TestActivityPackage testFirstSessionPackage = new TestActivityPackage(firstSessionPackage);
        testFirstSessionPackage.eventBufferingEnabled = true;
        testFirstSessionPackage.defaultTracker = "default1234tracker";

        // set first session
        testFirstSessionPackage.testSessionPackage(1);

        // first event
        ActivityPackage firstEventPackage = mockPackageHandler.queue.get(1);

        // create event package test
        TestActivityPackage testFirstEventPackage = new TestActivityPackage(firstEventPackage);

        // set event test parameters
        testFirstEventPackage.eventCount = "1";
        testFirstEventPackage.suffix = "(0.00100 EUR, 'event1')";
        testFirstEventPackage.revenueString = "0.00100";
        testFirstEventPackage.currency = "EUR";
        testFirstEventPackage.callbackParams = "{\"keyCall\":\"valueCall2\",\"fooCall\":\"barCall\"}";
        testFirstEventPackage.partnerParams = "{\"keyPartner\":\"valuePartner2\",\"fooPartner\":\"barPartner\"}";
        testFirstEventPackage.eventBufferingEnabled = true;

        // test first event
        testFirstEventPackage.testEventPackage("event1");

        // second event
        ActivityPackage secondEventPackage = mockPackageHandler.queue.get(2);

        // create event package test
        TestActivityPackage testSecondEventPackage = new TestActivityPackage(secondEventPackage);

        // set event test parameters
        testSecondEventPackage.eventCount = "2";
        testSecondEventPackage.suffix = "(0.00000 USD, 'event3')";
        testSecondEventPackage.revenueString = "0.00000";
        testSecondEventPackage.currency = "USD";
        testSecondEventPackage.eventBufferingEnabled = true;

        // test second event
        testSecondEventPackage.testEventPackage("event3");

        // third event
        ActivityPackage thirdEventPackage = mockPackageHandler.queue.get(3);

        // create event package test
        TestActivityPackage testThirdEventPackage = new TestActivityPackage(thirdEventPackage);

        // set event test parameters
        testThirdEventPackage.eventCount = "3";
        testThirdEventPackage.suffix = "'event4'";
        testThirdEventPackage.eventBufferingEnabled = true;

        // test third event
        testThirdEventPackage.testEventPackage("event4");
    }

    @Test
    public void testEventsNotBuffered() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testEventsNotBuffered");

        // create the config to start the session
        AdjustConfig config = getConfig();

        // start activity handler with config
        ActivityHandler activityHandler = startAndCheckFirstSession(config);

        // create the first Event
        AdjustEvent firstEvent = new AdjustEvent("event1");

        // track event
        activityHandler.trackEvent(firstEvent);

        SystemClock.sleep(1500);

        StateEvent stateEvent = new StateEvent();
        checkEvent(stateEvent);
    }

    @Test
    public void testEventBeforeStart() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testEventBeforeStart");

        // create the config to start the session
        AdjustConfig config = getConfig();

        // create the first Event
        AdjustEvent firstEvent = new AdjustEvent("event1");

        //  create handler and start the first session
        ActivityHandler activityHandler = getActivityHandler(config);

        // track event
        activityHandler.trackEvent(firstEvent);

        SystemClock.sleep(1500);

        // test init values
        checkInitTests(activityHandler);

        // does not start the activity because it was started by the track event

        // test session
        StateSession stateSession= new StateSession(StateSession.SessionType.NEW_SESSION);
        // does not start session
        stateSession.startSubsession = false;
        stateSession.toSend = false;

        checkStartInternal(stateSession);

        StateEvent stateEvent = new StateEvent();
        checkEvent(stateEvent);
    }

    @Test
    public void testChecks() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testChecks");

        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testChecks");

        // config with null app token
        AdjustConfig nullAppTokenConfig = new AdjustConfig(context, null, AdjustConfig.ENVIRONMENT_SANDBOX);

        assertUtil.error("Missing App Token");
        assertUtil.isFalse(nullAppTokenConfig.isValid());

        // config with wrong size app token
        AdjustConfig oversizeAppTokenConfig = new AdjustConfig(context, "1234567890123", AdjustConfig.ENVIRONMENT_SANDBOX);

        assertUtil.error("Malformed App Token '1234567890123'");
        assertUtil.isFalse(oversizeAppTokenConfig.isValid());

        // config with null environment
        AdjustConfig nullEnvironmentConfig = new AdjustConfig(context, "123456789012", null);

        assertUtil.error("Missing environment");
        assertUtil.isFalse(nullEnvironmentConfig.isValid());

        // config with wrong environment
        AdjustConfig wrongEnvironmentConfig = new AdjustConfig(context, "123456789012", "Other");

        assertUtil.error("Unknown environment 'Other'");
        assertUtil.isFalse(wrongEnvironmentConfig.isValid());

        // config with null context
        AdjustConfig nullContextConfig = new AdjustConfig(null, "123456789012", AdjustConfig.ENVIRONMENT_SANDBOX);

        assertUtil.error("Missing context");
        assertUtil.isFalse(nullContextConfig.isValid());

        // config without internet permission
        Context mockContext = new MockContext() {
            @Override
            public int checkCallingOrSelfPermission(String permission) {
                return PackageManager.PERMISSION_DENIED;
            }
        };
        AdjustConfig mockContextConfig = new AdjustConfig(mockContext, "123456789012", AdjustConfig.ENVIRONMENT_SANDBOX);

        assertUtil.error("Missing permission: INTERNET");
        assertUtil.isFalse(mockContextConfig.isValid());

        // config without access wifi state permission
        // TODO

        // start with null config
        ActivityHandler nullConfigactivityHandler = ActivityHandler.getInstance(null);

        assertUtil.error("AdjustConfig missing");
        assertUtil.isNull(nullConfigactivityHandler);

        ActivityHandler invalidConfigactivityHandler = ActivityHandler.getInstance(nullAppTokenConfig);

        assertUtil.error("AdjustConfig not initialized correctly");
        assertUtil.isNull(invalidConfigactivityHandler);

        // event with null event token
        AdjustEvent nullEventToken = new AdjustEvent(null);

        assertUtil.error("Missing Event Token");
        assertUtil.isFalse(nullEventToken.isValid());

        // event with wrong size
        AdjustEvent wrongEventTokenSize = new AdjustEvent("eventXX");

        assertUtil.error("Malformed Event Token 'eventXX'");
        assertUtil.isFalse(wrongEventTokenSize.isValid());

        // event
        AdjustEvent event = new AdjustEvent("event1");

        // event with negative revenue
        event.setRevenue(-0.001, "EUR");

        assertUtil.error("Invalid amount -0.001");

        // event with null currency
        event.setRevenue(0, null);

        assertUtil.error("Currency must be set with revenue");

        // event with empty currency
        event.setRevenue(0, "");

        assertUtil.error("Currency is empty");

        // callback parameter null key
        event.addCallbackParameter(null, "callValue");

        assertUtil.error("Callback parameter key is missing");

        // callback parameter empty key
        event.addCallbackParameter("", "callValue");

        assertUtil.error("Callback parameter key is empty");

        // callback parameter null value
        event.addCallbackParameter("keyCall", null);

        assertUtil.error("Callback parameter value is missing");

        // callback parameter empty value
        event.addCallbackParameter("keyCall", "");

        assertUtil.error("Callback parameter value is empty");

        // partner parameter null key
        event.addPartnerParameter(null, "callValue");

        assertUtil.error("Partner parameter key is missing");

        // partner parameter empty key
        event.addPartnerParameter("", "callValue");

        assertUtil.error("Partner parameter key is empty");

        // partner parameter null value
        event.addPartnerParameter("keyCall", null);

        assertUtil.error("Partner parameter value is missing");

        // partner parameter empty value
        event.addPartnerParameter("keyCall", "");

        assertUtil.error("Partner parameter value is empty");

        // create the config with wrong process name
        AdjustConfig configWrongProcess = new AdjustConfig(context, "123456789012", AdjustConfig.ENVIRONMENT_SANDBOX);
        configWrongProcess.setProcessName("com.wrong.process");

        // create handler and start the first session
        ActivityHandler.getInstance(configWrongProcess);

        assertUtil.info("Skipping initialization in background process (com.adjust.sdk.test.test)");

        // create the config with correct process name
        AdjustConfig configCorrectProcess = new AdjustConfig(context, "123456789012", AdjustConfig.ENVIRONMENT_SANDBOX);
        configCorrectProcess.setProcessName("com.adjust.sdk.test.test");

        // create handler and start the first session
        ActivityHandler.getInstance(configCorrectProcess);

        assertUtil.notInInfo("Skipping initialization in background process");

        // create the config to start the session
        AdjustConfig config = getConfig();

        // create handler and start the first session
        ActivityHandler activityHandler = startAndCheckFirstSession(config);

        // track null event
        activityHandler.trackEvent(null);
        SystemClock.sleep(1000);

        assertUtil.error("Event missing");

        activityHandler.trackEvent(nullEventToken);
        SystemClock.sleep(1000);

        assertUtil.error("Event not initialized correctly");

        activityHandler.resetSessionCallbackParameters();
        activityHandler.resetSessionPartnerParameters();

        activityHandler.removeSessionCallbackParameter(null);
        activityHandler.removeSessionCallbackParameter("");
        activityHandler.removeSessionCallbackParameter("nonExistent");

        activityHandler.removeSessionPartnerParameter(null);
        activityHandler.removeSessionPartnerParameter("");
        activityHandler.removeSessionPartnerParameter("nonExistent");

        activityHandler.addSessionCallbackParameter(null, "value");
        activityHandler.addSessionCallbackParameter("", "value");

        activityHandler.addSessionCallbackParameter("key", null);
        activityHandler.addSessionCallbackParameter("key", "");

        activityHandler.addSessionPartnerParameter(null, "value");
        activityHandler.addSessionPartnerParameter("", "value");

        activityHandler.addSessionPartnerParameter("key", null);
        activityHandler.addSessionPartnerParameter("key", "");

        activityHandler.removeSessionCallbackParameter("nonExistent");
        activityHandler.removeSessionPartnerParameter("nonExistent");

        SystemClock.sleep(1500);

        assertUtil.warn("Session Callback parameters are not set");
        assertUtil.warn("Session Partner parameters are not set");

        assertUtil.error("Session Callback parameter key is missing");
        assertUtil.error("Session Callback parameter key is empty");
        assertUtil.warn("Session Callback parameters are not set");

        assertUtil.error("Session Partner parameter key is missing");
        assertUtil.error("Session Partner parameter key is empty");
        assertUtil.warn("Session Partner parameters are not set");

        assertUtil.error("Session Callback parameter key is missing");
        assertUtil.error("Session Callback parameter key is empty");
        assertUtil.error("Session Callback parameter value is missing");
        assertUtil.error("Session Callback parameter value is empty");

        assertUtil.error("Session Partner parameter key is missing");
        assertUtil.error("Session Partner parameter key is empty");
        assertUtil.error("Session Partner parameter value is missing");
        assertUtil.error("Session Partner parameter value is empty");

        assertUtil.warn("Session Callback parameters are not set");
        assertUtil.warn("Session Partner parameters are not set");
    }

    @Test
    public void testSessions() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testSessions");

        // adjust the session intervals for testing
        AdjustFactory.setSessionInterval(4000);

        // create the config to start the session
        AdjustConfig config = getConfig();

        // start activity handler with config
        ActivityHandler activityHandler = startAndCheckFirstSession(config);

        // end subsession
        stopActivity(activityHandler);

        SystemClock.sleep(2000);

        // test the end of the subsession
        checkEndSession();

        // start a subsession
        resumeActivity(activityHandler);

        SystemClock.sleep(2000);

        // test the new sub session
        StateSession secondSubsession = new StateSession(StateSession.SessionType.NEW_SUBSESSION);
        secondSubsession.subsessionCount = 2;
        checkStartInternal(secondSubsession);

        stopActivity(activityHandler);

        SystemClock.sleep(5000);

        // test the end of the subsession
        checkEndSession();

        // trigger a new session
        activityHandler.onResume();

        SystemClock.sleep(1500);

        // new session
        StateSession secondSession = new StateSession(StateSession.SessionType.NEW_SESSION);
        secondSession.sessionCount = 2;
        checkStartInternal(secondSession);

        // stop and start the activity with little interval
        // so it won't trigger a sub session
        stopActivity(activityHandler);
        resumeActivity(activityHandler);

        SystemClock.sleep(1500);

        // test the end of the subsession
        StateEndSession stateEndSession = new StateEndSession();
        stateEndSession.pausing = false;

        checkEndSession(stateEndSession);

        // test non sub session
        StateSession nonSessionState = new StateSession(StateSession.SessionType.NONSESSION);
        checkStartInternal(nonSessionState);

        // 2 session packages
        assertUtil.isEqual(2, mockPackageHandler.queue.size());

        ActivityPackage firstSessionActivityPackage = mockPackageHandler.queue.get(0);

        // create activity package test
        TestActivityPackage testFirstSessionActivityPackage = new TestActivityPackage(firstSessionActivityPackage);

        // test first session
        testFirstSessionActivityPackage.testSessionPackage(1);

        // get second session package
        ActivityPackage secondSessionActivityPackage = mockPackageHandler.queue.get(1);

        // create second session test package
        TestActivityPackage testSecondSessionActivityPackage = new TestActivityPackage(secondSessionActivityPackage);

        // check if it saved the second subsession in the new package
        testSecondSessionActivityPackage.subsessionCount = 2;

        // test second session
        testSecondSessionActivityPackage.testSessionPackage(2);
    }

    @Test
    public void testDisable() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testDisable");

        // adjust the session intervals for testing
        AdjustFactory.setSessionInterval(4000);

        // create the config to start the session
        AdjustConfig config = getConfig();

        //  create handler and start the first session
        ActivityHandler activityHandler = getActivityHandler(config);

        // check that it is enabled
        assertUtil.isTrue(activityHandler.isEnabled());

        // disable sdk
        activityHandler.setEnabled(false);

        // check that it is disabled
        assertUtil.isFalse(activityHandler.isEnabled());

        SystemClock.sleep(1500);

        // not writing activity state because it set enable does not start the sdk
        assertUtil.notInDebug("Wrote Activity state");

        // check if message the disable of the SDK
        assertUtil.info("Handlers will start as paused due to the SDK being disabled");

        StateActivityHandlerInit stateActivityHandlerInit = new StateActivityHandlerInit(activityHandler);
        stateActivityHandlerInit.startsSending = false;
        stateActivityHandlerInit.startEnabled = false;

        checkInitTests(stateActivityHandlerInit);

        checkHandlerStatus(true);

        // start the sdk
        // foreground timer does not start because it's paused
        resumeActivity(activityHandler);
        AdjustEvent firstEvent = new AdjustEvent("event1");

        activityHandler.trackEvent(firstEvent);
        SystemClock.sleep(1500);

        // check initial created session
        StateSession sessionStartsPaused = new StateSession(StateSession.SessionType.NEW_SESSION);
        sessionStartsPaused.toSend = false;
        sessionStartsPaused.foregroundTimerStarts = false;

        checkStartInternal(sessionStartsPaused);

        // and failed event
        StateEvent stateFailedEvent = new StateEvent();
        stateFailedEvent.disabled = true;

        checkEvent(stateFailedEvent);

        // try to pause session
        stopActivity(activityHandler);
        SystemClock.sleep(1500);

        StateEndSession stateEndSession = new StateEndSession();
        stateEndSession.checkOnPause = true;
        stateEndSession.foregroundAlreadySuspended = true;

        checkEndSession(stateEndSession);

        SystemClock.sleep(4000);

        // try to generate a new session
        resumeActivity(activityHandler);

        SystemClock.sleep(1500);

        StateSession sessionDisabled = new StateSession(StateSession.SessionType.DISABLED);
        sessionDisabled.toSend = false;
        sessionDisabled.foregroundTimerStarts = false;
        sessionDisabled.disabled = true;

        checkStartInternal(sessionDisabled);

        // only the first session package should be sent
        assertUtil.isEqual(1, mockPackageHandler.queue.size());

        // put in offline mode
        activityHandler.setOfflineMode(true);

        // pausing due to offline mode
        assertUtil.info("Pausing handlers to put SDK offline mode");

        // wait to update status
        SystemClock.sleep(1500);

        // after pausing, even when it's already paused
        // tries to update the status
        checkHandlerStatus(true);

        // re-enable the SDK
        activityHandler.setEnabled(true);

        // check that it is enabled
        assertUtil.isTrue(activityHandler.isEnabled());

        // check message of SDK still paused
        assertUtil.info("Handlers remain paused");

        // wait to generate a new session
        SystemClock.sleep(5000);

        // even though it will remained paused,
        // it will update the status to paused
        checkHandlerStatus(true);

        // generate a new session
        resumeActivity(activityHandler);
        AdjustEvent secondEvent = new AdjustEvent("event2");

        activityHandler.trackEvent(secondEvent);

        SystemClock.sleep(1500);

        // difference from the first session is that now the foreground timer starts
        StateSession sessionOffline = new StateSession(StateSession.SessionType.NEW_SESSION);
        sessionOffline.toSend = false;
        sessionOffline.foregroundTimerStarts = true;
        sessionOffline.foregroundTimerAlreadyStarted = false;

        checkStartInternal(sessionOffline);

        // and the event does not fail
        StateEvent stateEvent = new StateEvent();

        checkEvent(stateEvent);

        // it should have the second session and the event
        assertUtil.isEqual(3, mockPackageHandler.queue.size());

        ActivityPackage secondSessionPackage = mockPackageHandler.queue.get(1);

        // create activity package test
        TestActivityPackage testSecondSessionPackage = new TestActivityPackage(secondSessionPackage);

        // set the sub sessions
        testSecondSessionPackage.subsessionCount = 1;

        // test second session
        testSecondSessionPackage.testSessionPackage(2);

        ActivityPackage eventPackage = mockPackageHandler.queue.get(2);

        // create activity package test
        TestActivityPackage testEventPackage = new TestActivityPackage(eventPackage);

        testEventPackage.suffix = "'event2'";

        // test event
        testEventPackage.testEventPackage("event2");

        // end the session
        stopActivity(activityHandler);

        SystemClock.sleep(1500);

        checkEndSession();

        // put in online mode
        activityHandler.setOfflineMode(false);

        // message that is finally resuming
        assertUtil.info("Resuming handlers to put SDK in online mode");

        SystemClock.sleep(5000);

        // after un-pausing the sdk, tries to update the handlers
        // it is still paused because it's on the background
        checkHandlerStatus(true);

        resumeActivity(activityHandler);

        SystemClock.sleep(1500);

        StateSession thirdSessionStarting = new StateSession(StateSession.SessionType.NEW_SESSION);
        thirdSessionStarting.sessionCount = 3;
        thirdSessionStarting.eventCount = 1;
        checkStartInternal(thirdSessionStarting);
    }

    @Test
    public void testOpenUrl() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testOpenUrl");

        // create the config to start the session
        AdjustConfig config = getConfig();

        // start activity handler with config
        ActivityHandler activityHandler = startAndCheckFirstSession(config);

        Uri attributions = Uri.parse("AdjustTests://example.com/path/inApp?adjust_tracker=trackerValue&other=stuff&adjust_campaign=campaignValue&adjust_adgroup=adgroupValue&adjust_creative=creativeValue");
        Uri extraParams = Uri.parse("AdjustTests://example.com/path/inApp?adjust_foo=bar&other=stuff&adjust_key=value");
        Uri mixed = Uri.parse("AdjustTests://example.com/path/inApp?adjust_foo=bar&other=stuff&adjust_campaign=campaignValue&adjust_adgroup=adgroupValue&adjust_creative=creativeValue");
        Uri encodedSeparators = Uri.parse("AdjustTests://example.com/path/inApp?adjust_foo=b%26a%3B%3Dr&adjust_campaign=campaign%3DValue%26&other=stuff");
        Uri emptyQueryString = Uri.parse("AdjustTests://");
        Uri emptyString = Uri.parse("");
        Uri nullUri = null;
        Uri single = Uri.parse("AdjustTests://example.com/path/inApp?adjust_foo");
        Uri prefix = Uri.parse("AdjustTests://example.com/path/inApp?adjust_=bar");
        Uri incomplete = Uri.parse("AdjustTests://example.com/path/inApp?adjust_foo=");

        long now = System.currentTimeMillis();

        activityHandler.readOpenUrl(attributions, now);
        activityHandler.readOpenUrl(extraParams, now);
        activityHandler.readOpenUrl(mixed, now);
        activityHandler.readOpenUrl(encodedSeparators, now);
        activityHandler.readOpenUrl(emptyQueryString, now);
        activityHandler.readOpenUrl(emptyString, now);
        activityHandler.readOpenUrl(nullUri, now);
        activityHandler.readOpenUrl(single, now);
        activityHandler.readOpenUrl(prefix, now);
        activityHandler.readOpenUrl(incomplete, now);

        SystemClock.sleep(1000);

        assertUtil.verbose("Url to parse (%s)", attributions);
        assertUtil.test("SdkClickHandler sendSdkClick");

        assertUtil.verbose("Url to parse (%s)", extraParams);
        assertUtil.test("SdkClickHandler sendSdkClick");

        assertUtil.verbose("Url to parse (%s)", mixed);
        assertUtil.test("SdkClickHandler sendSdkClick");

        assertUtil.verbose("Url to parse (%s)", encodedSeparators);
        assertUtil.test("SdkClickHandler sendSdkClick");

        assertUtil.verbose("Url to parse (%s)", emptyQueryString);
        assertUtil.test("SdkClickHandler sendSdkClick");

        assertUtil.verbose("Url to parse (%s)", single);
        assertUtil.test("SdkClickHandler sendSdkClick");

        assertUtil.verbose("Url to parse (%s)", prefix);
        assertUtil.test("SdkClickHandler sendSdkClick");

        assertUtil.verbose("Url to parse (%s)", incomplete);
        assertUtil.test("SdkClickHandler sendSdkClick");

        // check that it did not send any other click package
        assertUtil.notInTest("SdkClickHandler sendSdkClick");

        // 8 clicks
        assertUtil.isEqual(8, mockSdkClickHandler.queue.size());

        // get the click package
        ActivityPackage attributionClickPackage = mockSdkClickHandler.queue.get(0);

        // create activity package test
        TestActivityPackage testAttributionClickPackage = new TestActivityPackage(attributionClickPackage);

        // create the attribution
        AdjustAttribution firstAttribution = new AdjustAttribution();
        firstAttribution.trackerName = "trackerValue";
        firstAttribution.campaign = "campaignValue";
        firstAttribution.adgroup = "adgroupValue";
        firstAttribution.creative = "creativeValue";

        // and set it
        testAttributionClickPackage.attribution = firstAttribution;

        testAttributionClickPackage.deeplink = attributions.toString();

        // test the first deeplink
        testAttributionClickPackage.testClickPackage("deeplink");

        // get the click package
        ActivityPackage extraParamsClickPackage = mockSdkClickHandler.queue.get(1);

        // create activity package test
        TestActivityPackage testExtraParamsClickPackage = new TestActivityPackage(extraParamsClickPackage);

        // other deep link parameters
        testExtraParamsClickPackage.otherParameters = "{\"key\":\"value\",\"foo\":\"bar\"}";

        testExtraParamsClickPackage.deeplink = extraParams.toString();

        // test the second deeplink
        testExtraParamsClickPackage.testClickPackage("deeplink");

        // get the click package
        ActivityPackage mixedClickPackage = mockSdkClickHandler.queue.get(2);

        // create activity package test
        TestActivityPackage testMixedClickPackage = new TestActivityPackage(mixedClickPackage);

        // create the attribution
        AdjustAttribution secondAttribution = new AdjustAttribution();
        secondAttribution.campaign = "campaignValue";
        secondAttribution.adgroup = "adgroupValue";
        secondAttribution.creative = "creativeValue";

        // and set it
        testMixedClickPackage.attribution = secondAttribution;

        // other deep link parameters
        testMixedClickPackage.otherParameters = "{\"foo\":\"bar\"}";

        testMixedClickPackage.deeplink = mixed.toString();

        // test the third deeplink
        testMixedClickPackage.testClickPackage("deeplink");

        // get the click package
        ActivityPackage encodedClickPackage = mockSdkClickHandler.queue.get(3);

        // create activity package test
        TestActivityPackage testEncodedClickPackage = new TestActivityPackage(encodedClickPackage);

        // create the attribution
        AdjustAttribution thirdAttribution = new AdjustAttribution();
        thirdAttribution.campaign = "campaign=Value&";

        // and set it
        testEncodedClickPackage.attribution = thirdAttribution;

        // other deep link parameters
        testEncodedClickPackage.otherParameters = "{\"foo\":\"b&a;=r\"}";

        testEncodedClickPackage.deeplink = encodedSeparators.toString();

        // test the third deeplink
        testEncodedClickPackage.testClickPackage("deeplink");

        // get the click package
        ActivityPackage emptyQueryStringClickPackage = mockSdkClickHandler.queue.get(4);

        // create activity package test
        TestActivityPackage testEmptyQueryStringClickPackage = new TestActivityPackage(emptyQueryStringClickPackage);

        testEmptyQueryStringClickPackage.deeplink = emptyQueryString.toString();

        testEmptyQueryStringClickPackage.testClickPackage("deeplink");

        // get the click package
        ActivityPackage singleClickPackage = mockSdkClickHandler.queue.get(5);

        // create activity package test
        TestActivityPackage testSingleClickPackage = new TestActivityPackage(singleClickPackage);

        testSingleClickPackage.deeplink = single.toString();

        testSingleClickPackage.testClickPackage("deeplink");

        // get the click package
        ActivityPackage prefixClickPackage = mockSdkClickHandler.queue.get(6);

        // create activity package test
        TestActivityPackage testPrefixClickPackage = new TestActivityPackage(prefixClickPackage);

        testPrefixClickPackage.deeplink = prefix.toString();

        testPrefixClickPackage.testClickPackage("deeplink");

        // get the click package
        ActivityPackage incompleteClickPackage = mockSdkClickHandler.queue.get(7);

        // create activity package test
        TestActivityPackage testIncompleteClickPackage = new TestActivityPackage(incompleteClickPackage );

        testIncompleteClickPackage.deeplink = incomplete.toString();

        testIncompleteClickPackage.testClickPackage("deeplink");
    }

    @Test
    public void testAttributionDelegate() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testAttributionDelegate");

        // create the config to start the session
        AdjustConfig config = getConfig();

        config.setOnAttributionChangedListener(new OnAttributionChangedListener() {
            @Override
            public void onAttributionChanged(AdjustAttribution attribution) {
                mockLogger.test("onAttributionChanged: " + attribution);
            }
        });

        StateDelegates attributionDelegatePresent = new StateDelegates();
        attributionDelegatePresent.attributionDelegatePresent = true;
        checkFinishTasks(config, attributionDelegatePresent);
    }

    @Test
    public void testSuccessDelegates() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testSuccessDelegates");

        // create the config to start the session
        AdjustConfig config = getConfig();

        config.setOnEventTrackingSucceededListener(new OnEventTrackingSucceededListener() {
            @Override
            public void onFinishedEventTrackingSucceeded(AdjustEventSuccess eventSuccessResponseData) {
                mockLogger.test("onFinishedEventTrackingSucceeded: " + eventSuccessResponseData);
            }
        });

        config.setOnSessionTrackingSucceededListener(new OnSessionTrackingSucceededListener() {
            @Override
            public void onFinishedSessionTrackingSucceeded(AdjustSessionSuccess sessionSuccessResponseData) {
                mockLogger.test("onFinishedSessionTrackingSucceeded: " + sessionSuccessResponseData);
            }
        });

        StateDelegates successDelegatesPresent = new StateDelegates();
        successDelegatesPresent.eventSuccessDelegatePresent = true;
        successDelegatesPresent.sessionSuccessDelegatePresent = true;

        checkFinishTasks(config, successDelegatesPresent);
    }

    @Test
    public void testFailureDelegates() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testFailureDelegates");

        // create the config to start the session
        AdjustConfig config = getConfig();

        config.setOnEventTrackingFailedListener(new OnEventTrackingFailedListener() {
            @Override
            public void onFinishedEventTrackingFailed(AdjustEventFailure eventFailureResponseData) {
                mockLogger.test("onFinishedEventTrackingFailed: " + eventFailureResponseData);
            }
        });

        config.setOnSessionTrackingFailedListener(new OnSessionTrackingFailedListener() {
            @Override
            public void onFinishedSessionTrackingFailed(AdjustSessionFailure failureResponseData) {
                mockLogger.test("onFinishedSessionTrackingFailed: " + failureResponseData);
            }
        });

        StateDelegates failureDelegatesPresent = new StateDelegates();
        failureDelegatesPresent.sessionFailureDelegatePresent = true;
        failureDelegatesPresent.eventFailureDelegatePresent = true;

        checkFinishTasks(config, failureDelegatesPresent);
    }

    private void checkFinishTasks(AdjustConfig config,
                                 StateDelegates stateDelegates)
    {
        ActivityHandler activityHandler = startAndCheckFirstSession(config);

        // test first session package
        ActivityPackage firstSessionPackage = mockPackageHandler.queue.get(0);

        // create activity package test
        TestActivityPackage testActivityPackage = new TestActivityPackage(firstSessionPackage);

        // set first session
        testActivityPackage.testSessionPackage(1);

        long now = System.currentTimeMillis();
        String dateString = Util.dateFormatter.format(now);

        // simulate a successful session
        SessionResponseData successSessionResponseData = (SessionResponseData) ResponseData.buildResponseData(firstSessionPackage);
        successSessionResponseData.success = true;
        successSessionResponseData.message = "Session successfully tracked";
        successSessionResponseData.timestamp = dateString;
        successSessionResponseData.adid = "adidValue";

        activityHandler.finishedTrackingActivity(successSessionResponseData);
        SystemClock.sleep(1000);

        // attribution handler should always receive the session response
        assertUtil.test("AttributionHandler checkSessionResponse");

        // attribution handler does not receive sdk click
        assertUtil.notInTest("AttributionHandler checkSdkClickResponse");

        // the first session does not trigger the event response delegate
        assertUtil.notInDebug("Launching success event tracking listener");
        assertUtil.notInDebug("Launching failed event tracking listener");

        activityHandler.launchSessionResponseTasks(successSessionResponseData);
        SystemClock.sleep(1000);

        // if present, the first session triggers the success session delegate
        if (stateDelegates.sessionSuccessDelegatePresent) {
            assertUtil.debug("Launching success session tracking listener");
            assertUtil.test("onFinishedSessionTrackingSucceeded: Session Success msg:Session successfully tracked time:" + dateString + " adid:adidValue json:null");
        } else {
            assertUtil.notInDebug("Launching success session tracking delegate");
            assertUtil.notInTest("onFinishedSessionTrackingSucceeded: Session Success ");
        }
        // it doesn't trigger the failure session delegate
        assertUtil.notInDebug("Launching failed session tracking listener");

        // simulate a failure session
        SessionResponseData failureSessionResponseData = (SessionResponseData)ResponseData.buildResponseData(firstSessionPackage);
        failureSessionResponseData.success = false;
        failureSessionResponseData.message = "Session failure tracked";
        failureSessionResponseData.timestamp = dateString;
        failureSessionResponseData.adid = "adidValue";

        activityHandler.launchSessionResponseTasks(failureSessionResponseData);
        SystemClock.sleep(1000);

        // it doesn't trigger the success session delegate
        assertUtil.notInDebug("Launching success session tracking listener");

        // if present, the first session triggers the failure session delegate
        if (stateDelegates.sessionFailureDelegatePresent) {
            assertUtil.debug("Launching failed session tracking listener");
            assertUtil.test("onFinishedSessionTrackingFailed: Session Failure msg:Session failure tracked time:" + dateString+ " adid:adidValue retry:false json:null");
        } else {
            assertUtil.notInDebug("Launching failed session tracking listener");
            assertUtil.notInTest("onFinishedSessionTrackingFailed: Session Failure ");
        }

        // test success event response data
        activityHandler.trackEvent(new AdjustEvent("abc123"));
        SystemClock.sleep(1000);

        ActivityPackage eventPackage = mockPackageHandler.queue.get(1);
        EventResponseData eventSuccessResponseData = (EventResponseData)ResponseData.buildResponseData(eventPackage);
        eventSuccessResponseData.success = true;
        eventSuccessResponseData.message = "Event successfully tracked";
        eventSuccessResponseData.timestamp = dateString;
        eventSuccessResponseData.adid = "adidValue";

        activityHandler.finishedTrackingActivity(eventSuccessResponseData);
        SystemClock.sleep(1000);

        // attribution handler should never receive the event response
        assertUtil.notInTest("AttributionHandler checkSessionResponse");

        // if present, the success event triggers the success event delegate
        if (stateDelegates.eventSuccessDelegatePresent) {
            assertUtil.debug("Launching success event tracking listener");
            assertUtil.test("onFinishedEventTrackingSucceeded: Event Success msg:Event successfully tracked time:" + dateString + " adid:adidValue event:abc123 json:null");
        } else {
            assertUtil.notInDebug("Launching success event tracking listener");
            assertUtil.notInTest("onFinishedEventTrackingSucceeded: Event Success ");
        }
        // it doesn't trigger the failure event delegate
        assertUtil.notInDebug("Launching failed event tracking listener");

        // test failure event response data
        EventResponseData eventFailureResponseData = (EventResponseData)ResponseData.buildResponseData(eventPackage);
        eventFailureResponseData.success = false;
        eventFailureResponseData.message = "Event failure tracked";
        eventFailureResponseData.timestamp = dateString;
        eventFailureResponseData.adid = "adidValue";

        activityHandler.finishedTrackingActivity(eventFailureResponseData);
        SystemClock.sleep(1000);

        // attribution handler should never receive the event response
        assertUtil.notInTest("AttributionHandler checkSessionResponse");

        // if present, the failure event triggers the failure event delegate
        if (stateDelegates.eventFailureDelegatePresent) {
            assertUtil.debug("Launching failed event tracking listener");
            assertUtil.test("onFinishedEventTrackingFailed: Event Failure msg:Event failure tracked time:" + dateString + " adid:adidValue event:abc123 retry:false json:null");
        } else {
            assertUtil.notInDebug("Launching failed event tracking listener");
            assertUtil.notInTest("onFinishedEventTrackingFailed: Event Failure ");
        }
        // it doesn't trigger the success event delegate
        assertUtil.notInDebug("Launching success event tracking listener");

        // test click
        Uri attributions = Uri.parse("AdjustTests://example.com/path/inApp?adjust_tracker=trackerValue&other=stuff&adjust_campaign=campaignValue&adjust_adgroup=adgroupValue&adjust_creative=creativeValue");

        activityHandler.readOpenUrl(attributions, now);
        SystemClock.sleep(1000);

        assertUtil.test("SdkClickHandler sendSdkClick");

        // test sdk_click response data
        ActivityPackage sdkClickPackage = mockSdkClickHandler.queue.get(0);
        SdkClickResponseData sdkClickResponseData = (SdkClickResponseData) ResponseData.buildResponseData(sdkClickPackage);

        activityHandler.finishedTrackingActivity(sdkClickResponseData);
        SystemClock.sleep(1000);

        // attribution handler receives the click response
        assertUtil.test("AttributionHandler checkSdkClickResponse");

        // attribution handler does not receive session response
        assertUtil.notInTest("AttributionHandler checkSessionResponse");

        // it doesn't trigger the any event delegate
        assertUtil.notInDebug("Launching success event tracking listener");
        assertUtil.notInDebug("Launching failed event tracking listener");
    }

    @Test
    public void testLaunchDeepLink() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testLaunchDeepLink");

        // create the config to start the session
        AdjustConfig config = getConfig();

        // start activity handler with config
        ActivityHandler activityHandler = startAndCheckFirstSession(config);

        ResponseData responseDataNull = null;

        activityHandler.finishedTrackingActivity(responseDataNull);
        SystemClock.sleep(1500);

        // if the response is null
        assertUtil.notInTest("AttributionHandler checkAttribution");
        assertUtil.notInError("Unable to open deferred deep link");
        assertUtil.notInInfo("Open deferred deep link");

        // test success session response data
        SessionResponseData sessionResponseDeeplink = (SessionResponseData) ResponseData.buildResponseData(mockPackageHandler.queue.get(0));
        try {
            sessionResponseDeeplink.jsonResponse = new JSONObject("{ " +
                    "\"deeplink\" :  \"adjustTestSchema://\" }");
        } catch (JSONException e) {
            assertUtil.fail(e.getMessage());
        }

        activityHandler.launchSessionResponseTasks((SessionResponseData) sessionResponseDeeplink);
        SystemClock.sleep(1500);

        // check that it was unable to open the url
        assertUtil.notInError("Unable to open deferred deep link");
        assertUtil.notInInfo("Open deferred deep link");

        // test attribution response
        AttributionResponseData attributionResponseDeeplink = (AttributionResponseData)ResponseData.buildResponseData(mockAttributionHandler.attributionPackage);
        attributionResponseDeeplink.deeplink = Uri.parse("adjustTestSchema://");

        activityHandler.launchAttributionResponseTasks(attributionResponseDeeplink);
        SystemClock.sleep(1500);

        assertUtil.info("Deferred deeplink received (adjustTestSchema://)");
        assertUtil.notInError("Unable to open deferred deep link (adjustTestSchema://)");
        assertUtil.info("Open deferred deep link (adjustTestSchema://)");

        // checking the default values of the first session package
        // should only have one package
        assertUtil.isEqual(1, mockPackageHandler.queue.size());

        AttributionResponseData attributionResponseWrongDeeplink = (AttributionResponseData)ResponseData.buildResponseData(mockAttributionHandler.attributionPackage);
        attributionResponseWrongDeeplink.deeplink = Uri.parse("wrongDeeplink://");

        activityHandler.launchAttributionResponseTasks(attributionResponseWrongDeeplink);
        SystemClock.sleep(1500);

        assertUtil.info("Deferred deeplink received (wrongDeeplink://)");
        assertUtil.error("Unable to open deferred deep link (wrongDeeplink://)");
        assertUtil.notInInfo("Open deferred deep link (wrongDeeplink://)");
    }

    @Test
    public void testNotLaunchDeeplinkCallback() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testNotLaunchDeeplinkCallback");

        // create the config to start the session
        AdjustConfig config = getConfig();

        config.setOnDeeplinkResponseListener(new OnDeeplinkResponseListener() {
            @Override
            public boolean launchReceivedDeeplink(Uri deeplink) {
                mockLogger.test("launchReceivedDeeplink, " + deeplink);
                return false;
            }
        });

        // start activity handler with config
        ActivityHandler activityHandler = startAndCheckFirstSession(config);

        // test attribution response
        AttributionResponseData attributionResponseDeeplink = (AttributionResponseData)ResponseData.buildResponseData(mockAttributionHandler.attributionPackage);
        attributionResponseDeeplink.deeplink = Uri.parse("adjustTestSchema://");

        activityHandler.launchAttributionResponseTasks(attributionResponseDeeplink);
        SystemClock.sleep(1500);

        // received the deferred deeplink
        assertUtil.info("Deferred deeplink received (adjustTestSchema://)");
        // but it did not try to launch it
        assertUtil.test("launchReceivedDeeplink, adjustTestSchema://");
        assertUtil.notInError("Unable to open deferred deep link (adjustTestSchema://)");
        assertUtil.notInInfo("Open deferred deep link (adjustTestSchema://)");
    }

    @Test
    public void testDeeplinkCallback() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testDeeplinkCallback");

        // create the config to start the session
        AdjustConfig config = getConfig();

        config.setOnDeeplinkResponseListener(new OnDeeplinkResponseListener() {
            @Override
            public boolean launchReceivedDeeplink(Uri deeplink) {
                mockLogger.test("launchReceivedDeeplink, " + deeplink);
                return true;
            }
        });

        // start activity handler with config
        ActivityHandler activityHandler = startAndCheckFirstSession(config);

        // set package handler to respond with a valid attribution
        AttributionResponseData attributionResponseDeeplink = (AttributionResponseData)ResponseData.buildResponseData(mockAttributionHandler.attributionPackage);
        attributionResponseDeeplink.deeplink = Uri.parse("adjustTestSchema://");

        activityHandler.launchAttributionResponseTasks(attributionResponseDeeplink);
        SystemClock.sleep(2000);

        // received the deferred deeplink
        assertUtil.info("Deferred deeplink received (adjustTestSchema://)");
        // and it did launch it
        assertUtil.test("launchReceivedDeeplink, adjustTestSchema://");
        assertUtil.notInError("Unable to open deferred deep link (adjustTestSchema://)");
        assertUtil.info("Open deferred deep link (adjustTestSchema://)");
    }

    @Test
    public void testUpdateAttribution() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testUpdateAttribution");

        // create the config to start the session
        AdjustConfig config = getConfig();

        // start activity handler with config
        ActivityHandler firstActivityHandler = startAndCheckFirstSession(config);

        JSONObject nullJsonObject = null;
        AdjustAttribution nullAttribution = AdjustAttribution.fromJson(nullJsonObject, null); //XXX

        // check if Attribution wasn't built
        assertUtil.isNull(nullAttribution);

        // check that it does not update a null attribution
        assertUtil.isFalse(firstActivityHandler.updateAttributionI(nullAttribution));

        // create an empty attribution
        JSONObject emptyJsonResponse = null;
        try {
            emptyJsonResponse = new JSONObject("{ }");
        } catch (JSONException e) {
            assertUtil.fail(e.getMessage());
        }
        AdjustAttribution emptyAttribution = AdjustAttribution.fromJson(emptyJsonResponse, null); // XXX

        // check that updates attribution
        assertUtil.isTrue(firstActivityHandler.updateAttributionI(emptyAttribution));
        assertUtil.debug("Wrote Attribution: tt:null tn:null net:null cam:null adg:null cre:null cl:null");

        emptyAttribution = AdjustAttribution.fromJson(emptyJsonResponse, null); // XXX

        // test first session package
        ActivityPackage firstSessionPackage = mockPackageHandler.queue.get(0);

        // simulate a session response with attribution data
        SessionResponseData sessionResponseDataWithAttribution = (SessionResponseData)ResponseData.buildResponseData(firstSessionPackage);
        sessionResponseDataWithAttribution.attribution = emptyAttribution;

        // check that it does not update the attribution
        firstActivityHandler.launchSessionResponseTasks(sessionResponseDataWithAttribution);
        SystemClock.sleep(1000);
        assertUtil.notInDebug("Wrote Attribution");

        // end session
        firstActivityHandler.onPause();
        SystemClock.sleep(1000);

        checkEndSession();

        // create the new config
        config = getConfig();

        config.setOnAttributionChangedListener(new OnAttributionChangedListener() {
            @Override
            public void onAttributionChanged(AdjustAttribution attribution) {
                mockLogger.test("restartActivityHandler onAttributionChanged: " + attribution);
            }
        });

        ActivityHandler restartActivityHandler = getActivityHandler(config);

        SystemClock.sleep(1500);

        StateActivityHandlerInit restartActivityHandlerInit = new StateActivityHandlerInit(restartActivityHandler);
        restartActivityHandlerInit.activityStateAlreadyCreated = true;
        restartActivityHandlerInit.readActivityState = "ec:0 sc:1 ssc:1";
        restartActivityHandlerInit.readAttribution = "tt:null tn:null net:null cam:null adg:null cre:null cl:null";

        // test init values
        checkInitTests(restartActivityHandlerInit);

        resumeActivity(restartActivityHandler);

        SystemClock.sleep(2000);

        StateSession firstRestart = new StateSession(StateSession.SessionType.NEW_SUBSESSION);
        firstRestart.subsessionCount = 2;
        firstRestart.foregroundTimerAlreadyStarted = false;
        checkStartInternal(firstRestart);

        // check that it does not update the attribution after the restart
        assertUtil.isFalse(restartActivityHandler.updateAttributionI(emptyAttribution));
        assertUtil.notInDebug("Wrote Attribution");

        // new attribution
        JSONObject firstAttributionJson = null;
        try {
            firstAttributionJson = new JSONObject("{ " +
                    "\"tracker_token\" : \"ttValue\" , " +
                    "\"tracker_name\"  : \"tnValue\" , " +
                    "\"network\"       : \"nValue\" , " +
                    "\"campaign\"      : \"cpValue\" , " +
                    "\"adgroup\"       : \"aValue\" , " +
                    "\"creative\"      : \"ctValue\" , " +
                    "\"click_label\"   : \"clValue\" }");
        } catch (JSONException e) {
            assertUtil.fail(e.getMessage());
        }
        AdjustAttribution firstAttribution = AdjustAttribution.fromJson(firstAttributionJson, null); // XXX

        //check that it updates
        sessionResponseDataWithAttribution.attribution = firstAttribution;
        restartActivityHandler.launchSessionResponseTasks(sessionResponseDataWithAttribution);
        SystemClock.sleep(1000);

        assertUtil.debug("Wrote Attribution: tt:ttValue tn:tnValue net:nValue cam:cpValue adg:aValue cre:ctValue cl:clValue");
        assertUtil.test("restartActivityHandler onAttributionChanged: tt:ttValue tn:tnValue net:nValue cam:cpValue adg:aValue cre:ctValue cl:clValue");

        // test first session package
        ActivityPackage attributionPackage = mockAttributionHandler.attributionPackage;
        // simulate a session response with attribution data
        AttributionResponseData attributionResponseDataWithAttribution = (AttributionResponseData)ResponseData.buildResponseData(attributionPackage);

        attributionResponseDataWithAttribution.attribution = firstAttribution;
        // check that it does not update the attribution
        restartActivityHandler.launchAttributionResponseTasks(attributionResponseDataWithAttribution);
        SystemClock.sleep(1000);

        assertUtil.notInDebug("Wrote Attribution");

        // end session
        restartActivityHandler.onPause();
        SystemClock.sleep(1000);

        checkEndSession();

        config = getConfig();

        config.setOnAttributionChangedListener(new OnAttributionChangedListener() {
            @Override
            public void onAttributionChanged(AdjustAttribution attribution) {
                mockLogger.test("secondRestartActivityHandler onAttributionChanged: " + attribution);
            }
        });

        ActivityHandler secondRestartActivityHandler = getActivityHandler(config);;

        SystemClock.sleep(1500);

        StateActivityHandlerInit secondrestartActivityHandlerInit = new StateActivityHandlerInit(secondRestartActivityHandler);
        secondrestartActivityHandlerInit.activityStateAlreadyCreated = true;
        secondrestartActivityHandlerInit.readActivityState = "ec:0 sc:1 ssc:2";
        secondrestartActivityHandlerInit.readAttribution = "tt:ttValue tn:tnValue net:nValue cam:cpValue adg:aValue cre:ctValue cl:clValue";

        SystemClock.sleep(2000);

        // test init values
        checkInitTests(secondrestartActivityHandlerInit);

        resumeActivity(secondRestartActivityHandler);

        SystemClock.sleep(2000);

        StateSession secondRestart = new StateSession(StateSession.SessionType.NEW_SUBSESSION);
        secondRestart.subsessionCount = 3;
        secondRestart.foregroundTimerAlreadyStarted = false;

        checkStartInternal(secondRestart);

        // check that it does not update the attribution after the restart
        assertUtil.isFalse(secondRestartActivityHandler.updateAttributionI(firstAttribution));
        assertUtil.notInDebug("Wrote Attribution");

        // new attribution
        JSONObject secondAttributionJson = null;
        try {
            secondAttributionJson = new JSONObject("{ " +
                    "\"tracker_token\" : \"ttValue2\" , " +
                    "\"tracker_name\"  : \"tnValue2\" , " +
                    "\"network\"       : \"nValue2\" , " +
                    "\"campaign\"      : \"cpValue2\" , " +
                    "\"adgroup\"       : \"aValue2\" , " +
                    "\"creative\"      : \"ctValue2\" , " +
                    "\"click_label\"   : \"clValue2\" }");
        } catch (JSONException e) {
            assertUtil.fail(e.getMessage());
        }
        AdjustAttribution secondAttribution = AdjustAttribution.fromJson(secondAttributionJson, null); // XXX

        //check that it updates
        attributionResponseDataWithAttribution.attribution = secondAttribution;
        secondRestartActivityHandler.launchAttributionResponseTasks(attributionResponseDataWithAttribution);
        SystemClock.sleep(2000);

        assertUtil.debug("Wrote Attribution: tt:ttValue2 tn:tnValue2 net:nValue2 cam:cpValue2 adg:aValue2 cre:ctValue2 cl:clValue2");

        // check that it launch the saved attribute
        assertUtil.test("secondRestartActivityHandler onAttributionChanged: tt:ttValue2 tn:tnValue2 net:nValue2 cam:cpValue2 adg:aValue2 cre:ctValue2 cl:clValue2");

        // check that it does not update the attribution
        assertUtil.isFalse(secondRestartActivityHandler.updateAttributionI(secondAttribution));
        assertUtil.notInDebug("Wrote Attribution");
    }

    @Test
    public void testOfflineMode() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testOfflineMode");

        // adjust the session intervals for testing
        AdjustFactory.setSessionInterval(2000);
        AdjustFactory.setSubsessionInterval(500);

        // create the config to start the session
        AdjustConfig config = getConfig();

        // start activity handler with config
        ActivityHandler activityHandler = getActivityHandler(config);

        // put SDK offline
        activityHandler.setOfflineMode(true);

        ActivityHandler.InternalState internalState = activityHandler.getInternalState();

        // check if it's offline before the sdk starts
        assertUtil.isTrue(internalState.isOffline());

        SystemClock.sleep(1500);

        // not writing activity state because it set enable does not start the sdk
        assertUtil.notInDebug("Wrote Activity state");

        // check if message the disable of the SDK
        assertUtil.info("Handlers will start paused due to SDK being offline");

        // test init values
        checkInitTests(activityHandler);

        // check change from set offline mode
        checkHandlerStatus(true);

        // start the sdk
        // foreground timer starts because it's offline, not disabled
        resumeActivity(activityHandler);

        SystemClock.sleep(2500);

        // test first session start
        StateSession firstSessionStartPaused = new StateSession(StateSession.SessionType.NEW_SESSION);
        firstSessionStartPaused.toSend = false;
        firstSessionStartPaused.foregroundTimerStarts = true;

        // check session that is paused
        checkStartInternal(firstSessionStartPaused);

        stopActivity(activityHandler);
        SystemClock.sleep(1500);

        // test end session of offline
        StateEndSession stateOfflineEndSession = new StateEndSession();
        stateOfflineEndSession.checkOnPause = true;
        stateOfflineEndSession.updateActivityState = false; // update too late on the session
        checkEndSession(stateOfflineEndSession);

        // disable the SDK in the background
        activityHandler.setEnabled(false);

        // check that it is disabled
        assertUtil.isFalse(activityHandler.isEnabled());

        // writing activity state after disabling
        assertUtil.debug("Wrote Activity state: ec:0 sc:1 ssc:1");

        // check if message the disable of the SDK
        assertUtil.info("Pausing handlers due to SDK being disabled");

        // start a session while offline and disabled
        SystemClock.sleep(2500);

        // check handler status update of disable
        checkHandlerStatus(true);

        // try to start new session disabled
        resumeActivity(activityHandler);
        SystemClock.sleep(1500);

        // session not created for being disabled
        // foreground timer does not start because it's offline, not disabled
        StateSession sessionDisabled = new StateSession(StateSession.SessionType.DISABLED);
        sessionDisabled.toSend = false;
        sessionDisabled.foregroundTimerStarts = false;
        sessionDisabled.disabled = true;

        checkStartInternal(sessionDisabled);

        // put SDK back online
        activityHandler.setOfflineMode(false);

        assertUtil.info("Handlers remain paused");

        SystemClock.sleep(1500);

        // test the update status, still paused
        checkHandlerStatus(true);

        // try to do activities while SDK disabled
        resumeActivity(activityHandler);
        activityHandler.trackEvent(new AdjustEvent("event1"));

        SystemClock.sleep(2500);

        // check that timer was not executed
        checkForegroundTimerFired(false);

        // session not created for being disabled
        // foreground timer does not start because it's offline, not disabled
        checkStartInternal(sessionDisabled);

        // end the session
        stopActivity(activityHandler);

        SystemClock.sleep(1000);

        StateEndSession stateDisableEndSession = new StateEndSession();
        stateDisableEndSession.checkOnPause = true;
        stateDisableEndSession.foregroundAlreadySuspended = true; // did not start timer disabled
        stateDisableEndSession.updateActivityState = false; // update too late on the session
        checkEndSession(stateDisableEndSession);

        // enable the SDK again
        activityHandler.setEnabled(true);

        // check that is enabled
        assertUtil.isTrue(activityHandler.isEnabled());

        assertUtil.debug("Wrote Activity state");

        assertUtil.info("Resuming handlers due to SDK being enabled");

        SystemClock.sleep(2500);

        // it is still paused because it's on the background
        checkHandlerStatus(true);

        resumeActivity(activityHandler);

        SystemClock.sleep(1500);

        StateSession secondSessionState = new StateSession(StateSession.SessionType.NEW_SESSION);
        secondSessionState.sessionCount = 2;
        // test that is not paused anymore
        checkStartInternal(secondSessionState);
    }

    @Test
    public void testSendReferrer() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testSendReferrer");

        // create the config to start the session
        AdjustConfig config = getConfig();

        long now = System.currentTimeMillis();

        String referrerBeforeLaunch = "referrerBeforeLaunch";

        config.referrer = referrerBeforeLaunch;
        config.referrerClickTime = now;
        // start activity handler with config
        ActivityHandler activityHandler = getActivityHandler(config);

        SystemClock.sleep(1500);

        // test init values
        StateActivityHandlerInit stateActivityHandlerInit = new StateActivityHandlerInit(activityHandler);
        stateActivityHandlerInit.sendReferrer = referrerBeforeLaunch;
        checkInitTests(stateActivityHandlerInit);

        resumeActivity(activityHandler);

        SystemClock.sleep(1500);

        // test session
        checkFirstSession();

        String reftag = "adjust_reftag=referrerValue";
        String extraParams = "adjust_foo=bar&other=stuff&adjust_key=value";
        String mixed = "adjust_foo=bar&other=stuff&adjust_reftag=referrerValue";
        String encodedSeparators = "adjust_foo=b%26a%3B%3Dr&adjust_reftag=referrer%3DValue%26&other=stuff";
        String empty = "";
        String nullString = null;
        String single = "adjust_foo";
        String prefix = "adjust_=bar";
        String incomplete = "adjust_foo=";

        activityHandler.sendReferrer(reftag, now);
        activityHandler.sendReferrer(extraParams, now);
        activityHandler.sendReferrer(mixed, now);
        activityHandler.sendReferrer(encodedSeparators, now);
        activityHandler.sendReferrer(empty, now);
        activityHandler.sendReferrer(nullString, now);
        activityHandler.sendReferrer(single, now);
        activityHandler.sendReferrer(prefix, now);
        activityHandler.sendReferrer(incomplete, now);
        SystemClock.sleep(2000);

        assertUtil.verbose("Referrer to parse (%s)", reftag);
        assertUtil.test("SdkClickHandler sendSdkClick");

        assertUtil.verbose("Referrer to parse (%s)", extraParams);
        assertUtil.test("SdkClickHandler sendSdkClick");

        assertUtil.verbose("Referrer to parse (%s)", mixed);
        assertUtil.test("SdkClickHandler sendSdkClick");

        assertUtil.verbose("Referrer to parse (%s)", encodedSeparators);
        assertUtil.test("SdkClickHandler sendSdkClick");

        assertUtil.verbose("Referrer to parse (%s)", single);
        assertUtil.test("SdkClickHandler sendSdkClick");

        assertUtil.verbose("Referrer to parse (%s)", prefix);
        assertUtil.test("SdkClickHandler sendSdkClick");

        assertUtil.verbose("Referrer to parse (%s)", incomplete);
        assertUtil.test("SdkClickHandler sendSdkClick");

        // check that it did not send any other click package
        assertUtil.notInTest("SdkClickHandler sendSdkClick");

        // 7 click
        assertUtil.isEqual(8, mockSdkClickHandler.queue.size());

        ActivityPackage referrerBeforeLaunchPacakge = mockSdkClickHandler.queue.get(0);

        TestActivityPackage referrerBeforeLaunchTest = new TestActivityPackage(referrerBeforeLaunchPacakge);

        referrerBeforeLaunchTest.referrer = referrerBeforeLaunch;

        referrerBeforeLaunchTest.testClickPackage("reftag", false);

        ActivityPackage reftagClickPackage = mockSdkClickHandler.queue.get(1);

        TestActivityPackage reftagClickPackageTest = new TestActivityPackage(reftagClickPackage);

        reftagClickPackageTest.reftag = "referrerValue";
        reftagClickPackageTest.referrer = reftag;

        reftagClickPackageTest.testClickPackage("reftag");

        // get the click package
        ActivityPackage extraParamsClickPackage = mockSdkClickHandler.queue.get(2);

        // create activity package test
        TestActivityPackage testExtraParamsClickPackage = new TestActivityPackage(extraParamsClickPackage);

        // other deep link parameters
        testExtraParamsClickPackage.otherParameters = "{\"key\":\"value\",\"foo\":\"bar\"}";

        testExtraParamsClickPackage.referrer = extraParams;

        // test the second deeplink
        testExtraParamsClickPackage.testClickPackage("reftag");

        // get the click package
        ActivityPackage mixedClickPackage = mockSdkClickHandler.queue.get(3);

        // create activity package test
        TestActivityPackage testMixedClickPackage = new TestActivityPackage(mixedClickPackage);

        testMixedClickPackage.reftag = "referrerValue";
        testMixedClickPackage.referrer = mixed;

        // other deep link parameters
        testMixedClickPackage.otherParameters = "{\"foo\":\"bar\"}";

        // test the third deeplink
        testMixedClickPackage.testClickPackage("reftag");

        // get the click package
        ActivityPackage encodedClickPackage = mockSdkClickHandler.queue.get(4);

        // create activity package test
        TestActivityPackage testEncodedClickPackage = new TestActivityPackage(encodedClickPackage);

        testEncodedClickPackage.reftag = "referrer=Value&";
        testEncodedClickPackage.referrer = encodedSeparators;

        // other deep link parameters
        testEncodedClickPackage.otherParameters = "{\"foo\":\"b&a;=r\"}";

        // test the third deeplink
        testMixedClickPackage.testClickPackage("reftag");

        // get the click package
        ActivityPackage singleClickPackage = mockSdkClickHandler.queue.get(5);

        // create activity package test
        TestActivityPackage testSingleClickPackage = new TestActivityPackage(singleClickPackage);

        testSingleClickPackage.referrer = single;

        testSingleClickPackage.testClickPackage("reftag");

        // get the click package
        ActivityPackage prefixClickPackage = mockSdkClickHandler.queue.get(6);

        // create activity package test
        TestActivityPackage testPrefixClickPackage = new TestActivityPackage(prefixClickPackage);

        testPrefixClickPackage.referrer = prefix;

        testPrefixClickPackage.testClickPackage("reftag");

        // get the click package
        ActivityPackage incompleteClickPackage = mockSdkClickHandler.queue.get(7);

        // create activity package test
        TestActivityPackage testIncompleteClickPackage = new TestActivityPackage(incompleteClickPackage);

        testIncompleteClickPackage.referrer = incomplete;

        testIncompleteClickPackage.testClickPackage("reftag");
    }

    @Test
    public void testCheckAttributionState() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testCheckAttributionState");

        // if it's the first launch
        //if (internalState.isFirstLaunch()) {
        //    if (internalState.hasSessionResponseNotBeenProcessed()) {
        //        return;
        //    }
        //}
        //if (attribution != null && !activityState.askingAttribution) {
        //    return;
        //}
        //attributionHandler.getAttribution();

        AdjustFactory.setSessionInterval(4000);

        // create the config to start the session
        AdjustConfig config = getConfig();

        config.setOnAttributionChangedListener(new OnAttributionChangedListener() {
            @Override
            public void onAttributionChanged(AdjustAttribution attribution) {
                mockLogger.test("onAttributionChanged " + attribution);
            }
        });

        // start activity handler with config
        ActivityHandler activityHandler = getActivityHandler(config);

        SystemClock.sleep(1500);

        // test init values
        checkInitTests(activityHandler);

        resumeActivity(activityHandler);

        SystemClock.sleep(1500);

        // it's first launch
        // session response has not been processed
        // attribution is null
        // -> not called

        StateSession newSessionState = new StateSession(StateSession.SessionType.NEW_SESSION);
        newSessionState.getAttributionIsCalled = false;
        checkStartInternal(newSessionState);

        ActivityPackage firstSessionPackage = mockPackageHandler.queue.get(0);

        // trigger a new sub session
        activityHandler.onResume();
        SystemClock.sleep(2000);

        // does not call because the session has not been processed
        checkSubSession(1, 2, false);

        // it's first launch
        // session response has been processed
        // attribution is null
        // -> called

        // simulate a successful session
        SessionResponseData successSessionResponseData = (SessionResponseData) ResponseData.buildResponseData(firstSessionPackage);
        successSessionResponseData.success = true;
        successSessionResponseData.message = "Session successfully tracked";
        successSessionResponseData.adid = "adidValue";

        activityHandler.launchSessionResponseTasks(successSessionResponseData);

        // trigger a new sub session
        activityHandler.onResume();
        SystemClock.sleep(2000);

        // does call because the session has been processed
        checkSubSession(1, 3, true);

        // it's first launch
        // session response has been processed
        // attribution is not null
        // askingAttribution is false
        // -> not called

        // save the new attribution
        successSessionResponseData.attribution = new AdjustAttribution();
        successSessionResponseData.attribution.trackerName = "trackerName";
        activityHandler.launchSessionResponseTasks(successSessionResponseData);

        activityHandler.setAskingAttribution(false);

        // trigger a new sub session
        activityHandler.onResume();
        SystemClock.sleep(2000);

        // does call because the session has been processed
        checkSubSession(1, 4, false);

        // it's first launch
        // session response has been processed
        // attribution is not null
        // askingAttribution is true
        // -> called

        activityHandler.setAskingAttribution(true);

        // trigger a new sub session
        activityHandler.onResume();
        SystemClock.sleep(2000);

        // does call because the session has been processed
        checkSubSession(1, 5, true);

        // it's not first launch
        // attribution is null
        // -> called

        // finish activity handler
        activityHandler.teardown(false);
        // delete attribution
        ActivityHandler.deleteAttribution(context);

        // start new activity handler
        SystemClock.sleep(5000);
        activityHandler = getActivityHandler(config);

        SystemClock.sleep(1500);

        // test init values
        StateActivityHandlerInit stateActivityHandlerInit = new StateActivityHandlerInit(activityHandler);
        stateActivityHandlerInit.readActivityState = "ec:0 sc:1 ssc:5";

        checkInitTests(stateActivityHandlerInit);

        resumeActivity(activityHandler);

        SystemClock.sleep(1500);

        newSessionState = new StateSession(StateSession.SessionType.NEW_SESSION);
        newSessionState.getAttributionIsCalled = true;
        newSessionState.sessionCount = 2;
        checkStartInternal(newSessionState);

        // it's not first launch
        // attribution is not null
        // askingAttribution is true
        // -> called

        // save the new attribution
        successSessionResponseData.attribution = new AdjustAttribution();
        successSessionResponseData.attribution.trackerName = "trackerName";
        activityHandler.launchSessionResponseTasks(successSessionResponseData);

        activityHandler.setAskingAttribution(true);

        // trigger a new sub session
        activityHandler.onResume();
        SystemClock.sleep(2000);

        // does call because the session has been processed
        checkSubSession(2, 2, true);

        // it's not first launch
        // attribution is not null
        // askingAttribution is false
        // -> not called

        activityHandler.setAskingAttribution(false);

        // trigger a new sub session
        activityHandler.onResume();
        SystemClock.sleep(2000);

        // does call because the session has been processed
        checkSubSession(2, 3, false);
    }

    @Test
    public void testForegroundTimer() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testForegroundTimer");

        AdjustFactory.setTimerInterval(4000);
        AdjustFactory.setTimerStart(4000);

        // create the config to start the session
        AdjustConfig config = getConfig();

        // start activity handler with config
        ActivityHandler activityHandler = getActivityHandler(config);

        SystemClock.sleep(1500);

        // test init values
        StateActivityHandlerInit stateActivityHandlerInit = new StateActivityHandlerInit(activityHandler);
        stateActivityHandlerInit.foregroundTimerStart = 4;
        stateActivityHandlerInit.foregroundTimerCycle = 4;
        checkInitTests(stateActivityHandlerInit);

        resumeActivity(activityHandler);

        SystemClock.sleep(1500);

        // test session
        checkFirstSession();

        // wait enough to fire the first cycle
        SystemClock.sleep(3000);

        checkForegroundTimerFired(true);

        // end subsession to stop timer
        activityHandler.onPause();

        // wait enough for a new cycle
        SystemClock.sleep(6000);

        // start a new session
        activityHandler.onResume();

        SystemClock.sleep(1000);

        // check that not enough time passed to fire again
        checkForegroundTimerFired(false);
    }

    @Test
    public void testSendBackground() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testSendBackground");

        AdjustFactory.setTimerInterval(4000);

        // create the config to start the session
        AdjustConfig config = getConfig();

        // enable send in the background
        config.setSendInBackground(true);

        // create activity handler without starting
        ActivityHandler activityHandler = getActivityHandler(config);

        SystemClock.sleep(1500);

        // handlers start sending
        StateActivityHandlerInit stateActivityHandlerInit = new StateActivityHandlerInit(activityHandler);
        stateActivityHandlerInit.startsSending = true;
        stateActivityHandlerInit.sendInBackgroundConfigured = true;
        stateActivityHandlerInit.foregroundTimerCycle = 4;
        checkInitTests(stateActivityHandlerInit);

        resumeActivity(activityHandler);

        SystemClock.sleep(1500);

        // test session
        StateSession newStateSession= new StateSession(StateSession.SessionType.NEW_SESSION);
        newStateSession.sendInBackgroundConfigured = true;
        newStateSession.toSend = true;
        checkStartInternal(newStateSession);

        // end subsession
        // background timer starts
        stopActivity(activityHandler);

        SystemClock.sleep(1500);

        // session end does not pause the handlers
        StateEndSession stateEndSession1 = new StateEndSession();
        stateEndSession1.pausing = false;
        stateEndSession1.checkOnPause = true;
        stateEndSession1.backgroundTimerStarts = true;
        checkEndSession(stateEndSession1);

        // end subsession again
        // to test if background timer starts again
        stopActivity(activityHandler);

        SystemClock.sleep(1500);

        // session end does not pause the handlers
        StateEndSession stateEndSession2 = new StateEndSession();
        stateEndSession2.pausing = false;
        stateEndSession2.checkOnPause = true;
        stateEndSession2.foregroundAlreadySuspended = true;
        checkEndSession(stateEndSession2);

        // wait for background timer launch
        SystemClock.sleep(3000);

        // background timer fired
        assertUtil.test("PackageHandler sendFirstPackage");

        // wait enough time
        SystemClock.sleep(3000);

        // check that background timer does not fire again
        assertUtil.notInTest("PackageHandler sendFirstPackage");

        activityHandler.trackEvent(new AdjustEvent("abc123"));

        SystemClock.sleep(1000);

        StateEvent stateEvent = new StateEvent();
        stateEvent.backgroundTimerStarts = 4;
        checkEvent(stateEvent);

        // disable and enable the sdk while in the background
        activityHandler.setEnabled(false);

        // check that it is disabled
        assertUtil.isFalse(activityHandler.isEnabled());

        // check if message the disable of the SDK
        assertUtil.info("Pausing handlers due to SDK being disabled");

        SystemClock.sleep(1000);

        // handlers being paused because of the disable
        checkHandlerStatus(true);

        activityHandler.setEnabled(true);

        // check that it is enabled
        assertUtil.isTrue(activityHandler.isEnabled());

        // check if message the enable of the SDK
        assertUtil.info("Resuming handlers due to SDK being enabled");

        SystemClock.sleep(1000);

        // handlers being resumed because of the enable
        // even in the background because of the sendInBackground option
        checkHandlerStatus(false);

        // set offline and online the sdk while in the background
        activityHandler.setOfflineMode(true);

        ActivityHandler.InternalState internalState = activityHandler.getInternalState();

        // check that it is offline
        assertUtil.isTrue(internalState.isOffline());

        // check if message the offline of the SDK
        assertUtil.info("Pausing handlers to put SDK offline mode");

        SystemClock.sleep(1000);

        // handlers being paused because of the offline
        checkHandlerStatus(true);

        activityHandler.setOfflineMode(false);

        // check that it is online
        assertUtil.isTrue(internalState.isOnline());

        // check if message the online of the SDK
        assertUtil.info("Resuming handlers to put SDK in online mode");

        SystemClock.sleep(1000);

        // handlers being resumed because of the online
        // even in the background because of the sendInBackground option
        checkHandlerStatus(false);
    }

    @Test
    public void testSessionParameters() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testSessionParameters");

        // create the config to start the session
        AdjustConfig config = getConfig();

        //  create handler and start the first session
        config.sessionParametersActionsArray = new ArrayList<IRunActivityHandler>();
        config.sessionParametersActionsArray.add(new IRunActivityHandler() {
            @Override
            public void run(ActivityHandler activityHandler) {
                //
                activityHandler.addSessionCallbackParameterI("cKey", "cValue");
                activityHandler.addSessionCallbackParameterI("cFoo", "cBar");

                activityHandler.addSessionCallbackParameterI("cKey", "cValue2");
                activityHandler.resetSessionCallbackParametersI();

                activityHandler.addSessionCallbackParameterI("cKey", "cValue");
                activityHandler.addSessionCallbackParameterI("cFoo", "cBar");
                activityHandler.removeSessionCallbackParameterI("cKey");

                //
                activityHandler.addSessionPartnerParameterI("pKey", "pValue");
                activityHandler.addSessionPartnerParameterI("pFoo", "pBar");

                activityHandler.addSessionPartnerParameterI("pKey", "pValue2");
                activityHandler.resetSessionPartnerParametersI();

                activityHandler.addSessionPartnerParameterI("pKey", "pValue");
                activityHandler.addSessionPartnerParameterI("pFoo", "pBar");
                activityHandler.removeSessionPartnerParameterI("pKey");
            }
        });

        ActivityHandler activityHandler = getActivityHandler(config);

        SystemClock.sleep(1500);

        // test init values
        checkInitTests(activityHandler);

        checkSessionParameters();

        resumeActivity(activityHandler);

        AdjustEvent firstEvent = new AdjustEvent("event1");
        activityHandler.trackEvent(firstEvent);

        SystemClock.sleep(1500);

        // test session
        checkFirstSession();

        StateEvent stateEvent1 = new StateEvent();
        checkEvent(stateEvent1);

        // 1 session + 1 event
        assertUtil.isEqual(2, mockPackageHandler.queue.size());

        // get the session package
        ActivityPackage firstSessionPackage = mockPackageHandler.queue.get(0);

        // create event package test
        TestActivityPackage testFirstSessionPackage = new TestActivityPackage(firstSessionPackage);

        // set event test parameters
        testFirstSessionPackage.callbackParams = "{\"cFoo\":\"cBar\"}";
        testFirstSessionPackage.partnerParams = "{\"pFoo\":\"pBar\"}";

        testFirstSessionPackage.testSessionPackage(1);

        // get the event
        ActivityPackage firstEventPackage = mockPackageHandler.queue.get(1);

        // create event package test
        TestActivityPackage testFirstEventPackage = new TestActivityPackage(firstEventPackage);

        // set event test parameters
        testFirstEventPackage.eventCount = "1";
        testFirstEventPackage.suffix = "'event1'";
        testFirstEventPackage.callbackParams = "{\"cFoo\":\"cBar\"}";
        testFirstEventPackage.partnerParams = "{\"pFoo\":\"pBar\"}";

        testFirstEventPackage.testEventPackage("event1");

        // end current session
        stopActivity(activityHandler);
        SystemClock.sleep(1000);

        checkEndSession();
        activityHandler.teardown(false);
        activityHandler = null;

        AdjustConfig newConfig = getConfig();
        ActivityHandler restartActivityHandler = getActivityHandler(newConfig);

        SystemClock.sleep(1500);

        // start new one
        // delay start not configured because activity state is already created
        StateActivityHandlerInit restartActivityHandlerInit = new StateActivityHandlerInit(restartActivityHandler);
        restartActivityHandlerInit.activityStateAlreadyCreated = true;
        restartActivityHandlerInit.readActivityState = "ec:1 sc:1";
        restartActivityHandlerInit.readCallbackParameters = "{cFoo=cBar}";
        restartActivityHandlerInit.readPartnerParameters = "{pFoo=pBar}";

        // test init values
        checkInitTests(restartActivityHandlerInit);

        resumeActivity(restartActivityHandler);

        SystemClock.sleep(1500);

        StateSession stateRestartSession = new StateSession(StateSession.SessionType.NEW_SUBSESSION);
        stateRestartSession.subsessionCount = 2;
        stateRestartSession.eventCount = 1;

        checkStartInternal(stateRestartSession);

        // create the second Event
        AdjustEvent secondEvent = new AdjustEvent("event2");
        secondEvent.addCallbackParameter("ceFoo", "ceBar");
        secondEvent.addPartnerParameter("peFoo", "peBar");

        restartActivityHandler.trackEvent(secondEvent);

        AdjustEvent thirdEvent = new AdjustEvent("event3");
        thirdEvent.addCallbackParameter("cFoo", "ceBar");
        thirdEvent.addPartnerParameter("pFoo", "peBar");

        restartActivityHandler.trackEvent(thirdEvent);

        SystemClock.sleep(1500);

        // 2 events
        assertUtil.isEqual(2, mockPackageHandler.queue.size());

        // get the event
        ActivityPackage secondEventPackage = mockPackageHandler.queue.get(0);

        // create event package test
        TestActivityPackage testSecondEventPackage = new TestActivityPackage(secondEventPackage);

        // set event test parameters
        testSecondEventPackage.eventCount = "2";
        testSecondEventPackage.suffix = "'event2'";
        testSecondEventPackage.callbackParams = "{\"ceFoo\":\"ceBar\",\"cFoo\":\"cBar\"}";
        testSecondEventPackage.partnerParams = "{\"peFoo\":\"peBar\",\"pFoo\":\"pBar\"}";

        testSecondEventPackage.testEventPackage("event2");

        // get the event
        ActivityPackage thirdEventPackage = mockPackageHandler.queue.get(1);

        // create event package test
        TestActivityPackage testThirdEventPackage = new TestActivityPackage(thirdEventPackage);

        // set event test parameters
        testThirdEventPackage.eventCount = "3";
        testThirdEventPackage.suffix = "'event3'";
        testThirdEventPackage.callbackParams = "{\"cFoo\":\"ceBar\"}";
        testThirdEventPackage.partnerParams = "{\"pFoo\":\"peBar\"}";

        testThirdEventPackage.testEventPackage("event3");
    }

    private void checkSessionParameters() {
        //
        assertUtil.debug("Wrote Session Callback parameters: {cKey=cValue}");
        assertUtil.debug("Wrote Session Callback parameters: {cKey=cValue, cFoo=cBar}");

        assertUtil.warn("Key cKey will be overwritten");
        assertUtil.debug("Wrote Session Callback parameters: {cKey=cValue2, cFoo=cBar}");

        assertUtil.debug("Wrote Session Callback parameters: null");

        assertUtil.debug("Wrote Session Callback parameters: {cKey=cValue}");
        assertUtil.debug("Wrote Session Callback parameters: {cKey=cValue, cFoo=cBar}");

        assertUtil.debug("Key cKey will be removed");
        assertUtil.debug("Wrote Session Callback parameters: {cFoo=cBar}");

        //
        assertUtil.debug("Wrote Session Partner parameters: {pKey=pValue}");
        assertUtil.debug("Wrote Session Partner parameters: {pKey=pValue, pFoo=pBar}");

        assertUtil.warn("Key pKey will be overwritten");
        assertUtil.debug("Wrote Session Partner parameters: {pKey=pValue2, pFoo=pBar}");

        assertUtil.debug("Wrote Session Partner parameters: null");

        assertUtil.debug("Wrote Session Partner parameters: {pKey=pValue}");
        assertUtil.debug("Wrote Session Partner parameters: {pKey=pValue, pFoo=pBar}");

        assertUtil.debug("Key pKey will be removed");
        assertUtil.debug("Wrote Session Partner parameters: {pFoo=pBar}");
    }

    @Test
    public void testDelayStartTimerFirst() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testDelayStartTimerFirst");

        // create the config to start the session
        AdjustConfig config = getConfig();

        config.setDelayStart(4);

        config.sessionParametersActionsArray = new ArrayList<IRunActivityHandler>();
        config.sessionParametersActionsArray.add(new IRunActivityHandler() {
            @Override
            public void run(ActivityHandler activityHandler) {
                activityHandler.addSessionCallbackParameterI("scpKey", "scpValue");
                activityHandler.addSessionPartnerParameterI("sppKey", "sppValue");
            }
        });

        //  create handler and start the first session
        // start activity handler with config
        ActivityHandler activityHandler = getActivityHandler(config);

        SystemClock.sleep(1500);

        // test init values
        StateActivityHandlerInit stateActivityHandlerInit = new StateActivityHandlerInit(activityHandler);
        stateActivityHandlerInit.delayStartConfigured = true;
        checkInitTests(stateActivityHandlerInit);

        resumeActivity(activityHandler);
        resumeActivity(activityHandler);

        SystemClock.sleep(1000);

        StateSession newStateSession = new StateSession(StateSession.SessionType.NEW_SESSION);
        // delay start means it starts paused
        newStateSession.toSend = false;
        // sdk click handler does not start paused
        newStateSession.sdkClickHandlerAlsoStartsPaused = false;
        // delay configured
        newStateSession.delayStart = "4.0";

        checkStartInternal(newStateSession);

        // change state session for non session
        StateSession nonSession = new StateSession(StateSession.SessionType.NONSESSION);
        // delay already processed
        nonSession.delayStart = null;
        nonSession.toSend = false;
        nonSession.sdkClickHandlerAlsoStartsPaused = false;
        nonSession.foregroundTimerAlreadyStarted = true;

        checkStartInternal(nonSession);

        // create the first Event object with callback and partner parameters
        AdjustEvent firstEvent = new AdjustEvent("event1");

        firstEvent.addCallbackParameter("keyCall", "valueCall");
        firstEvent.addPartnerParameter("keyPartner", "valuePartner");

        activityHandler.trackEvent(firstEvent);

        SystemClock.sleep(1000);

        StateEvent stateEvent = new StateEvent();
        checkEvent(stateEvent);

        SystemClock.sleep(4000);

        assertUtil.verbose("Delay Start timer fired");

        checkSendFirstPackages(true, activityHandler.getInternalState(), true, false);

        activityHandler.sendFirstPackages();
        SystemClock.sleep(1000);

        checkSendFirstPackages(false, activityHandler.getInternalState(), true, false);

        // 1 session + 1 event
        assertUtil.isEqual(2, mockPackageHandler.queue.size());

        // get the first event
        ActivityPackage firstEventPackage = mockPackageHandler.queue.get(1);

        // create event package test
        TestActivityPackage testFirstEventPackage = new TestActivityPackage(firstEventPackage);

        // set event test parameters
        testFirstEventPackage.eventCount = "1";
        testFirstEventPackage.savedCallbackParameters = new HashMap<String, String>(1);
        testFirstEventPackage.savedCallbackParameters.put("keyCall", "valueCall");
        testFirstEventPackage.savedPartnerParameters = new HashMap<String, String>(1);
        testFirstEventPackage.savedPartnerParameters.put("keyPartner", "valuePartner");
        testFirstEventPackage.suffix = "'event1'";

        // test first event
        testFirstEventPackage.testEventPackage("event1");
    }

    @Test
    public void testDelayStartSendFirst() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testDelayStartSendFirst");

        // create the config to start the session
        AdjustConfig config = getConfig();

        config.setDelayStart(5);

        // start activity handler with config
        ActivityHandler activityHandler = getActivityHandler(config);

        SystemClock.sleep(1500);

        // test init values
        StateActivityHandlerInit stateActivityHandlerInit = new StateActivityHandlerInit(activityHandler);
        stateActivityHandlerInit.delayStartConfigured = true;
        checkInitTests(stateActivityHandlerInit);

        resumeActivity(activityHandler);
        resumeActivity(activityHandler);

        SystemClock.sleep(1000);

        StateSession newStateSession = new StateSession(StateSession.SessionType.NEW_SESSION);
        // delay start means it starts paused
        newStateSession.toSend = false;
        // sdk click handler does not start paused
        newStateSession.sdkClickHandlerAlsoStartsPaused = false;
        // delay configured
        newStateSession.delayStart = "5.0";

        checkStartInternal(newStateSession);

        // change state session for non session
        StateSession nonSession = new StateSession(StateSession.SessionType.NONSESSION);
        // delay already processed
        nonSession.delayStart = null;
        nonSession.toSend = false;
        nonSession.sdkClickHandlerAlsoStartsPaused = false;
        nonSession.foregroundTimerAlreadyStarted = true;

        checkStartInternal(nonSession);

        activityHandler.sendFirstPackages();

        SystemClock.sleep(3000);

        assertUtil.notInVerbose("Delay Start timer fired");

        checkSendFirstPackages(true,activityHandler.getInternalState(), true, false);

        activityHandler.sendFirstPackages();
        SystemClock.sleep(1000);

        checkSendFirstPackages(false,activityHandler.getInternalState(), true, false);
    }

    @Test
    public void testPushToken() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testPushToken");

        AdjustConfig config = getConfig();
        // set the push token before the sdk starts
        config.pushToken = "preStartPushToken";

        // start activity handler with config
        ActivityHandler activityHandler = getActivityHandler(config);

        SystemClock.sleep(1500);

        // test init values
        StateActivityHandlerInit stateActivityHandlerInit = new StateActivityHandlerInit(activityHandler);
        stateActivityHandlerInit.pushToken = "preStartPushToken";
        checkInitTests(stateActivityHandlerInit);

        resumeActivity(activityHandler);

        SystemClock.sleep(1500);

        // test session
        checkFirstSession();

        // create the first Event
        AdjustEvent firstEvent = new AdjustEvent("event1");

        // track event
        activityHandler.trackEvent(firstEvent);

        SystemClock.sleep(1500);

        // checking the default values of the first session package
        assertUtil.isEqual(2, mockPackageHandler.queue.size());

        ActivityPackage activityPackage = mockPackageHandler.queue.get(0);

        // create activity package test
        TestActivityPackage testActivityPackage = new TestActivityPackage(activityPackage);
        testActivityPackage.pushToken = "preStartPushToken";

        // set first session
        testActivityPackage.testSessionPackage(1);

        // first event
        ActivityPackage firstEventPackage = mockPackageHandler.queue.get(1);

        // create event package test
        TestActivityPackage testFirstEventPackage = new TestActivityPackage(firstEventPackage);

        // set event test parameters
        testFirstEventPackage.eventCount = "1";
        testFirstEventPackage.suffix = "'event1'";
        testFirstEventPackage.pushToken = "preStartPushToken";

        // test first event
        testFirstEventPackage.testEventPackage("event1");

        // try to update with the same push token
        activityHandler.setPushToken("preStartPushToken");
        SystemClock.sleep(1500);

        // should not have added a new package either in the package handler
        assertUtil.isEqual(2, mockPackageHandler.queue.size());

        // nor the click handler
        assertUtil.notInTest("SdkClickHandler sendSdkClick");
        assertUtil.isEqual(0, mockSdkClickHandler.queue.size());

        // update with new push token
        activityHandler.setPushToken("newPushToken");
        SystemClock.sleep(1500);

        // check it was added to sdk click handler
        assertUtil.notInTest("SdkClickHandler sendSdkClick");
        assertUtil.isEqual(0, mockSdkClickHandler.queue.size());

        // check that info package was added
        assertUtil.test("PackageHandler addPackage");

        // check that event was sent to package handler
        assertUtil.test("PackageHandler sendFirstPackage");

        // checking that the info package was added
        assertUtil.isEqual(3, mockPackageHandler.queue.size());

        // get the click package
        ActivityPackage sdkInfoPackage = mockPackageHandler.queue.get(2);

        // create activity package test
        TestActivityPackage testInfoPackage = new TestActivityPackage(sdkInfoPackage);

        testInfoPackage.pushToken = "newPushToken";

        // test the first deeplink
        testInfoPackage.testInfoPackage("push");
    }

    @Test
    public void testUpdateStart() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testUpdateStart");

        // create the config to start the session
        AdjustConfig config = getConfig();

        config.setDelayStart(10.1);

        ActivityHandler activityHandler = getActivityHandler(config);
        SystemClock.sleep(1500);

        // test init values
        StateActivityHandlerInit stateActivityHandlerInit = new StateActivityHandlerInit(activityHandler);
        stateActivityHandlerInit.delayStartConfigured = true;
        checkInitTests(stateActivityHandlerInit);

        resumeActivity(activityHandler);
        SystemClock.sleep(1000);

        StateSession newStateSession = new StateSession(StateSession.SessionType.NEW_SESSION);
        // delay start means it starts paused
        newStateSession.toSend = false;
        // sdk click handler does not start paused
        newStateSession.sdkClickHandlerAlsoStartsPaused = false;
        // delay configured
        newStateSession.delayStart = "10.0";

        stopActivity(activityHandler);
        SystemClock.sleep(1000);

        StateEndSession stateEndSession = new StateEndSession();
        checkEndSession(stateEndSession);

        activityHandler.teardown(false);
        activityHandler = null;

        SystemClock.sleep(1000);

        ActivityHandler restartActivityHandler = getActivityHandler(config);

        SystemClock.sleep(1500);

        // start new one
        // delay start not configured because activity state is already created
        StateActivityHandlerInit restartActivityHandlerInit = new StateActivityHandlerInit(restartActivityHandler);
        restartActivityHandlerInit.activityStateAlreadyCreated = true;
        restartActivityHandlerInit.readActivityState = "ec:0 sc:1";
        restartActivityHandlerInit.updatePackages = true;

        // test init values
        checkInitTests(restartActivityHandlerInit);

        resumeActivity(restartActivityHandler);

        SystemClock.sleep(1500);

        StateSession stateRestartSession = new StateSession(StateSession.SessionType.NEW_SUBSESSION);
        stateRestartSession.activityStateAlreadyCreated = true;
        stateRestartSession.subsessionCount = 2;

        checkStartInternal(stateRestartSession);
    }

    @Test
    public void testLogLevel() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testLogLevel");

        AdjustConfig config = getConfig();

        config.setLogLevel(LogLevel.VERBOSE);
        config.setLogLevel(LogLevel.DEBUG);
        config.setLogLevel(LogLevel.INFO);
        config.setLogLevel(LogLevel.WARN);
        config.setLogLevel(LogLevel.ERROR);
        config.setLogLevel(LogLevel.ASSERT);

        assertUtil.test("MockLogger setLogLevel: " + LogLevel.VERBOSE + ", isProductionEnvironment: false");
        assertUtil.test("MockLogger setLogLevel: " + LogLevel.DEBUG + ", isProductionEnvironment: false");
        assertUtil.test("MockLogger setLogLevel: " + LogLevel.INFO + ", isProductionEnvironment: false");
        assertUtil.test("MockLogger setLogLevel: " + LogLevel.WARN + ", isProductionEnvironment: false");
        assertUtil.test("MockLogger setLogLevel: " + LogLevel.ERROR + ", isProductionEnvironment: false");
        assertUtil.test("MockLogger setLogLevel: " + LogLevel.ASSERT + ", isProductionEnvironment: false");

        config.setLogLevel(LogLevel.SUPRESS);

        // chooses Assert because config object was not configured to allow suppress
        //assertUtil.test("MockLogger setLogLevel: " + LogLevel.ASSERT);
        // changed when log in production was introduced
        assertUtil.test("MockLogger setLogLevel: " + LogLevel.SUPRESS + ", isProductionEnvironment: false");

        // init log level with assert because it was not configured to allow suppress
        config = getConfig("production", "123456789012", false, context);

        config.setLogLevel(LogLevel.SUPRESS);

        // chooses Assert because config object was not configured to allow suppress
        //assertUtil.test("MockLogger setLogLevel: " + LogLevel.ASSERT);
        // changed when log in production was introduced
        assertUtil.test("MockLogger setLogLevel: " + LogLevel.SUPRESS + ", isProductionEnvironment: true");

        // init with info because it's sandbox
        config = getConfig("sandbox", "123456789012", true, context);

        config.setLogLevel(LogLevel.SUPRESS);
        // chooses Supress because config object was configured to allow suppress
        assertUtil.test("MockLogger setLogLevel: " + LogLevel.SUPRESS + ", isProductionEnvironment: false");

        // init with info because it's sandbox
        config = getConfig("production", "123456789012", true, context);

        config.setLogLevel(LogLevel.ASSERT);

        // chooses Supress because config object was configured to allow suppress
        //assertUtil.test("MockLogger setLogLevel: " + LogLevel.SUPRESS);
        // changed when log in production was introduced
        assertUtil.test("MockLogger setLogLevel: " + LogLevel.ASSERT + ", isProductionEnvironment: true");
    }

    @Test
    public void testTeardown() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testTeardown");

        //  change the timer defaults
        AdjustFactory.setTimerInterval(4000);

        // create the config to start the session
        AdjustConfig config = getConfig();

        config.setDelayStart(4);

        // enable send in the background
        config.setSendInBackground(true);

        // start activity handler with config
        ActivityHandler activityHandler = getActivityHandler(config);

        SystemClock.sleep(1500);

        // handlers start sending
        StateActivityHandlerInit stateActivityHandlerInit = new StateActivityHandlerInit(activityHandler);
        stateActivityHandlerInit.startsSending = false;
        stateActivityHandlerInit.sendInBackgroundConfigured = true;
        stateActivityHandlerInit.foregroundTimerCycle = 4;
        stateActivityHandlerInit.delayStartConfigured = true;
        stateActivityHandlerInit.sdkClickHandlerAlsoStartsPaused = false;
        checkInitTests(stateActivityHandlerInit);

        resumeActivity(activityHandler);

        SystemClock.sleep(1500);

        // test session
        StateSession newStateSession= new StateSession(StateSession.SessionType.NEW_SESSION);
        newStateSession.sendInBackgroundConfigured = true;
        newStateSession.toSend = false;
        newStateSession.sdkClickHandlerAlsoStartsPaused = false;
        newStateSession.delayStart = "4.0";
        checkStartInternal(newStateSession);

        activityHandler.teardown(false);

        assertUtil.test("PackageHandler teardown deleteState, false");
        assertUtil.test("AttributionHandler teardown");
        assertUtil.test("SdkClickHandler teardown");

        activityHandler.teardown(false);

        assertUtil.notInTest("PackageHandler teardown deleteState, false");
        assertUtil.notInTest("AttributionHandler teardown");
        assertUtil.notInTest("SdkClickHandler teardown");
    }

    private AdjustConfig getConfig() {
        return getConfig("sandbox", "123456789012", false, context);
    }

    private AdjustConfig getConfig(String environment,
                                   String appToken,
                                   boolean allowSupressLogLevel,
                                   Context context)
    {
        AdjustConfig adjustConfig = null;

        if (allowSupressLogLevel) {
            adjustConfig = new AdjustConfig(context, appToken, environment, allowSupressLogLevel);
        } else {
            adjustConfig = new AdjustConfig(context, appToken, environment);
        }

        if (adjustConfig != null) {
            if (environment == "sandbox") {
                assertUtil.test("MockLogger setLogLevel: " + LogLevel.INFO + ", isProductionEnvironment: " + false);
                assertUtil.warn("SANDBOX: Adjust is running in Sandbox mode. Use this setting for testing. Don't forget to set the environment to `production` before publishing!");
            } else if (environment == "production" && !allowSupressLogLevel) {
                assertUtil.test("MockLogger setLogLevel: " + LogLevel.INFO + ", isProductionEnvironment: " + true);
                assertUtil.warn("PRODUCTION: Adjust is running in Production mode. Use this setting only for the build that you want to publish. Set the environment to `sandbox` if you want to test your app!");
            } else if (environment == "production" && allowSupressLogLevel) {
                assertUtil.test("MockLogger setLogLevel: " + LogLevel.SUPRESS + ", isProductionEnvironment: " + true);
                assertUtil.warn("PRODUCTION: Adjust is running in Production mode. Use this setting only for the build that you want to publish. Set the environment to `sandbox` if you want to test your app!");
            } else {
                assertUtil.fail();
            }
        }

        return adjustConfig;
    }

    private ActivityHandler startAndCheckFirstSession(AdjustConfig config) {
        // start activity handler with config
        ActivityHandler activityHandler = getActivityHandler(config);

        SystemClock.sleep(1500);

        startAndCheckFirstSession(activityHandler);

        return activityHandler;
    }

    private void startAndCheckFirstSession(ActivityHandler activityHandler) {
        // test init values
        checkInitTests(activityHandler);

        resumeActivity(activityHandler);

        SystemClock.sleep(1500);

        // test session
        checkFirstSession();
    }

    private ActivityHandler getActivityHandler(AdjustConfig config) {
        ActivityHandler activityHandler = ActivityHandler.getInstance(config);

        if (activityHandler != null) {
            assertUtil.test("MockLogger lockLogLevel");

            ActivityHandler.InternalState internalState = activityHandler.getInternalState();
            // test default values
            assertUtil.isTrue(internalState.isEnabled());
            assertUtil.isTrue(internalState.isOnline());
            assertUtil.isTrue(internalState.isInBackground());
            assertUtil.isTrue(internalState.isNotInDelayedStart());
            assertUtil.isFalse(internalState.itHasToUpdatePackages());
        }

        return activityHandler;
    }

    private void checkInitAndStart(ActivityHandler activityHandler, StateActivityHandlerInit initState, StateSession stateSession) {
        checkInitTests(initState);

        resumeActivity(activityHandler);

        SystemClock.sleep(1500);

        checkStartInternal(stateSession);
    }

    private void checkInitTests(ActivityHandler activityHandler) {
        StateActivityHandlerInit stateActivityHandlerInit = new StateActivityHandlerInit(activityHandler);
        checkInitTests(stateActivityHandlerInit);
    }

    private void checkInitTests(StateActivityHandlerInit sInit) {
        checkReadFile(sInit.readAttribution, "Attribution");
        checkReadFile(sInit.readActivityState, "Activity state");
        checkReadFile(sInit.readCallbackParameters, "Session Callback parameters");
        checkReadFile(sInit.readPartnerParameters, "Session Partner parameters");

        // check values read from activity state
        assertUtil.isEqual(sInit.internalState.isEnabled(), sInit.startEnabled);
        //assertUtil.isEqual(sInit.internalState.itHasToUpdatePackages(), sInit.updatePackages);

        // check event buffering
        if (sInit.eventBufferingIsEnabled) {
            assertUtil.info("Event buffering is enabled");
        } else {
            assertUtil.notInInfo("Event buffering is enabled");
        }

        // check Google play is set
        assertUtil.info("Google Play Services Advertising ID read correctly at start time");

        // check default tracker
        if (sInit.defaultTracker != null) {
            assertUtil.info("Default tracker: '%s'", sInit.defaultTracker);
        } else {
            assertUtil.notInInfo("Default tracker: ");
        }

        // check push token
        if (sInit.pushToken != null) {
            assertUtil.info("Push token: '%s'", sInit.pushToken);
        } else {
            assertUtil.notInInfo("Push token: ");
        }

        // check foreground timer was created
        assertUtil.verbose("Foreground timer configured to fire after "+ sInit.foregroundTimerStart
                + ".0 seconds of starting and cycles every " + sInit.foregroundTimerCycle + ".0 seconds");

        // check background timer was created
        if (sInit.sendInBackgroundConfigured) {
            assertUtil.info("Send in background configured");
        } else {
            assertUtil.notInInfo("Send in background configured");
        }

        if (sInit.delayStartConfigured) {
            assertUtil.info("Delay start configured");
            assertUtil.isTrue(sInit.internalState.isInDelayedStart());
        } else {
            assertUtil.notInInfo("Delay start configured");
            assertUtil.isFalse(sInit.internalState.isInDelayedStart());
        }

        if (sInit.startsSending) {
            assertUtil.test("PackageHandler init, startsSending: true");
            assertUtil.test("AttributionHandler init, startsSending: true");
            assertUtil.test("SdkClickHandler init, startsSending: true");
        } else {
            assertUtil.test("PackageHandler init, startsSending: false");
            assertUtil.test("AttributionHandler init, startsSending: false");
            if (sInit.sdkClickHandlerAlsoStartsPaused) {
                assertUtil.test("SdkClickHandler init, startsSending: false");
            } else {
                assertUtil.test("SdkClickHandler init, startsSending: true");
            }
        }

        if (sInit.updatePackages) {
            checkUpdatePackages(sInit.internalState, sInit.activityStateAlreadyCreated);
        } else {
            assertUtil.notInTest("PackageHandler updatePackages");
        }

        if (sInit.sendReferrer != null) {
            assertUtil.verbose("Referrer to parse (%s)", sInit.sendReferrer);
            assertUtil.test("SdkClickHandler sendSdkClick");
        } else {
            assertUtil.notInVerbose("Referrer to parse ");
            assertUtil.notInTest("SdkClickHandler sendSdkClick");
        }
    }

    private void checkReadFile(String fileLog, String objectName) {
        if (fileLog == null) {
            assertUtil.debug(objectName + " file not found");
        } else {
            assertUtil.debug("Read "+ objectName +": " + fileLog);
        }
    }

    private void resumeActivity(ActivityHandler activityHandler) {
        // start activity
        activityHandler.onResume();

        ActivityHandler.InternalState internalState = activityHandler.getInternalState();

        // comes to the foreground
        assertUtil.isTrue(internalState.isInForeground());
    }

    private void checkFirstSession() {
        StateSession newStateSession= new StateSession(StateSession.SessionType.NEW_SESSION);
        checkStartInternal(newStateSession);
    }

    private void checkSubSession(int sessionCount, int subsessionCount, boolean getAttributionIsCalled) {
        StateSession subSessionState = new StateSession(StateSession.SessionType.NEW_SUBSESSION);
        subSessionState.sessionCount = sessionCount;
        subSessionState.subsessionCount = subsessionCount;
        subSessionState.getAttributionIsCalled = getAttributionIsCalled;
        subSessionState.foregroundTimerAlreadyStarted = true;
        checkStartInternal(subSessionState);
    }

    private void checkFurtherSessions(int sessionCount, boolean getAttributionIsCalled) {
        StateSession subSessionState = new StateSession(StateSession.SessionType.NEW_SESSION);
        subSessionState.sessionCount = sessionCount;
        subSessionState.getAttributionIsCalled = getAttributionIsCalled;
        subSessionState.foregroundTimerAlreadyStarted = true;
        checkStartInternal(subSessionState);
    }

    private void checkStartInternal(StateSession stateSession)
    {
        // check delay start
        checkDelayStart(stateSession);

        // check onResume
        checkOnResume(stateSession);

        // update Handlers Status
        checkHandlerStatus((stateSession.disabled ? null : !stateSession.toSend), stateSession.eventBufferingIsEnabled, stateSession.sdkClickHandlerAlsoStartsPaused);

        // process Session
        switch (stateSession.sessionType) {
            case NEW_SESSION:
                // if the package was build, it was sent to the Package Handler
                assertUtil.test("PackageHandler addPackage");

                // after adding, the activity handler ping the Package handler to send the package
                assertUtil.test("PackageHandler sendFirstPackage");

                // writes activity state
                assertUtil.debug("Wrote Activity state: " +
                        "ec:" + stateSession.eventCount + " sc:" + stateSession.sessionCount + " ssc:" + stateSession.subsessionCount);
                break;
            case NEW_SUBSESSION:
                // test the subsession message
                assertUtil.verbose("Started subsession " + stateSession.subsessionCount + " of session " + stateSession.sessionCount);
                // writes activity state
                assertUtil.debug("Wrote Activity state: " +
                        "ec:" + stateSession.eventCount + " sc:" + stateSession.sessionCount + " ssc:" + stateSession.subsessionCount);
                break;
            case NONSESSION:
                // stopped for a short time, not enough for a new sub subsession
                assertUtil.verbose("Time span since last activity too short for a new subsession");
                // does not writes activity state
                assertUtil.notInDebug("Wrote Activity state: ");
                break;
            case TIME_TRAVEL:
                assertUtil.error("Time travel!");
                // writes activity state
                assertUtil.debug("Wrote Activity state: " +
                        "ec:" + stateSession.eventCount + " sc:" + stateSession.sessionCount + " ssc:" + stateSession.subsessionCount);
                break;
            case DISABLED:
                assertUtil.notInTest("PackageHandler addPackage");
                assertUtil.notInVerbose("Started subsession ");
                assertUtil.notInVerbose("Time span since last activity too short for a new subsession");
                assertUtil.notInError("Time travel!");
                // does not writes activity state
                assertUtil.notInDebug("Wrote Activity state: ");
                break;
        }

        /*
        // after processing the session, writes the activity state
        if (stateSession.sessionType != stateSession.sessionType.NONSESSION &&
                stateSession.sessionType != stateSession.sessionType.DISABLED)
        {
            assertUtil.debug("Wrote Activity state: " +
                    "ec:" + stateSession.eventCount + " sc:" + stateSession.sessionCount + " ssc:" + stateSession.subsessionCount);
        } else {

        }
        */
        // check Attribution State
        if (stateSession.getAttributionIsCalled != null) {
            if (stateSession.getAttributionIsCalled) {
                assertUtil.test("AttributionHandler getAttribution");
            } else {
                assertUtil.notInTest("AttributionHandler getAttribution");
            }
        }
    }

    private void checkDelayStart(StateSession stateSession) {
        if (stateSession.delayStart == null) {
            assertUtil.notInWarn("Waiting");
            return;
        }

        if (stateSession.delayStart.equals("10.1")) {
            assertUtil.warn("Delay start of 10.1 seconds bigger than max allowed value of 10.0 seconds");
            stateSession.delayStart = "10.0";
        }

        assertUtil.info("Waiting " + stateSession.delayStart + " seconds before starting first session");

        assertUtil.verbose("Delay Start timer starting. Launching in " + stateSession.delayStart + " seconds");

        if (stateSession.activityStateAlreadyCreated) {
            assertUtil.verbose("Wrote Activity state");
        }
    }

    private void checkOnResume(StateSession stateSession) {
        if (!stateSession.startSubsession) {
            assertUtil.notInVerbose("Background timer canceled");
            assertUtil.notInVerbose("Foreground timer is already started");
            assertUtil.notInVerbose("Foreground timer starting");
            assertUtil.notInVerbose("Subsession start");
            return;
        }
        // TODO check delay start

        // stops background timer
        if (stateSession.sendInBackgroundConfigured) {
            assertUtil.verbose("Background timer canceled");
        } else {
            assertUtil.notInVerbose("Background timer canceled");
        }

        // start foreground timer
        if (stateSession.foregroundTimerStarts) {
            if (stateSession.foregroundTimerAlreadyStarted) {
                assertUtil.verbose("Foreground timer is already started");
            } else {
                assertUtil.verbose("Foreground timer starting");
            }
        } else {
            assertUtil.notInVerbose("Foreground timer is already started");
            assertUtil.notInVerbose("Foreground timer starting");
        }

        // starts the subsession
        assertUtil.verbose("Subsession start");
    }

    private void checkHandlerStatus(Boolean pausing) {
        checkHandlerStatus(pausing, false, true);
    }

    private void checkHandlerStatus(Boolean pausing, boolean eventBufferingEnabled, boolean sdkClickHandlerAlsoPauses) {
        if (pausing == null) {
            assertUtil.notInTest("AttributionHandler pauseSending");
            assertUtil.notInTest("PackageHandler pauseSending");
            assertUtil.notInTest("SdkClickHandler pauseSending");
            assertUtil.notInTest("AttributionHandler resumeSending");
            assertUtil.notInTest("PackageHandler resumeSending");
            assertUtil.notInTest("SdkClickHandler resumeSending");
            return;
        }
        if (pausing) {
            assertUtil.test("AttributionHandler pauseSending");
            assertUtil.test("PackageHandler pauseSending");
            if (sdkClickHandlerAlsoPauses) {
                assertUtil.test("SdkClickHandler pauseSending");
            } else {
                assertUtil.test("SdkClickHandler resumeSending");
            }
        } else {
            assertUtil.test("AttributionHandler resumeSending");
            assertUtil.test("PackageHandler resumeSending");
            assertUtil.test("SdkClickHandler resumeSending");
            if (!eventBufferingEnabled) {
                assertUtil.test("PackageHandler sendFirstPackage");
            }
        }
    }

    private void checkEvent(StateEvent stateEvent) {
        if (stateEvent.disabled) {
            assertUtil.notInInfo("Skipping duplicated order ID ");
            assertUtil.notInVerbose("Added order ID ");
            assertUtil.notInTest("PackageHandler addPackage");
            assertUtil.notInInfo("Buffered event ");
            assertUtil.notInTest("PackageHandler sendFirstPackage");
            assertUtil.notInDebug("Wrote Activity state");
            return;
        }
        if (stateEvent.duplicatedOrderId) {
            // dropping duplicate transaction id
            assertUtil.info("Skipping duplicated order ID '" + stateEvent.orderId + "'");
            // check that event package was not added
            assertUtil.notInTest("PackageHandler addPackage");
            return;
        }

        if (stateEvent.orderId != null) {
            // check order id was added
            assertUtil.verbose("Added order ID '" + stateEvent.orderId + "'");
        } else {
            // check order id was not added
            assertUtil.notInVerbose("Added order ID");
        }

        // check that event package was added
        assertUtil.test("PackageHandler addPackage");

        if(stateEvent.bufferedSuffix != null) {
            // check that event was buffered
            assertUtil.info("Buffered event " + stateEvent.bufferedSuffix);

            // and not sent to package handler
            assertUtil.notInTest("PackageHandler sendFirstPackage");
        } else {
            // check that event was sent to package handler
            assertUtil.test("PackageHandler sendFirstPackage");
            // and not buffered
            assertUtil.notInInfo("Buffered event");
        }

        if (stateEvent.backgroundTimerStarts != null) {
            // does not fire background timer
            assertUtil.verbose("Background timer starting. Launching in " + stateEvent.backgroundTimerStarts + ".0 seconds");
        } else {
            // does not fire background timer
            assertUtil.notInVerbose("Background timer starting");
        }

        // after tracking the event it should write the activity state
        if (stateEvent.activityStateSuffix != null) {
            assertUtil.debug("Wrote Activity state: " + stateEvent.activityStateSuffix);
        } else {
            assertUtil.debug("Wrote Activity state");
        }
    }

    private void stopActivity(ActivityHandler activityHandler) {
        // stop activity
        activityHandler.onPause();

        ActivityHandler.InternalState internalState = activityHandler.getInternalState();

        // goes to the background
        assertUtil.isTrue(internalState.isInBackground());
    }

    private void checkEndSession() {
        StateEndSession stateEndSession = new StateEndSession();
        checkEndSession(stateEndSession);
    }

    private void checkEndSession(StateEndSession stateEndSession)
    {
        if (stateEndSession.checkOnPause) {
            checkOnPause(stateEndSession.foregroundAlreadySuspended, stateEndSession.backgroundTimerStarts);
        }

        if (stateEndSession.pausing) {
            checkHandlerStatus(stateEndSession.pausing, stateEndSession.eventBufferingEnabled, true);
        }

        if (stateEndSession.updateActivityState) {
            assertUtil.debug("Wrote Activity state: ");
        } else {
            assertUtil.notInDebug("Wrote Activity state: ");
        }
    }

    private void checkOnPause(boolean foregroundAlreadySuspended,
                              boolean backgroundTimerStarts) {
        // stop foreground timer
        if (foregroundAlreadySuspended) {
            assertUtil.verbose("Foreground timer is already suspended");
        } else {
            assertUtil.verbose("Foreground timer suspended");
        }

        // start background timer
        if (backgroundTimerStarts) {
            assertUtil.verbose("Background timer starting.");
        } else {
            assertUtil.notInVerbose("Background timer starting.");
        }

        // starts the subsession
        assertUtil.verbose("Subsession end");
    }

    private void checkForegroundTimerFired(boolean timerFired) {
        // timer fired
        if (timerFired) {
            assertUtil.verbose("Foreground timer fired");
        } else {
            assertUtil.notInVerbose("Foreground timer fired");
        }
    }

    private void checkSendFirstPackages(boolean delayStart,
                                        ActivityHandler.InternalState internalState,
                                        boolean activityStateCreated,
                                        boolean pausing)
    {
        if (!delayStart) {
            assertUtil.info("Start delay expired or never configured");
            // did not update package
            assertUtil.notInTest("PackageHandler updatePackages");
            return;
        }
        assertUtil.notInInfo("Start delay expired or never configured");

        // update packages
        checkUpdatePackages(internalState, activityStateCreated);

        // no longer is in delay start
        assertUtil.isFalse(internalState.delayStart);

        // cancel timer
        assertUtil.verbose("Delay Start timer canceled");

        checkHandlerStatus(pausing, false, false);
    }

    private void checkUpdatePackages(ActivityHandler.InternalState internalState,
                                     boolean activityStateCreated) {
        // update packages
        assertUtil.test("PackageHandler updatePackages");
        assertUtil.isFalse(internalState.updatePackages);
        if (activityStateCreated) {
            assertUtil.debug("Wrote Activity state");
        } else {
            assertUtil.notInDebug("Wrote Activity state");
        }

    }
}
