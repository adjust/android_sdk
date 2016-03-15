package com.adjust.sdk.test;

import android.content.Context;
import android.os.SystemClock;
import android.test.ActivityInstrumentationTestCase2;

import com.adjust.sdk.ActivityHandler;
import com.adjust.sdk.ActivityPackage;
import com.adjust.sdk.AdjustConfig;
import com.adjust.sdk.AdjustFactory;
import com.adjust.sdk.AttributionHandler;
import com.adjust.sdk.ResponseData;
import com.adjust.sdk.SessionResponseData;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by pfms on 28/01/15.
 */
public class TestAttributionHandler extends ActivityInstrumentationTestCase2<UnitTestActivity> {
    private MockLogger mockLogger;
    private MockActivityHandler mockActivityHandler;
    private MockHttpsURLConnection mockHttpsURLConnection;
    private AssertUtil assertUtil;
    private UnitTestActivity activity;
    private Context context;
    private ActivityPackage attributionPackage;
    private ActivityPackage firstSessionPackage;

    public TestAttributionHandler() {
        super(UnitTestActivity.class);
    }

    public TestAttributionHandler(Class<UnitTestActivity> activityClass) {
        super(activityClass);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mockLogger = new MockLogger();
        mockActivityHandler = new MockActivityHandler(mockLogger);
        mockHttpsURLConnection = new MockHttpsURLConnection(null, mockLogger);

        assertUtil = new AssertUtil(mockLogger);

        AdjustFactory.setLogger(mockLogger);
        AdjustFactory.setActivityHandler(mockActivityHandler);
        AdjustFactory.setMockHttpsURLConnection(mockHttpsURLConnection);

        activity = getActivity();
        context = activity.getApplicationContext();

        savePackages();

        mockLogger.reset();
    }

    private void savePackages() {
        MockAttributionHandler mockAttributionHandler = new MockAttributionHandler(mockLogger);
        MockPackageHandler mockPackageHandler = new MockPackageHandler(mockLogger);

        AdjustFactory.setAttributionHandler(mockAttributionHandler);
        AdjustFactory.setPackageHandler(mockPackageHandler);

        // deleting the activity state file to simulate a first session
        boolean activityStateDeleted = ActivityHandler.deleteActivityState(context);
        boolean attributionDeleted = ActivityHandler.deleteAttribution(context);

        // create the config to start the session
        AdjustConfig config = new AdjustConfig(context, "123456789012", AdjustConfig.ENVIRONMENT_SANDBOX);

        // start activity handler with config
        ActivityHandler activityHandler = ActivityHandler.getInstance(config);

        if (activityHandler != null) {
            activityHandler.trackSubsessionStart();
        }

        SystemClock.sleep(3000);

        ActivityPackage attributionPackage = activityHandler.getAttributionPackage();

        TestActivityPackage attributionPackageTest = new TestActivityPackage(attributionPackage);

        attributionPackageTest.testAttributionPackage();

        this.firstSessionPackage = mockPackageHandler.queue.get(0);

        this.attributionPackage = attributionPackage;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        AdjustFactory.setMockHttpsURLConnection(null);
        AdjustFactory.setActivityHandler(null);
        AdjustFactory.setLogger(null);

        activity = null;
        context = null;
    }

    public void testGetAttribution() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestAttributionHandler testGetAttribution");

        AttributionHandler attributionHandler = new AttributionHandler(mockActivityHandler,
                attributionPackage, false, true);

        // test null client
        nullClientTest(attributionHandler);

        // test client exception
        clientExceptionTest(attributionHandler);

        // test wrong json response
        wrongJsonTest(attributionHandler);

        // test empty response
        emptyJsonResponseTest(attributionHandler);

        // test server error
        serverErrorTest(attributionHandler);

