function AdjustThirdPartySharing(enableOrElseDisable) {
    this.enableOrElseDisable = enableOrElseDisable;
    this.granularOptions = [];
}

AdjustEvent.prototype.addGranularOption = function(partnerName, key, value) {
    this.granularOptions.push(partnerName);
    this.granularOptions.push(key);
    this.granularOptions.push(value);
};
