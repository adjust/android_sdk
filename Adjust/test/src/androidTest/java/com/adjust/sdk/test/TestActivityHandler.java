package com.adjust.sdk.test;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.SystemClock;
import android.test.ActivityInstrumentationTestCase2;
import android.test.mock.MockContext;

import com.adjust.sdk.ActivityHandler;
import com.adjust.sdk.ActivityPackage;
import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustConfig;
import com.adjust.sdk.AdjustFactory;
import com.adjust.sdk.Attribution;
import com.adjust.sdk.Constants;
import com.adjust.sdk.Event;
import com.adjust.sdk.Logger;
import com.adjust.sdk.OnFinishedListener;

import com.adjust.sdk.Logger.LogLevel;

import org.json.JSONException;
import org.json.JSONObject;


public class TestActivityHandler extends ActivityInstrumentationTestCase2<UnitTestActivity> {
    protected MockLogger mockLogger;
    protected MockPackageHandler mockPackageHandler;
    protected MockAttributionHandler mockAttributionHandler;
    protected UnitTestActivity activity;
    protected Context context;
    protected AssertUtil assertUtil;

    public TestActivityHandler(){
        super(UnitTestActivity.class);
    }

    public TestActivityHandler(Class<UnitTestActivity> mainActivity){
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
    protected void tearDown() throws Exception{
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
        AdjustConfig config = AdjustConfig.getInstance(context,"123456789012", AdjustConfig.SANDBOX_ENVIRONMENT);

        // start activity handler with config
        ActivityHandler activityHandler = startActivityHandler(config);

        // test init values
        initTests(AdjustConfig.SANDBOX_ENVIRONMENT, "INFO", false);

        // test first session start
        firstSessionStartTests();

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
        AdjustConfig config = AdjustConfig.getInstance(context,"123456789012", AdjustConfig.SANDBOX_ENVIRONMENT);

        // buffer events
        config.setEventBufferingEnabled(true);

        // set verbose log level
        config.setLogLevel(LogLevel.VERBOSE);

        // start activity handler with config
        ActivityHandler activityHandler = startActivityHandler(config);

        // test init values
        initTests(AdjustConfig.SANDBOX_ENVIRONMENT, "VERBOSE", true);

        // test first session start
        firstSessionStartTests();

        // create the first Event
        Event firstEvent = Event.getInstance("event1");

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
        Event secondEvent = Event.getInstance("event2");

        // add empty revenue
        secondEvent.setRevenue(0, "USD");

        // track second event
        activityHandler.trackEvent(secondEvent);

        // create third Event
        Event thirdEvent = Event.getInstance("event3");

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
        assertUtil.info("Buffered event  (0.001 cent, 'event1')");

        // and not sent to package handler
        assertUtil.notInTest("PackageHandler sendFirstPackage");

        // after tracking the event it should write the activity state
        assertUtil.debug("Wrote Activity state");

        // test second event
        // check that event package was added
        assertUtil.test("PackageHandler addPackage");

        // check that event was buffered
        assertUtil.info("Buffered event  (0.000 cent, 'event2')");

        // and not sent to package handler
        assertUtil.notInTest("PackageHandler sendFirstPackage");

        // after tracking the event it should write the activity state
        assertUtil.debug("Wrote Activity state");

        // test third event
        // check that event package was added
        assertUtil.test("PackageHandler addPackage");

        // check that event was buffered
        assertUtil.info("Buffered event  'event3'");

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
        testFirstEventPackage.suffix = " (0.001 cent, 'event1')";
        testFirstEventPackage.revenueString = "0.001";
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
        testSecondEventPackage.suffix = " (0.000 cent, 'event2')";
        testSecondEventPackage.revenueString = "0.000";
        testSecondEventPackage.currency = "USD";

        // test second event
        testSecondEventPackage.testEventPackage("event2");

        // third event
        ActivityPackage thirdEventPackage = mockPackageHandler.queue.get(3);

        // create event package test
        TestActivityPackage testThirdEventPackage = new TestActivityPackage(thirdEventPackage);

        // set event test parameters
        testThirdEventPackage.eventCount = "3";
        testThirdEventPackage.suffix = " 'event3'";

        // test third event
        testThirdEventPackage.testEventPackage("event3");
    }

    public void testEventsNotBuffered() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testEventsNotBuffered");

        // create the config to start the session
        AdjustConfig config = AdjustConfig.getInstance(context,"123456789012", AdjustConfig.SANDBOX_ENVIRONMENT);

        // set log level
        config.setLogLevel(LogLevel.DEBUG);

        // start activity handler with config
        ActivityHandler activityHandler = startActivityHandler(config);

        // test init values
        initTests(AdjustConfig.SANDBOX_ENVIRONMENT, "DEBUG", false);

        // test first session start
        firstSessionStartTests();

        // create the first Event
        Event firstEvent = Event.getInstance("event1");

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
        AdjustConfig nullAppTokenConfig = AdjustConfig.getInstance(context, null, AdjustConfig.SANDBOX_ENVIRONMENT);

        assertUtil.error("Missing App Token.");
        assertUtil.isNull(nullAppTokenConfig);

        // config with wrong size app token
        AdjustConfig oversizeAppTokenConfig = AdjustConfig.getInstance(context, "1234567890123", AdjustConfig.SANDBOX_ENVIRONMENT);

        assertUtil.error("Malformed App Token '1234567890123'");
        assertUtil.isNull(oversizeAppTokenConfig);

        // config with null environment
        AdjustConfig nullEnvironmentConfig = AdjustConfig.getInstance(context, "123456789012", null);

        assertUtil.error("Missing environment");
        assertUtil.isNull(nullEnvironmentConfig);

        // config with wrong environment
        AdjustConfig wrongEnvironmentConfig = AdjustConfig.getInstance(context, "123456789012", "Unknown");

        assertUtil.error("Malformed environment 'Unknown'");
        assertUtil.isNull(wrongEnvironmentConfig);

        // config with null context
        AdjustConfig nullContextConfig = AdjustConfig.getInstance(null, "123456789012", AdjustConfig.SANDBOX_ENVIRONMENT);

        assertUtil.error("Missing context");
        assertUtil.isNull(nullContextConfig);

        // config without internet permission
        Context mockContext = new MockContext () {
            @Override
            public int checkCallingOrSelfPermission(String permission) {
                return PackageManager.PERMISSION_DENIED;
            }
        };
        AdjustConfig mockContextConfig = AdjustConfig.getInstance(mockContext, "123456789012", AdjustConfig.SANDBOX_ENVIRONMENT);

        assertUtil.error("Missing permission: INTERNET");
        assertUtil.isNull(mockContextConfig);

        // config without access wifi state permission
        // TODO

        // start with null config
        ActivityHandler nullConfigactivityHandler = ActivityHandler.getInstance(null);

        assertUtil.error("AdjustConfig not initialized correctly");
        assertUtil.isNull(nullConfigactivityHandler);

        // event with null event token
        Event nullEventToken = Event.getInstance(null);

        assertUtil.error("Missing Event Token");
        assertUtil.isNull(nullEventToken);

        // event with wrong size
        Event wrongEventTokenSize = Event.getInstance("eventXX");

        assertUtil.error("Malformed Event Token 'eventXX'");
        assertUtil.isNull(wrongEventTokenSize);

        // event
        Event event = Event.getInstance("event1");

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

        // create the config to start the session
        AdjustConfig config = AdjustConfig.getInstance(context, "123456789012", AdjustConfig.SANDBOX_ENVIRONMENT);

        // set the log level
        config.setLogLevel(LogLevel.WARN);

        // create handler and start the first session
        ActivityHandler activityHandler = startActivityHandler(config);

        // test init values
        initTests(AdjustConfig.SANDBOX_ENVIRONMENT, "WARN", false);

        // test first session start
        firstSessionStartTests();

        // track null event
        activityHandler.trackEvent(null);
        SystemClock.sleep(1000);

        assertUtil.error("Event missing");
    }