        // test ok response with message
        okMessageTest(attributionHandler);

    }

    public void testCheckSessionResponse() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestAttributionHandler testCheckSessionResponse");

        AttributionHandler attributionHandler = new AttributionHandler(mockActivityHandler,
                attributionPackage, false, true);

        // new attribution
        JSONObject attributionJson = null;
        try {
            attributionJson = new JSONObject("{ " +
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

        SessionResponseData sessionResponseData = (SessionResponseData) ResponseData.buildResponseData(firstSessionPackage);
        sessionResponseData.jsonResponse = attributionJson;

        attributionHandler.checkSessionResponse(sessionResponseData);

        SystemClock.sleep(1000);

        // updated set askingAttribution to false
        assertUtil.test("ActivityHandler setAskingAttribution, false");

        // it did not update to true
        assertUtil.notInTest("ActivityHandler setAskingAttribution, true");

        // and waiting for query
        assertUtil.notInDebug("Waiting to query attribution");

        // check attribution was called without ask_in
        assertUtil.test("ActivityHandler launchSessionResponseTasks, message:null timestamp:null json:{\"tracker_token\":\"ttValue\",\"tracker_name\":\"tnValue\",\"network\":\"nValue\",\"campaign\":\"cpValue\",\"adgroup\":\"aValue\",\"creative\":\"ctValue\",\"click_label\":\"clValue\"}");
    }

    public void testAskIn() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestAttributionHandler testAskIn");

        AttributionHandler attributionHandler = new AttributionHandler(mockActivityHandler,
                attributionPackage, false, true);

        String response = "Response: { \"ask_in\" : 4000 }";

        JSONObject askIn4sJson = null;
        try {
            askIn4sJson = new JSONObject("{ \"ask_in\" : 4000 }");
        } catch (JSONException e) {
            fail(e.getMessage());
        }

        mockHttpsURLConnection.responseType = ResponseType.MESSAGE;

        SessionResponseData sessionResponseData = (SessionResponseData)ResponseData.buildResponseData(firstSessionPackage);
        sessionResponseData.jsonResponse = askIn4sJson;

        attributionHandler.checkSessionResponse(sessionResponseData);;

        // sleep enough not to trigger the timer
        SystemClock.sleep(1000);

        // change the response to avoid a cycle;
        mockHttpsURLConnection.responseType = ResponseType.MESSAGE;

        // check attribution was called with ask_in
        assertUtil.notInTest("ActivityHandler updateAttribution");

        // it did update to true
        assertUtil.test("ActivityHandler setAskingAttribution, true");

        // and waited to for query
        assertUtil.debug("Waiting to query attribution in 4000 milliseconds");

        SystemClock.sleep(2000);

        JSONObject askIn5sJson = null;
        try {
            askIn5sJson = new JSONObject("{ \"ask_in\" : 5000 }");
        } catch (JSONException e) {
            fail(e.getMessage());
        }

        sessionResponseData.jsonResponse = askIn5sJson;
        attributionHandler.checkSessionResponse(sessionResponseData);

        // sleep enough not to trigger the old timer
        SystemClock.sleep(3000);

        // it did update to true
        assertUtil.test("ActivityHandler setAskingAttribution, true");

        // and waited to for query
        assertUtil.debug("Waiting to query attribution in 5000 milliseconds");

        // it was been waiting for 1000 + 2000 + 3000 = 6 seconds
        // check that the mock http client was not called because the original clock was reseted
        assertUtil.notInTest("HttpClient execute");

        // check that it was finally called after 6 seconds after the second ask_in
        SystemClock.sleep(4000);

        okMessageTestLogs();

        //requestTest(mockHttpClient.lastRequest);
    }

    public void testPause() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestAttributionHandler testPause");

        AttributionHandler attributionHandler = new AttributionHandler(mockActivityHandler,
                attributionPackage, true, true);

        mockHttpsURLConnection.responseType = ResponseType.MESSAGE;

        attributionHandler.getAttribution();

        SystemClock.sleep(1000);

        // check that the activity handler is paused
        assertUtil.debug("Attribution handler is paused");

        // and it did not call the http client
        //assertUtil.isNull(mockHttpClient.lastRequest);

        assertUtil.notInTest("MockHttpsURLConnection getInputStream");
    }

    public void testWithoutListener() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestAttributionHandler testPause");

        AttributionHandler attributionHandler = new AttributionHandler(mockActivityHandler,
                attributionPackage, false, false);

        mockHttpsURLConnection.responseType = ResponseType.MESSAGE;

        attributionHandler.getAttribution();

        SystemClock.sleep(1000);

        // check that the activity handler is not paused
        assertUtil.notInDebug("Attribution handler is paused");

        // but it did not call the http client
        //assertUtil.isNull(mockHttpClient.lastRequest);

        assertUtil.notInTest("MockHttpsURLConnection getInputStream");
    }

    private void nullClientTest(AttributionHandler attributionHandler) {
        startGetAttributionTest(attributionHandler, null);

        // check response was not logged
        assertUtil.notInVerbose("Response");
    }

    private void clientExceptionTest(AttributionHandler attributionHandler) {
        startGetAttributionTest(attributionHandler, ResponseType.CLIENT_PROTOCOL_EXCEPTION);

        // check the client error
        assertUtil.error("Failed to get attribution (testResponseError)");
    }

    private void wrongJsonTest(AttributionHandler attributionHandler) {
        startGetAttributionTest(attributionHandler, ResponseType.WRONG_JSON);

        // check that the mock http client was called
        assertUtil.test("MockHttpsURLConnection getInputStream");

        assertUtil.verbose("Response: not a json response");

        assertUtil.error("Failed to parse json response. (Value not of type java.lang.String cannot be converted to JSONObject)");
    }

    private void emptyJsonResponseTest(AttributionHandler attributionHandler) {
        startGetAttributionTest(attributionHandler, ResponseType.EMPTY_JSON);

        // check that the mock http client was called
        assertUtil.test("MockHttpsURLConnection getInputStream");

        assertUtil.verbose("Response: { }");

        assertUtil.info("No message found");

        // check attribution was called without ask_in
        assertUtil.test("ActivityHandler setAskingAttribution, false");

        assertUtil.test("ActivityHandler launchAttributionResponseTasks, message:null timestamp:null json:{}");
    }

    private void serverErrorTest(AttributionHandler attributionHandler) {
        startGetAttributionTest(attributionHandler, ResponseType.INTERNAL_SERVER_ERROR);

        // check that the mock http client was called
        assertUtil.test("MockHttpsURLConnection getErrorStream");

        // the response logged
        assertUtil.verbose("Response: { \"message\": \"testResponseError\"}");

        // the message in the response
        assertUtil.error("testResponseError");

        // check attribution was called without ask_in
        assertUtil.test("ActivityHandler setAskingAttribution, false");

        assertUtil.test("ActivityHandler launchAttributionResponseTasks, message:testResponseError timestamp:null json:{\"message\":\"testResponseError\"}");
    }

    private void okMessageTest(AttributionHandler attributionHandler) {
        startGetAttributionTest(attributionHandler, ResponseType.MESSAGE);

        okMessageTestLogs();
    }

    private void okMessageTestLogs() {
        // check that the mock http client was called
        assertUtil.test("MockHttpsURLConnection getInputStream");

        // the response logged
        assertUtil.verbose("Response: { \"message\" : \"response OK\"}");

        // the message in the response
        assertUtil.info("response OK");

        assertUtil.test("ActivityHandler setAskingAttribution, false");

        // check attribution was called without ask_in
        assertUtil.test("ActivityHandler launchAttributionResponseTasks, message:response OK timestamp:null json:{\"message\":\"response OK\"}");
    }

    private void startGetAttributionTest(AttributionHandler attributionHandler, ResponseType responseType) {
        mockHttpsURLConnection.responseType = responseType;

        attributionHandler.getAttribution();

        SystemClock.sleep(1000);
    }
}
