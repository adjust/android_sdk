package com.adjust.sdk;

import org.json.JSONObject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

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
            new ObjectStreamField("costType", String.class),
            new ObjectStreamField("costAmount", Double.class),
            new ObjectStreamField("costCurrency", String.class),
            new ObjectStreamField("fbInstallReferrer", String.class),
    };

    public String trackerToken;
    public String trackerName;
    public String network;
    public String campaign;
    public String adgroup;
    public String creative;
    public String clickLabel;
    public String costType;
    public Double costAmount;
    public String costCurrency;
    public String fbInstallReferrer;

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
        if (!Util.equalString(costType, otherAttribution.costType)) return false;
        if (!Util.equalsDouble(costAmount, otherAttribution.costAmount)) return false;
        if (!Util.equalString(costCurrency, otherAttribution.costCurrency)) return false;
        if (!Util.equalString(fbInstallReferrer, otherAttribution.fbInstallReferrer)) return false;

        return true;
    }

    public Map<String, String> toMap() {
        Map<String, String> fields = new HashMap<>();
        if (trackerToken != null) fields.put("trackerToken", trackerToken);
        if (trackerName != null) fields.put("trackerName", trackerName);
        if (network != null) fields.put("network", network);
        if (campaign != null) fields.put("campaign", campaign);
        if (adgroup != null) fields.put("adgroup", adgroup);
        if (creative != null) fields.put("creative", creative);
        if (clickLabel != null) fields.put("clickLabel", clickLabel);
        if (costType != null) fields.put("costType", costType);
        if (costAmount != null) fields.put("costAmount", costAmount.toString());
        if (costCurrency != null) fields.put("costCurrency", costCurrency);
        if (fbInstallReferrer != null) fields.put("fbInstallReferrer", fbInstallReferrer);

        return fields;
    }

    @Override
    public int hashCode() {
        int hashCode = 17;
        hashCode = Util.hashString(trackerToken, hashCode);
        hashCode = Util.hashString(trackerName, hashCode);
        hashCode = Util.hashString(network, hashCode);
        hashCode = Util.hashString(campaign, hashCode);
        hashCode = Util.hashString(adgroup, hashCode);
        hashCode = Util.hashString(creative, hashCode);
        hashCode = Util.hashString(clickLabel, hashCode);
        hashCode = Util.hashString(costType, hashCode);
        hashCode = Util.hashDouble(costAmount, hashCode);
        hashCode = Util.hashString(costCurrency, hashCode);
        hashCode = Util.hashString(fbInstallReferrer, hashCode);

        return hashCode;
    }

    @Override
    public String toString() {
        return Util.formatString(
                "tt:%s tn:%s net:%s cam:%s adg:%s cre:%s cl:%s ct:%s ca:%.2f cc:%s fir:%s",
                trackerToken, trackerName, network, campaign, adgroup, creative, clickLabel,
                costType, costAmount, costCurrency, fbInstallReferrer);
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
    }

    private void readObject(ObjectInputStream stream) throws ClassNotFoundException, IOException {
        stream.defaultReadObject();
    }
}