    public void testSessions() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testSessions");

        // adjust the session intervals for testing
        AdjustFactory.setSessionInterval(4000);
        AdjustFactory.setSubsessionInterval(1000);

        // create the config to start the session
        AdjustConfig config = AdjustConfig.getInstance(context,"123456789012", AdjustConfig.SANDBOX_ENVIRONMENT);

        // start activity handler with config
        ActivityHandler activityHandler = startActivityHandler(config);

        // trigger a new sub session session
        activityHandler.trackSubsessionStart();

        SystemClock.sleep(5000);

        // trigger a new session
        activityHandler.trackSubsessionStart();

        SystemClock.sleep(1000);

        // end the session
        activityHandler.trackSubsessionEnd();

        SystemClock.sleep(1000);

        // set verbose log level
        config.setLogLevel(LogLevel.INFO);

        // test init values
        initTests(AdjustConfig.SANDBOX_ENVIRONMENT, "INFO", false);

        // test first session start
        firstSessionStartTests();

        // test the new sub session
        assertUtil.test("PackageHandler resumeSending");

        // save activity state
        assertUtil.debug("Wrote Activity state: ec:0 sc:1 ssc:2");

        // test the subsession message
        assertUtil.info("Started subsession 2 of session 1");

        // test the new timer
        timerFiredTest();

