package com.adeven.adjustio;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;

public class AdjustIoFactory {
	private static IPackageHandler packageHandler;
	private static IRequestHandler requestHandler;
	private static Logger logger;
	
	public static IPackageHandler getPackageHandler(Context context, boolean dropOfflineActivities) {
		if (packageHandler == null) {
			packageHandler = new PackageHandler(context, dropOfflineActivities);
		}
		return packageHandler;
	}
	
	public static IRequestHandler getRequestHandler(IPackageHandler packageHandler) {
		if (requestHandler == null) {
			requestHandler = new RequestHandler(packageHandler);
		}
		return requestHandler; 
	}

	public static Logger getLogger() {
		if (logger == null) {
			logger = new LogCatLogger();
		}
		
		return logger;
	}
	
	public static void setPackageHandler(IPackageHandler packageHandler) {
		AdjustIoFactory.packageHandler = packageHandler;
	}

	public static void setRequestHandler(IRequestHandler requestHandler) {
		AdjustIoFactory.requestHandler = requestHandler;
	}

	public static void setLogger(Logger logger) {
		AdjustIoFactory.logger = logger;
	}
	
}
