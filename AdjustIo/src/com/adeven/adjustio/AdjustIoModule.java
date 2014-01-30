package com.adeven.adjustio;

public class AdjustIoModule  {
	
	public static void registerAdjustIoModule() {
		AdjustIoFactory.registerType(Logger.class, new LogCatLogger());
		AdjustIoFactory.registerType(PackageHandler.class, new QueuePackageHandler());
		AdjustIoFactory.registerType(RequestHandler.class, new HttpRequestHandler());
	}
	
}
