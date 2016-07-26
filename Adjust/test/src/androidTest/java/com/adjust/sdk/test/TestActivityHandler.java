package com.adjust.sdk.test;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.SystemClock;
import android.test.ActivityInstrumentationTestCase2;
import android.test.mock.MockContext;

import com.adjust.sdk.ActivityHandler;
import com.adjust.sdk.ActivityHandler.InternalState;
import com.adjust.sdk.ActivityPackage;
import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustAttribution;
import com.adjust.sdk.AdjustConfig;
import com.adjust.sdk.AdjustEvent;
import com.adjust.sdk.AdjustEventFailure;
import com.adjust.sdk.AdjustEventSuccess;
import com.adjust.sdk.AdjustFactory;
import com.adjust.sdk.AdjustSessionFailure;
import com.adjust.sdk.AdjustSessionSuccess;
import com.adjust.sdk.AttributionResponseData;
import com.adjust.sdk.ClickResponseData;
import com.adjust.sdk.Constants;
import com.adjust.sdk.EventResponseData;
import com.adjust.sdk.LogLevel;
import com.adjust.sdk.OnAttributionChangedListener;
import com.adjust.sdk.OnDeeplinkResponseListener;
import com.adjust.sdk.OnEventTrackingFailedListener;
import com.adjust.sdk.OnEventTrackingSucceededListener;
import com.adjust.sdk.OnSessionTrackingFailedListener;
import com.adjust.sdk.OnSessionTrackingSucceededListener;
import com.adjust.sdk.ResponseData;
import com.adjust.sdk.SessionResponseData;

import org.json.JSONException;
import org.json.JSONObject;


public class TestActivityHandler extends ActivityInstrumentationTestCase2<UnitTestActivity> {
    protected MockLogger mockLogger;
    protected MockPackageHandler mockPackageHandler;
    protected MockAttributionHandler mockAttributionHandler;
    protected MockSdkClickHandler mockSdkClickHandler;
    protected UnitTestActivity activity;
    protected Context context;
    protected AssertUtil assertUtil;

    public TestActivityHandler() {
        super(UnitTestActivity.class);
    }

    public TestActivityHandler(Class<UnitTestActivity> mainActivity) {
        super(mainActivity);
    }

    // TODO: 7/22/16 Try to remove the MockLogger and use JUnit and Mockito all the way
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mockLogger = new MockLogger();
        mockPackageHandler = new MockPackageHandler(mockLogger);
        mockAttributionHandler = new MockAttributionHandler(mockLogger);
        mockSdkClickHandler = new MockSdkClickHandler(mockLogger);
        assertUtil = new AssertUtil(mockLogger);

        AdjustFactory.setLogger(mockLogger);
        AdjustFactory.setPackageHandler(mockPackageHandler);
        AdjustFactory.setAttributionHandler(mockAttributionHandler);
        AdjustFactory.setSdkClickHandler(mockSdkClickHandler);

        activity = getActivity();
        context = activity.getApplicationContext();

        // deleting the activity state file to simulate a first session
        boolean activityStateDeleted = ActivityHandler.deleteActivityState(context);
        boolean attributionDeleted = ActivityHandler.deleteAttribution(context);

        mockLogger.test("Was AdjustActivityState deleted? " + activityStateDeleted);

        // deleting the attribution file to simulate a first session
        mockLogger.test("Was Attribution deleted? " + attributionDeleted);

        // check the server url
        assertEquals(Constants.BASE_URL, "https://app.adjust.com");
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

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

    public void testFirstSession() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testFirstSession");

        // create the config to start the session
        AdjustConfig config = getConfig();

        // start activity handler with config
        startAndCheckFirstSession(config);

        // checking the default values of the first session package
        // should only have one package
        assertEquals(1, mockPackageHandler.queue.size());

        ActivityPackage activityPackage = mockPackageHandler.queue.get(0);

        // create activity package test
        TestActivityPackage testActivityPackage = new TestActivityPackage(activityPackage);

