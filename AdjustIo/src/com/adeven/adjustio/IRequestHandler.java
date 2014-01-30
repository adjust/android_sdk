package com.adeven.adjustio;

public interface IRequestHandler {

	public void setPackageHandler(IPackageHandler packageHandler);
	
	public void sendPackage(ActivityPackage pack);
}
