package com.adeven.adjustio.test;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.os.SystemClock;
import android.test.ActivityInstrumentationTestCase2;

import com.adeven.adjustio.ActivityHandler;
import com.adeven.adjustio.ActivityPackage;
import com.adeven.adjustio.AdjustIo;
import com.adeven.adjustio.AdjustIoFactory;
import com.adeven.adjustio.IPackageHandler;
import com.adeven.adjustio.IRequestHandler;
import com.adeven.adjustio.Logger;
import com.adeven.adjustio.Logger.LogLevel;
import com.adeven.adjustio.PackageHandler;

public class TestActivityHandler extends ActivityInstrumentationTestCase2<UnitTestActivity> {

	protected MockLogger testLogger;
	protected MockPackageHandler testPackageHandler;
	protected MockRequestHandler testRequestHandler;
	protected UnitTestActivity activity;

	public TestActivityHandler(){
		super(UnitTestActivity.class);
	}
	
	public TestActivityHandler(Class<UnitTestActivity> mainActivity){
		super(mainActivity);
	}
	
	@Override protected void setUp() throws Exception {
		super.setUp();
		testLogger = new MockLogger();
		testPackageHandler = new MockPackageHandler(testLogger);

		AdjustIoFactory.setLogger(testLogger);
		AdjustIoFactory.setPackageHandler(testPackageHandler);
		
		activity = getActivity();
	}

	@Override protected void tearDown() throws Exception{
		super.tearDown();
		
		AdjustIoFactory.setPackageHandler(null);
		AdjustIoFactory.setLogger(null);
	}
	
	public void testActivityHandlerFirstSession() {
		Context context = activity.getApplicationContext();

		testLogger.test("Was AdjustIoActivityState deleted? " + ActivityHandler.deleteActivityState(context));
		
		ActivityHandler activityHandler = new ActivityHandler(activity);
		// it's necessary to sleep the activity for a while after each handler call
		//  to let the internal queue act 
		SystemClock.sleep(1000);
		
		// test if the environment was set to Sandbox from the bundle
		assertTrue(testLogger.toString(),
			testLogger.containsMessage(LogLevel.ASSERT, "SANDBOX: AdjustIo is running in Sandbox mode"));
		
		// test that the file did not exist in the first run of the application
		assertTrue(testLogger.toString(), 
			testLogger.containsMessage(LogLevel.VERBOSE, "Activity state file not found"));

		//  start the first session
		activityHandler.trackSubsessionStart();
		SystemClock.sleep(1000);

		// a new session package 
		checkPackageHandler();
		
		// TODO: test resetSessionAttributes ?

		// Write the activity state for the first time 
		assertTrue(testLogger.toString(), 
				testLogger.containsMessage(LogLevel.VERBOSE, "Wrote activity state"));

		// ending of first session 
		assertTrue(testLogger.toString(), 
				testLogger.containsMessage(LogLevel.INFO, "First session"));
	}
	
