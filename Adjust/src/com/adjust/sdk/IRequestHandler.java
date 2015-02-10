package com.adjust.sdk;

public interface IRequestHandler {
    public void sendPackage(ActivityPackage pack);

    public void sendClickPackage(ActivityPackage clickPackage);
}
