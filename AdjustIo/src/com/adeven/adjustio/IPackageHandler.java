package com.adeven.adjustio;

public interface IPackageHandler {			
	public void addPackage(ActivityPackage pack);
	
	public void sendFirstPackage();
	
	public void sendNextPackage();
	
	public void closeFirstPackage();
	
	public void pauseSending();
	
	public void resumeSending();
	
	public String getFailureMessage();
	
}
