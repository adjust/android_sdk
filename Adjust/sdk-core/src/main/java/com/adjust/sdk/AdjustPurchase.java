package com.adjust.sdk;

public class AdjustPurchase {
        private final String sku;
        private final String purchaseToken;

    public AdjustPurchase(final String sku, final String purchaseToken) {
        this.sku = sku;
        this.purchaseToken = purchaseToken;
    }

    String getSku() {
        return sku;
    }

    String getPurchaseToken() {
        return purchaseToken;
    }
}
