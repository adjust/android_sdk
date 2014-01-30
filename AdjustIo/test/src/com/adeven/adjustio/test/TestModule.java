package com.adeven.adjustio.test;

import com.adeven.adjustio.AdjustIoFactory;
import com.adeven.adjustio.HttpRequestHandler;
import com.adeven.adjustio.LogCatLogger;
import com.adeven.adjustio.Logger;
import com.adeven.adjustio.PackageHandler;
import com.adeven.adjustio.QueuePackageHandler;
import com.adeven.adjustio.RequestHandler;

public class TestModule {

	public static void registerAdjustIoModule() {
		AdjustIoFactory.registerType(Logger.class, new TestLogger());
		AdjustIoFactory.registerType(PackageHandler.class, new TestPackageHandler());
		AdjustIoFactory.registerType(RequestHandler.class, new TestRequestHandler());
	}
	
}