        // save activity state
        assertUtil.debug("Wrote Activity state: ec:0 sc:1 ssc:2");

        // new session
        startSessionTest();

        // test the new subsession
        assertUtil.debug("Wrote Activity state: ec:0 sc:2 ssc:1");

        // test the new timer
        timerFiredTest();

        // save activity state
        assertUtil.debug("Wrote Activity state: ec:0 sc:2 ssc:1");

        // pause sending
        assertUtil.test("PackageHandler pauseSending");

        // save activity state
        assertUtil.debug("Wrote Activity state: ec:0 sc:2 ssc:1");

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
        AdjustConfig config = AdjustConfig.getInstance(context,"123456789012", AdjustConfig.SANDBOX_ENVIRONMENT);

        // set log level
        config.setLogLevel(LogLevel.ERROR);

        // start activity handler with config
        ActivityHandler activityHandler = ActivityHandler.getInstance(config);

        mockPackageHandler.activityHandler = activityHandler;
        mockAttributionHandler.activityHandler = activityHandler;

        // check that is true by default
        assertUtil.isTrue(activityHandler.isEnabled());

        // disable sdk
        activityHandler.setEnabled(false);

        // check that it is disabled
        assertUtil.isFalse(activityHandler.isEnabled());

        // check if message the disable of the SDK
        //assertUtil.info("Pausing package handler to disable the SDK");

        // it's necessary to sleep the activity for a while after each handler call
        // to let the internal queue act
        SystemClock.sleep(2000);

        // test init values
        initTests(AdjustConfig.SANDBOX_ENVIRONMENT, "ERROR", false);

        // test first session start
        firstSessionStartWithoutTimerTests();

        // try to do activities while SDK disabled
        activityHandler.trackSubsessionStart();
        activityHandler.trackEvent(Event.getInstance("event1"));

        SystemClock.sleep(2000);

        // check that timer was not executed
        assertUtil.notInTest("PackageHandler sendFirstPackage");

        // but that it was paused
        assertUtil.test("PackageHandler pauseSending");
        assertUtil.debug("Wrote Activity state: ec:0 sc:1 ssc:1");

        // check that it did not resume
        assertUtil.notInTest("PackageHandler resumeSending");

        // check that it did not wrote activity state from new session or subsession
        assertUtil.notInDebug("Wrote Activity state");

        // or that started the timer
        assertUtil.notInTest("PackageHandler sendFirstPackage");

        // check that it did not add any event package
        assertUtil.notInTest("PackageHandler addPackage");

        // only the first session package should be sent
        assertEquals(1, mockPackageHandler.queue.size());

        // re-enable the SDK
        activityHandler.setEnabled(true);

        // check that it is enabled
        assertUtil.isTrue(activityHandler.isEnabled());

        // check message of enabling the SDK
        //assertUtil.info("Resuming package handler to enabled the SDK");

        SystemClock.sleep(5000);

        // check that started again
        assertUtil.test("PackageHandler resumeSending");

        // check that it wrote the sub session session
        assertUtil.debug("Wrote Activity state: ec:0 sc:1 ssc:2");

        // check the sub session
        assertUtil.info("Started subsession 2 of session 1");

        // and that it fired the timer
        timerFiredTest();
        assertUtil.debug("Wrote Activity state: ec:0 sc:1 ssc:2");

        // start a new session and event
        activityHandler.trackSubsessionStart();

        SystemClock.sleep(1000);

        startSessionTest();

        // check that it wrote the third session
        assertUtil.debug("Wrote Activity state: ec:0 sc:2");

        // and that it fired the timer
        timerFiredTest();
        assertUtil.debug("Wrote Activity state: ec:0 sc:2");

        // track an event
        activityHandler.trackEvent(Event.getInstance("event1"));

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
        testSecondSessionPackage.subsessionCount = 2;

        // test third session
        testSecondSessionPackage.testSessionPackage(2);

        ActivityPackage eventPackage = mockPackageHandler.queue.get(2);

        // create activity package test
        TestActivityPackage testEventPackage = new TestActivityPackage(eventPackage);

        testEventPackage.suffix = " 'event1'";

