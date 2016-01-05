package com.adjust.sdk.test;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.SystemClock;
import android.test.ActivityInstrumentationTestCase2;
import android.test.mock.MockContext;

import com.adjust.sdk.ActivityHandler;
import com.adjust.sdk.ActivityPackage;
import com.adjust.sdk.AdjustAttribution;
import com.adjust.sdk.AdjustConfig;
import com.adjust.sdk.AdjustEvent;
import com.adjust.sdk.AdjustFactory;
import com.adjust.sdk.Constants;
import com.adjust.sdk.LogLevel;
import com.adjust.sdk.OnAttributionChangedListener;
import com.adjust.sdk.ResponseData;

import org.json.JSONException;
import org.json.JSONObject;


public class TestActivityHandler extends ActivityInstrumentationTestCase2<UnitTestActivity> {
    protected MockLogger mockLogger;
    protected MockPackageHandler mockPackageHandler;
    protected MockAttributionHandler mockAttributionHandler;
    protected UnitTestActivity activity;
    protected Context context;
    protected AssertUtil assertUtil;

    public TestActivityHandler() {
        super(UnitTestActivity.class);
    }

    public TestActivityHandler(Class<UnitTestActivity> mainActivity) {
        super(mainActivity);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mockLogger = new MockLogger();
        mockPackageHandler = new MockPackageHandler(mockLogger);
        mockAttributionHandler = new MockAttributionHandler(mockLogger);
        assertUtil = new AssertUtil(mockLogger);

        AdjustFactory.setLogger(mockLogger);
        AdjustFactory.setPackageHandler(mockPackageHandler);
        AdjustFactory.setAttributionHandler(mockAttributionHandler);

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
        AdjustConfig config = new AdjustConfig(context, "123456789012", AdjustConfig.ENVIRONMENT_SANDBOX);

        // start activity handler with config
        ActivityHandler activityHandler = getActivityHandler(config);

        SystemClock.sleep(3000);

        // test init values
        initTests(AdjustConfig.ENVIRONMENT_SANDBOX, "INFO", false);

        // test first session start
        checkFirstSession();

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
        AdjustConfig config = new AdjustConfig(context, "123456789012", AdjustConfig.ENVIRONMENT_SANDBOX);

        // buffer events
        config.setEventBufferingEnabled(true);

        // set verbose log level
        config.setLogLevel(LogLevel.VERBOSE);

        // start activity handler with config
        ActivityHandler activityHandler = getActivityHandler(config);

        SystemClock.sleep(3000);

        // test init values
        initTests(AdjustConfig.ENVIRONMENT_SANDBOX, "VERBOSE", true);

        // test first session start
        checkFirstSession();

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

        // after tracking the event it should write the activity state
        assertUtil.debug("Wrote Activity state");

        // test second event
        // check that event package was added
        assertUtil.test("PackageHandler addPackage");

        // check that event was buffered
        assertUtil.info("Buffered event (0.00000 USD, 'event2')");

        // and not sent to package handler
        assertUtil.notInTest("PackageHandler sendFirstPackage");

        // after tracking the event it should write the activity state
        assertUtil.debug("Wrote Activity state");

        // test third event
        // check that event package was added
        assertUtil.test("PackageHandler addPackage");

        // check that event was buffered
        assertUtil.info("Buffered event 'event3'");

        // and not sent to package handler
        assertUtil.notInTest("PackageHandler sendFirstPackage");

        // after tracking the event it should write the activity state
        assertUtil.debug("Wrote Activity state");

        // check the number of activity packages
        // 1 session + 3 events
        assertEquals(4, mockPackageHandler.queue.size());

        ActivityPackage firstSessionPackage = mockPackageHandler.queue.get(0);

        // create activity package test
        TestActivityPackage testFirstSessionPackage = new TestActivityPackage(firstSessionPackage);

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

        // test second event
        testSecondEventPackage.testEventPackage("event2");

        // third event
        ActivityPackage thirdEventPackage = mockPackageHandler.queue.get(3);

        // create event package test
        TestActivityPackage testThirdEventPackage = new TestActivityPackage(thirdEventPackage);

        // set event test parameters
        testThirdEventPackage.eventCount = "3";
        testThirdEventPackage.suffix = "'event3'";

        // test third event
        testThirdEventPackage.testEventPackage("event3");
    }

