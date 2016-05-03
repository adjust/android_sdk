package com.adjust.sdk.test;

import android.content.Context;
import android.net.Uri;
import android.os.SystemClock;
import android.test.ActivityInstrumentationTestCase2;

import com.adjust.sdk.ActivityHandler;
import com.adjust.sdk.ActivityPackage;
import com.adjust.sdk.AdjustConfig;
import com.adjust.sdk.AdjustFactory;
import com.adjust.sdk.BackoffStrategy;
import com.adjust.sdk.SdkClickHandler;

/**
 * Created by pfms on 14/04/16.
 */
public class TestSdkClickHandler extends ActivityInstrumentationTestCase2<UnitTestActivity> {
    private MockLogger mockLogger;
    private AssertUtil assertUtil;
    private MockHttpsURLConnection mockHttpsURLConnection;
    private UnitTestActivity activity;
    private Context context;
    private ActivityPackage sdkClickPackage;

    public TestSdkClickHandler() {
        super(UnitTestActivity.class);
    }

    public TestSdkClickHandler(Class<UnitTestActivity> activityClass) {
        super(activityClass);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mockLogger = new MockLogger();
        mockHttpsURLConnection = new MockHttpsURLConnection(null, mockLogger);

        assertUtil = new AssertUtil(mockLogger);

        AdjustFactory.setLogger(mockLogger);
        AdjustFactory.setHttpsURLConnection(mockHttpsURLConnection);

        activity = getActivity();
        context = activity.getApplicationContext();
        sdkClickPackage = getClickPackage();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        AdjustFactory.setHttpsURLConnection(null);
        AdjustFactory.setLogger(null);
    }

    private ActivityPackage getClickPackage() {
        MockSdkClickHandler mockSdkClickHandler = new MockSdkClickHandler(mockLogger);
        MockAttributionHandler mockAttributionHandler = new MockAttributionHandler(mockLogger);
        MockPackageHandler mockPackageHandler = new MockPackageHandler(mockLogger);

        AdjustFactory.setPackageHandler(mockPackageHandler);
        AdjustFactory.setSdkClickHandler(mockSdkClickHandler);
        AdjustFactory.setAttributionHandler(mockAttributionHandler);

        // create the config to start the session
        AdjustConfig config = new AdjustConfig(context, "123456789012", AdjustConfig.ENVIRONMENT_SANDBOX);

        // start activity handler with config
        ActivityHandler activityHandler = ActivityHandler.getInstance(config);
        activityHandler.onResume();
        activityHandler.readOpenUrl(Uri.parse("AdjustTests://"), System.currentTimeMillis());
        SystemClock.sleep(2000);

        ActivityPackage sdkClickPackage = mockSdkClickHandler.queue.get(0);
        mockLogger.reset();

        return sdkClickPackage;
    }

    public void testPaused() {
        sdkClickPackage.setClientSdk("Test-First-Click");
        ActivityPackage secondSdkClickPackage = getClickPackage();
        secondSdkClickPackage.setClientSdk("Test-Second-Click");

        AdjustFactory.setSdkClickBackoffStrategy(BackoffStrategy.NO_WAIT);

        // assert test name to read better in logcat
        mockLogger.Assert("TestRequestHandler testPaused");

        SdkClickHandler sdkClickHandler = new SdkClickHandler(false);

        mockHttpsURLConnection.responseType = ResponseType.CLIENT_PROTOCOL_EXCEPTION;

        sdkClickHandler.sendSdkClick(sdkClickPackage);
        SystemClock.sleep(1000);

        // added first click package to the queue
        assertUtil.debug("Added sdk_click 1");

        assertUtil.verbose("Path:      /sdk_click\n" +
                "ClientSdk: Test-First-Click");

        // but not send because it's paused
        assertUtil.notInTest("MockHttpsURLConnection getInputStream");

        // send second sdk click
        sdkClickHandler.sendSdkClick(secondSdkClickPackage);
        SystemClock.sleep(1000);

        // added second click package to the queue
        assertUtil.debug("Added sdk_click 2");

        assertUtil.verbose("Path:      /sdk_click\n" +
                "ClientSdk: Test-Second-Click");

        // wait two seconds before sending
        mockHttpsURLConnection.waitingTime = 2000L;

        // try to send first package
        checkSendFirstPackage(sdkClickHandler, 1);
        // and then the second
        checkSendSecondPackage(sdkClickHandler, 1);

        // try to send first package again
        checkSendFirstPackage(sdkClickHandler, 2);
        // and then the second again
        checkSendSecondPackage(sdkClickHandler, 2);
    }

    private void checkSendFirstPackage(SdkClickHandler sdkClickHandler, int retries) {
        // try to send the first package
        sdkClickHandler.resumeSending();

        SystemClock.sleep(1000);

        // prevent sending next again
        sdkClickHandler.pauseSending();

        SystemClock.sleep(2000);

        // check that it tried to send the first package
        assertUtil.test("MockHttpsURLConnection setRequestProperty, field Client-SDK, newValue Test-First-Click");

        // and that it will try to send it again
        assertUtil.error("Retrying sdk_click package for the " + retries + " time");

        // first package added again on the end of the queue
        assertUtil.debug("Added sdk_click 2");

        assertUtil.verbose("Path:      /sdk_click\n" +
                "ClientSdk: Test-First-Click");

        // does not continue to send because it was paused
        assertUtil.notInTest("MockHttpsURLConnection setRequestProperty");
    }

