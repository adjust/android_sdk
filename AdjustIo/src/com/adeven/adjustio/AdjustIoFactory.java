package com.adeven.adjustio;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;

import android.content.Context;

public class AdjustIoFactory {
	private static IPackageHandler packageHandler = null;
	private static IRequestHandler requestHandler = null;
	private static Logger logger = null;
	private static HttpClient httpClient = null;
	
	private static long timerInterval = -1;
	private static long sessionInterval = -1;
	private static long subsessionInterval = -1;
	
	public static IPackageHandler getPackageHandler(Context context, boolean dropOfflineActivities) {
		if (packageHandler == null) {
			return new PackageHandler(context, dropOfflineActivities);
		}
		return packageHandler;
	}
	
	public static IRequestHandler getRequestHandler(IPackageHandler packageHandler) {
		if (requestHandler == null) {
			return new RequestHandler(packageHandler);
		}
		return requestHandler; 
	}

	public static Logger getLogger() {
		if (logger == null) {
			//  Logger needs to be "static" to retain the configuration throughout the app
			logger = new LogCatLogger();
		}
		return logger;
	}
	
	public static HttpClient getHttpClient(HttpParams params) {
		if (httpClient == null) {
			return new DefaultHttpClient(params);
		}
		return httpClient;
	}
	
	public static long getTimerInterval() {
		if (timerInterval == -1) {
			return Constants.ONE_MINUTE;
		}
		return timerInterval;
	}

	public static long getSessionInterval() {
		if (sessionInterval == -1) {
			return Constants.THIRTY_MINUTES;
		}
		return sessionInterval;
	}

	public static long getSubsessionInterval() {
		if (subsessionInterval == -1) {
			return Constants.ONE_SECOND;
		}
		return subsessionInterval;
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
	
	public static void setHttpClient(HttpClient httpClient) {
		AdjustIoFactory.httpClient = httpClient;
	}

	public static void setTimerInterval(long timerInterval) {
		AdjustIoFactory.timerInterval = timerInterval;
	}

	public static void setSessionInterval(long sessionInterval) {
		AdjustIoFactory.sessionInterval = sessionInterval;
	}

	public static void setSubsessionInterval(long subsessionInterval) {
		AdjustIoFactory.subsessionInterval = subsessionInterval;
	}
	
}
