package com.adjust.sdk;

import org.json.JSONObject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;

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
            new ObjectStreamField("creative", String.class)
    };

    public String trackerToken;
    public String trackerName;
    public String network;
    public String campaign;
    public String adgroup;
    public String creative;

    public static AdjustAttribution fromJson(JSONObject jsonObject) {
        if (jsonObject == null) return null;

        AdjustAttribution attribution = new AdjustAttribution();

        attribution.trackerToken = jsonObject.optString("tracker_token", null);
        attribution.trackerName = jsonObject.optString("tracker_name", null);
        attribution.network = jsonObject.optString("network", null);
        attribution.campaign = jsonObject.optString("campaign", null);
        attribution.adgroup = jsonObject.optString("adgroup", null);
        attribution.creative = jsonObject.optString("creative", null);

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
        return true;
    }


    @Override
    public String toString() {
        return String.format("tt:%s tn:%s net:%s cam:%s adg:%s cre:%s",
                trackerToken, trackerName, network, campaign, adgroup, creative);
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
    }

    private void readObject(ObjectInputStream stream) throws ClassNotFoundException, IOException {
        stream.defaultReadObject();
    }

}