        // test event
        testEventPackage.testEventPackage("event1");
    }

    public void testOpenUrl () {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testOpenUrl");

        // create the config to start the session
        AdjustConfig config = AdjustConfig.getInstance(context,"123456789012", AdjustConfig.SANDBOX_ENVIRONMENT);

        // set log level
        config.setLogLevel(LogLevel.ASSERT);

        // start activity handler with config
        ActivityHandler activityHandler = startActivityHandler(config);

        // test init values
        initTests(AdjustConfig.SANDBOX_ENVIRONMENT, "ASSERT", false);

        // test first session start
        firstSessionStartTests();

        Uri attributions = Uri.parse("AdjustTests://example.com/path/inApp?adjust_tracker=trackerValue&other=stuff&adjust_campaign=campaignValue&adjust_adgroup=adgroupValue&adjust_creative=creativeValue");
        Uri nonAttributions = Uri.parse("AdjustTests://example.com/path/inApp?adjust_foo=bar&other=stuff&adjust_key=value");
        Uri mixed= Uri.parse("AdjustTests://example.com/path/inApp?adjust_foo=bar&other=stuff&adjust_campaign=campaignValue&adjust_adgroup=adgroupValue&adjust_creative=creativeValue");
        Uri emptyQueryString = Uri.parse("AdjustTests://");
        Uri emptyString = Uri.parse("");
        Uri nullUri = null;
        Uri single = Uri.parse("AdjustTests://example.com/path/inApp?adjust_foo");
        Uri prefix = Uri.parse("AdjustTests://example.com/path/inApp?adjust_=bar");
        Uri incomplete = Uri.parse("AdjustTests://example.com/path/inApp?adjust_foo=");

        activityHandler.readOpenUrl(attributions);
        activityHandler.readOpenUrl(nonAttributions);
        activityHandler.readOpenUrl(mixed);
        activityHandler.readOpenUrl(emptyQueryString);
        activityHandler.readOpenUrl(emptyString);
        activityHandler.readOpenUrl(nullUri);
        activityHandler.readOpenUrl(single);
        activityHandler.readOpenUrl(prefix);
        activityHandler.readOpenUrl(incomplete);

        SystemClock.sleep(1000);

        // three click packages: attributions, nonAttributions and mixed
        for (int i = 3; i > 0; i--) {
            assertUtil.test("AttributionHandler getAttribution");
            assertUtil.test("PackageHandler sendClickPackage");
        }

        // check that it did not send any other click package
        assertUtil.notInTest("AttributionHandler getAttribution");
        assertUtil.notInTest("PackageHandler sendClickPackage");

        // checking the default values of the first session package
        // 1 session + 3 click
        assertEquals(4, mockPackageHandler.queue.size());

        // get the click package
        ActivityPackage firstClickPackage = mockPackageHandler.queue.get(1);

        // create activity package test
        TestActivityPackage testFirstClickPackage = new TestActivityPackage(firstClickPackage);

        // create the attribution
        Attribution firstAttribution = new Attribution();
        firstAttribution.trackerName = "trackerValue";
        firstAttribution.campaign = "campaignValue";
        firstAttribution.adgroup = "adgroupValue";
        firstAttribution.creative = "creativeValue";

        // and set it
        testFirstClickPackage.attribution = firstAttribution;

        // test the first deeplink
        testFirstClickPackage.testClickPackage("deeplink");

        // get the click package
        ActivityPackage secondClickPackage = mockPackageHandler.queue.get(2);

        // create activity package test
        TestActivityPackage testSecondClickPackage = new TestActivityPackage(secondClickPackage);

        // other deep link parameters
        testSecondClickPackage.deepLinkParameters = "{\"key\":\"value\",\"foo\":\"bar\"}";

        // test the second deeplink
        testSecondClickPackage.testClickPackage("deeplink");

        // get the click package
        ActivityPackage thirdClickPackage = mockPackageHandler.queue.get(3);

        // create activity package test
        TestActivityPackage testThirdClickPackage = new TestActivityPackage(thirdClickPackage);

        // create the attribution
        Attribution secondAttribution = new Attribution();
        secondAttribution.campaign = "campaignValue";
        secondAttribution.adgroup = "adgroupValue";
        secondAttribution.creative = "creativeValue";

        // and set it
        testThirdClickPackage.attribution = secondAttribution;

        // other deep link parameters
        testThirdClickPackage.deepLinkParameters = "{\"foo\":\"bar\"}";

        // test the third deeplink
        testThirdClickPackage.testClickPackage("deeplink");
    }

    public void testFinishedTrackingActivity() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testFinishedTrackingActivity");

        // create the config to start the session
        AdjustConfig config = AdjustConfig.getInstance(context,"123456789012", AdjustConfig.PRODUCTION_ENVIRONMENT);

        // set verbose log level
        config.setLogLevel(LogLevel.VERBOSE);

        config.setOnFinishedListener(new OnFinishedListener() {
            @Override
            public void onFinishedTracking(Attribution attribution) {
                mockLogger.test("onFinishedTracking: " + attribution);
            }
        });

        // start activity handler with config
        ActivityHandler activityHandler = startActivityHandler(config);

        // test init values
        initTests(AdjustConfig.PRODUCTION_ENVIRONMENT, "ASSERT", false);

        // test first session start
        firstSessionStartTests();

        JSONObject responseNull = null;

        activityHandler.finishedTrackingActivity(responseNull);
        SystemClock.sleep(1000);

        // if the response is null
        assertUtil.notInTest("AttributionHandler checkAttribution");
        assertUtil.notInError("Unable to open deep link");
        assertUtil.notInInfo("Open deep link");

        // set package handler to respond with a valid attribution
        JSONObject wrongDeeplinkResponse = null;
        try {
            wrongDeeplinkResponse = new JSONObject("{ " +
                    "\"deeplink\" :  \"wrongDeeplink://\" }");
        } catch (JSONException e) {
            fail(e.getMessage());
        }

        activityHandler.finishedTrackingActivity(wrongDeeplinkResponse);
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
        testActivityPackage.environment = AdjustConfig.PRODUCTION_ENVIRONMENT;

        // set first session
        testActivityPackage.testSessionPackage(1);
    }

    public void testUpdateAttribution() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testUpdateAttribution");

        // create the config to start the session
        AdjustConfig config = AdjustConfig.getInstance(context,"123456789012", AdjustConfig.SANDBOX_ENVIRONMENT);

        // start activity handler with config
        ActivityHandler firstActivityHandler = startActivityHandler(config);

        // test init values
        initTests(AdjustConfig.SANDBOX_ENVIRONMENT, "INFO", false);

        // test first session start
        firstSessionStartTests();

        JSONObject nullJsonObject = null;
        Attribution nullAttribution = Attribution.fromJson(nullJsonObject);

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
        Attribution emptyAttribution = Attribution.fromJson(emptyJsonResponse);

        // check that updates attribution
        assertUtil.isTrue(firstActivityHandler.updateAttribution(emptyAttribution));
        assertUtil.debug("Wrote Attribution: tt:null tn:null net:null cam:null adg:null cre:null");

        // check that it doesn't launch the saved attribute
        firstActivityHandler.launchAttributionDelegate();
        assertUtil.notInTest("onFinishedTracking");

        emptyAttribution = Attribution.fromJson(emptyJsonResponse);

        // check that it does not update the attribution
        assertUtil.isFalse(firstActivityHandler.updateAttribution(emptyAttribution));
        assertUtil.notInDebug("Wrote Attribution");

        // end session
        firstActivityHandler.trackSubsessionEnd();
        SystemClock.sleep(1000);

        endSessionTest();

        config = AdjustConfig.getInstance(context,"123456789012", AdjustConfig.SANDBOX_ENVIRONMENT);

        config.setOnFinishedListener(new OnFinishedListener() {
            @Override
            public void onFinishedTracking(Attribution attribution) {
                mockLogger.test("onFinishedTracking: " + attribution);
            }
        });

        ActivityHandler restartActivityHandler = startActivityHandler(config);

        // test init values
        initTests(AdjustConfig.SANDBOX_ENVIRONMENT, "INFO", false);

        firstSessionSubsessionsTest(2);

        // check that it launch the saved attribute
        restartActivityHandler.launchAttributionDelegate();
        SystemClock.sleep(1000);

        assertUtil.test("onFinishedTracking: tt:null tn:null net:null cam:null adg:null cre:null");

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
                    "\"creative\"      : \"ctValue\" }");
        } catch (JSONException e) {
            fail(e.getMessage());
        }
        Attribution firstAttribution = Attribution.fromJson(firstAttributionJson);

        //check that it updates
        assertUtil.isTrue(restartActivityHandler.updateAttribution(firstAttribution));
        assertUtil.debug("Wrote Attribution: tt:ttValue tn:tnValue net:nValue cam:cpValue adg:aValue cre:ctValue");

        // check that it launch the saved attribute
        restartActivityHandler.launchAttributionDelegate();
        SystemClock.sleep(1000);

        assertUtil.test("onFinishedTracking: tt:ttValue tn:tnValue net:nValue cam:cpValue adg:aValue cre:ctValue");

        // check that it does not update the attribution
        assertUtil.isFalse(restartActivityHandler.updateAttribution(firstAttribution));
        assertUtil.notInDebug("Wrote Attribution");

        // end session
        restartActivityHandler.trackSubsessionEnd();
        SystemClock.sleep(1000);

        endSessionTest();

        config = AdjustConfig.getInstance(context,"123456789012", AdjustConfig.SANDBOX_ENVIRONMENT);

        config.setOnFinishedListener(new OnFinishedListener() {
            @Override
            public void onFinishedTracking(Attribution attribution) {
                mockLogger.test("onFinishedTracking: " + attribution);
            }
        });

        ActivityHandler secondRestartActivityHandler = startActivityHandler(config);

        // test init values
        initTests(AdjustConfig.SANDBOX_ENVIRONMENT, "INFO", false);

        firstSessionSubsessionsTest(3);

        // check that it launch the saved attribute
        secondRestartActivityHandler.launchAttributionDelegate();
        SystemClock.sleep(1000);

        assertUtil.test("onFinishedTracking: tt:ttValue tn:tnValue net:nValue cam:cpValue adg:aValue cre:ctValue");

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
                    "\"creative\"      : \"ctValue2\" }");
        } catch (JSONException e) {
            fail(e.getMessage());
        }
        Attribution secondAttribution = Attribution.fromJson(secondAttributionJson);

        //check that it updates
        assertUtil.isTrue(secondRestartActivityHandler.updateAttribution(secondAttribution));
        assertUtil.debug("Wrote Attribution: tt:ttValue2 tn:tnValue2 net:nValue2 cam:cpValue2 adg:aValue2 cre:ctValue2");

        // check that it launch the saved attribute
        secondRestartActivityHandler.launchAttributionDelegate();
        SystemClock.sleep(1000);

        assertUtil.test("onFinishedTracking: tt:ttValue2 tn:tnValue2 net:nValue2 cam:cpValue2 adg:aValue2 cre:ctValue2");

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
        AdjustConfig config = AdjustConfig.getInstance(context,"123456789012", AdjustConfig.SANDBOX_ENVIRONMENT);

        // start activity handler with config
        ActivityHandler activityHandler = startActivityHandler(config);

        // test init values
        initTests(AdjustConfig.SANDBOX_ENVIRONMENT, "INFO", false);

        // test first session start
        firstSessionStartTests();

        // set offline for new session
        activityHandler.setOfflineMode(true);

        SystemClock.sleep(1000);

        // check offline message
        assertUtil.info("Pausing package handler to put in offline mode");

        // end session
        assertUtil.test("PackageHandler pauseSending");

        // trigger new session
        activityHandler.trackSubsessionStart();

        SystemClock.sleep(3000);

        // check that it did not un-pause sending in the new session
        assertUtil.notInTest("PackageHandler resumeSending");

        // send new session to package handler
        assertUtil.test("PackageHandler addPackage");
        assertUtil.test("PackageHandler sendFirstPackage");
        assertUtil.debug("Wrote Activity state: ec:0 sc:2");

        // check that the second session package was added
        // 2 session packages
        assertEquals(2, mockPackageHandler.queue.size());

        // get second session package
        ActivityPackage secondSessionActivityPackage = mockPackageHandler.queue.get(1);

        // create second session test package
        TestActivityPackage testSecondSessionActivityPackage = new TestActivityPackage(secondSessionActivityPackage);

        // test second session
        testSecondSessionActivityPackage.testSessionPackage(2);

        // set offline mode to false again
        activityHandler.setOfflineMode(false);

        SystemClock.sleep(1000);

        // check online message
        assertUtil.info("Resuming package handler to put in online mode");

        // check that it did un-pause sending in the new session
        assertUtil.test("PackageHandler resumeSending");

        // send new session to package handler
        assertUtil.test("PackageHandler addPackage");
        assertUtil.test("PackageHandler sendFirstPackage");
        assertUtil.debug("Wrote Activity state: ec:0 sc:3");

    }

    public void testSendReferrer() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testSendReferrer");

        // create the config to start the session
        AdjustConfig config = AdjustConfig.getInstance(context,"123456789012", AdjustConfig.SANDBOX_ENVIRONMENT);

        // start activity handler with config
        ActivityHandler activityHandler = startActivityHandler(config);

        // test init values
        initTests(AdjustConfig.SANDBOX_ENVIRONMENT, "INFO", false);

        // test first session start
        firstSessionStartTests();

        activityHandler.setReferrer("referrerValue");

        SystemClock.sleep(1000);

        assertUtil.test("PackageHandler sendClickPackage");

        // 1 session + 1 refferer package
        assertEquals(2, mockPackageHandler.queue.size());

        ActivityPackage reffererPackage =  mockPackageHandler.queue.get(1);

        TestActivityPackage reffererPackageTest = new TestActivityPackage(reffererPackage);

        reffererPackageTest.referrer = "referrerValue";

        reffererPackageTest.testClickPackage("referrer");
    }

    public void testGetAttribution() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testGetAttribution");

        AdjustFactory.setTimerStart(500);
        /***
         * if (attribution == null || activityState.askingAttribution) {
         *   if (shouldGetAttribution) {
         *     getAttributionHandler().getAttribution();
         *   }
         * }
         *
         *  9 possible states with variables
         *  attribution = null          -> attrNul
         *  askingAttribution = true    -> askAttr
         *  shouldGetAttribution = true -> shldGet
         *  getAttribution is called    -> getAttr
         *
         *  State   Number  attrNul |   askAttr |   shldGet ->  getAttr
         *  000->0  0       False   |   False   |   False   ->  False
         *  001->0  1       False   |   False   |   True    ->  False
         *  010->0  2       False   |   True    |   False   ->  False
         *  011->1  3       False   |   True    |   True    ->  True
         *  100->0  4       True    |   False   |   False   ->  False
         *  101->1  5       True    |   False   |   True    ->  True
         *  110->0  6       True    |   True    |   False   ->  False
         *  111->1  7       True    |   True    |   True    ->  True
         */

        // create the config to start the session
        AdjustConfig config = AdjustConfig.getInstance(context,"123456789012", AdjustConfig.SANDBOX_ENVIRONMENT);

        config.setOnFinishedListener(new OnFinishedListener() {
            @Override
            public void onFinishedTracking(Attribution attribution) {
                mockLogger.test("onFinishedTracking " + attribution);
            }
        });

        // start activity handler with config
        ActivityHandler activityHandler = startActivityHandler(config);

        // test init values
        initTests(AdjustConfig.SANDBOX_ENVIRONMENT, "INFO", false);

        // state 100->0 number 4
        // attribution is null,
        // askingAttribution is false by default,
        // shouldGetAttribution is false after a session

        // test first session start
        firstSessionStartWithoutTimerTests();

        timerFiredTest();

        // there shouldn't be a getAttribution
        assertUtil.notInTest("AttributionHandler getAttribution");

        // save activity state
        assertUtil.debug("Wrote Activity state: ec:0 sc:1 ssc:1");

        // state 110->0 number 6
        // attribution is null,
        // askingAttribution is set to true,
        // shouldGetAttribution is false after a session

        // set asking attribution
        activityHandler.setAskingAttribution(true);
        assertUtil.debug("Wrote Activity state: ec:0 sc:1 ssc:1");

        // trigger a new session
        activityHandler.trackSubsessionStart();
        SystemClock.sleep(2000);

        subsessionGetAttributionTest(2, false);

        // state 010->0 number 2
        // attribution is set,
        // askingAttribution is set to true,
        // shouldGetAttribution is false after a session

        JSONObject jsonAttribution = null;

        try {
            jsonAttribution = new JSONObject("{ " +
                    "\"tracker_token\" : \"ttValue\" , " +
                    "\"tracker_name\"  : \"tnValue\" , " +
                    "\"network\"       : \"nValue\" , " +
                    "\"campaign\"      : \"cpValue\" , " +
                    "\"adgroup\"       : \"aValue\" , " +
                    "\"creative\"      : \"ctValue\" }");
        } catch (JSONException e) {
            fail(e.getMessage());
        }
        Attribution attribution = Attribution.fromJson(jsonAttribution);

        // update the attribution
        activityHandler.updateAttribution(attribution);

        // attribution was updated
        assertUtil.debug("Wrote Attribution: tt:ttValue tn:tnValue net:nValue cam:cpValue adg:aValue cre:ctValue");

        // trigger a new sub session
        activityHandler.trackSubsessionStart();
        SystemClock.sleep(2000);

        subsessionGetAttributionTest(3, false);

        // state 000->0 number 0
        // attribution is set,
        // askingAttribution is set to false
        // shouldGetAttribution is false after a session

        activityHandler.setAskingAttribution(false);
        assertUtil.debug("Wrote Activity state: ec:0 sc:1 ssc:3");

        // trigger a new sub session
        activityHandler.trackSubsessionStart();
        SystemClock.sleep(2000);

        subsessionGetAttributionTest(4, false);

        // state 101->1 number 5
        // attribution is null,
        // askingAttribution is set to false from the previous activity handler
        // shouldGetAttribution is true after restarting with no new session

        // delete attribution to start as null
        ActivityHandler.deleteAttribution(context);

        // deleting the attribution file to simulate a first session
        mockLogger.test("Was Attribution deleted? " + true);

        // reset activity handler with previous saved activity state
        config = AdjustConfig.getInstance(context,"123456789012", AdjustConfig.SANDBOX_ENVIRONMENT);

        config.setOnFinishedListener(new OnFinishedListener() {
            @Override
            public void onFinishedTracking(Attribution attribution) {
                mockLogger.test("onFinishedTracking " + attribution);
            }
        });
        activityHandler = startActivityHandler(config);

        // test init values
        initTests(AdjustConfig.SANDBOX_ENVIRONMENT, "INFO", false);

        subsessionGetAttributionTest(5, true);

        // state 111->1 number 7
        // attribution is null,
        // askingAttribution is set to true,
        // shouldGetAttribution is still true

        activityHandler.setAskingAttribution(true);
        assertUtil.debug("Wrote Activity state: ec:0 sc:1 ssc:5");

        // trigger a new sub session
        activityHandler.trackSubsessionStart();
        SystemClock.sleep(2000);

        subsessionGetAttributionTest(6, true);

        // state 011->1 number 3
        // attribution is set,
        // askingAttribution is set to true,
        // shouldGetAttribution is still true

        // update the attribution
        activityHandler.updateAttribution(attribution);

        // attribution was updated
        assertUtil.debug("Wrote Attribution: tt:ttValue tn:tnValue net:nValue cam:cpValue adg:aValue cre:ctValue");

        // trigger a new sub session
        activityHandler.trackSubsessionStart();
        SystemClock.sleep(2000);

        subsessionGetAttributionTest(7, true);

        // state 001->0 number 1
        // attribution is set,
        // askingAttribution is set to false,
        // shouldGetAttribution is still true

        activityHandler.setAskingAttribution(false);
        assertUtil.debug("Wrote Activity state: ec:0 sc:1 ssc:7");

        // trigger a new sub session
        activityHandler.trackSubsessionStart();
        SystemClock.sleep(2000);

        subsessionGetAttributionTest(8, false);
    }

    private void firstSessionSubsessionsTest(int subsessionCount) {
        subsessionGetAttributionTest(subsessionCount, null);
    }

    private void subsessionGetAttributionTest(int subsessionCount, Boolean getAttributionIsCalled) {
        // test the new sub session
        assertUtil.test("PackageHandler resumeSending");

        // save activity state
        assertUtil.debug("Wrote Activity state: ec:0 sc:1 ssc:" + subsessionCount);

        // test the subsession message
        assertUtil.info("Started subsession " + subsessionCount  +" of session 1");

        if (getAttributionIsCalled != null) {
            if (getAttributionIsCalled) {
                assertUtil.test("AttributionHandler getAttribution");
            } else {
                assertUtil.notInTest("AttributionHandler getAttribution");
            }
        }

        // test the new timer
        timerFiredTest();
    }

    private ActivityHandler startActivityHandler(AdjustConfig config) {
        //  create handler and start the first session
        ActivityHandler activityHandler = ActivityHandler.getInstance(config);

        mockPackageHandler.activityHandler = activityHandler;
        mockAttributionHandler.activityHandler = activityHandler;

        // it's necessary to sleep the activity for a while after each handler call
        // to let the internal queue act
        SystemClock.sleep(3000);

        return activityHandler;
    }

    private void initTests(String environment, String logLevel, boolean eventBuffering) {
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
    }

    private void firstSessionStartTests() {

        firstSessionStartWithoutTimerTests();

        timerFiredTest();

        // save activity state
        assertUtil.debug("Wrote Activity state: ec:0 sc:1 ssc:1");
    }

    private void firstSessionStartWithoutTimerTests () {
        //  test that the attribution file did not exist in the first run of the application
        assertUtil.verbose("Attribution file not found");

        //  test that the activity state file did not exist in the first run of the application
        assertUtil.verbose("Activity state file not found");

        startSessionTest();

        // after sending the first package saves the activity state
        assertUtil.debug("Wrote Activity state: ec:0 sc:1 ssc:1 sl:0.0 ts:0.0");
    }

    private void startSessionTest() {
        // when a session package is being sent the package handler should resume sending
        assertUtil.test("PackageHandler resumeSending");

        // if the package was build, it was sent to the Package Handler
        assertUtil.test("PackageHandler addPackage");

        // after adding, the activity handler ping the Package handler to send the package
        assertUtil.test("PackageHandler sendFirstPackage");
    }

    private void timerFiredTest() {
        // timer fired
        assertUtil.test("PackageHandler sendFirstPackage");
    }

    private void endSessionTest() {
        assertUtil.test("PackageHandler pauseSending");
    }
}