    public void testEventsNotBuffered() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testEventsNotBuffered");

        // create the config to start the session
        AdjustConfig config = new AdjustConfig(context, "123456789012", AdjustConfig.ENVIRONMENT_SANDBOX);

        // set log level
        config.setLogLevel(LogLevel.DEBUG);

        // start activity handler with config
        ActivityHandler activityHandler = getActivityHandler(config);

        SystemClock.sleep(3000);

        // test init values
        initTests(AdjustConfig.ENVIRONMENT_SANDBOX, "DEBUG", false);

        // test first session start
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
        AdjustConfig config = new AdjustConfig(context, "123456789012", AdjustConfig.ENVIRONMENT_SANDBOX);

        // set the log level
        config.setLogLevel(LogLevel.WARN);

        // create handler and start the first session
        ActivityHandler activityHandler = getActivityHandler(config);

        SystemClock.sleep(3000);

        // test init values
        initTests(AdjustConfig.ENVIRONMENT_SANDBOX, "WARN", false);

        // test first session start
        checkFirstSession();

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
        AdjustConfig config = new AdjustConfig(context, "123456789012", AdjustConfig.ENVIRONMENT_SANDBOX);

        // set verbose log level
        config.setLogLevel(LogLevel.INFO);

        // start activity handler with config
        ActivityHandler activityHandler = getActivityHandler(config);

        SystemClock.sleep(3000);

        // test init values
        initTests(AdjustConfig.ENVIRONMENT_SANDBOX, "INFO", false);

        // test first session start
        checkFirstSession();

        // trigger a new sub session session
        activityHandler.trackSubsessionStart();

        // and end it
        activityHandler.trackSubsessionEnd();

        SystemClock.sleep(5000);

        // test the new sub session
        checkSubsession(1, 2, true);

        // test the end of the subsession
        checkEndSession();

        // trigger a new session
        activityHandler.trackSubsessionStart();

        SystemClock.sleep(1000);

        // new session
        checkNewSession(false, 2, 0, true);

        // end the session
        activityHandler.trackSubsessionEnd();

        SystemClock.sleep(1000);

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
        AdjustConfig config = new AdjustConfig(context, "123456789012", AdjustConfig.ENVIRONMENT_SANDBOX);

        // set log level
        config.setLogLevel(LogLevel.ERROR);

        // start activity handler with config
        ActivityHandler activityHandler = ActivityHandler.getInstance(config);

        // check that is true by default
        assertUtil.isTrue(activityHandler.isEnabled());

        // disable sdk
        activityHandler.setEnabled(false);

        // check that it is disabled
        assertUtil.isFalse(activityHandler.isEnabled());

        // not writing activity state because it did not had time to start
        assertUtil.notInDebug("Wrote Activity state");

        // check if message the disable of the SDK
        assertUtil.info("Pausing package handler and attribution handler to disable the SDK");

        // it's necessary to sleep the activity for a while after each handler call
        // to let the internal queue act
        SystemClock.sleep(2000);

        // test init values
        initTests(AdjustConfig.ENVIRONMENT_SANDBOX, "ERROR", false);

        // test first session start without attribution handler
        checkFirstSession(true);

        // test end session of disable
        checkEndSession();

        // try to do activities while SDK disabled
        activityHandler.trackSubsessionStart();
        activityHandler.trackEvent(new AdjustEvent("event1"));

        SystemClock.sleep(3000);

        // check that timer was not executed
        checkTimerIsFired(false);

        // check that it did not resume
        assertUtil.notInTest("PackageHandler resumeSending");

        // check that it did not wrote activity state from new session or subsession
        assertUtil.notInDebug("Wrote Activity state");

        // check that it did not add any event package
        assertUtil.notInTest("PackageHandler addPackage");

        // only the first session package should be sent
        assertEquals(1, mockPackageHandler.queue.size());

        // put in offline mode
        activityHandler.setOfflineMode(true);

        // pausing due to offline mode
        assertUtil.info("Pausing package and attribution handler to put in offline mode");

