package com.adeven.adjustio.test;

import android.content.Context;
import android.os.SystemClock;
import android.test.ActivityInstrumentationTestCase2;

import com.adeven.adjustio.ActivityHandler;
import com.adeven.adjustio.AdjustIoFactory;
import com.adeven.adjustio.Logger;
import com.adeven.adjustio.PackageHandler;
import com.adeven.adjustio.RequestHandler;

public class FirstRunTest extends ActivityInstrumentationTestCase2<MainActivity> {

	protected TestLogger testLogger;
	protected ActivityHandler activityHandler;
	protected MainActivity activity;


	public FirstRunTest(){
		super(MainActivity.class);
	}
	
	public FirstRunTest(Class<MainActivity> mainActivity){
		super(mainActivity);
	}
	
	@Override protected void setUp() {
		TestModule.registerAdjustIoModule();
		AdjustIoFactory.setLocked(true);
		
		testLogger = (TestLogger) AdjustIoFactory.getInstance(Logger.class);
		
		activity = getActivity();
		//activity.onCreate(new Bundle());
		Context context = activity.getApplicationContext();
		contextPreConditions(context);
		factoryPreCondition();
		
		activityHandler = new ActivityHandler(activity);
		
		// wait for the background threads do their work
		SystemClock.sleep(3000);
		
		activityHandler.trackSubsessionStart();
	}
	
	protected void contextPreConditions(Context context) {
		//delete Activity and Package queue files
		context.deleteFile("AdjustIoActivityState");
		context.deleteFile("AdjustIoPackageQueue");
	}
	
	protected void factoryPreCondition(){
		assertNotNull(AdjustIoFactory.getInstance(Logger.class));
		assertNotNull(AdjustIoFactory.getInstance(RequestHandler.class));
		assertNotNull(AdjustIoFactory.getInstance(PackageHandler.class));
	}
	
	@Override protected void tearDown() {
		AdjustIoFactory.setLocked(false);
	}

	public void testEnvironment() {
		// test if the environment was set to Sandbox from the bundle
		assertTrue(testLogger.toString(), 
			testLogger.containsMessage("SANDBOX: AdjustIo is running in Sandbox mode"));
	}

	public void testFileNotFound() {
		// test that the file did not exist in the first run of the application
		assertTrue(testLogger.toString(), 
			testLogger.containsMessage("Activity state file not found"));
	}	

	public void testSendFirstSession() {
		// when a session package is being sent the package handler should resume sending
		assertTrue(testLogger.toString(), 
			testLogger.containsMessage("TestPackageHandler resumeSending"));
		
		// a new session package 
		checkPackage();
		
		// the package is sent through the package handler
		assertTrue(testLogger.toString(), 
				testLogger.containsMessage("TestPackageHandler sendFirstPackage"));
		
		
		// TODO: test resetSessionAttributes ?

		// Write the activity state for the first time 
		assertTrue(testLogger.toString(), 
				testLogger.containsMessage("Wrote activity state"));

		// ending of first session 
		assertTrue(testLogger.toString(), 
				testLogger.containsMessage("First session"));
	}
	
	private void checkPackage() {
		assertTrue(testLogger.toString(), 
				testLogger.containsMessage("TestPackageHandler addPackage"));
	}
}
