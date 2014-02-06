package com.adeven.adjustio.test;

import java.util.Arrays;

import com.adeven.adjustio.ActivityPackage;
import com.adeven.adjustio.AdjustIoFactory;
import com.adeven.adjustio.IPackageHandler;
import com.adeven.adjustio.Logger.LogLevel;
import com.adeven.adjustio.PackageBuilder;
import com.adeven.adjustio.PackageHandler;
import com.adeven.adjustio.RequestHandler;

import android.content.Context;
import android.os.SystemClock;
import android.test.ActivityInstrumentationTestCase2;

public class TestPackageHandler extends
		ActivityInstrumentationTestCase2<UnitTestActivity> {

	protected MockLogger mockLogger;
	protected MockRequestHandler mockRequestHandler;
	protected Context context;
	
	public TestPackageHandler() {
		super(UnitTestActivity.class);
	}
	
	public TestPackageHandler(Class<UnitTestActivity> activityClass) {
		super(activityClass);
	}

	@Override protected void setUp() throws Exception {
		super.setUp();
		
		mockLogger = new MockLogger();
		mockRequestHandler = new MockRequestHandler(mockLogger);

		AdjustIoFactory.setLogger(mockLogger);
		AdjustIoFactory.setRequestHandler(mockRequestHandler);
				
		context = getActivity().getApplicationContext();
	}
	
	@Override protected void tearDown() throws Exception {
		super.tearDown();
		
		AdjustIoFactory.setRequestHandler(null);
		AdjustIoFactory.setLogger(null);
	}
	
	public void testFirstPackage() {
		//  delete previously created Package queue file to make a new queue
		mockLogger.test("Was AdjustIoPackageQueue deleted? " + PackageHandler.deletePackageQueue(context));
		
		//  initialize Package Handler
		PackageHandler packageHandler = new PackageHandler(context, false);
		// it's necessary to sleep the activity for a while after each handler call
		//  to let the internal queue act 
		SystemClock.sleep(1000);
		
		//  test that the file did not exist in the first run of the application
		assertTrue(mockLogger.toString(), 
			mockLogger.containsMessage(LogLevel.VERBOSE, "Package queue file not found"));
		
		//  enable sending packages to Request Handler
		packageHandler.resumeSending();
		
		// build and add a package the queue
		PackageBuilder builder = new PackageBuilder();
		ActivityPackage sessionPackage = builder.buildSessionPackage();
		packageHandler.addPackage(sessionPackage);
		SystemClock.sleep(1000);

		//  check that added first package to a previous empty queue
		//TODO add the toString of the activity package
		assertTrue(mockLogger.toString(), 
			mockLogger.containsMessage(LogLevel.DEBUG, "Added package 1 (session start)"));
		
		//TODO add the verbose message
		
		//  it should write the package queue with the first session package
		assertTrue(mockLogger.toString(),
			mockLogger.containsMessage(LogLevel.DEBUG, "Package handler wrote 1 packages"));
		
		//  set the package handler in the mock request handler to respond
		mockRequestHandler.setPackageHandler(packageHandler);
		
		//  send the first package in the queue to the mock request handler
		packageHandler.sendFirstPackage();		
		SystemClock.sleep(1000);
		
		//  check that the Request Handler was called to send the package
		assertTrue(mockLogger.toString(),
			mockLogger.containsTestMessage("RequestHandler sendPackage"));
		
		//  check that the package was removed from the queue and 0 packages were written
		assertTrue(mockLogger.toString(),
			mockLogger.containsMessage(LogLevel.DEBUG, "Package handler wrote 0 packages"));
	}
	
	public void testPause() {
		//  initialize Package Handler
		PackageHandler packageHandler = new PackageHandler(context, false);
		SystemClock.sleep(1000);

		//  disable sending packages to Request Handler
		packageHandler.pauseSending();
		SystemClock.sleep(1000);

		// build and add a package the queue
		PackageBuilder builder = new PackageBuilder();
		ActivityPackage sessionPackage = builder.buildSessionPackage();
		packageHandler.addPackage(sessionPackage);
		SystemClock.sleep(1000);
		
		//  check that a package was added
		assertTrue(mockLogger.toString(), 
			mockLogger.containsMessage(LogLevel.DEBUG, "Added package "));

		//  set the package handler in the mock request handler to verify if it was called
		mockRequestHandler.setPackageHandler(packageHandler);
		
		//  try to send the first package in the queue to the mock request handler
		packageHandler.sendFirstPackage();		
		SystemClock.sleep(1000);

		//  check that the mock request handler was NOT called to send the package
		assertTrue(mockLogger.toString(),
			mockLogger.doesNotContain("RequestHandler sendPackage"));
		
		//  check that the package handler is paused
		assertTrue(mockLogger.toString(),
			mockLogger.containsMessage(LogLevel.DEBUG, "Package handler is paused"));
	}
	
	public void testDropOfflineActivities() {
		//  initialize Package Handler with 
		PackageHandler packageHandler = new PackageHandler(context, true);
		SystemClock.sleep(1000);
		
		// check that it did NOT try to read the package queue file
		assertTrue(mockLogger.toString(),
			mockLogger.doesNotContain("Package handler read "));
		assertTrue(mockLogger.toString(),
			mockLogger.doesNotContain("Package queue file not found"));
		assertTrue(mockLogger.toString(),
			mockLogger.doesNotContain("Failed to"));
		
		//  enable sending packages to Request Handler
		packageHandler.resumeSending();
		
		// build and add a package the queue
		PackageBuilder builder = new PackageBuilder();
		ActivityPackage sessionPackage = builder.buildSessionPackage();
		packageHandler.addPackage(sessionPackage);
		SystemClock.sleep(1000);
		
		// we'll add 2 packages so we can later see the second being sent
		packageHandler.addPackage(sessionPackage);
		SystemClock.sleep(1000);
		
		//  check that it did NOT try to write the package queue file
		assertTrue(mockLogger.toString(),
			mockLogger.doesNotContain("Package handler wrote"));
		assertTrue(mockLogger.toString(),
			mockLogger.doesNotContain("Failed to serialize packages"));
		assertTrue(mockLogger.toString(),
			mockLogger.doesNotContain("Failed to write packages"));
		
		//  set the mock request handler to simulate an error sending the package
		mockRequestHandler.setPackageHandler(packageHandler);
		mockRequestHandler.setErrorNextSend(true);

		//  try to send the first package in the queue to the mock request handler
		packageHandler.sendFirstPackage();		
		SystemClock.sleep(1000);
		
		//  check that the Request Handler was called to send the first package
		assertTrue(mockLogger.toString(),
			mockLogger.containsTestMessage("RequestHandler sendPackage"));
		
		//  check the failure message for the package handler was called
		assertTrue(mockLogger.toString(),
			mockLogger.containsTestMessage("Dropping offline activity"));
		
		//  check that the Request Handler was called to send the second package
		assertTrue(mockLogger.toString(),
			mockLogger.containsTestMessage("RequestHandler sendPackage"));
	}
	
	public void testMultiplePackages() {		
		//  delete previously created Package queue file to make a new queue
		mockLogger.test("Was AdjustIoPackageQueue deleted? " + PackageHandler.deletePackageQueue(context));

		//  initialize Package Handler
		PackageHandler packageHandler = new PackageHandler(context, false);
		SystemClock.sleep(1000);

		//  test that the file did not exist in the first run of the application
		assertTrue(mockLogger.toString(), 
			mockLogger.containsMessage(LogLevel.VERBOSE, "Package queue file not found"));

		//  enable sending packages to Request Handler
		packageHandler.resumeSending();
		
		// build and add 3 packages the queue
		PackageBuilder builder = new PackageBuilder();
		ActivityPackage sessionPackage = builder.buildSessionPackage();

		packageHandler.addPackage(sessionPackage);		
		packageHandler.addPackage(sessionPackage);
		packageHandler.addPackage(sessionPackage);
		SystemClock.sleep(1000);

		//  check that added the third package to the queue and wrote to a file
		assertTrue(mockLogger.toString(), 
			mockLogger.containsMessage(LogLevel.DEBUG, "Added package 3"));
		assertTrue(mockLogger.toString(),
			mockLogger.containsMessage(LogLevel.DEBUG, "Package handler wrote 3 packages"));
		
		//  try to send two packages without closing the first
		packageHandler.sendFirstPackage();
		packageHandler.sendFirstPackage();
		SystemClock.sleep(1000);
		
		//  check that the package handler was already sending one package before
		assertTrue(mockLogger.toString(),
			mockLogger.containsMessage(LogLevel.VERBOSE, "Package handler is already sending"));
		
		//  create a new package handler to simulate a new launch
		packageHandler = new PackageHandler(context, false);
		SystemClock.sleep(1000);
		
		//  check that it reads the same 3 packages in the file
		assertTrue(mockLogger.toString(),
			mockLogger.containsMessage(LogLevel.DEBUG, "Package handler read 3 packages"));
	}
}
