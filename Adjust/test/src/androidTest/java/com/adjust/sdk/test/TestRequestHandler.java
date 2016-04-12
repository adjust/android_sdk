package com.adjust.sdk.test;

import android.content.Context;
import android.os.SystemClock;
import android.test.ActivityInstrumentationTestCase2;

import com.adjust.sdk.ActivityHandler;
import com.adjust.sdk.ActivityPackage;
import com.adjust.sdk.AdjustConfig;
import com.adjust.sdk.AdjustFactory;
import com.adjust.sdk.RequestHandler;

/**
 * Created by pfms on 30/01/15.
 */
public class TestRequestHandler extends ActivityInstrumentationTestCase2<UnitTestActivity> {
    private MockLogger mockLogger;
    private MockPackageHandler mockPackageHandler;
    private MockHttpsURLConnection mockHttpsURLConnection;
    private AssertUtil assertUtil;
    private UnitTestActivity activity;
    private Context context;

    private ActivityPackage sessionPackage;
    private RequestHandler requestHandler;

    public TestRequestHandler() {
        super(UnitTestActivity.class);
    }

    public TestRequestHandler(Class<UnitTestActivity> activityClass) {
        super(activityClass);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mockLogger = new MockLogger();
        mockPackageHandler = new MockPackageHandler(mockLogger);
        mockHttpsURLConnection = new MockHttpsURLConnection(null, mockLogger);

        assertUtil = new AssertUtil(mockLogger);

        AdjustFactory.setLogger(mockLogger);
        AdjustFactory.setPackageHandler(mockPackageHandler);
        AdjustFactory.setHttpsURLConnection(mockHttpsURLConnection);

        activity = getActivity();
        context = activity.getApplicationContext();

        sessionPackage = getSessionPackage();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        AdjustFactory.setHttpsURLConnection(null);
        AdjustFactory.setPackageHandler(null);
        AdjustFactory.setLogger(null);
    }

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

        assertUtil.error("Failed to read response. (null)");

        assertUtil.error("Failed to track session. (Runtime exception: java.lang.NullPointerException)");

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