        // set first session
        testActivityPackage.testSessionPackage(1);
    }

    public void testEventsBuffered() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testEventsBuffered");

        // create the config to start the session
        AdjustConfig config = getConfig(LogLevel.VERBOSE);

        // buffer events
        config.setEventBufferingEnabled(true);

        // start activity handler with config
        ActivityHandler activityHandler = getFirstActivityHandler(config, LogLevel.VERBOSE);

        SystemClock.sleep(2000);

        // test init values
        checkInitTests(
                true,   // eventBuffering
                null,   // defaultTracker
                false);  // startsSending

        startActivity(activityHandler);

        SystemClock.sleep(2000);

        SessionState newSessionState = new SessionState(SessionType.NEW_SESSION);
        newSessionState.eventBufferingIsEnabled = true;

        // test first session start
        checkStartInternal(newSessionState);

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

        // add revenue
        firstEvent.setRevenue(0.001, "EUR");

        // track event
        activityHandler.trackEvent(firstEvent);

        // create the second Event
        AdjustEvent secondEvent = new AdjustEvent("event2");

        // add empty revenue
        secondEvent.setRevenue(0, "USD");

        // track second event
        activityHandler.trackEvent(secondEvent);

        // create third Event
        AdjustEvent thirdEvent = new AdjustEvent("event3");

        // track third event
        activityHandler.trackEvent(thirdEvent);

        SystemClock.sleep(3000);

        // test first event
        // check that callback parameter was overwritten
        assertUtil.warn("key keyCall was overwritten");

        // check that partner parameter was overwritten
        assertUtil.warn("key keyPartner was overwritten");

        // check that event package was added
        assertUtil.test("PackageHandler addPackage");

        // check that event was buffered
        assertUtil.info("Buffered event (0.00100 EUR, 'event1')");

        // and not sent to package handler
        assertUtil.notInTest("PackageHandler sendFirstPackage");

        // does not fire background timer
        assertUtil.notInVerbose("Background timer starting");

        // after tracking the event it should write the activity state
        assertUtil.debug("Wrote Activity state");

        // test second event
        // check that event package was added
        assertUtil.test("PackageHandler addPackage");

        // check that event was buffered
        assertUtil.info("Buffered event (0.00000 USD, 'event2')");

        // and not sent to package handler
        assertUtil.notInTest("PackageHandler sendFirstPackage");

        // does not fire background timer
        assertUtil.notInVerbose("Background timer starting");

        // after tracking the event it should write the activity state
        assertUtil.debug("Wrote Activity state");

        // test third event
        // check that event package was added
        assertUtil.test("PackageHandler addPackage");

        // check that event was buffered
        assertUtil.info("Buffered event 'event3'");

        // and not sent to package handler
        assertUtil.notInTest("PackageHandler sendFirstPackage");

        // does not fire background timer
        assertUtil.notInVerbose("Background timer starting");

        // after tracking the event it should write the activity state
        assertUtil.debug("Wrote Activity state");

        // check the number of activity packages
        // 1 session + 3 events
        assertEquals(4, mockPackageHandler.queue.size());

        ActivityPackage firstSessionPackage = mockPackageHandler.queue.get(0);

        // create activity package test
        TestActivityPackage testFirstSessionPackage = new TestActivityPackage(firstSessionPackage);
        testFirstSessionPackage.eventBufferingEnabled = true;

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
        testSecondEventPackage.suffix = "(0.00000 USD, 'event2')";
        testSecondEventPackage.revenueString = "0.00000";
        testSecondEventPackage.currency = "USD";
        testSecondEventPackage.eventBufferingEnabled = true;

        // test second event
        testSecondEventPackage.testEventPackage("event2");

        // third event
        ActivityPackage thirdEventPackage = mockPackageHandler.queue.get(3);

        // create event package test
        TestActivityPackage testThirdEventPackage = new TestActivityPackage(thirdEventPackage);

        // set event test parameters
        testThirdEventPackage.eventCount = "3";
        testThirdEventPackage.suffix = "'event3'";
        testThirdEventPackage.eventBufferingEnabled = true;

        // test third event
        testThirdEventPackage.testEventPackage("event3");
    }

    public void testEventsNotBuffered() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testEventsNotBuffered");

        // create the config to start the session
        AdjustConfig config = getConfig(LogLevel.DEBUG);

        // start activity handler with config
        ActivityHandler activityHandler = getFirstActivityHandler(config, LogLevel.DEBUG);

        SystemClock.sleep(2000);

        // test init values
        checkInitTests();

        startActivity(activityHandler);

        SystemClock.sleep(2000);

        // test session
        checkFirstSession();

        // create the first Event
        AdjustEvent firstEvent = new AdjustEvent("event1");

        // track event
        activityHandler.trackEvent(firstEvent);

        SystemClock.sleep(2000);

        // check that event package was added
        assertUtil.test("PackageHandler addPackage");

        // check that event was sent to package handler
        assertUtil.test("PackageHandler sendFirstPackage");

        // and not buffered
        assertUtil.notInInfo("Buffered event");

        // does not fire background timer
        assertUtil.notInVerbose("Background timer starting");

        // after tracking the event it should write the activity state
        assertUtil.debug("Wrote Activity state");
    }

    public void testChecks() {
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
        AdjustConfig config = getConfig(LogLevel.WARN);

        // create handler and start the first session
        ActivityHandler activityHandler = startAndCheckFirstSession(config, LogLevel.WARN);

        // track null event
        activityHandler.trackEvent(null);
        SystemClock.sleep(1000);

        assertUtil.error("Event missing");

        activityHandler.trackEvent(nullEventToken);
        SystemClock.sleep(1000);

        assertUtil.error("Event not initialized correctly");
    }

    public void testSessions() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testSessions");

        // adjust the session intervals for testing
        AdjustFactory.setSessionInterval(4000);
        AdjustFactory.setSubsessionInterval(1000);

        // create the config to start the session
        AdjustConfig config = getConfig(LogLevel.INFO);

        // start activity handler with config
        ActivityHandler activityHandler = startAndCheckFirstSession(config, LogLevel.INFO);

        // end subsession
        stopActivity(activityHandler);

        SystemClock.sleep(1000);

        // test the end of the subsession
        checkEndSession();

        startActivity(activityHandler);

        SystemClock.sleep(1000);

        // test the new sub session
        SessionState secondSubsession = new SessionState(SessionType.NEW_SUBSESSION);
        secondSubsession.subsessionCount = 2;
        checkStartInternal(secondSubsession);

        stopActivity(activityHandler);

        SystemClock.sleep(5000);

        // test the end of the subsession
        checkEndSession();

        // trigger a new session
        activityHandler.onResume();

        SystemClock.sleep(1000);

        // new session
        SessionState secondSession = new SessionState(SessionType.NEW_SESSION);
        secondSession.sessionCount = 2;
        secondSession.timerAlreadyStarted = true;
        checkStartInternal(secondSession);

        // stop and start the activity with little interval
        // so it won't trigger a sub session
        stopActivity(activityHandler);
        startActivity(activityHandler);

        SystemClock.sleep(1000);

        // test the end of the subsession
        checkEndSession(false, true, false);

        // test non sub session
        SessionState nonSessionState = new SessionState(SessionType.NONSESSION);
        checkStartInternal(nonSessionState);

        // 2 session packages
        assertEquals(2, mockPackageHandler.queue.size());

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

    public void testDisable() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testDisable");

        // adjust the session intervals for testing
        AdjustFactory.setSessionInterval(4000);
        AdjustFactory.setSubsessionInterval(1000);

        // create the config to start the session
        AdjustConfig config = getConfig(LogLevel.ERROR);

        // start activity handler with config
        ActivityHandler activityHandler = getFirstActivityHandler(config, LogLevel.ERROR);

        // disable sdk while it has not started yet
        activityHandler.setEnabled(false);

        // check that it is disabled
        assertUtil.isFalse(activityHandler.isEnabled());

        SystemClock.sleep(2000);

        // not writing activity state because it set enable does not start the sdk
        assertUtil.notInDebug("Wrote Activity state");

        // check if message the disable of the SDK
        assertUtil.info("Package handler and attribution handler will start as paused due to the SDK being disabled");

        checkInitTests(false);

        checkHandlerStatus(true);

        // start the sdk
        // foreground timer does not start because it's paused
        startActivity(activityHandler);

        SystemClock.sleep(2000);

        SessionState sessionStartsPaused = new SessionState(SessionType.NEW_SESSION);
        sessionStartsPaused.paused = true;
        sessionStartsPaused.toSend = false;
        sessionStartsPaused.foregroundTimerStarts = false;
        sessionStartsPaused.foregroundTimerAlreadyStarted = false;

        // check session that is paused
        checkStartInternal(sessionStartsPaused);

        stopActivity(activityHandler);

        SystemClock.sleep(1000);

        // test end session of disable
        checkEndSession(true,   //pausing
                        true,   // updateActivityState
                        false); // eventBufferingEnabled


        SystemClock.sleep(1000);

        // try to do activities while SDK disabled
        activityHandler.onResume();
        activityHandler.trackEvent(new AdjustEvent("event1"));

        SystemClock.sleep(3000);

        checkStartDisable();

        // only the first session package should be sent
        assertEquals(1, mockPackageHandler.queue.size());

        // put in offline mode
        activityHandler.setOfflineMode(true);

        // pausing due to offline mode
        assertUtil.info("Pausing package and attribution handler to put SDK offline mode");

        // wait to update status
        SystemClock.sleep(5000);

        // after pausing, even when it's already paused
        // tries to update the status
        checkHandlerStatus(true);

        // re-enable the SDK
        activityHandler.setEnabled(true);

        // check that it is enabled
        assertUtil.isTrue(activityHandler.isEnabled());

        // check message of SDK still paused
        assertUtil.info("Package and attribution handler remain paused due to SDK being offline");

        SystemClock.sleep(1000);

        // due to the fact it will remained paused,
        // there is no need to try to update the status
        checkHandlerStatus(null);

        // start the sdk
        // foreground timer does not start because it's offline
        startActivity(activityHandler);

        SystemClock.sleep(1000);

        SessionState SecondPausedSession = new SessionState(SessionType.NEW_SESSION);
        SecondPausedSession.toSend = false;
        SecondPausedSession.paused = true;
        SecondPausedSession.sessionCount = 2;
        SecondPausedSession.timerAlreadyStarted = null;
        SecondPausedSession.foregroundTimerStarts = false;
        sessionStartsPaused.foregroundTimerAlreadyStarted = false;
        checkStartInternal(SecondPausedSession);

        // track an event
        activityHandler.trackEvent(new AdjustEvent("event1"));

        SystemClock.sleep(5000);

        // check that it did add the event package
        assertUtil.test("PackageHandler addPackage");

        // and send it
        assertUtil.test("PackageHandler sendFirstPackage");

        // does not fire background timer
        assertUtil.notInVerbose("Background timer starting");

        // it should have the second session and the event
        assertEquals(3, mockPackageHandler.queue.size());

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

        testEventPackage.suffix = "'event1'";

        // test event
        testEventPackage.testEventPackage("event1");

        // end the session
        stopActivity(activityHandler);

        SystemClock.sleep(1000);

        checkEndSession();

        // put in online mode
        activityHandler.setOfflineMode(false);

        // message that is finally resuming
        assertUtil.info("Resuming package handler and attribution handler to put SDK in online mode");

        SystemClock.sleep(1000);

        // after un-pausing the sdk, tries to update the handlers
        // it is still paused because it's on the background
        checkHandlerStatus(true);

        startActivity(activityHandler);

        SystemClock.sleep(1000);

        SessionState ThirdSessionStarting = new SessionState(SessionType.NEW_SESSION);
        ThirdSessionStarting.sessionCount = 3;
        ThirdSessionStarting.eventCount = 1;
        ThirdSessionStarting.timerAlreadyStarted = false;
        checkStartInternal(ThirdSessionStarting);
    }

    public void testOpenUrl() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testOpenUrl");

        // create the config to start the session
        AdjustConfig config = getConfig(LogLevel.ASSERT);

        // start activity handler with config
        ActivityHandler activityHandler = startAndCheckFirstSession(config, LogLevel.ASSERT);

        Uri attributions = Uri.parse("AdjustTests://example.com/path/inApp?adjust_tracker=trackerValue&other=stuff&adjust_campaign=campaignValue&adjust_adgroup=adgroupValue&adjust_creative=creativeValue");
        Uri extraParams = Uri.parse("AdjustTests://example.com/path/inApp?adjust_foo=bar&other=stuff&adjust_key=value");
        Uri mixed = Uri.parse("AdjustTests://example.com/path/inApp?adjust_foo=bar&other=stuff&adjust_campaign=campaignValue&adjust_adgroup=adgroupValue&adjust_creative=creativeValue");
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
        activityHandler.readOpenUrl(emptyQueryString, now);
        activityHandler.readOpenUrl(emptyString, now);
        activityHandler.readOpenUrl(nullUri, now);
        activityHandler.readOpenUrl(single, now);
        activityHandler.readOpenUrl(prefix, now);
        activityHandler.readOpenUrl(incomplete, now);

        SystemClock.sleep(1000);

        // three click packages: attributions, extraParams and mixed
        for (int i = 7; i > 0; i--) {
            assertUtil.test("SdkClickHandler sendSdkClick");
        }

        assertUtil.notInTest("SdkClickHandler sendSdkClick");

        // 7 clicks
        assertEquals(7, mockSdkClickHandler.queue.size());

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
        ActivityPackage emptyQueryStringClickPackage = mockSdkClickHandler.queue.get(3);

        // create activity package test
        TestActivityPackage testEmptyQueryStringClickPackage = new TestActivityPackage(emptyQueryStringClickPackage);

        testEmptyQueryStringClickPackage.deeplink = emptyQueryString.toString();

        testEmptyQueryStringClickPackage.testClickPackage("deeplink");

        // get the click package
        ActivityPackage singleClickPackage = mockSdkClickHandler.queue.get(4);

        // create activity package test
        TestActivityPackage testSingleClickPackage = new TestActivityPackage(singleClickPackage);

        testSingleClickPackage.deeplink = single.toString();

        testSingleClickPackage.testClickPackage("deeplink");

        // get the click package
        ActivityPackage prefixClickPackage = mockSdkClickHandler.queue.get(5);

        // create activity package test
        TestActivityPackage testPrefixClickPackage = new TestActivityPackage(prefixClickPackage);

        testPrefixClickPackage.deeplink = prefix.toString();

        testPrefixClickPackage.testClickPackage("deeplink");

        // get the click package
        ActivityPackage incompleteClickPackage = mockSdkClickHandler.queue.get(6);

        // create activity package test
        TestActivityPackage testIncompleteClickPackage = new TestActivityPackage(incompleteClickPackage );

        testIncompleteClickPackage.deeplink = incomplete.toString();

        testIncompleteClickPackage.testClickPackage("deeplink");
    }

    public void testAttributionDelegate() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testAttributionDelegate");

        // create the config to start the session
        AdjustConfig config = new AdjustConfig(context, "123456789012", AdjustConfig.ENVIRONMENT_SANDBOX);

        config.setOnAttributionChangedListener(new OnAttributionChangedListener() {
            @Override
            public void onAttributionChanged(AdjustAttribution attribution) {
                mockLogger.test("onAttributionChanged: " + attribution);
            }
        });

        DelegatesPresent attributionDelegatePresent = new DelegatesPresent();
        attributionDelegatePresent.attributionDelegatePresent = true;
        checkFinishTasks(config, attributionDelegatePresent);
    }

    public void testSuccessDelegates() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testSuccessDelegates");

        // create the config to start the session
        AdjustConfig config = new AdjustConfig(context, "123456789012", AdjustConfig.ENVIRONMENT_SANDBOX);

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

        DelegatesPresent successDelegatesPresent = new DelegatesPresent();
        successDelegatesPresent.eventSuccessDelegatePresent = true;
        successDelegatesPresent.sessionSuccessDelegatePresent = true;

        checkFinishTasks(config, successDelegatesPresent);
    }

    public void testFailureDelegates() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testFailureDelegates");

        // create the config to start the session
        AdjustConfig config = new AdjustConfig(context, "123456789012", AdjustConfig.ENVIRONMENT_SANDBOX);

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

        DelegatesPresent failureDelegatesPresent = new DelegatesPresent();
        failureDelegatesPresent.sessionFailureDelegatePresent = true;
        failureDelegatesPresent.eventFailureDelegatePresent = true;

        checkFinishTasks(config, failureDelegatesPresent);
    }

    public void testLaunchDeepLink() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testLaunchDeepLink");

        // create the config to start the session
        AdjustConfig config = getConfig(LogLevel.VERBOSE, AdjustConfig.ENVIRONMENT_PRODUCTION, "123456789012", context);

        // start activity handler with config
        ActivityHandler activityHandler = getActivityHandler(config,
                true, // startEnabled,
                null, // readActivityState,
                null, // readAttribution,
                true, // isProductionEnvironment,
                LogLevel.VERBOSE); // logLevel

        SystemClock.sleep(2000);

        checkInitTests(
                false,  // eventBuffering
                null,   // defaultTracker
                false);  // startsSending

        startActivity(activityHandler);

        SystemClock.sleep(2000);

        // test first session start
        checkFirstSession();

        ResponseData responseDataNull = null;

        //activityHandler.finishedTrackingActivity(responseDataNull);
        SystemClock.sleep(1000);

        // if the response is null
        assertUtil.notInTest("AttributionHandler checkAttribution");
        assertUtil.notInError("Unable to open deep link");
        assertUtil.notInInfo("Open deep link");

        // set package handler to respond with a valid attribution
        ResponseData wrongDeeplinkResponseData = ResponseData.buildResponseData(mockPackageHandler.queue.get(0));
        try {
            wrongDeeplinkResponseData.jsonResponse = new JSONObject("{ " +
                    "\"deeplink\" :  \"wrongDeeplink://\" }");
        } catch (JSONException e) {
            fail(e.getMessage());
        }

        activityHandler.launchSessionResponseTasks((SessionResponseData) wrongDeeplinkResponseData);
        SystemClock.sleep(2000);

        // check that it was unable to open the url
        assertUtil.error("Unable to open deep link (wrongDeeplink://)");

        // TODO add test that opens url

        // checking the default values of the first session package
        // should only have one package
        assertEquals(1, mockPackageHandler.queue.size());

        ActivityPackage activityPackage = mockPackageHandler.queue.get(0);

        // create activity package test
        TestActivityPackage testActivityPackage = new TestActivityPackage(activityPackage);

        testActivityPackage.environment = AdjustConfig.ENVIRONMENT_PRODUCTION;

        // set first session
        testActivityPackage.testSessionPackage(1);
    }

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

        // set package handler to respond with a valid attribution
        ResponseData wrongDeeplinkResponseData = ResponseData.buildResponseData(mockPackageHandler.queue.get(0));
        try {
            wrongDeeplinkResponseData.jsonResponse = new JSONObject("{ " +
                    "\"deeplink\" :  \"wrongDeeplink://\" }");
        } catch (JSONException e) {
            fail(e.getMessage());
        }

        activityHandler.launchSessionResponseTasks((SessionResponseData) wrongDeeplinkResponseData);
        SystemClock.sleep(2000);

        // callback called
        assertUtil.test("launchReceivedDeeplink, wrongDeeplink://");

        // but deeplink not launched
        assertUtil.notInError("Unable to open deep link");
    }

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
        ResponseData wrongDeeplinkResponseData = ResponseData.buildResponseData(mockPackageHandler.queue.get(0));
        try {
            wrongDeeplinkResponseData.jsonResponse = new JSONObject("{ " +
                    "\"deeplink\" :  \"wrongDeeplink://\" }");
        } catch (JSONException e) {
            fail(e.getMessage());
        }

        activityHandler.launchSessionResponseTasks((SessionResponseData) wrongDeeplinkResponseData);
        SystemClock.sleep(2000);

        // callback called
        assertUtil.test("launchReceivedDeeplink, wrongDeeplink://");

        // but deeplink not launched
        assertUtil.error("Unable to open deep link");
    }

    public void testUpdateAttribution() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testUpdateAttribution");

        // create the config to start the session
        AdjustConfig config = getConfig();

        // start activity handler with config
        ActivityHandler firstActivityHandler = startAndCheckFirstSession(config);

        JSONObject nullJsonObject = null;
        AdjustAttribution nullAttribution = AdjustAttribution.fromJson(nullJsonObject);

        // check if Attribution wasn't built
        assertUtil.isNull(nullAttribution);

        // check that it does not update a null attribution
        assertUtil.isFalse(firstActivityHandler.updateAttribution(nullAttribution));

        // create an empty attribution
        JSONObject emptyJsonResponse = null;
        try {
            emptyJsonResponse = new JSONObject("{ }");
        } catch (JSONException e) {
            fail(e.getMessage());
        }
        AdjustAttribution emptyAttribution = AdjustAttribution.fromJson(emptyJsonResponse);

        // check that updates attribution
        assertUtil.isTrue(firstActivityHandler.updateAttribution(emptyAttribution));
        assertUtil.debug("Wrote Attribution: tt:null tn:null net:null cam:null adg:null cre:null cl:null");

        emptyAttribution = AdjustAttribution.fromJson(emptyJsonResponse);

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

        ActivityHandler restartActivityHandler = getActivityHandler (config,
                true,               // startEnabled
                "ec:0 sc:1 ssc:1",  // readActivityState
                "tt:null tn:null net:null cam:null adg:null cre:null cl:null", // readAttribution
                false,              // isProductionEnvironment
                LogLevel.INFO);     // loglevel

        SystemClock.sleep(2000);

        // test init values
        checkInitTests();

        startActivity(restartActivityHandler);

        SystemClock.sleep(2000);

        SessionState firstRestart = new SessionState(SessionType.NEW_SUBSESSION);
        firstRestart.subsessionCount = 2;
        firstRestart.timerAlreadyStarted = false;
        checkStartInternal(firstRestart);

        // check that it does not update the attribution after the restart
        assertUtil.isFalse(restartActivityHandler.updateAttribution(emptyAttribution));
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
            fail(e.getMessage());
        }
        AdjustAttribution firstAttribution = AdjustAttribution.fromJson(firstAttributionJson);

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

        ActivityHandler secondRestartActivityHandler = getActivityHandler(config,
                true,               // startEnabled
                "ec:0 sc:1 ssc:2",  // readActivityState
                "tt:ttValue tn:tnValue net:nValue cam:cpValue adg:aValue cre:ctValue cl:clValue",   // readAttribution
                false,              // isProductionEnvironment
                LogLevel.INFO);     // loglevel

        SystemClock.sleep(2000);

        // test init values
        checkInitTests();

        startActivity(secondRestartActivityHandler);

        SystemClock.sleep(2000);

        SessionState secondRestart = new SessionState(SessionType.NEW_SUBSESSION);
        secondRestart.subsessionCount = 3;
        secondRestart.timerAlreadyStarted = false;

        checkStartInternal(secondRestart);

        // check that it does not update the attribution after the restart
        assertUtil.isFalse(secondRestartActivityHandler.updateAttribution(firstAttribution));
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
            fail(e.getMessage());
        }
        AdjustAttribution secondAttribution = AdjustAttribution.fromJson(secondAttributionJson);

        //check that it updates
        attributionResponseDataWithAttribution.attribution = secondAttribution;
        secondRestartActivityHandler.launchAttributionResponseTasks(attributionResponseDataWithAttribution);
        SystemClock.sleep(2000);

        assertUtil.debug("Wrote Attribution: tt:ttValue2 tn:tnValue2 net:nValue2 cam:cpValue2 adg:aValue2 cre:ctValue2 cl:clValue2");

        // check that it launch the saved attribute
        assertUtil.test("secondRestartActivityHandler onAttributionChanged: tt:ttValue2 tn:tnValue2 net:nValue2 cam:cpValue2 adg:aValue2 cre:ctValue2 cl:clValue2");

        // check that it does not update the attribution
        assertUtil.isFalse(secondRestartActivityHandler.updateAttribution(secondAttribution));
        assertUtil.notInDebug("Wrote Attribution");
    }

    public void testOfflineMode() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testOfflineMode");

        // adjust the session intervals for testing
        AdjustFactory.setSessionInterval(2000);
        AdjustFactory.setSubsessionInterval(500);

        // create the config to start the session
        AdjustConfig config = getConfig();

        // start activity handler with config
        ActivityHandler activityHandler = getFirstActivityHandler(config);

        // put SDK offline
        activityHandler.setOfflineMode(true);

        InternalState internalState = activityHandler.getInternalState();

        // check if it's offline before the sdk starts
        assertUtil.isTrue(internalState.isOffline());

        SystemClock.sleep(2000);

        // not writing activity state because it set enable does not start the sdk
        assertUtil.notInDebug("Wrote Activity state");

        // check if message the disable of the SDK
        assertUtil.info("Package handler and attribution handler will start paused due to SDK being offline");

        // test init values
        checkInitTests(false);

        checkHandlerStatus(true);

        // start the sdk
        // foreground timer does not start because it's paused
        startActivity(activityHandler);

        SystemClock.sleep(2000);

        // test first session start
        SessionState firstSessionStartPaused = new SessionState(SessionType.NEW_SESSION);
        firstSessionStartPaused.paused = true;
        firstSessionStartPaused.toSend = false;
        firstSessionStartPaused.foregroundTimerStarts = false;
        firstSessionStartPaused.foregroundTimerAlreadyStarted = false;

        // check session that is paused
        checkStartInternal(firstSessionStartPaused);

        stopActivity(activityHandler);

        SystemClock.sleep(1000);

        // test end session of disable
        checkEndSession(true,   //pausing
                false,   // updateActivityState
                false); // eventBufferingEnabled

        // disable the SDK
        activityHandler.setEnabled(false);

        // check that it is disabled
        assertUtil.isFalse(activityHandler.isEnabled());

        // writing activity state after disabling
        assertUtil.debug("Wrote Activity state: ec:0 sc:1 ssc:1");

        // check if message the disable of the SDK
        assertUtil.info("Pausing package handler and attribution handler due to SDK being disabled");

        SystemClock.sleep(1000);

        checkHandlerStatus(true);

        // put SDK back online
        activityHandler.setOfflineMode(false);

        assertUtil.info("Package and attribution handler remain paused due to SDK being disabled");

        SystemClock.sleep(1000);

        // test the update status, still paused
        checkHandlerStatus(null);

        // try to do activities while SDK disabled
        activityHandler.onResume();
        activityHandler.trackEvent(new AdjustEvent("event1"));

        SystemClock.sleep(3000);

        // check that timer was not executed
        checkForegroundTimerFired(false);

        // check that it did not wrote activity state from new session or subsession
        assertUtil.notInDebug("Wrote Activity state");

        // check that it did not add any package
        assertUtil.notInTest("PackageHandler addPackage");

        // end the session
        stopActivity(activityHandler);

        SystemClock.sleep(1000);

        checkEndSession(false);

        // enable the SDK again
        activityHandler.setEnabled(true);

        // check that is enabled
        assertUtil.isTrue(activityHandler.isEnabled());

        assertUtil.debug("Wrote Activity state");

        assertUtil.info("Resuming package handler and attribution handler due to SDK being enabled");

        SystemClock.sleep(1000);

        // it is still paused because it's on the background
        checkHandlerStatus(true);

        startActivity(activityHandler);

        SystemClock.sleep(1000);

        SessionState secondSessionState = new SessionState(SessionType.NEW_SESSION);
        secondSessionState.sessionCount = 2;
        // test that is not paused anymore
        checkStartInternal(secondSessionState);
    }

    public void testSendReferrer() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testSendReferrer");

        // create the config to start the session
        AdjustConfig config = getConfig();

        // start activity handler with config
        ActivityHandler activityHandler = startAndCheckFirstSession(config);

        long now = System.currentTimeMillis();

        String reftag = "adjust_reftag=referrerValue";
        String extraParams = "adjust_foo=bar&other=stuff&adjust_key=value";
        String mixed = "adjust_foo=bar&other=stuff&adjust_reftag=referrerValue";
        String empty = "";
        String nullString = null;
        String single = "adjust_foo";
        String prefix = "adjust_=bar";
        String incomplete = "adjust_foo=";

        activityHandler.sendReferrer(reftag, now);
        SystemClock.sleep(1000);
        activityHandler.sendReferrer(extraParams, now);
        SystemClock.sleep(1000);
        activityHandler.sendReferrer(mixed, now);
        SystemClock.sleep(1000);
        activityHandler.sendReferrer(empty, now);
        SystemClock.sleep(1000);
        activityHandler.sendReferrer(nullString, now);
        SystemClock.sleep(1000);
        activityHandler.sendReferrer(single, now);
        SystemClock.sleep(1000);
        activityHandler.sendReferrer(prefix, now);
        SystemClock.sleep(1000);
        activityHandler.sendReferrer(incomplete, now);
        SystemClock.sleep(1000);

        // three click packages: reftag, extraParams, mixed, empty, single, prefix and incomplete
        for (int i = 6; i > 0; i--) {
            assertUtil.test("SdkClickHandler sendSdkClick");
        }

        // check that it did not send any other click package
        assertUtil.notInTest("SdkClickHandler sendSdkClick");

        // 6 click
        assertEquals(6, mockSdkClickHandler.queue.size());

        ActivityPackage reftagClickPackage = mockSdkClickHandler.queue.get(0);

        TestActivityPackage reftagClickPackageTest = new TestActivityPackage(reftagClickPackage);

        reftagClickPackageTest.reftag = "referrerValue";
        reftagClickPackageTest.referrer = reftag;

        reftagClickPackageTest.testClickPackage("reftag");

        // get the click package
        ActivityPackage extraParamsClickPackage = mockSdkClickHandler.queue.get(1);

        // create activity package test
        TestActivityPackage testExtraParamsClickPackage = new TestActivityPackage(extraParamsClickPackage);

        // other deep link parameters
        testExtraParamsClickPackage.otherParameters = "{\"key\":\"value\",\"foo\":\"bar\"}";

        testExtraParamsClickPackage.referrer = extraParams;

        // test the second deeplink
        testExtraParamsClickPackage.testClickPackage("reftag");

        // get the click package
        ActivityPackage mixedClickPackage = mockSdkClickHandler.queue.get(2);

        // create activity package test
        TestActivityPackage testMixedClickPackage = new TestActivityPackage(mixedClickPackage);

        testMixedClickPackage.reftag = "referrerValue";
        testMixedClickPackage.referrer = mixed;

        // other deep link parameters
        testMixedClickPackage.otherParameters = "{\"foo\":\"bar\"}";

        // test the third deeplink
        testMixedClickPackage.testClickPackage("reftag");

        // get the click package
        ActivityPackage singleClickPackage = mockSdkClickHandler.queue.get(3);

        // create activity package test
        TestActivityPackage testSingleClickPackage = new TestActivityPackage(singleClickPackage);

        testSingleClickPackage.referrer = single;

        testSingleClickPackage.testClickPackage("reftag");

        // get the click package
        ActivityPackage prefixClickPackage = mockSdkClickHandler.queue.get(4);

        // create activity package test
        TestActivityPackage testPrefixClickPackage = new TestActivityPackage(prefixClickPackage);

        testPrefixClickPackage.referrer = prefix;

        testPrefixClickPackage.testClickPackage("reftag");

        // get the click package
        ActivityPackage incompleteClickPackage = mockSdkClickHandler.queue.get(5);

        // create activity package test
        TestActivityPackage testIncompleteClickPackage = new TestActivityPackage(incompleteClickPackage);

        testIncompleteClickPackage.referrer = incomplete;

        testIncompleteClickPackage.testClickPackage("reftag");
    }

    public void testGetAttribution() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testGetAttribution");

        //AdjustFactory.setTimerStart(500);
        AdjustFactory.setSessionInterval(4000);

        /// if (activityState.subsessionCount > 1) {
        ///     if (attribution == null || activityState.askingAttribution) {
        ///         getAttributionHandler().getAttribution();
        ///     }
        /// }

        // create the config to start the session
        AdjustConfig config = getConfig();

        config.setOnAttributionChangedListener(new OnAttributionChangedListener() {
            @Override
            public void onAttributionChanged(AdjustAttribution attribution) {
                mockLogger.test("onAttributionChanged " + attribution);
            }
        });

        // start activity handler with config
        ActivityHandler activityHandler = getFirstActivityHandler(config);

        SystemClock.sleep(2000);

        // test init values
        checkInitTests();

        startActivity(activityHandler);

        SystemClock.sleep(2000);

        // subsession count is 1
        // attribution is null,
        // askingAttribution is false by default,
        // -> Not called

        // test first session start
        SessionState newSessionState = new SessionState(SessionType.NEW_SESSION);
        newSessionState.getAttributionIsCalled = false;
        checkStartInternal(newSessionState);

        // subsession count increased to 2
        // attribution is still null,
        // askingAttribution is still false,
        // -> Called

        // trigger a new sub session
        activityHandler.onResume();
        SystemClock.sleep(2000);

        checkSubSession(1, 2, true);

        // subsession count increased to 3
        // attribution is still null,
        // askingAttribution is set to true,
        // -> Called

        // set asking attribution
        activityHandler.setAskingAttribution(true);
        assertUtil.debug("Wrote Activity state: ec:0 sc:1 ssc:2");

        // trigger a new session
        activityHandler.onResume();
        SystemClock.sleep(2000);

        checkSubSession(1, 3, true);

        // subsession is reset to 1 with new session
        // attribution is still null,
        // askingAttribution is set to true,
        // -> Not called

        SystemClock.sleep(3000); // 5 seconds = 2 + 3
        activityHandler.onResume();
        SystemClock.sleep(2000);

        checkFurtherSessions(2, false);

        // subsession count increased to 2
        // attribution is set,
        // askingAttribution is set to true,
        // -> Called

        JSONObject jsonAttribution = null;

        try {
            jsonAttribution = new JSONObject("{ " +
                    "\"tracker_token\" : \"ttValue\" , " +
                    "\"tracker_name\"  : \"tnValue\" , " +
                    "\"network\"       : \"nValue\" , " +
                    "\"campaign\"      : \"cpValue\" , " +
                    "\"adgroup\"       : \"aValue\" , " +
                    "\"creative\"      : \"ctValue\" , " +
                    "\"click_label\"   : \"clValue\" }");
        } catch (JSONException e) {
            fail(e.getMessage());
        }
        AdjustAttribution attribution = AdjustAttribution.fromJson(jsonAttribution);

        // update the attribution
        activityHandler.updateAttribution(attribution);

        // attribution was updated
        assertUtil.debug("Wrote Attribution: tt:ttValue tn:tnValue net:nValue cam:cpValue adg:aValue cre:ctValue cl:clValue");

        // trigger a new sub session
        activityHandler.onResume();
        SystemClock.sleep(2000);

        checkSubSession(2, 2, true);
        // subsession count is reset to 1
        // attribution is set,
        // askingAttribution is set to true,
        // -> Not called

        SystemClock.sleep(3000); // 5 seconds = 2 + 3
        activityHandler.onResume();
        SystemClock.sleep(2000);

        checkFurtherSessions(3, false);
        // subsession increased to 2
        // attribution is set,
        // askingAttribution is set to false
        // -> Not called

        activityHandler.setAskingAttribution(false);
        assertUtil.debug("Wrote Activity state: ec:0 sc:3 ssc:1");

        // trigger a new sub session
        activityHandler.onResume();
        SystemClock.sleep(2000);

        checkSubSession(3, 2, false);

        // subsession is reset to 1
        // attribution is set,
        // askingAttribution is set to false
        // -> Not called

        SystemClock.sleep(3000); // 5 seconds = 2 + 3
        activityHandler.onResume();
        SystemClock.sleep(2000);

        checkFurtherSessions(4, false);
    }

    public void testForegroundTimer() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testForegroundTimer");

        AdjustFactory.setTimerInterval(4000);
        AdjustFactory.setTimerStart(4000);

        // create the config to start the session
        AdjustConfig config = getConfig();

        // start activity handler with config
        ActivityHandler activityHandler = startAndCheckFirstSession(config);

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

    public void testSendBackground() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testSendBackground");

        AdjustFactory.setTimerInterval(4000);

        // create the config to start the session
        AdjustConfig config = getConfig();

        // enable send in the background
        config.setSendInBackground(true);

        // create activity handler without starting
        ActivityHandler activityHandler = getActivityHandler(config,
                                            true,           // startEnabled
                                            null,           // readActivityState
                                            null,           // readAttribution
                                            false,          // isProductionEnvironment
                                            LogLevel.INFO); // logLevel

        SystemClock.sleep(2000);

        // handlers start sending
        checkInitTests(
                false,   // eventBuffering
                null,   // defaultTracker
                true);  // startsSending

        startActivity(activityHandler);

        SystemClock.sleep(2000);

        // test session
        checkFirstSession();

        // end subsession
        // background timer starts
        stopActivity(activityHandler);

        SystemClock.sleep(1000);

        // session end does not pause the handlers
        checkEndSession(false,   //pausing
                true,   // updateActivityState
                false,  // eventBufferingEnabled
                true,   // checkOnPause,
                false,  // forgroundAlreadySuspended
                true);  // backgroundTimerStarts

        // end subsession again
        // to test if background timer starts again
        stopActivity(activityHandler);

        SystemClock.sleep(1000);

        // session end does not pause the handlers
        checkEndSession(false,   //pausing
                true,   // updateActivityState
                false,  // eventBufferingEnabled
                true,   // checkOnPause,
                true,   // forgroundAlreadySuspended
                false); // backgroundTimerStarts

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

        // check that event package was added
        assertUtil.test("PackageHandler addPackage");

        // check that event was sent to package handler
        assertUtil.test("PackageHandler sendFirstPackage");

        // and not buffered
        assertUtil.notInInfo("Buffered event");

        // does fire background timer
        assertUtil.verbose("Background timer starting. Launching in 4.0 seconds");

        // after tracking the event it should write the activity state
        assertUtil.debug("Wrote Activity state");

        // disable and enable the sdk while in the background
        activityHandler.setEnabled(false);

        // check that it is disabled
        assertUtil.isFalse(activityHandler.isEnabled());

        // check if message the disable of the SDK
        assertUtil.info("Pausing package handler and attribution handler due to SDK being disabled");

        SystemClock.sleep(1000);

        // handlers being paused because of the disable
        checkHandlerStatus(true);

        activityHandler.setEnabled(true);

        // check that it is enabled
        assertUtil.isTrue(activityHandler.isEnabled());

        // check if message the enable of the SDK
        assertUtil.info("Resuming package handler and attribution handler due to SDK being enabled");

        SystemClock.sleep(1000);

        // handlers being resumed because of the enable
        // even in the background because of the sendInBackground option
        checkHandlerStatus(false);

        // set offline and online the sdk while in the background
        activityHandler.setOfflineMode(true);

        InternalState internalState = activityHandler.getInternalState();

        // check that it is offline
        assertUtil.isTrue(internalState.isOffline());

        // check if message the offline of the SDK
        assertUtil.info("Pausing package and attribution handler to put SDK offline mode");

        SystemClock.sleep(1000);

        // handlers being paused because of the offline
        checkHandlerStatus(true);

        activityHandler.setOfflineMode(false);

        // check that it is online
        assertUtil.isTrue(internalState.isOnline());

        // check if message the online of the SDK
        assertUtil.info("Resuming package handler and attribution handler to put SDK in online mode");

        SystemClock.sleep(1000);

        // handlers being resumed because of the online
        // even in the background because of the sendInBackground option
        checkHandlerStatus(false);
    }

    public void checkFinishTasks(AdjustConfig config,
                                 DelegatesPresent delegatesPresent)
    {
        ActivityHandler activityHandler = startAndCheckFirstSession(config);

        // test first session package
        ActivityPackage firstSessionPackage = mockPackageHandler.queue.get(0);

        // create activity package test
        TestActivityPackage testActivityPackage = new TestActivityPackage(firstSessionPackage);

        testActivityPackage.needsResponseDetails =
                delegatesPresent.attributionDelegatePresent ||
                        delegatesPresent.eventSuccessDelegatePresent ||
                        delegatesPresent.eventFailureDelegatePresent ||
                        delegatesPresent.sessionSuccessDelegatePresent ||
                        delegatesPresent.sessionFailureDelegatePresent;

        // set first session
        testActivityPackage.testSessionPackage(1);

        // simulate a successful session
        SessionResponseData successSessionResponseData = (SessionResponseData) ResponseData.buildResponseData(firstSessionPackage);
        successSessionResponseData.success = true;

        activityHandler.finishedTrackingActivity(successSessionResponseData);
        SystemClock.sleep(1000);

        // attribution handler should always receive the session response
        assertUtil.test("AttributionHandler checkSessionResponse");
        // the first session does not trigger the event response delegate

        assertUtil.notInDebug("Launching success event tracking listener");
        assertUtil.notInDebug("Launching failed event tracking listener");

        activityHandler.launchSessionResponseTasks(successSessionResponseData);
        SystemClock.sleep(1000);

        // if present, the first session triggers the success session delegate
        if (delegatesPresent.sessionSuccessDelegatePresent) {
            assertUtil.debug("Launching success session tracking listener");
        } else {
            assertUtil.notInDebug("Launching success session tracking delegate");
        }
        // it doesn't trigger the failure session delegate
        assertUtil.notInDebug("Launching failed session tracking listener");

        // simulate a failure session
        SessionResponseData failureSessionResponseData = (SessionResponseData)ResponseData.buildResponseData(firstSessionPackage);
        failureSessionResponseData.success = false;

        activityHandler.launchSessionResponseTasks(failureSessionResponseData);
        SystemClock.sleep(1000);

        // it doesn't trigger the success session delegate
        assertUtil.notInDebug("Launching success session tracking listener");

        // if present, the first session triggers the failure session delegate
        if (delegatesPresent.sessionFailureDelegatePresent) {
            assertUtil.debug("Launching failed session tracking listener");
        } else {
            assertUtil.notInDebug("Launching failed session tracking listener");
        }

        // test success event response data
        activityHandler.trackEvent(new AdjustEvent("abc123"));
        SystemClock.sleep(1000);

        ActivityPackage eventPackage = mockPackageHandler.queue.get(1);
        EventResponseData eventSuccessResponseData = (EventResponseData)ResponseData.buildResponseData(eventPackage);
        eventSuccessResponseData.success = true;

        activityHandler.finishedTrackingActivity(eventSuccessResponseData);
        SystemClock.sleep(1000);

        // attribution handler should never receive the event response
        assertUtil.notInTest("AttributionHandler checkSessionResponse");

        // if present, the success event triggers the success event delegate
        if (delegatesPresent.eventSuccessDelegatePresent) {
            assertUtil.debug("Launching success event tracking listener");
        } else {
            assertUtil.notInDebug("Launching success event tracking listener");
        }
        // it doesn't trigger the failure event delegate
        assertUtil.notInDebug("Launching failed event tracking listener");

        // test failure event response data
        EventResponseData eventFailureResponseData = (EventResponseData)ResponseData.buildResponseData(eventPackage);
        eventFailureResponseData.success = false;

        activityHandler.finishedTrackingActivity(eventFailureResponseData);
        SystemClock.sleep(1000);

        // attribution handler should never receive the event response
        assertUtil.notInTest("AttributionHandler checkSessionResponse");

        // if present, the failure event triggers the failure event delegate
        if (delegatesPresent.eventFailureDelegatePresent) {
            assertUtil.debug("Launching failed event tracking listener");
        } else {
            assertUtil.notInDebug("Launching failed event tracking listener");
        }
        // it doesn't trigger the success event delegate
        assertUtil.notInDebug("Launching success event tracking listener");

        // test click
        Uri attributions = Uri.parse("AdjustTests://example.com/path/inApp?adjust_tracker=trackerValue&other=stuff&adjust_campaign=campaignValue&adjust_adgroup=adgroupValue&adjust_creative=creativeValue");
        long now = System.currentTimeMillis();

        activityHandler.readOpenUrl(attributions, now);
        SystemClock.sleep(1000);

        assertUtil.test("PackageHandler addPackage");
        assertUtil.test("PackageHandler sendFirstPackage");

        // test sdk_click response data
        ActivityPackage sdkClickPackage = mockSdkClickHandler.queue.get(0);
        ClickResponseData clickResponseData = (ClickResponseData)ResponseData.buildResponseData(sdkClickPackage);

        activityHandler.finishedTrackingActivity(clickResponseData);
        SystemClock.sleep(1000);

        // attribution handler should never receive the click response
        assertUtil.notInTest("AttributionHandler checkSessionResponse");
        // it doesn't trigger the any event delegate
        assertUtil.notInDebug("Launching success event tracking listener");
        assertUtil.notInDebug("Launching failed event tracking listener");
    }

    private class DelegatesPresent {
        boolean attributionDelegatePresent;
        boolean eventSuccessDelegatePresent;
        boolean eventFailureDelegatePresent;
        boolean sessionSuccessDelegatePresent;
        boolean sessionFailureDelegatePresent;
    }

    private class SessionState {
        boolean toSend = true;
        boolean paused = false;
        int sessionCount = 1;
        int subsessionCount = 1;
        SessionType sessionType = null;
        int eventCount = 0;
        Boolean getAttributionIsCalled = null;
        Boolean timerAlreadyStarted = false;
        boolean eventBufferingIsEnabled = false;
        boolean foregroundTimerStarts = true;
        boolean foregroundTimerAlreadyStarted = false;

        SessionState(SessionType sessionType) {
            switch (sessionType) {
                case NEW_SUBSESSION:
                case NONSESSION:
                    timerAlreadyStarted = true;
                    break;
            }
            this.sessionType = sessionType;
        }
    }

    private enum SessionType {
        NEW_SESSION,
        NEW_SUBSESSION,
        TIME_TRAVEL,
        NONSESSION;
    }

    private void checkFirstSession() {
        SessionState newSessionState = new SessionState(SessionType.NEW_SESSION);
        checkStartInternal(newSessionState);
    }

    private void checkSubSession(int sessionCount, int subsessionCount, boolean getAttributionIsCalled) {
        SessionState subSessionState = new SessionState(SessionType.NEW_SUBSESSION);
        subSessionState.sessionCount = sessionCount;
        subSessionState.subsessionCount = subsessionCount;
        subSessionState.getAttributionIsCalled = getAttributionIsCalled;
        subSessionState.foregroundTimerAlreadyStarted = true;
        checkStartInternal(subSessionState);
    }

    private void checkFurtherSessions(int sessionCount, boolean getAttributionIsCalled) {
        SessionState subSessionState = new SessionState(SessionType.NEW_SESSION);
        subSessionState.sessionCount = sessionCount;
        subSessionState.timerAlreadyStarted = true;
        subSessionState.getAttributionIsCalled = getAttributionIsCalled;
        subSessionState.foregroundTimerAlreadyStarted = true;
        checkStartInternal(subSessionState);
    }

    private void checkStartDisable() {
        assertUtil.notInTest("AttributionHandler resumeSending");
        assertUtil.notInTest("PackageHandler resumeSending");
        assertUtil.notInTest("SdkClickHandler resumeSending");
        assertUtil.notInTest("AttributionHandler pauseSending");
        assertUtil.notInTest("PackageHandler pauseSending");
        assertUtil.notInTest("SdkClickHandler pauseSending");
        assertUtil.notInTest("PackageHandler addPackage");
        assertUtil.notInTest("PackageHandler sendFirstPackage");
        assertUtil.notInVerbose("Started subsession");
        assertUtil.notInVerbose("Time span since last activity too short for a new subsession");
        assertUtil.notInError("Time travel!");
        assertUtil.notInDebug("Wrote Activity state: ");
        assertUtil.notInTest("AttributionHandler getAttribution");
        checkForegroundTimerFired(false);
    }

    private void checkStartInternal(SessionState sessionState)
    {
        // check onResume
        checkOnResume(sessionState);

        // update Handlers Status
        checkHandlerStatus(!sessionState.toSend, sessionState.eventBufferingIsEnabled);

        // process Session
        switch (sessionState.sessionType) {
            case NEW_SESSION:
                // if the package was build, it was sent to the Package Handler
                assertUtil.test("PackageHandler addPackage");

                // after adding, the activity handler ping the Package handler to send the package
                assertUtil.test("PackageHandler sendFirstPackage");
                break;
            case NEW_SUBSESSION:
                // test the subsession message
                assertUtil.verbose("Started subsession " + sessionState.subsessionCount + " of session " + sessionState.sessionCount);
                break;
            case NONSESSION:
                // stopped for a short time, not enough for a new sub subsession
                assertUtil.verbose("Time span since last activity too short for a new subsession");
                break;
            case TIME_TRAVEL:
                assertUtil.error("Time travel!");
                break;
        }

        // after processing the session, writes the activity state
        if (sessionState.sessionType != SessionType.NONSESSION) {
            assertUtil.debug("Wrote Activity state: " +
                    "ec:" + sessionState.eventCount + " sc:" + sessionState.sessionCount + " ssc:" + sessionState.subsessionCount);
        }
        // check Attribution State
        if (sessionState.getAttributionIsCalled != null) {
            if (sessionState.getAttributionIsCalled) {
                assertUtil.test("AttributionHandler getAttribution");
            } else {
                assertUtil.notInTest("AttributionHandler getAttribution");
            }
        }

        /*
        // start Foreground Timer
        if (sessionState.paused) {
            // foreground timer doesn't start when it's paused
            assertUtil.notInDebug("Foreground timer started");
            checkForegroundTimerFired(false);
        } else if (sessionState.timerAlreadyStarted != null) {
            checkForegroundTimerFired(!sessionState.timerAlreadyStarted);
        }
        */
    }
