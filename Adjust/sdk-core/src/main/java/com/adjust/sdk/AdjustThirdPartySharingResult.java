package com.adjust.sdk;

public class AdjustThirdPartySharingResult {
    private final String thirdPartySharingSettingsJson;

    public AdjustThirdPartySharingResult(final String thirdPartySharingSettingsJson) {
        this.thirdPartySharingSettingsJson = thirdPartySharingSettingsJson;
    }

    /**
     * Returns third party sharing settings as a raw JSON string received from the backend.
     */
    public String getThirdPartySharingSettingsJson() {
        return thirdPartySharingSettingsJson;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof AdjustThirdPartySharingResult)) return false;

        AdjustThirdPartySharingResult otherResult = (AdjustThirdPartySharingResult) other;
        return Util.equalString(thirdPartySharingSettingsJson, otherResult.thirdPartySharingSettingsJson);
    }

    @Override
    public int hashCode() {
        return thirdPartySharingSettingsJson != null ? thirdPartySharingSettingsJson.hashCode() : 0;
    }

}
