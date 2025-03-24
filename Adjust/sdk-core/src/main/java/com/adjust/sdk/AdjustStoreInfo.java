package com.adjust.sdk;

public class AdjustStoreInfo {
    String storeName;
    String storeAppId;

    public AdjustStoreInfo(String storeName, String storeAppId) {
        this.storeName = storeName;
        this.storeAppId = storeAppId;
    }

    public String getStoreName() {
        return storeName;
    }

    public String getStoreAppId() {
        return storeAppId;
    }
}
