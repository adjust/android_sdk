package com.adeven.adjustio.test;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.os.SystemClock;
import android.test.ActivityInstrumentationTestCase2;

import com.adeven.adjustio.ActivityHandler;
import com.adeven.adjustio.ActivityPackage;
import com.adeven.adjustio.AdjustIoFactory;
import com.adeven.adjustio.IPackageHandler;
import com.adeven.adjustio.IRequestHandler;
import com.adeven.adjustio.Logger;
import com.adeven.adjustio.Logger.LogLevel;

public class TestActivityHandler extends ActivityInstrumentationTestCase2<MainActivity> {

	protected MockLogger testLogger;
	protected MockPackageHandler testPackageHandler;
	protected MockRequestHandler testRequestHandler;
	protected MainActivity activity;


	public TestActivityHandler(){
		super(MainActivity.class);
	}
	
	public TestActivityHandler(Class<MainActivity> mainActivity){
		super(mainActivity);
	}
	
	@Override protected void setUp() {
		testLogger = new MockLogger();
		testPackageHandler = new MockPackageHandler(testLogger);
		testRequestHandler = new MockRequestHandler(testLogger);

		AdjustIoFactory.setLogger(testLogger);
		AdjustIoFactory.setPackageHandler(testPackageHandler);
		AdjustIoFactory.setRequestHandler(testRequestHandler);
		
		activity = getActivity();
	}

	@Override protected void tearDown() {
		AdjustIoFactory.setLogger(null);
		AdjustIoFactory.setPackageHandler(null);
		AdjustIoFactory.setRequestHandler(null);
	}
	
	public void testActivityHandlerFirstSession() {
		setUpFirstRun(activity);

		// test if the environment was set to Sandbox from the bundle
		assertTrue(testLogger.toString(),
			testLogger.containsMessage(LogLevel.ASSERT, "SANDBOX: AdjustIo is running in Sandbox mode"));
		
		// test that the file did not exist in the first run of the application
		assertTrue(testLogger.toString(), 
			testLogger.containsMessage(LogLevel.VERBOSE, "Activity state file not found"));
		
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
	
	public static ActivityHandler setUpFirstRun(Activity activity) {
		
		Context context = activity.getApplicationContext();
		
		//delete Activity and Package queue files
		context.deleteFile("AdjustIoActivityState");
		context.deleteFile("AdjustIoPackageQueue");

		//throw new AssertionError(Arrays.toString(context.fileList()));
		
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
		}
		
		try {
			context.openFileInput("AdjustIoPackageQueue");
			throw new AssertionError("AdjustIoPackageQueue " + Arrays.toString(context.fileList()));
		} catch (FileNotFoundException fne) {
			/// the file should not exist
		}
		
		ActivityHandler activityHandler = new ActivityHandler(activity);
		
		activityHandler.trackSubsessionStart();
		
		return activityHandler;
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
}
