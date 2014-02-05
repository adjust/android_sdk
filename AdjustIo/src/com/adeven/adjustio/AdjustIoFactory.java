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
	private static HttpParams httpParams = null;
	
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
		AdjustIoFactory.httpParams = params;
		return httpClient;
	}
	
	public static HttpParams getHttpParams() {
		return httpParams;
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
	
}
