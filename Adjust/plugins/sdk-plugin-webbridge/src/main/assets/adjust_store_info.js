function AdjustStoreInfo(storeName) {
    this.storeName = storeName;
    this.storeAppId = null;
}

AdjustStoreInfo.prototype.setStoreAppId = function(storeAppId) {
    this.storeAppId = storeAppId;
};