/*
    private void checkInitAndFirstSession() {
        SessionState newSessionState = new SessionState(SessionType.NEW_SESSION);
        checkInitAndFirstSession(newSessionState);
    }

    private void checkInitAndFirstSession(SessionState sessionState) {
        checkInitTests(sessionState.toSend);
        checkStartInternal(sessionState);
    }
*/
    private void checkForegroundTimerFired(boolean timerFired) {
        // timer fired
        if (timerFired) {
            assertUtil.verbose("Foreground timer fired");
        } else {
            assertUtil.notInVerbose("Foreground timer fired");
        }
    }

    private void checkInitTests() {
        checkInitTests(false);
    }

    private void checkInitTests(boolean startsSending) {
        checkInitTests(false, null, startsSending);
    }


    private void checkInitTests(boolean eventBuffering,
                                String defaultTracker,
                                boolean startsSending)
    {
        // check event buffering
        if (eventBuffering) {
            assertUtil.info("Event buffering is enabled");
        } else {
            assertUtil.notInInfo("Event buffering is enabled");
        }

        // check Google play is not set
        assertUtil.info("Google Play Services Advertising ID read correctly at start time");

        // check default tracker
        if (defaultTracker != null) {
            assertUtil.info("Default tracker: '%s'", defaultTracker);
        }

        if (startsSending) {
            assertUtil.test("PackageHandler init, startsSending: true");
            assertUtil.test("AttributionHandler init, startsSending: true");
            assertUtil.test("SdkClickHandler init, startsSending: true");
        } else {
            assertUtil.test("PackageHandler init, startsSending: false");
            assertUtil.test("AttributionHandler init, startsSending: false");
            assertUtil.test("SdkClickHandler init, startsSending: false");
        }
    }

    private void checkEndSession() {
        checkEndSession(true);
    }

    private void checkEndSession(boolean updateActivityState) {
        checkEndSession(true, updateActivityState, false, false, false, false);
    }

    private void checkEndSession(boolean pausing,
                                 boolean updateActivityState,
                                 boolean eventBufferingEnabled) {
        checkEndSession(pausing, updateActivityState, eventBufferingEnabled, false, false, false);
    }

    private void checkEndSession(boolean pausing,
                                 boolean updateActivityState,
                                 boolean eventBufferingEnabled,
                                 boolean checkOnPause,
                                 boolean forgroundAlreadySuspended,
                                 boolean backgroundTimerStarts)
    {
        if (checkOnPause) {
            checkOnPause(forgroundAlreadySuspended, backgroundTimerStarts);
        }

        if (pausing) {
            checkHandlerStatus(true, eventBufferingEnabled);
        }

        if (updateActivityState) {
            assertUtil.debug("Wrote Activity state: ");
        } else {
            assertUtil.notInDebug("Wrote Activity state: ");
        }
    }


    private AdjustConfig getConfig() {
        return getConfig(null);
    }

    private AdjustConfig getConfig(LogLevel logLevel) {
        return getConfig(logLevel, "sandbox", "123456789012", context);
    }

    private AdjustConfig getConfig(LogLevel logLevel,
                                   String environment,
                                   String appToken,
                                   Context context)
    {
        AdjustConfig adjustConfig = new AdjustConfig(context, appToken, environment);

        if (adjustConfig != null) {
            if (environment == "sandbox") {
                assertUtil.Assert("SANDBOX: Adjust is running in Sandbox mode. Use this setting for testing. Don't forget to set the environment to `production` before publishing!");
            } else if (environment == "production") {
                assertUtil.Assert("PRODUCTION: Adjust is running in Production mode. Use this setting only for the build that you want to publish. Set the environment to `sandbox` if you want to test your app!");
            } else {
                fail();
            }

            if (logLevel != null) {
                adjustConfig.setLogLevel(logLevel);
            }
        }

        return adjustConfig;
    }

    private ActivityHandler getFirstActivityHandler(AdjustConfig config) {
        return getActivityHandler(config, true, null, null, false, LogLevel.INFO);
    }

    private ActivityHandler getFirstActivityHandler(AdjustConfig config,
                                                    LogLevel logLevel) {
        return getActivityHandler(config, true, null, null, false, logLevel);
    }

    private ActivityHandler getActivityHandler(AdjustConfig config,
                                               boolean startEnabled,
                                               String readActivityState,
                                               String readAttribution,
                                               boolean isProductionEnvironment,
                                               LogLevel logLevel) {
        ActivityHandler activityHandler = ActivityHandler.getInstance(config);

        if (activityHandler != null) {
            // check log level
            if (isProductionEnvironment) {
                assertUtil.test("MockLogger setLogLevel: " + LogLevel.ASSERT);
            } else {
                assertUtil.test("MockLogger setLogLevel: " + logLevel);
            }

            // check if files are read in constructor
            checkReadFiles(readActivityState, readAttribution);

            InternalState internalState = activityHandler.getInternalState();
            // test default values
            assertUtil.isEqual(startEnabled, internalState.isEnabled());
            assertUtil.isTrue(internalState.isOnline());
            assertUtil.isTrue(internalState.isBackground());
        }

        return activityHandler;
    }

    private ActivityHandler startAndCheckFirstSession(AdjustConfig config) {
        return startAndCheckFirstSession(config, LogLevel.INFO);
    }

    private ActivityHandler startAndCheckFirstSession(AdjustConfig config, LogLevel logLevel) {
        // start activity handler with config
        ActivityHandler activityHandler = getFirstActivityHandler(config, logLevel);

        SystemClock.sleep(2000);

        // test init values
        checkInitTests();

        startActivity(activityHandler);

        SystemClock.sleep(2000);

        // test session
        checkFirstSession();

        return activityHandler;
    }

    private void startActivity(ActivityHandler activityHandler) {
        // start activity
        activityHandler.onResume();

        InternalState internalState = activityHandler.getInternalState();

        // comes to the foreground
        assertUtil.isTrue(internalState.isForeground());
    }

    private void checkOnResume(SessionState sessionState) {
        // stops background timer
        assertUtil.verbose("Background timer canceled");

        // start foreground timer
        if (sessionState.foregroundTimerStarts) {
            if (sessionState.foregroundTimerAlreadyStarted) {
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

    private void stopActivity(ActivityHandler activityHandler) {
        // stop activity
        activityHandler.onPause();

        InternalState internalState = activityHandler.getInternalState();

        // goes to the background
        assertUtil.isTrue(internalState.isBackground());

    }

    private void checkOnPause(boolean forgroundAlreadySuspended,
                              boolean backgroundTimerStarts) {
        // stop foreground timer
        if (forgroundAlreadySuspended) {
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

    private void checkReadFiles(String readActivityState, String readAttribution) {
        if (readAttribution == null) {
            //  test that the attribution file did not exist in the first run of the application
            assertUtil.debug("Attribution file not found");
        } else {
            assertUtil.debug("Read Attribution: " + readAttribution);
        }

        if (readActivityState == null) {
            //  test that the activity state file did not exist in the first run of the application
            assertUtil.debug("Activity state file not found");
        } else {
            assertUtil.debug("Read Activity state: " + readActivityState);
        }
    }

    private void checkHandlerStatus(Boolean pausing) {
        checkHandlerStatus(pausing, false);
    }


    private void checkHandlerStatus(Boolean pausing, boolean eventBufferingEnabled) {
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
            assertUtil.test("SdkClickHandler pauseSending");
        } else {
            assertUtil.test("AttributionHandler resumeSending");
            assertUtil.test("PackageHandler resumeSending");
            assertUtil.test("SdkClickHandler resumeSending");
            if (!eventBufferingEnabled) {
                assertUtil.test("PackageHandler sendFirstPackage");
            }
        }
    }
}
