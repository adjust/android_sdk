package com.adjust.sdk;

public class AdjustPlayStorePurchase {
        private final String productId;
        private final String purchaseToken;

    public AdjustPlayStorePurchase(final String productId, final String purchaseToken) {
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
