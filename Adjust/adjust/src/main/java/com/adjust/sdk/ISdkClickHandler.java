package com.adjust.sdk;

/**
 * SdkClickHandler interface.
 *
 * @author Pedro Silva (nonelse)
 * @since 31st March 2016
 */

public interface ISdkClickHandler {
    /**
     * Initialise SdkClickHandler instance.
     *
     * @param activityHandler Activity handler instance.
     * @param startsSending   Is sending paused?
     */
    void init(IActivityHandler activityHandler, boolean startsSending);

    /**
     * Pause sending from SdkClickHandler.
     */
    void pauseSending();

    /**
     * Resume sending from SdkClickHandler.
     */
    void resumeSending();

    /**
     * Send sdk_click package.
     *
     * @param sdkClick sdk_click package to be sent.
     */
    void sendSdkClick(ActivityPackage sdkClick);

    /**
     * Teardown SdkClickHandler instance.
     */
    void teardown();
}
