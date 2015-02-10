package com.adjust.sdk;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by pfms on 07/11/14.
 */
public class Attribution implements Serializable {
    private static final long serialVersionUID = 1L;

    protected String trackerToken;
    protected String trackerName;
    protected String network;
    protected String campaign;
    protected String adgroup;
    protected String creative;
    protected boolean finalAttribution;

    public String getTrackerToken() {
        return trackerToken;
    }

    public String getTrackerName() {
        return trackerName;
    }

    public String getNetwork() {
        return network;
    }

    public String getCampaign() {
        return campaign;
    }

    public String getAdgroup() {
        return adgroup;
    }

    public String getCreative() {
        return creative;
    }

    public boolean isFinalAttribution() {
        return finalAttribution;
    }

    public static Attribution fromJson(JSONObject jsonObject) {
        if (jsonObject == null) return null;

        Attribution attribution = new Attribution();

        attribution.trackerToken = jsonObject.optString("tracker_token", null);
        attribution.trackerName = jsonObject.optString("tracker_name", null);
        attribution.network = jsonObject.optString("network", null);
        attribution.campaign = jsonObject.optString("campaign", null);
        attribution.adgroup = jsonObject.optString("adgroup", null);
        attribution.creative = jsonObject.optString("creative", null);

        return attribution;
    }

    public boolean equals(Object other) {
        if (other == this) return true;
        if (other == null) return false;
        if (getClass() != other.getClass()) return false;
        Attribution otherAttribution = (Attribution) other;

        if (!equalString(trackerToken, otherAttribution.trackerToken)) return false;
        if (!equalString(trackerName, otherAttribution.trackerName)) return false;
        if (!equalString(network, otherAttribution.network)) return false;
        if (!equalString(campaign, otherAttribution.campaign)) return false;
        if (!equalString(adgroup, otherAttribution.adgroup)) return false;
        if (!equalString(creative, otherAttribution.creative)) return false;
        return finalAttribution == otherAttribution.finalAttribution;
    }

    private boolean equalString(String first, String second) {
        if (first == null || second == null) {
            return first == null && second == null;
        }
        return first.equals(second);
    }
}