    private void checkSendSecondPackage(SdkClickHandler sdkClickHandler, int retries) {
        // try to send the second package that is at the start of the queue
        sdkClickHandler.resumeSending();

        SystemClock.sleep(1000);

        // prevent sending next again
        sdkClickHandler.pauseSending();

        SystemClock.sleep(2000);

        // check that it tried to send the second package
        assertUtil.test("MockHttpsURLConnection setRequestProperty, field Client-SDK, newValue Test-Second-Click");

        // and that it will try to send it again
        assertUtil.error("Retrying sdk_click package for the " + retries + " time");

        // second package added again on the end of the queue
        assertUtil.debug("Added sdk_click 2");

        assertUtil.verbose("Path:      /sdk_click\n" +
                "ClientSdk: Test-Second-Click");

        // does not continue to send because it was paused
        assertUtil.notInTest("MockHttpsURLConnection setRequestProperty");
    }

    public void testNullResponse() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestRequestHandler testNullResponse");

        AdjustFactory.setSdkClickBackoffStrategy(BackoffStrategy.NO_WAIT);

        SdkClickHandler sdkClickHandler = new SdkClickHandler(true);

        mockHttpsURLConnection.responseType = null;

        sdkClickHandler.sendSdkClick(sdkClickPackage);

        SystemClock.sleep(1000);

        assertUtil.debug("Added sdk_click 1");

        assertUtil.test("MockHttpsURLConnection getInputStream, responseType: null");

        assertUtil.error("Failed to read response. (null)");

        assertUtil.error("Failed to track click. (Sdk_click runtime exception: java.lang.NullPointerException)");

        // does not to try to retry
        assertUtil.notInError("Retrying sdk_click package for the");
        assertUtil.notInDebug("Added sdk_click");
    }

    public void testClientException() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestRequestHandler testClientException");

        AdjustFactory.setSdkClickBackoffStrategy(BackoffStrategy.NO_WAIT);

        SdkClickHandler sdkClickHandler = new SdkClickHandler(true);

        mockHttpsURLConnection.responseType = ResponseType.CLIENT_PROTOCOL_EXCEPTION;

        sdkClickHandler.sendSdkClick(sdkClickPackage);
        SystemClock.sleep(1000);

        assertUtil.test("MockHttpsURLConnection getInputStream, responseType: CLIENT_PROTOCOL_EXCEPTION");

        assertUtil.error("Failed to track click. (Sdk_click request failed. Will retry later: java.io.IOException: testResponseError)");

        // tries to retry
        assertUtil.error("Retrying sdk_click package for the 1 time");

        // adds to end of the queue
        assertUtil.debug("Added sdk_click");
    }

    public void testServerError() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestRequestHandler testServerError");

        AdjustFactory.setSdkClickBackoffStrategy(BackoffStrategy.NO_WAIT);

        SdkClickHandler sdkClickHandler = new SdkClickHandler(true);

        mockHttpsURLConnection.responseType = ResponseType.INTERNAL_SERVER_ERROR;

        sdkClickHandler.sendSdkClick(sdkClickPackage);
        SystemClock.sleep(1000);

        assertUtil.test("MockHttpsURLConnection getErrorStream, responseType: INTERNAL_SERVER_ERROR");

        assertUtil.verbose("Response: { \"message\": \"testResponseError\"}");

        assertUtil.error("testResponseError");
    }

    public void testWrongJson() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestRequestHandler testWrongJson");

        AdjustFactory.setSdkClickBackoffStrategy(BackoffStrategy.NO_WAIT);

        SdkClickHandler sdkClickHandler = new SdkClickHandler(true);

        mockHttpsURLConnection.responseType = ResponseType.WRONG_JSON;

        sdkClickHandler.sendSdkClick(sdkClickPackage);
        SystemClock.sleep(1000);

        assertUtil.test("MockHttpsURLConnection getInputStream, responseType: WRONG_JSON");

        assertUtil.verbose("Response: not a json response");

        assertUtil.error("Failed to parse json response. (Value not of type java.lang.String cannot be converted to JSONObject)");
    }

    public void testEmptyJson() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestRequestHandler testWrongJson");

        AdjustFactory.setSdkClickBackoffStrategy(BackoffStrategy.NO_WAIT);

        SdkClickHandler sdkClickHandler = new SdkClickHandler(true);

        mockHttpsURLConnection.responseType = ResponseType.EMPTY_JSON;

        sdkClickHandler.sendSdkClick(sdkClickPackage);
        SystemClock.sleep(1000);

        assertUtil.test("MockHttpsURLConnection getInputStream, responseType: EMPTY_JSON");

        assertUtil.verbose("Response: { }");

        assertUtil.info("No message found");
    }

    public void testMessage() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestRequestHandler testWrongJson");

        AdjustFactory.setSdkClickBackoffStrategy(BackoffStrategy.NO_WAIT);

        SdkClickHandler sdkClickHandler = new SdkClickHandler(true);

        mockHttpsURLConnection.responseType = ResponseType.MESSAGE;

        sdkClickHandler.sendSdkClick(sdkClickPackage);
        SystemClock.sleep(1000);

        TestActivityPackage.testQueryStringRequest(mockHttpsURLConnection.readRequest(), 0);

        assertUtil.test("MockHttpsURLConnection getInputStream, responseType: MESSAGE");

        assertUtil.verbose("Response: { \"message\" : \"response OK\"}");

        assertUtil.info("response OK");
    }
}
