package com.adjust.sdk;

public interface IRequestHandler {
    public void init(IPackageHandler packageHandler);

    public void sendPackage(ActivityPackage activityPackage, int queueSize);
}
