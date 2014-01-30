package com.adeven.adjustio;

public interface RequestHandler {

	public void setPackageHandler(PackageHandler packageHandler);
	
	public void sendPackage(ActivityPackage pack);
}