package com.adjust.sdk;

public class AdjustStoreInfo {
    String storeType;
    String appId;

    public AdjustStoreInfo(String storeType, String appId) {
        this.storeType = storeType;
        this.appId = appId;
    }

    public String getStoreType() {
        return storeType;
    }

    public String getAppId() {
        return appId;
    }
}
