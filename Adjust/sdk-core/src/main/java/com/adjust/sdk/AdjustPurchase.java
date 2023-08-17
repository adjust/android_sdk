package com.adjust.sdk;

public class AdjustPurchase {
        private final String productId;
        private final String purchaseToken;

    public AdjustPurchase(final String productId, final String purchaseToken) {
        this.productId = productId;
        this.purchaseToken = purchaseToken;
    }

    String getProductId() {
        return productId;
    }

    String getPurchaseToken() {
        return purchaseToken;
    }
}
