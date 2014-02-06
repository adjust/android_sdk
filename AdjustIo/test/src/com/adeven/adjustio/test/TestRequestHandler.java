package com.adeven.adjustio.test;

import org.apache.http.params.HttpParams;

import com.adeven.adjustio.ActivityPackage;
import com.adeven.adjustio.AdjustIoFactory;
import com.adeven.adjustio.Logger.LogLevel;
import com.adeven.adjustio.PackageBuilder;
import com.adeven.adjustio.RequestHandler;

import android.os.SystemClock;
import android.test.ActivityInstrumentationTestCase2;

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

	protected void setUp() throws Exception {
		super.setUp();

		mockLogger = new MockLogger();
		mockPackageHandler = new MockPackageHandler(mockLogger);
		mockHttpClient = new MockHttpClient(mockLogger);
		
		//  inject the mocks used in the request handler
		AdjustIoFactory.setLogger(mockLogger);
		AdjustIoFactory.setHttpClient(mockHttpClient);
		
		//  inject the mock package handler to our request handler
		requestHandler = new RequestHandler(mockPackageHandler);
		
		// it's necessary to sleep the activity for a while after each handler call
		//  to let the internal queue act 
		SystemClock.sleep(1000);

		// build a default session package 
		PackageBuilder builder = new PackageBuilder();
		sessionPackage = builder.buildSessionPackage();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		
		AdjustIoFactory.setHttpClient(null);
		AdjustIoFactory.setLogger(null);
	}
	
	public void testSendFirstPackage() {
		//  send a default session package
		requestHandler.sendPackage(sessionPackage);
		SystemClock.sleep(1000);
		
		//  check that the http client was called
		assertTrue(mockLogger.toString(),
			mockLogger.containsTestMessage("HttpClient execute HttpUriRequest request"));
		
		//  check the status received is ok
		assertTrue(mockLogger.toString(),
			mockLogger.containsMessage(LogLevel.INFO, "Tracked session start"));
		
		//  check that the package handler was called to send the next package
		assertTrue(mockLogger.toString(),
			mockLogger.containsTestMessage("PackageHandler sendNextPackage"));
	}
	
	public void testErrorSendPackage() {
		//  set the mock http client to throw an Exception
		mockHttpClient.setMessageError("testErrorSendPackage");
		
		//  send a default session package
		requestHandler.sendPackage(sessionPackage);
		SystemClock.sleep(1000);

		//  check that the http client was called
		assertTrue(mockLogger.toString(),
			mockLogger.containsTestMessage("HttpClient execute HttpUriRequest request"));
		
		//  check the error message
		assertTrue(mockLogger.toString(),
			mockLogger.containsMessage(LogLevel.ERROR,
				"Failed to track session start. (Client protocol error: org.apache.http.client.ClientProtocolException: testErrorSendPackage) Will retry later."));
		
		//  check that the package handler was called to close the failed package
		assertTrue(mockLogger.toString(),
			mockLogger.containsTestMessage("PackageHandler closeFirstPackage"));
	}

}