        // wait to update status
        SystemClock.sleep(6000);

        // test end session of offline
        checkEndSession(false);

        // re-enable the SDK
        activityHandler.setEnabled(true);

        // check that it is enabled
        assertUtil.isTrue(activityHandler.isEnabled());

        // check message of SDK still paused
        assertUtil.info("Package and attribution handler remain paused due to the SDK is offline");

        activityHandler.trackSubsessionStart();
        SystemClock.sleep(1000);

        checkNewSession(true, 2, 0);

        // and that the timer was not fired
        checkTimerIsFired(false);

        // track an event
        activityHandler.trackEvent(new AdjustEvent("event1"));

        SystemClock.sleep(1000);

        // check that it did add the event package
        assertUtil.test("PackageHandler addPackage");

        // and send it
        assertUtil.test("PackageHandler sendFirstPackage");

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

        // put in online mode
        activityHandler.setOfflineMode(false);

        // message that is finally resuming
        assertUtil.info("Resuming package handler and attribution handler to put in online mode");

        SystemClock.sleep(6000);

        // check status update
        assertUtil.test("AttributionHandler resumeSending");
        assertUtil.test("PackageHandler resumeSending");

        // track sub session
        activityHandler.trackSubsessionStart();

        SystemClock.sleep(1000);

        // test session not paused
        checkNewSession(false, 3, 1, true);
    }

    public void testOpenUrl() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testOpenUrl");

        // create the config to start the session
        AdjustConfig config = new AdjustConfig(context, "123456789012", AdjustConfig.ENVIRONMENT_SANDBOX);

        // set log level
        config.setLogLevel(LogLevel.ASSERT);

        // start activity handler with config
        ActivityHandler activityHandler = getActivityHandler(config);

        SystemClock.sleep(3000);

        // test init values
        initTests(AdjustConfig.ENVIRONMENT_SANDBOX, "ASSERT", false);

        // test first session start
        checkFirstSession();

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
        for (int i = 3; i > 0; i--) {
            assertUtil.test("PackageHandler addPackage");
        }

        // checking the default values of the first session package
        // 1 session + 3 click
        assertEquals(4, mockPackageHandler.queue.size());

        // get the click package
        ActivityPackage attributionClickPackage = mockPackageHandler.queue.get(1);

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

        // test the first deeplink
        testAttributionClickPackage.testClickPackage("deeplink");

        // get the click package
        ActivityPackage extraParamsClickPackage = mockPackageHandler.queue.get(2);

        // create activity package test
        TestActivityPackage testExtraParamsClickPackage = new TestActivityPackage(extraParamsClickPackage);

        // other deep link parameters
        testExtraParamsClickPackage.deepLinkParameters = "{\"key\":\"value\",\"foo\":\"bar\"}";

        // test the second deeplink
        testExtraParamsClickPackage.testClickPackage("deeplink");

        // get the click package
        ActivityPackage mixedClickPackage = mockPackageHandler.queue.get(3);

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
        testMixedClickPackage.deepLinkParameters = "{\"foo\":\"bar\"}";

        // test the third deeplink
        testMixedClickPackage.testClickPackage("deeplink");
    }

    public void testFinishedTrackingActivity() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testFinishedTrackingActivity");

        // create the config to start the session
        AdjustConfig config = new AdjustConfig(context, "123456789012", AdjustConfig.ENVIRONMENT_PRODUCTION);

        // set verbose log level
        config.setLogLevel(LogLevel.VERBOSE);

        config.setOnAttributionChangedListener(new OnAttributionChangedListener() {
            @Override
            public void onAttributionChanged(AdjustAttribution attribution) {
                mockLogger.test("onAttributionChanged: " + attribution);
            }
        });

        // start activity handler with config
        ActivityHandler activityHandler = getActivityHandler(config);

        activityHandler.trackSubsessionStart();

        SystemClock.sleep(3000);

        // test init values
        initTests(AdjustConfig.ENVIRONMENT_PRODUCTION, "ASSERT", false);

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
        ResponseData wrongDeeplinkResponseData = new ResponseData(null);
        try {
            wrongDeeplinkResponseData.jsonResponse = new JSONObject("{ " +
                    "\"deeplink\" :  \"wrongDeeplink://\" }");
        } catch (JSONException e) {
            fail(e.getMessage());
        }

        //activityHandler.finishedTrackingActivity(wrongDeeplinkResponseData);
        SystemClock.sleep(1000);

        // check that it was unable to open the url
        assertUtil.error("Unable to open deep link (wrongDeeplink://)");

        // and it check the attribution
        assertUtil.test("AttributionHandler checkAttribution");
        // TODO add test that opens url

        // checking the default values of the first session package
        // should only have one package
        assertEquals(1, mockPackageHandler.queue.size());

        ActivityPackage activityPackage = mockPackageHandler.queue.get(0);

        // create activity package test
        TestActivityPackage testActivityPackage = new TestActivityPackage(activityPackage);

        testActivityPackage.needsAttributionData = true;
        testActivityPackage.environment = AdjustConfig.ENVIRONMENT_PRODUCTION;

        // set first session
        testActivityPackage.testSessionPackage(1);
    }

    public void testUpdateAttribution() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testUpdateAttribution");

        // create the config to start the session
        AdjustConfig config = new AdjustConfig(context, "123456789012", AdjustConfig.ENVIRONMENT_SANDBOX);

        // start activity handler with config
        ActivityHandler firstActivityHandler = getActivityHandler(config);

        SystemClock.sleep(3000);

        // test init values
        initTests();

        // test first session start
        checkFirstSession();

        JSONObject nullJsonObject = null;
        AdjustAttribution nullAttribution = AdjustAttribution.fromJson(nullJsonObject);

        // check if Attribution wasn't built
        assertUtil.isNull(nullAttribution);

        // check that it does not update a null attribution
        //assertUtil.isFalse(firstActivityHandler.tryUpdateAttribution(nullAttribution));

        // create an empty attribution
        JSONObject emptyJsonResponse = null;
        try {
            emptyJsonResponse = new JSONObject("{ }");
        } catch (JSONException e) {
            fail(e.getMessage());
        }
        AdjustAttribution emptyAttribution = AdjustAttribution.fromJson(emptyJsonResponse);

        // check that updates attribution
        //assertUtil.isTrue(firstActivityHandler.tryUpdateAttribution(emptyAttribution));
        assertUtil.debug("Wrote Attribution: tt:null tn:null net:null cam:null adg:null cre:null cl:null");

        emptyAttribution = AdjustAttribution.fromJson(emptyJsonResponse);

        // check that it does not update the attribution
        //assertUtil.isFalse(firstActivityHandler.tryUpdateAttribution(emptyAttribution));
        assertUtil.notInDebug("Wrote Attribution");

        // end session
        firstActivityHandler.trackSubsessionEnd();
        SystemClock.sleep(1000);

        checkEndSession();

        config = new AdjustConfig(context, "123456789012", AdjustConfig.ENVIRONMENT_SANDBOX);

        config.setOnAttributionChangedListener(new OnAttributionChangedListener() {
            @Override
            public void onAttributionChanged(AdjustAttribution attribution) {
                mockLogger.test("onAttributionChanged: " + attribution);
            }
        });

        ActivityHandler restartActivityHandler = getActivityHandler(config);

        SystemClock.sleep(3000);

        // test init values
        initTests(AdjustConfig.ENVIRONMENT_SANDBOX, "INFO", false, "ec:0 sc:1 ssc:1", "tt:null tn:null net:null cam:null adg:null cre:null cl:null");

        checkFirstSessionSubsession(2);

        // check that it does not update the attribution after the restart
        //assertUtil.isFalse(restartActivityHandler.tryUpdateAttribution(emptyAttribution));
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
        //assertUtil.isTrue(restartActivityHandler.tryUpdateAttribution(firstAttribution));
        assertUtil.debug("Wrote Attribution: tt:ttValue tn:tnValue net:nValue cam:cpValue adg:aValue cre:ctValue cl:clValue");

        // check that it launch the saved attribute
        SystemClock.sleep(1000);

        assertUtil.test("onAttributionChanged: tt:ttValue tn:tnValue net:nValue cam:cpValue adg:aValue cre:ctValue cl:clValue");

        // check that it does not update the attribution
        //assertUtil.isFalse(restartActivityHandler.tryUpdateAttribution(firstAttribution));
        assertUtil.notInDebug("Wrote Attribution");

        // end session
        restartActivityHandler.trackSubsessionEnd();
        SystemClock.sleep(1000);

        checkEndSession();

        config = new AdjustConfig(context, "123456789012", AdjustConfig.ENVIRONMENT_SANDBOX);

        config.setOnAttributionChangedListener(new OnAttributionChangedListener() {
            @Override
            public void onAttributionChanged(AdjustAttribution attribution) {
                mockLogger.test("onAttributionChanged: " + attribution);
            }
        });

        ActivityHandler secondRestartActivityHandler = getActivityHandler(config);

        SystemClock.sleep(3000);

        // test init values
        initTests(AdjustConfig.ENVIRONMENT_SANDBOX, "INFO", false, "ec:0 sc:1 ssc:2", "tt:ttValue tn:tnValue net:nValue cam:cpValue adg:aValue cre:ctValue cl:clValue");

        checkFirstSessionSubsession(3);

        // check that it does not update the attribution after the restart
        //assertUtil.isFalse(secondRestartActivityHandler.tryUpdateAttribution(firstAttribution));
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
        //assertUtil.isTrue(secondRestartActivityHandler.tryUpdateAttribution(secondAttribution));
        assertUtil.debug("Wrote Attribution: tt:ttValue2 tn:tnValue2 net:nValue2 cam:cpValue2 adg:aValue2 cre:ctValue2 cl:clValue2");

        // check that it launch the saved attribute
        SystemClock.sleep(1000);

        assertUtil.test("onAttributionChanged: tt:ttValue2 tn:tnValue2 net:nValue2 cam:cpValue2 adg:aValue2 cre:ctValue2 cl:clValue2");

        // check that it does not update the attribution
        //assertUtil.isFalse(secondRestartActivityHandler.tryUpdateAttribution(secondAttribution));
        assertUtil.notInDebug("Wrote Attribution");
    }

    public void testOfflineMode() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testOfflineMode");

        // adjust the session intervals for testing
        AdjustFactory.setSessionInterval(2000);
        AdjustFactory.setSubsessionInterval(500);

        // create the config to start the session
        AdjustConfig config = new AdjustConfig(context, "123456789012", AdjustConfig.ENVIRONMENT_SANDBOX);

        // start activity handler with config
        ActivityHandler activityHandler = getActivityHandler(config);

        // put SDK offline
        activityHandler.setOfflineMode(true);

        SystemClock.sleep(3000);

        // check if message the disable of the SDK
        assertUtil.info("Pausing package and attribution handler to put in offline mode");

        // test init values
        initTests();

        // test first session start
        checkFirstSession(true);

        // test end of session logs
        checkEndSession();

        // disable the SDK
        activityHandler.setEnabled(false);

        // check that it is disabled
        assertUtil.isFalse(activityHandler.isEnabled());

        // writing activity state after disabling
        assertUtil.debug("Wrote Activity state: ec:0 sc:1 ssc:1");

        // check if message the disable of the SDK
        assertUtil.info("Pausing package handler and attribution handler to disable the SDK");

        SystemClock.sleep(1000);

        // test end session logs
        checkEndSession(false);

        // put SDK back online
        activityHandler.setOfflineMode(false);

        assertUtil.info("Package and attribution handler remain paused because the SDK is disabled");

        SystemClock.sleep(1000);

        // test the update status, still paused
        assertUtil.notInTest("AttributionHandler pauseSending");
        assertUtil.notInTest("PackageHandler pauseSending");

        // try to do activities while SDK disabled
        activityHandler.trackSubsessionStart();
        activityHandler.trackEvent(new AdjustEvent("event1"));

        SystemClock.sleep(3000);

        // check that timer was not executed
        checkTimerIsFired(false);

        // check that it did not wrote activity state from new session or subsession
        assertUtil.notInDebug("Wrote Activity state");

        // check that it did not add any package
        assertUtil.notInTest("PackageHandler addPackage");

        // enable the SDK again
        activityHandler.setEnabled(true);

        // check that is enabled
        assertUtil.isTrue(activityHandler.isEnabled());

        SystemClock.sleep(1000);

        // test that is not paused anymore
        checkNewSession(false, 2, 0);
    }

    public void testSendReferrer() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testSendReferrer");

        // create the config to start the session
        AdjustConfig config = new AdjustConfig(context, "123456789012", AdjustConfig.ENVIRONMENT_SANDBOX);

        // start activity handler with config
        ActivityHandler activityHandler = getActivityHandler(config);

        SystemClock.sleep(3000);

        // test init values
        initTests();

        // test first session start
        checkFirstSession();

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

        // three click packages: reftag, extraParams and mixed
        for (int i = 3; i > 0; i--) {
            //assertUtil.test("AttributionHandler getAttribution");
            assertUtil.test("PackageHandler addPackage");
        }

        // check that it did not send any other click package
        assertUtil.notInTest("PackageHandler sendClickPackage");

        // checking the default values of the first session package
        // 1 session + 3 click
        assertEquals(4, mockPackageHandler.queue.size());

        ActivityPackage reftagClickPackage = mockPackageHandler.queue.get(1);

        TestActivityPackage reftagClickPackageTest = new TestActivityPackage(reftagClickPackage);

        reftagClickPackageTest.reftag = "referrerValue";
        reftagClickPackageTest.referrer = reftag;

        reftagClickPackageTest.testClickPackage("reftag");

        // get the click package
        ActivityPackage extraParamsClickPackage = mockPackageHandler.queue.get(2);

        // create activity package test
        TestActivityPackage testExtraParamsClickPackage = new TestActivityPackage(extraParamsClickPackage);

        // other deep link parameters
        testExtraParamsClickPackage.deepLinkParameters = "{\"key\":\"value\",\"foo\":\"bar\"}";

        testExtraParamsClickPackage.referrer = extraParams;

        // test the second deeplink
        testExtraParamsClickPackage.testClickPackage("reftag");

        // get the click package
        ActivityPackage mixedClickPackage = mockPackageHandler.queue.get(3);

        // create activity package test
        TestActivityPackage testMixedClickPackage = new TestActivityPackage(mixedClickPackage);

        testMixedClickPackage.reftag = "referrerValue";
        testMixedClickPackage.referrer = mixed;

        // other deep link parameters
        testMixedClickPackage.deepLinkParameters = "{\"foo\":\"bar\"}";

        // test the third deeplink
        testMixedClickPackage.testClickPackage("reftag");
    }

    public void testGetAttribution() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testGetAttribution");

        //AdjustFactory.setTimerStart(500);
        AdjustFactory.setSessionInterval(4000);
        /***
         * if (activityState.subsessionCount > 1) {
         *     if (attribution == null || activityState.askingAttribution) {
         *         getAttributionHandler().getAttribution();
         *     }
         * }
         */

        // create the config to start the session
        AdjustConfig config = new AdjustConfig(context, "123456789012", AdjustConfig.ENVIRONMENT_SANDBOX);

        config.setOnAttributionChangedListener(new OnAttributionChangedListener() {
            @Override
            public void onAttributionChanged(AdjustAttribution attribution) {
                mockLogger.test("onAttributionChanged " + attribution);
            }
        });

        // start activity handler with config
        ActivityHandler activityHandler = getActivityHandler(config);

        SystemClock.sleep(3000);

        // test init values
        initTests();

        // subsession count is 1
        // attribution is null,
        // askingAttribution is false by default,
        // -> Not called

        // test first session start
        checkFirstSession();

        // test that get attribution wasn't called
        assertUtil.notInTest("AttributionHandler getAttribution");

        // subsession count increased to 2
        // attribution is still null,
        // askingAttribution is still false,
        // -> Called

        // trigger a new sub session
        activityHandler.trackSubsessionStart();
        SystemClock.sleep(2000);

        checkSubsession(1, 2, true, true);

        // subsession count increased to 3
        // attribution is still null,
        // askingAttribution is set to true,
        // -> Called

        // set asking attribution
        activityHandler.setAskingAttribution(true);
        assertUtil.debug("Wrote Activity state: ec:0 sc:1 ssc:2");

        // trigger a new session
        activityHandler.trackSubsessionStart();
        SystemClock.sleep(2000);

        checkSubsession(1, 3, true, true);

        // subsession is reset to 1 with new session
        // attribution is still null,
        // askingAttribution is set to true,
        // -> Not called

        SystemClock.sleep(3000); // 5 seconds = 2 + 3
        activityHandler.trackSubsessionStart();
        SystemClock.sleep(2000);

        checkSubsession(2, 1, true, false);

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
        //activityHandler.tryUpdateAttribution(attribution);

        // attribution was updated
        assertUtil.debug("Wrote Attribution: tt:ttValue tn:tnValue net:nValue cam:cpValue adg:aValue cre:ctValue cl:clValue");

        // trigger a new sub session
        activityHandler.trackSubsessionStart();
        SystemClock.sleep(2000);

        checkSubsession(2, 2, true, true);
        // subsession count is reset to 1
        // attribution is set,
        // askingAttribution is set to true,
        // -> Not called

        SystemClock.sleep(3000); // 5 seconds = 2 + 3
        activityHandler.trackSubsessionStart();
        SystemClock.sleep(2000);

        checkSubsession(3, 1, true, false);
        // subsession increased to 2
        // attribution is set,
        // askingAttribution is set to false
        // -> Not called

        activityHandler.setAskingAttribution(false);
        assertUtil.debug("Wrote Activity state: ec:0 sc:3 ssc:1");

        // trigger a new sub session
        activityHandler.trackSubsessionStart();
        SystemClock.sleep(2000);

        checkSubsession(3, 2, true, false);

        // subsession is reset to 1
        // attribution is set,
        // askingAttribution is set to false
        // -> Not called

        SystemClock.sleep(3000); // 5 seconds = 2 + 3
        activityHandler.trackSubsessionStart();
        SystemClock.sleep(2000);

        checkSubsession(4, 1, true, false);
    }

    public void testTimer() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testTimer");

        AdjustFactory.setTimerInterval(4000);
        AdjustFactory.setTimerStart(0);

        // create the config to start the session
        AdjustConfig config = new AdjustConfig(context, "123456789012", AdjustConfig.ENVIRONMENT_SANDBOX);

        // start activity handler with config
        ActivityHandler activityHandler = getActivityHandler(config);

        SystemClock.sleep(2000);

        // test init values
        initTests();

        // test first session start
        checkFirstSession();

        // wait enough to fire the first cycle
        SystemClock.sleep(3000);

        checkTimerIsFired(true);

        // end subsession to stop timer
        activityHandler.trackSubsessionEnd();

        // wait enough for a new cycle
        SystemClock.sleep(6000);

        activityHandler.trackSubsessionStart();

        SystemClock.sleep(1000);

        checkTimerIsFired(false);
    }

    private void checkFirstSessionSubsession(int subsessionCount) {
        checkSubsession(1, subsessionCount);
    }

    private void checkSubsession(int sessionCount,
                                 int subsessionCount,
                                 boolean timerAlreadyStarted,
                                 boolean getAttributionIsCalled) {
        checkSubsession(sessionCount, subsessionCount);

        if (getAttributionIsCalled) {
            assertUtil.test("AttributionHandler getAttribution");
        } else {
            assertUtil.notInTest("AttributionHandler getAttribution");
        }

        checkTimerIsFired(!timerAlreadyStarted);
    }

    private void checkSubsession(int sessionCount, int subsessionCount, boolean timerAlreadyStarted) {
        checkSubsession(sessionCount, subsessionCount);

        checkTimerIsFired(!timerAlreadyStarted);
    }

    private void checkSubsession(int sessionCount, int subsessionCount) {
        // test the new sub session
        assertUtil.test("PackageHandler resumeSending");

        // save activity state
        assertUtil.debug("Wrote Activity state: ec:0 sc:" + sessionCount + " ssc:" + subsessionCount);

        if (subsessionCount > 1) {
            // test the subsession message
            assertUtil.info("Started subsession " + subsessionCount + " of session " + sessionCount);
        } else {
            // test the subsession message
            assertUtil.notInInfo("Started subsession ");
        }
    }

    private void initTests() {
        initTests("sandbox", "INFO", false, null, null);
    }

    private void initTests(String environment, String logLevel, boolean eventBuffering) {
        initTests(environment, logLevel, eventBuffering, null, null);
    }

    private void initTests(String environment, String logLevel, boolean eventBuffering,
                           String readActivityState, String readAttribution) {
        // check environment level
        if (environment == "sandbox") {
            assertUtil.Assert("SANDBOX: Adjust is running in Sandbox mode. Use this setting for testing. Don't forget to set the environment to `production` before publishing!");
        } else if (environment == "production") {
            assertUtil.Assert("PRODUCTION: Adjust is running in Production mode. Use this setting only for the build that you want to publish. Set the environment to `sandbox` if you want to test your app!");
        } else {
            fail();
        }

        // check log level
        assertUtil.test("MockLogger setLogLevel: " + logLevel);

        // check event buffering
        if (eventBuffering) {
            assertUtil.info("Event buffering is enabled");
        } else {
            assertUtil.notInInfo("Event buffering is enabled");
        }

        // check Google play is not set
        assertUtil.info("Unable to get Google Play Services Advertising ID at start time");

        readFiles(readActivityState, readAttribution);
    }

    private void readFiles(String readActivityState, String readAttribution) {
        if (readAttribution == null) {
            //  test that the attribution file did not exist in the first run of the application
            assertUtil.verbose("Attribution file not found");
        } else {
            assertUtil.debug("Read Attribution: " + readAttribution);
        }

        if (readActivityState == null) {
            //  test that the activity state file did not exist in the first run of the application
            assertUtil.verbose("Activity state file not found");
        } else {
            assertUtil.debug("Read Activity state: " + readActivityState);
        }
    }

    private void checkFirstSession() {
        checkFirstSession(false);
    }

    private void checkFirstSession(boolean paused) {
        if (paused) {
            assertUtil.test("PackageHandler init, startPaused: true");
        } else {
            assertUtil.test("PackageHandler init, startPaused: false");
        }

        checkNewSession(paused,1, 0, false);
    }

    private void checkNewSession(boolean paused,
                                 int sessionCount,
                                 int eventCount) {
        checkNewSession(paused, sessionCount, eventCount, false);
    }

    private void checkNewSession(boolean paused,
                                 int sessionCount,
                                 int eventCount,
                                 boolean timerAlreadyStarted)
    {
        // when a session package is being sent the attribution handler should resume sending
        if (paused) {
            assertUtil.test("AttributionHandler pauseSending");
        } else {
            assertUtil.test("AttributionHandler resumeSending");
        }

        // when a session package is being sent the package handler should resume sending
        if (paused) {
            assertUtil.test("PackageHandler pauseSending");
        } else {
            assertUtil.test("PackageHandler resumeSending");
        }

        // if the package was build, it was sent to the Package Handler
        assertUtil.test("PackageHandler addPackage");

        // after adding, the activity handler ping the Package handler to send the package
        assertUtil.test("PackageHandler sendFirstPackage");

        // after sending a package saves the activity state
        assertUtil.debug("Wrote Activity state: " +
                "ec:" + eventCount + " sc:" + sessionCount + " ssc:1" );

        checkTimerIsFired(!(paused || timerAlreadyStarted));
    }

    private void checkTimerIsFired(boolean timerFired) {
        // timer fired
        if (timerFired) {
            assertUtil.debug("Session timer fired");
        } else {
            assertUtil.notInDebug("Session timer fired");
        }
    }

    private void checkEndSession() {
        checkEndSession(true);
    }

    private void checkEndSession(boolean updateActivityState) {
        assertUtil.test("PackageHandler pauseSending");

        assertUtil.test("AttributionHandler pauseSending");

        if (updateActivityState) {
            assertUtil.debug("Wrote Activity state: ");
        }
    }

    private ActivityHandler getActivityHandler(AdjustConfig config) {
        ActivityHandler activityHandler = ActivityHandler.getInstance(config);

        if (activityHandler != null) {
            activityHandler.trackSubsessionStart();
        }

        return activityHandler;
    }
}
