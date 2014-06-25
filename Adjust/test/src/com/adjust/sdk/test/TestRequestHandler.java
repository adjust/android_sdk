package com.adjust.sdk.test;

import android.content.Context;
import android.os.SystemClock;
import android.test.ActivityInstrumentationTestCase2;

import com.adjust.sdk.ActivityPackage;
import com.adjust.sdk.AdjustFactory;
import com.adjust.sdk.Logger.LogLevel;
import com.adjust.sdk.PackageBuilder;
import com.adjust.sdk.RequestHandler;

public class TestRequestHandler extends ActivityInstrumentationTestCase2<UnitTestActivity> {

    protected MockLogger mockLogger;
    protected MockPackageHandler mockPackageHandler;
    protected MockHttpClient mockHttpClient;

    protected RequestHandler requestHandler;
    protected ActivityPackage sessionPackage;

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

        // inject the mocks used in the request handler
        AdjustFactory.setLogger(mockLogger);
        AdjustFactory.setHttpClient(mockHttpClient);

        // inject the mock package handler to our request handler
        requestHandler = new RequestHandler(mockPackageHandler);

        // it's necessary to sleep the activity for a while after each handler call
        // to let the internal queue act
        SystemClock.sleep(1000);

        Context context = getActivity().getApplicationContext();

        // build a default session package
        PackageBuilder builder = new PackageBuilder(context);
        sessionPackage = builder.buildSessionPackage();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        AdjustFactory.setHttpClient(null);
        AdjustFactory.setLogger(null);
    }

    public void testSendFirstPackage() {
        // send a default session package
        requestHandler.sendPackage(sessionPackage);
        SystemClock.sleep(1000);

        // check that the http client was called
        assertTrue(mockLogger.toString(),
            mockLogger.containsTestMessage("HttpClient execute HttpUriRequest request"));

        // check the status received is ok
        assertTrue(mockLogger.toString(),
            mockLogger.containsMessage(LogLevel.INFO, "Tracked session"));

        // calls delegate
        assertTrue(mockLogger.toString(),
                mockLogger.containsTestMessage("PackageHandler finishedTrackingActivity"));

        // check the response data
        assertTrue(mockLogger.toString(),
                mockLogger.containsTestMessage("[kind:unknown success:true willRetry:false error:null trackerToken:token trackerName:name network:network campaign:campaign adgroup:adgroup creative:creative]"));

        // check that the package handler was called to send the next package
        assertTrue(mockLogger.toString(),
            mockLogger.containsTestMessage("PackageHandler sendNextPackage"));
    }

    public void testErrorSendPackage() {
        // set the mock http client to throw an Exception
        mockHttpClient.setMessageError("testErrorSendPackage");

        // send a default session package
        requestHandler.sendPackage(sessionPackage);
        SystemClock.sleep(1000);

        // check that the http client was called
        assertTrue(mockLogger.toString(),
            mockLogger.containsTestMessage("HttpClient execute HttpUriRequest request"));

        // check the error message
        assertTrue(mockLogger.toString(),
            mockLogger.containsMessage(LogLevel.ERROR,
                "Failed to track session. (Client protocol error: org.apache.http.client.ClientProtocolException: testErrorSendPackage) Will retry later."));

        // check that the package handler was called to close the failed package
        assertTrue(mockLogger.toString(),
            mockLogger.containsTestMessage("PackageHandler closeFirstPackage"));
    }

    public void testResponseError() {
        // set the mock http client to send an error response
        mockHttpClient.setResponseError("testResponseError");

        // send a default session package
        requestHandler.sendPackage(sessionPackage);
        SystemClock.sleep(1000);

        // check that the http client was called
        assertTrue(mockLogger.toString(),
            mockLogger.containsTestMessage("HttpClient execute HttpUriRequest request"));

        // check the status received is not ok
        assertTrue(mockLogger.toString(),
            mockLogger.containsMessage(LogLevel.ERROR, "Failed to track session. (testResponseError)"));

        // calls delegate
        assertTrue(mockLogger.toString(),
                mockLogger.containsTestMessage("PackageHandler finishedTrackingActivity"));

        // check the response data
        assertTrue(mockLogger.toString(),
                mockLogger.containsTestMessage("[kind:unknown success:false willRetry:false error:testResponseError trackerToken:null trackerName:null network:null campaign:null adgroup:null creative:null]"));

        // check that the package handler was called to send the next package
        assertTrue(mockLogger.toString(),
            mockLogger.containsTestMessage("PackageHandler sendNextPackage"));


    }

}
