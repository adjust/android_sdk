package com.adjust.sdk;

/**
 * Created by pfms on 15/12/14.
 */
public interface IAttributionHandler {
    public void init(IActivityHandler activityHandler,
                     ActivityPackage attributionPackage,
                     boolean startsSending,
                     boolean hasListener);

    public void getAttribution();

    public void checkSessionResponse(SessionResponseData responseData);

    public void pauseSending();

    public void resumeSending();
}
