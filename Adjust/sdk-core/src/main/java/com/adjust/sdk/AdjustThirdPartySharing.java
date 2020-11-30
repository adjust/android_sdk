package com.adjust.sdk;

import java.util.HashMap;
import java.util.Map;

public class AdjustThirdPartySharing {
    Boolean enableOrElseDisable;
    Map<String, Map<String, String>> granularOptions;

    public AdjustThirdPartySharing(final Boolean enableOrElseDisable) {
        this.enableOrElseDisable = enableOrElseDisable;

        granularOptions = new HashMap<>();
    }

    public void addGranularOption(final String partnerName,
                                  final String key,
                                  final String value)
    {
        Map<String, String> partnerOptions = granularOptions.get(partnerName);
        if (partnerOptions == null) {
            partnerOptions = new HashMap<>();
            granularOptions.put(partnerName, partnerOptions);
        }

        partnerOptions.put(key, value);
    }
}
