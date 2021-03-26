package com.adjust.sdk;

import java.util.LinkedHashMap;
import java.util.Map;

public class AdjustAdRevenue {
    String source;
    Double revenue;
    String currency;
    Map<String, String> callbackParameters;
    Map<String, String> partnerParameters;

    private static final String KEY_REVENUE = "revenue";
    private static final String KEY_CURRENCY = "currency";

    private static final ILogger logger = AdjustFactory.getLogger();

    public AdjustAdRevenue(final String source) {
        if (!isValidSource(source)) {
            return;
        }

        this.source = source;
    }

    public void setRevenue(final Double revenue, final String currency) {
        if (!isValidRevenue(revenue, currency)) {
            return;
        }

        this.revenue = revenue;
        this.currency = currency;
    }

    public void addCallbackParameter(final String key, final String value) {
        if (!Util.isValidParameter(key, "key", "Callback")) return;
        if (!Util.isValidParameter(value, "value", "Callback")) return;

        if (callbackParameters == null) {
            callbackParameters = new LinkedHashMap<String, String>();
        }

        String previousValue = callbackParameters.put(key, value);

        if (previousValue != null) {
            logger.warn("Key %s was overwritten", key);
        }
    }

    public void addPartnerParameter(String key, String value) {
        if (!Util.isValidParameter(key, "key", "Partner")) return;
        if (!Util.isValidParameter(value, "value", "Partner")) return;

        if (partnerParameters == null) {
            partnerParameters = new LinkedHashMap<String, String>();
        }

        String previousValue = partnerParameters.put(key, value);

        if (previousValue != null) {
            logger.warn("Key %s was overwritten", key);
        }
    }

    public boolean isValid() {
        return isValidSource(this.source) && isValidRevenue(this.revenue, this.currency);
    }

    private boolean isValidSource(final String source) {
        if (source == null) {
            logger.error("Missing source");
            return false;
        }
        if (source.isEmpty()) {
            logger.error("Source can't be empty");
            return false;
        }
        return true;
    }

    private boolean isValidRevenue(final Double revenue, final String currency) {
        if (revenue == null) {
            logger.error("Missing revenue");
            return false;
        }

        if (currency == null) {
            logger.error("Missing currency");
            return false;
        }

        if (revenue < 0.0) {
            logger.error("Invalid amount %.5f", revenue);
            return false;
        }

        if (currency.isEmpty()) {
            logger.error("Currency is empty");
            return false;
        }

        return true;
    }
}
