package com.adjust.sdk;

import java.util.HashMap;
import java.util.Map;

public class AdjustThirdPartySharing {
    Boolean isEnabled;
    Map<String, Map<String, String>> granularOptions;
    Map<String, Map<String, Boolean>> partnerSharingSettings;

    public AdjustThirdPartySharing(final Boolean isEnabled) {
        this.isEnabled = isEnabled;
        granularOptions = new HashMap<>();
        partnerSharingSettings = new HashMap<>();
    }

    public void addGranularOption(final String partnerName,
                                  final String key,
                                  final String value)
    {
        if (partnerName == null || key == null || value == null) {
            ILogger logger = AdjustFactory.getLogger();
            logger.error("Cannot add granular option with any null value");
            return;
        }

        Map<String, String> partnerOptions = granularOptions.get(partnerName);
        if (partnerOptions == null) {
            partnerOptions = new HashMap<>();
            granularOptions.put(partnerName, partnerOptions);
        }

        partnerOptions.put(key, value);
    }

    public void addPartnerSharingSetting(final String partnerName,
                                         final String key,
                                         final boolean value)
    {
        if (partnerName == null || key == null) {
            ILogger logger = AdjustFactory.getLogger();
            logger.error("Cannot add partner sharing setting with any null value");
            return;
        }

        Map<String, Boolean> partnerSharingSetting = this.partnerSharingSettings.get(partnerName);
        if (partnerSharingSetting == null) {
            partnerSharingSetting = new HashMap<>();
            partnerSharingSettings.put(partnerName, partnerSharingSetting);
        }

        partnerSharingSetting.put(key, value);
    }
}
