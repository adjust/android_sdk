package com.adeven.adjustio.test;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.adeven.adjustio.ActivityPackage;
import com.adeven.adjustio.AdjustIoFactory;
import com.adeven.adjustio.Logger;
import com.adeven.adjustio.IPackageHandler;

public class TestPackageHandler implements IPackageHandler {
	
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
	public void setConstructorArguments(Context context,
			boolean dropOfflineActivities) {
		logger.debug(prefix +  "setConstructorArguments");
	}

	@Override
	public String getFailureMessage() {
		logger.debug(prefix +  "getFailureMessage");
		return null;
	}

}
