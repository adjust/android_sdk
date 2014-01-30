package com.adeven.adjustio.test;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.adeven.adjustio.ActivityPackage;
import com.adeven.adjustio.AdjustIoFactory;
import com.adeven.adjustio.Logger;
import com.adeven.adjustio.PackageHandler;
import com.adeven.adjustio.RequestHandler;

public class TestPackageHandler implements PackageHandler {
	
	private Logger logger;
	private String prefix = "TestPackageHandler ";
	public List<ActivityPackage> queue;
	
	public TestPackageHandler() {
		logger = (Logger) AdjustIoFactory.getInstance(Logger.class);
		queue = new ArrayList<ActivityPackage>();
	}
	
	@Override
	public void addPackage(ActivityPackage pack) {
		logger.debug(prefix +  "addPackage");
		queue.add(pack);
	}

	@Override
	public void sendFirstPackage() {
		logger.debug(prefix +  "sendFirstPackage");
	}

	@Override
	public void sendNextPackage() {
		logger.debug(prefix +  "sendNextPackage");
	}

	@Override
	public void closeFirstPackage() {
		logger.debug(prefix +  "closeFirstPackage");
	}

	@Override
	public void pauseSending() {
		logger.debug(prefix +  "pauseSending");
	}

	@Override
	public void resumeSending() {
		logger.debug(prefix +  "resumeSending");
	}

	@Override
	public void setContext(Context context) {
		logger.debug(prefix +  "setContext");
	}

}