	private void checkPackageHandler() {
		// when a session package is being sent the package handler should resume sending
		assertTrue(testLogger.toString(), 
			testLogger.containsTestMessage("PackageHandler resumeSending"));
		
		// if the package was build, it was sent to the Package Handler
		assertTrue(testLogger.toString(), 
				testLogger.containsTestMessage("PackageHandler addPackage"));
		
		// checking the default values of the first session package
		//  should only have one package
		assertEquals(1, testPackageHandler.queue.size());
		
		ActivityPackage activityPackage = testPackageHandler.queue.get(0);
		
		//  check the Sdk version is being tested
		assertEquals(activityPackage.getExtendedString(),
			"android2.1.6", activityPackage.getClientSdk());
		
		Map<String, String> parameters = activityPackage.getParameters();
		
		//  check appToken? 
		//  device specific attributes: setMacShortMd5, setMacSha1, setAndroidId, setUserAgent
		//  how to test setFbAttributionId, defaultTracker?

		//  session atributes
		//   sessionCount 1, because is the first session
		assertEquals(activityPackage.getExtendedString(),
			1, Integer.parseInt(parameters.get("session_count")));
		//   subSessionCount -1, because we didn't had any subsessions yet
		//   because only values > 0 are added to parameters, therefore is not present
		assertNull(activityPackage.getExtendedString(),
			parameters.get("subsession_count"));
		//   sessionLenght -1, same as before
		assertNull(activityPackage.getExtendedString(),
			parameters.get("session_length"));
		//   timeSpent -1, same as before
		assertNull(activityPackage.getExtendedString(), 
			parameters.get("time_spent"));
		//   createdAt
		//    test diff with current now?
		//   lastInterval -1, same as before
		assertNull(activityPackage.getExtendedString(), 
			parameters.get("last_interval"));
		//   packageType should be SESSION_START
		assertEquals(activityPackage.getExtendedString(), 
			"/startup", activityPackage.getPath());
		
		
		// after adding, the activity handler ping the Package handler to send the package
		assertTrue(testLogger.toString(), 
				testLogger.containsTestMessage("PackageHandler sendFirstPackage"));
		
	}
	

	private static void deleteFiles(Context context, MockLogger testLogger) {
		//  steps needed to delete file
		//   open file in write mode in the application context to create them if it does not exist
		try {
			FileOutputStream outputStream = context.openFileOutput("AdjustIoActivityState", Context.MODE_PRIVATE);
            BufferedOutputStream bufferedStream = new BufferedOutputStream(outputStream);
            ObjectOutputStream objectStream = new ObjectOutputStream(bufferedStream);
            objectStream.writeObject("invalid object");
		} catch (FileNotFoundException fnf) {
			testLogger.test("AdjustIoPackageQueue did not exist begore");
		} catch (Exception e) {
			throw new AssertionError(e.getMessage());
		}
		testLogger.test("file List before deleting " + Arrays.toString(context.fileList()));
		
		//   delete the file from the application context
		Boolean fileDeleted = context.deleteFile("AdjustIoActivityState");
		testLogger.test("Was AdjustIoActivityState deleted? " + fileDeleted);
		
		try {
			FileOutputStream outputStream = context.openFileOutput("AdjustIoPackageQueue", Context.MODE_PRIVATE);
            BufferedOutputStream bufferedStream = new BufferedOutputStream(outputStream);
            ObjectOutputStream objectStream = new ObjectOutputStream(bufferedStream);
            objectStream.writeObject("invalid object");
		} catch (FileNotFoundException fnf) {
			testLogger.test("AdjustIoPackageQueue did not exist begore");
		} catch (Exception e) {
			throw new AssertionError(e.getMessage());
		}
		fileDeleted = context.deleteFile("AdjustIoPackageQueue");
		testLogger.test("Was AdjustIoPackageQueue deleted? " + fileDeleted);

		testLogger.test("file List after deleting " + Arrays.toString(context.fileList()));
		
		for (String fileName : context.fileList()) {
			if (fileName == "AdjustIoActivityState") {
				throw new AssertionError("AdjustIoActivityState " + Arrays.toString(context.fileList()));
			}
			
			if (fileName == "AdjustIoPackageQueue") {
				throw new AssertionError("AdjustIoPackageQueue " + Arrays.toString(context.fileList()));
			}
		}
		
		try {
			context.openFileInput("AdjustIoActivityState");
			throw new AssertionError("AdjustIoActivityState " + Arrays.toString(context.fileList()));
		} catch (FileNotFoundException fne) {
			// the file should not exist
			testLogger.test("AdjustIoActivityState not found");
		}
		
		try {
			context.openFileInput("AdjustIoPackageQueue");
			throw new AssertionError("AdjustIoPackageQueue " + Arrays.toString(context.fileList()));
		} catch (FileNotFoundException fne) {
			/// the file should not exist
			testLogger.test("AdjustIoPackageQueue not found");
		}
	}
}
