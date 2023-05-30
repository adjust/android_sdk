package com.adjust.sdk;

import com.adjust.sdk.network.IActivityPackageSender;

/**
 * PurchaseVerificationHandler interface.
 *
 * @author Ugljesa Erceg (uerceg)
 * @since 30th May 2023
 */

public interface IPurchaseVerificationHandler {
    /**
     * Initialise PurchaseVerificationHandler instance.
     *
     * @param activityHandler Activity handler instance.
     * @param startsSending   Is sending paused?
     */
    void init(IActivityHandler activityHandler,
              boolean startsSending,
              IActivityPackageSender purchaseVerificationHandlerActivityPackageSender);

    /**
     * Pause sending from PurchaseVerificationHandler.
     */
    void pauseSending();

    /**
     * Resume sending from PurchaseVerificationHandler.
     */
    void resumeSending();

    /**
     * Send purchase_verification package.
     *
     * @param purchaseVerification purchase_verification package to be sent.
     */
    void sendPurchaseVerificationPackage(ActivityPackage purchaseVerification);

    /**
     * Teardown PurchaseVerificationHandler instance.
     */
    void teardown();
}
