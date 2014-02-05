package com.adeven.adjustio.test;

import java.util.Arrays;

import com.adeven.adjustio.ActivityPackage;
import com.adeven.adjustio.AdjustIoFactory;
import com.adeven.adjustio.IPackageHandler;
import com.adeven.adjustio.Logger.LogLevel;
import com.adeven.adjustio.PackageBuilder;
import com.adeven.adjustio.PackageHandler;

import android.content.Context;
import android.os.SystemClock;
import android.test.ActivityInstrumentationTestCase2;

public class TestPackageHandler extends
		ActivityInstrumentationTestCase2<UnitTestActivity> {

	protected MockLogger testLogger;
	protected MockRequestHandler testRequestHandler;
	protected UnitTestActivity activity;
	
	public TestPackageHandler() {
		super(UnitTestActivity.class);
	}
	
	public TestPackageHandler(Class<UnitTestActivity> activityClass) {
		super(activityClass);
	}

	@Override protected void setUp() throws Exception {
		super.setUp();
		
		testLogger = new MockLogger();
		testRequestHandler = new MockRequestHandler(testLogger);

		AdjustIoFactory.setLogger(testLogger);
		AdjustIoFactory.setRequestHandler(testRequestHandler);
				
		activity = getActivity();
	}
	
	@Override protected void tearDown() throws Exception {
		super.tearDown();
		
		AdjustIoFactory.setRequestHandler(null);
		AdjustIoFactory.setLogger(null);
	}
	
	public void testFirstPackage() {
		Context context = activity.getApplicationContext();
		
		//  delete previously created Package queue file to make a new queue
		testLogger.test("Was AdjustIoPackageQueue deleted? " + PackageHandler.deletePackageQueue(context));
		
		//  initialize Package Handler
		PackageHandler packageHandler = new PackageHandler(context, false);
		// it's necessary to sleep the activity for a while after each handler call
		//  to let the internal queue act 
		SystemClock.sleep(1000);
		
		//  test that the file did not exist in the first run of the application
		assertTrue(testLogger.toString(), 
			testLogger.containsMessage(LogLevel.VERBOSE, "Package queue file not found"));
		
		//  enable sending packages to Request Handler
		packageHandler.resumeSending();
		
		// build and add a package the queue
		PackageBuilder builder = new PackageBuilder();
		ActivityPackage sessionPackage = builder.buildSessionPackage();
		packageHandler.addPackage(sessionPackage);
		SystemClock.sleep(1000);

		//  check that added first package to a previous empty queue
		//TODO add the toString of the activity package
		assertTrue(testLogger.toString(), 
			testLogger.containsMessage(LogLevel.DEBUG, "Added package 1 (session start)"));
		
		//TODO add the verbose message
		
		//  it should write the package queue with the first session package
		assertTrue(testLogger.toString(),
			testLogger.containsMessage(LogLevel.DEBUG, "Package handler wrote 1 packages"));
		
		//  set the package handler in the mock request handler to respond
		testRequestHandler.setPackageHandler(packageHandler);
		
		//  send the first package in the queue to the mock request handler
		packageHandler.sendFirstPackage();		
		SystemClock.sleep(1000);
		
		//  check that the Request Handler was called to send the package
		assertTrue(testLogger.toString(),
			testLogger.containsTestMessage("RequestHandler sendPackage"));
		
		//  check that the package was removed from the queue and 0 packages were written
		assertTrue(testLogger.toString(),
			testLogger.containsMessage(LogLevel.DEBUG, "Package handler wrote 0 packages"));
	}
}
