package com.adjust.sdk;

import android.content.Context;
import android.os.SystemClock;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;

import com.adjust.sdk.test.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by pfms on 30/01/15.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class TestRequestHandler {
    private MockLogger mockLogger;
    private MockPackageHandler mockPackageHandler;
    private MockHttpsURLConnection mockHttpsURLConnection;
    private AssertUtil assertUtil;
    private com.adjust.sdk.test.UnitTestActivity activity;
    private Context context;

    private ActivityPackage sessionPackage;
    private RequestHandler requestHandler;

    @Rule
    public ActivityTestRule<com.adjust.sdk.test.UnitTestActivity> mActivityRule = new ActivityTestRule(com.adjust.sdk.test.UnitTestActivity.class);

    @Before
    public void setUp() throws Exception {
        mockLogger = new MockLogger();
        mockPackageHandler = new MockPackageHandler(mockLogger);
        mockHttpsURLConnection = new MockHttpsURLConnection(null, mockLogger);

        assertUtil = new AssertUtil(mockLogger);

        AdjustFactory.setLogger(mockLogger);
        AdjustFactory.setPackageHandler(mockPackageHandler);
        AdjustFactory.setHttpsURLConnection(mockHttpsURLConnection);

        activity = mActivityRule.getActivity();
        context = activity.getApplicationContext();

        sessionPackage = getSessionPackage();
    }

    @After
    public void tearDown() throws Exception {
        AdjustFactory.setHttpsURLConnection(null);
        AdjustFactory.setPackageHandler(null);
        AdjustFactory.setLogger(null);
    }

    @Test
    public void testSend() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestRequestHandler testSend");

        requestHandler = new RequestHandler(mockPackageHandler);

        nullResponseTest();

        clientExceptionTest();

        serverErrorTest();

        wrongJsonTest();

        emptyJsonTest();

        messageTest();
    }

/* configure local test server
    public void testTimeout() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestRequestHandler testTimeout");

        mockHttpsURLConnection.responseType = ResponseType.EMPTY_JSON;

        mockHttpsURLConnection.timeout = true;

        requestHandler = new RequestHandler(mockPackageHandler);

        requestHandler.sendPackage(sessionPackage);

        SystemClock.sleep(15000);

        assertUtil.fail();
    }
*/

    private void nullResponseTest() {
        mockHttpsURLConnection.responseType = null;

        requestHandler.sendPackage(sessionPackage, -1);
        SystemClock.sleep(1000);

        assertUtil.test("MockHttpsURLConnection getInputStream, responseType: null");

        assertUtil.error("Failed to read response. (lock == null)");

        assertUtil.error("Failed to track session. (Runtime exception: java.lang.NullPointerException: lock == null)");

        assertUtil.test("PackageHandler sendNextPackage");
    }

    private void clientExceptionTest() {
        mockHttpsURLConnection.responseType = ResponseType.CLIENT_PROTOCOL_EXCEPTION;

        requestHandler.sendPackage(sessionPackage, -1);
        SystemClock.sleep(1000);

        assertUtil.test("MockHttpsURLConnection getInputStream, responseType: CLIENT_PROTOCOL_EXCEPTION");

        assertUtil.error("Failed to track session. (Request failed: java.io.IOException: testResponseError) Will retry later");

        assertUtil.test("PackageHandler closeFirstPackage");
    }

    private void serverErrorTest() {
        mockHttpsURLConnection.responseType = ResponseType.INTERNAL_SERVER_ERROR;

        requestHandler.sendPackage(sessionPackage, -1);
        SystemClock.sleep(1000);

        assertUtil.test("MockHttpsURLConnection getErrorStream, responseType: INTERNAL_SERVER_ERROR");

        assertUtil.verbose("Response: { \"message\": \"testResponseError\"}");

        assertUtil.error("testResponseError");

        assertUtil.test("PackageHandler sendNextPackage, message:testResponseError timestamp:null json:{\"message\":\"testResponseError\"}");
    }

    private void wrongJsonTest() {
        mockHttpsURLConnection.responseType = ResponseType.WRONG_JSON;

        requestHandler.sendPackage(sessionPackage, -1);
        SystemClock.sleep(1000);

        assertUtil.test("MockHttpsURLConnection getInputStream, responseType: WRONG_JSON");

        assertUtil.verbose("Response: not a json response");

        assertUtil.error("Failed to parse json response. (Value not of type java.lang.String cannot be converted to JSONObject)");

        assertUtil.test("PackageHandler closeFirstPackage");
    }

    private void emptyJsonTest() {
        mockHttpsURLConnection.responseType = ResponseType.EMPTY_JSON;

        requestHandler.sendPackage(sessionPackage, -1);
        SystemClock.sleep(1000);

        assertUtil.test("MockHttpsURLConnection getInputStream, responseType: EMPTY_JSON");

        assertUtil.verbose("Response: { }");

        assertUtil.info("No message found");

        assertUtil.test("PackageHandler sendNextPackage, message:null timestamp:null json:{}");
    }

    private void messageTest() {
        mockHttpsURLConnection.responseType = ResponseType.MESSAGE;

        requestHandler.sendPackage(sessionPackage, 1);
        SystemClock.sleep(1000);

        TestActivityPackage.testQueryStringRequest(mockHttpsURLConnection.readRequest(), 1);

        assertUtil.test("MockHttpsURLConnection getInputStream, responseType: MESSAGE");

        assertUtil.verbose("Response: { \"message\" : \"response OK\"}");

        assertUtil.info("response OK");

        assertUtil.test("PackageHandler sendNextPackage, message:response OK timestamp:null json:{\"message\":\"response OK\"}");
    }

    private ActivityPackage getSessionPackage() {
        MockAttributionHandler mockAttributionHandler = new MockAttributionHandler(mockLogger);
        MockSdkClickHandler mockSdkClickHandler = new MockSdkClickHandler(mockLogger);

        AdjustFactory.setAttributionHandler(mockAttributionHandler);
        AdjustFactory.setSdkClickHandler(mockSdkClickHandler);

        // deleting the activity state file to simulate a first session
        boolean activityStateDeleted = ActivityHandler.deleteActivityState(context);
        boolean attributionDeleted = ActivityHandler.deleteAttribution(context);

        mockLogger.test("Was AdjustActivityState deleted? " + activityStateDeleted);

        // deleting the attribution file to simulate a first session
        mockLogger.test("Was Attribution deleted? " + attributionDeleted);

        // create the config to start the session
        AdjustConfig config = new AdjustConfig(context, "123456789012", AdjustConfig.ENVIRONMENT_SANDBOX);

        // start activity handler with config
        ActivityHandler activityHandler = ActivityHandler.getInstance(config);
        activityHandler.onResume();
        SystemClock.sleep(3000);

        ActivityPackage sessionPackage = mockPackageHandler.queue.get(0);

        mockLogger.reset();

        return sessionPackage;
    }
}
