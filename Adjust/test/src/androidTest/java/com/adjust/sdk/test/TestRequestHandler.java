package com.adjust.sdk.test;

import android.content.Context;
import android.os.SystemClock;
import android.test.ActivityInstrumentationTestCase2;

import com.adjust.sdk.ActivityHandler;
import com.adjust.sdk.ActivityPackage;
import com.adjust.sdk.AdjustConfig;
import com.adjust.sdk.AdjustFactory;
import com.adjust.sdk.RequestHandler;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Created by pfms on 30/01/15.
 */
public class TestRequestHandler extends ActivityInstrumentationTestCase2<UnitTestActivity> {
    private MockLogger mockLogger;
    private MockPackageHandler mockPackageHandler;
    private MockHttpClient mockHttpClient;
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
        mockHttpClient = new MockHttpClient(mockLogger);

        assertUtil = new AssertUtil(mockLogger);

        AdjustFactory.setLogger(mockLogger);
        AdjustFactory.setPackageHandler(mockPackageHandler);
        AdjustFactory.setHttpClient(mockHttpClient);

        activity = getActivity();
        context = activity.getApplicationContext();

        sessionPackage = getSessionPackage();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        AdjustFactory.setPackageHandler(null);
        AdjustFactory.setLogger(null);
    }

    public void testSend() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestRequestHandler testSend");

        requestHandler = new RequestHandler(mockPackageHandler);

        sendPackageOrClickTest(false);

        sendPackageOrClickTest(true);
    }

    /*
    public void testTimeout() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestRequestHandler testTimeout");

        //mockHttpClient.timeout = true;

        AdjustFactory.setHttpClient(null);

        requestHandler = new RequestHandler(mockPackageHandler);

        requestHandler.sendPackage(sessionPackage);

        SystemClock.sleep(5000);

        requestHandler.sendClickPackage(sessionPackage);

        SystemClock.sleep(5000);
    }
    */

    private void sendPackageOrClickTest(boolean sendClick) {
        nullResponseTest(sendClick);

        clientExceptionTest(sendClick);

        serverErrorTest(sendClick);

        wrongJsonTest(sendClick);

        emptyJsonTest(sendClick);

        messageTest(sendClick);

        attributionTest(sendClick);
    }

    private void nullResponseTest(boolean sendClick) {
        mockHttpClient.responseType = null;

        sendPackageOrClick(sendClick);

        assertUtil.test("HttpClient execute, responseType: null");

        closeFirstPackage(sendClick);
    }

    private void clientExceptionTest(boolean sendClick) {
        mockHttpClient.responseType = ResponseType.CLIENT_PROTOCOL_EXCEPTION;

        sendPackageOrClick(sendClick);

        assertUtil.test("HttpClient execute, responseType: CLIENT_PROTOCOL_EXCEPTION");

        assertUtil.test("PackageHandler getFailureMessage");

        assertUtil.error("Failed to track session. (Client protocol error: org.apache.http.client.ClientProtocolException: testResponseError) Mock Failure Message.");

        closeFirstPackage(sendClick);
    }

    private void serverErrorTest(boolean sendClick) {
        mockHttpClient.responseType = ResponseType.INTERNAL_SERVER_ERROR;

        sendPackageOrClick(sendClick);

        assertUtil.test("HttpClient execute, responseType: INTERNAL_SERVER_ERROR");

        assertUtil.verbose("Response: { \"message\": \"testResponseError\"}");

        assertUtil.error("testResponseError");

        assertUtil.test("PackageHandler finishedTrackingActivity, {\"message\":\"testResponseError\"}");

        sendNextPackage(sendClick);
    }

    private void wrongJsonTest(boolean sendClick) {
        mockHttpClient.responseType = ResponseType.WRONG_JSON;

        sendPackageOrClick(sendClick);

        assertUtil.test("HttpClient execute, responseType: WRONG_JSON");

        assertUtil.verbose("Response: not a json response");

        assertUtil.error("Failed to parse json response: not a json response (Value not of type java.lang.String cannot be converted to JSONObject)");

        closeFirstPackage(sendClick);
    }

    private void emptyJsonTest(boolean sendClick) {
        mockHttpClient.responseType = ResponseType.EMPTY_JSON;

        sendPackageOrClick(sendClick);

        assertUtil.test("HttpClient execute, responseType: EMPTY_JSON");

        assertUtil.verbose("Response: { }");

        assertUtil.info("No message found");

        assertUtil.test("PackageHandler finishedTrackingActivity, {}");

        sendNextPackage(sendClick);
    }

    private void messageTest(boolean sendClick) {
        mockHttpClient.responseType = ResponseType.MESSAGE;

        sendPackageOrClick(sendClick);

        assertUtil.test("HttpClient execute, responseType: MESSAGE");

        assertUtil.verbose("Response: { \"message\" : \"response OK\"}");

        assertUtil.info("response OK");

        assertUtil.test("PackageHandler finishedTrackingActivity, {\"message\":\"response OK\"}");

        sendNextPackage(sendClick);
    }

    private void attributionTest(boolean sendClick) {
        mockHttpClient.responseType = ResponseType.ATTRIBUTION;

        sendPackageOrClick(sendClick);

        assertUtil.test("HttpClient execute, responseType: ATTRIBUTION");

        assertUtil.verbose("Response: { \"attribution\" : {\"tracker_token\" : \"ttValue\" , \"tracker_name\"  : \"tnValue\" , \"network\"       : \"nValue\" , \"campaign\"      : \"cpValue\" , \"adgroup\"       : \"aValue\" , \"creative\"      : \"ctValue\" , \"click_label\"   : \"clValue\" } }");

        assertUtil.info("No message found");

        JsonParser parser = new JsonParser();
        JsonElement e1 = parser.parse("{\"attribution\":{\"tracker_token\":\"ttValue\",\"tracker_name\":\"tnValue\",\"network\":\"nValue\",\"campaign\":\"cpValue\",\"adgroup\":\"aValue\",\"creative\":\"ctValue\",\"click_label\":\"clValue\"}}");
        JsonElement e2 = parser.parse(mockPackageHandler.jsonResponse.toString());

        assertEquals(e1, e2);

        sendNextPackage(sendClick);
    }

    private void sendPackageOrClick(boolean sendClick) {
        if (sendClick) {
            requestHandler.sendClickPackage(sessionPackage);
        } else {
            requestHandler.sendPackage(sessionPackage);
        }
        SystemClock.sleep(1000);
    }

    private void closeFirstPackage(boolean sendClick) {
        if (sendClick) {
            assertUtil.notInTest("PackageHandler closeFirstPackage");
        } else {
            assertUtil.test("PackageHandler closeFirstPackage");
        }
    }

    private void sendNextPackage(boolean sendClick) {
        if (sendClick) {
            assertUtil.notInTest("PackageHandler sendNextPackage");
        } else {
            assertUtil.test("PackageHandler sendNextPackage");
        }
    }

    private ActivityPackage getSessionPackage() {
        MockAttributionHandler mockAttributionHandler = new MockAttributionHandler(mockLogger);

        AdjustFactory.setAttributionHandler(mockAttributionHandler);

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
        activityHandler.trackSubsessionStart();
        SystemClock.sleep(3000);

        ActivityPackage sessionPackage = mockPackageHandler.queue.get(0);

        mockLogger.reset();

        return sessionPackage;
    }
}
