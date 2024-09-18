function AdjustThirdPartySharing(isEnabled) {
    this.isEnabled = isEnabled;
    this.granularOptions = [];
    this.partnerSharingSettings = [];
}

AdjustThirdPartySharing.prototype.addGranularOption = function(partnerName, key, value) {
    if (typeof partnerName !== 'string' || typeof key !== 'string' || typeof value !== 'string') {
        console.log('[Adjust]: Passed partnerName, key or value is not of string type');
        return;
    }
    this.granularOptions.push(partnerName);
    this.granularOptions.push(key);
    this.granularOptions.push(value);
};

AdjustThirdPartySharing.prototype.addPartnerSharingSetting = function(partnerName, key, value) {
    if (typeof partnerName !== 'string' || typeof key !== 'string' || typeof value !== 'boolean') {
        console.log('[Adjust]: Passed partnerName or key is not of string type or value is not of boolean type');
        return;
    }
    this.partnerSharingSettings.push(partnerName);
    this.partnerSharingSettings.push(key);
    this.partnerSharingSettings.push(value);
};
