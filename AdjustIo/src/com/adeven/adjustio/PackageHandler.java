package com.adeven.adjustio;

import android.content.Context;

public interface PackageHandler {
	public void setContext(Context context);
			
	public void addPackage(ActivityPackage pack);
	
	public void sendFirstPackage();
	
	public void sendNextPackage();
	
	public void closeFirstPackage();
	
	public void pauseSending();
	
	public void resumeSending();
	
}