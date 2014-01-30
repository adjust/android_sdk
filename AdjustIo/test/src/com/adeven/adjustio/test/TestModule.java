package com.adeven.adjustio.test;

import com.adeven.adjustio.AdjustIoFactory;
import com.adeven.adjustio.IPackageHandler;
import com.adeven.adjustio.IRequestHandler;
import com.adeven.adjustio.Logger;

public class TestModule {

	public static void registerAdjustIoModule() {
		AdjustIoFactory.registerType(Logger.class, new TestLogger());
		AdjustIoFactory.registerType(IPackageHandler.class, new TestPackageHandler());
		AdjustIoFactory.registerType(IRequestHandler.class, new TestRequestHandler());
	}
	
}
