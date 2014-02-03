package com.adeven.adjustio.test;

import java.util.Arrays;

import com.adeven.adjustio.AdjustIoFactory;
import com.adeven.adjustio.IPackageHandler;
import com.adeven.adjustio.Logger.LogLevel;

import android.test.ActivityInstrumentationTestCase2;

public class TestPackageHandler extends
		ActivityInstrumentationTestCase2<MainActivity> {

	protected MockLogger testLogger;
	protected MockRequestHandler testRequestHandler;
	protected MainActivity activity;
	
	public TestPackageHandler() {
		super(MainActivity.class);
	}
	
	public TestPackageHandler(Class<MainActivity> activityClass) {
		super(activityClass);
	}

	@Override protected void setUp() {
		testLogger = new MockLogger();
		testRequestHandler = new MockRequestHandler(testLogger);

		AdjustIoFactory.setLogger(testLogger);
		AdjustIoFactory.setRequestHandler(testRequestHandler);
		
		activity = getActivity();
	}
	
	@Override protected void tearDown() {
		AdjustIoFactory.setLogger(null);
		AdjustIoFactory.setRequestHandler(null);
	}
	
	public void testFirstPackage() {
		IPackageHandler packageHandler = AdjustIoFactory.getPackageHandler(activity.getApplicationContext(), false);
		
		//  simulate adding the first session package from the ActivityHandler
		TestActivityHandler.setUpFirstRun(activity);
		
		//  test that the file did not exist in the first run of the application
		assertTrue(testLogger.toString(), 
			testLogger.containsMessage(LogLevel.VERBOSE, "Package queue file not found"));
		
		//  check that added first package to a previous empty queue
		//TODO add the toString of the activity package
		assertTrue(testLogger.containsMessage(LogLevel.DEBUG, "Added package 1 ("));
		
		//TODO add the verbose message
		
		//  it should write the package queue with the first session package
		assertTrue(testLogger.toString(),
			testLogger.containsMessage(LogLevel.DEBUG, "Package handler wrote 1 packages"));
		
		//  check that the Request Handler was called to send the package
		assertTrue(testLogger.toString(),
			testLogger.containsTestMessage("RequestHandler sendPackage"));
		
		//  check that the package sent is the first one
		assertEquals(Arrays.toString(testRequestHandler.queue.toArray()),
				1, testRequestHandler.queue.get(0).getParameters().get("session_count"));
	}
}
