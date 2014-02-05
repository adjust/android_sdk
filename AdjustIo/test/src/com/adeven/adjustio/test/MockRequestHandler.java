package com.adeven.adjustio.test;

import java.util.ArrayList;
import java.util.List;

import com.adeven.adjustio.ActivityPackage;
import com.adeven.adjustio.AdjustIoFactory;
import com.adeven.adjustio.IPackageHandler;
import com.adeven.adjustio.IRequestHandler;
import com.adeven.adjustio.Logger;

public class MockRequestHandler implements IRequestHandler {

	private MockLogger testLogger;
	private String prefix = "RequestHandler ";
	private IPackageHandler packageHandler;
	
	public MockRequestHandler(MockLogger testLogger) {
		this.testLogger = testLogger;  
	}
	
	@Override
	public void sendPackage(ActivityPackage pack) {
		testLogger.test(prefix +  "sendPackage");
		
		//  respond successfully to the package handler
		if (packageHandler != null) {
			packageHandler.sendNextPackage();
		}
	}

	public void setPackageHandler(IPackageHandler packageHandler) {
		this.packageHandler = packageHandler;
	}
}
