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

}
