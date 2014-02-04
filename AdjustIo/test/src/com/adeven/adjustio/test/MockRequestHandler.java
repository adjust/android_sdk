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
	public List<ActivityPackage> queue;
	
	public MockRequestHandler(MockLogger testLogger) {
		this.testLogger = testLogger;  
		queue = new ArrayList<ActivityPackage>();
	}
	
	@Override
	public void sendPackage(ActivityPackage pack) {
		testLogger.test(prefix +  "sendPackage");
		queue.add(pack);
	}


}
