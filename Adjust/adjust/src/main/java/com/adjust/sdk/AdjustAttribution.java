package com.adjust.sdk;

import org.json.JSONObject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.util.Locale;

/**
 * Created by pfms on 07/11/14.
 */
public class AdjustAttribution implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final ObjectStreamField[] serialPersistentFields = {
            new ObjectStreamField("trackerToken", String.class),
            new ObjectStreamField("trackerName", String.class),
            new ObjectStreamField("network", String.class),
            new ObjectStreamField("campaign", String.class),
            new ObjectStreamField("adgroup", String.class),
            new ObjectStreamField("creative", String.class),
            new ObjectStreamField("clickLabel", String.class),
            new ObjectStreamField("adid", String.class),
    };

    public String trackerToken;
    public String trackerName;
    public String network;
    public String campaign;
    public String adgroup;
    public String creative;
    public String clickLabel;
    public String adid;

    public static AdjustAttribution fromJson(JSONObject jsonObject, String adid) {
        if (jsonObject == null) return null;

        AdjustAttribution attribution = new AdjustAttribution();

        attribution.trackerToken = jsonObject.optString("tracker_token", null);
        attribution.trackerName = jsonObject.optString("tracker_name", null);
        attribution.network = jsonObject.optString("network", null);
        attribution.campaign = jsonObject.optString("campaign", null);
        attribution.adgroup = jsonObject.optString("adgroup", null);
        attribution.creative = jsonObject.optString("creative", null);
        attribution.clickLabel = jsonObject.optString("click_label", null);
        attribution.adid = adid;

        return attribution;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (other == null) return false;
        if (getClass() != other.getClass()) return false;
        AdjustAttribution otherAttribution = (AdjustAttribution) other;

        if (!Util.equalString(trackerToken, otherAttribution.trackerToken)) return false;
        if (!Util.equalString(trackerName, otherAttribution.trackerName)) return false;
        if (!Util.equalString(network, otherAttribution.network)) return false;
        if (!Util.equalString(campaign, otherAttribution.campaign)) return false;
        if (!Util.equalString(adgroup, otherAttribution.adgroup)) return false;
        if (!Util.equalString(creative, otherAttribution.creative)) return false;
        if (!Util.equalString(clickLabel, otherAttribution.clickLabel)) return false;
        if (!Util.equalString(adid, otherAttribution.adid)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int hashCode = 17;
        hashCode = 37 * hashCode + Util.hashString(trackerToken);
        hashCode = 37 * hashCode + Util.hashString(trackerName);
        hashCode = 37 * hashCode + Util.hashString(network);
        hashCode = 37 * hashCode + Util.hashString(campaign);
        hashCode = 37 * hashCode + Util.hashString(adgroup);
        hashCode = 37 * hashCode + Util.hashString(creative);
        hashCode = 37 * hashCode + Util.hashString(clickLabel);
        hashCode = 37 * hashCode + Util.hashString(adid);

        return hashCode;
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "tt:%s tn:%s net:%s cam:%s adg:%s cre:%s cl:%s adid:%s",
                trackerToken, trackerName, network, campaign, adgroup, creative, clickLabel, adid);
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
    }

    private void readObject(ObjectInputStream stream) throws ClassNotFoundException, IOException {
        stream.defaultReadObject();
    }
}
