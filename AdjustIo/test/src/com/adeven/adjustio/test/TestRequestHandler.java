package com.adeven.adjustio.test;

import java.util.List;

import com.adeven.adjustio.ActivityPackage;
import com.adeven.adjustio.IPackageHandler;
import com.adeven.adjustio.IRequestHandler;
import com.adeven.adjustio.Logger;

public class TestRequestHandler implements IRequestHandler {

	private Logger logger;
	private String prefix = "TestRequestHandler ";
	public List<ActivityPackage> queue;
	
	@Override
	public void sendPackage(ActivityPackage pack) {
		logger.debug(prefix +  "sendPackage");
		queue.add(pack);
	}

	@Override
	public void setPackageHandler(IPackageHandler packageHandler) {
		logger.debug(prefix +  "setPackageHandler");
	}

}
