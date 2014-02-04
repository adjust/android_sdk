package com.adeven.adjustio;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;

public class AdjustIoFactory {
	private static IPackageHandler packageHandler = null;
	private static IRequestHandler requestHandler = null;
	private static Logger logger = null;
	
	public static IPackageHandler getPackageHandler(Context context, boolean dropOfflineActivities) {
		if (packageHandler == null) {
			logger.debug("AdjustIoFactory getPackageHandler null");
			packageHandler = new PackageHandler(context, dropOfflineActivities);
		}
		return packageHandler;
	}
	
	public static IRequestHandler getRequestHandler(IPackageHandler packageHandler) {
		if (requestHandler == null) {
			logger.debug("AdjustIoFactory getRequestHandler null");
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
		logger.debug("AdjustIoFactory setPackageHandler");
		AdjustIoFactory.packageHandler = packageHandler;
	}

	public static void setRequestHandler(IRequestHandler requestHandler) {		
		logger.debug("AdjustIoFactory setRequestHandler");
		AdjustIoFactory.requestHandler = requestHandler;
	}

	public static void setLogger(Logger logger) {
		AdjustIoFactory.logger = logger;
	}
	
}
