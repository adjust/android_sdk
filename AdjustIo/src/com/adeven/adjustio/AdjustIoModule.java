package com.adeven.adjustio;

public class AdjustIoModule  {
	
	public static void registerAdjustIoModule() {
		AdjustIoFactory.registerType(Logger.class, new LogCatLogger());
		AdjustIoFactory.registerType(IPackageHandler.class, new PackageHandler());
		AdjustIoFactory.registerType(IRequestHandler.class, new RequestHandler());
	}
	
}
