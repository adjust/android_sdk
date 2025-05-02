package com.adjust.sdk;

public class AdjustStoreInfo {
    String storeName;
    String storeAppId;

    private static final ILogger logger = AdjustFactory.getLogger();

    public AdjustStoreInfo(String storeName) {
        if (!isValidStore(storeName)) {
            return;
        }

        this.storeName = storeName;
    }

    public void setStoreAppId(String storeAppId) {
        this.storeAppId = storeAppId;
    }

    private boolean isValidStore(final String storeName) {
        if (storeName == null) {
            logger.error("Missing store name");
            return false;
        }
        if (storeName.isEmpty()) {
            logger.error("Store name can't be empty");
            return false;
        }
        return true;
    }
}
