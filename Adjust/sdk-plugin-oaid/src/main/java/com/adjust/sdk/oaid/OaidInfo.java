package com.adjust.sdk.oaid;

public class OaidInfo {
    private String oaid;
    private boolean trackingEnabled;

    public OaidInfo(String oaid, boolean trackingEnabled) {
        this.oaid = oaid;
        this.trackingEnabled = trackingEnabled;
    }

    public String getOaid() {
        return oaid;
    }

    public boolean isTrackingEnabled() {
        return trackingEnabled;
    }
}
