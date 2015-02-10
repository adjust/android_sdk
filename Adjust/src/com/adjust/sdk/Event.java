package com.adjust.sdk;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by pfms on 05/11/14.
 */
public class Event {
    String eventToken;
    Double revenue;
    String currency;
    Map<String, String> callbackParameters;
    Map<String, String> partnerParameters;

    private Event() {
    }

    public static Event getInstance(String eventToken) {
        Logger logger = AdjustFactory.getLogger();

        if (eventToken == null) {
            logger.error("Missing Event Token");
            return null;
        }
        if (eventToken.length() != 6) {
            logger.error("Malformed Event Token '%s'", eventToken);
            return null;
        }

        Event event = new Event();
        event.eventToken = eventToken;

        return event;
    }

    public static Event getInstance(String eventToken, double revenue, String currency) {
        Event event = getInstance(eventToken);
        if (event == null) {
            return null;
        }

        Logger logger = AdjustFactory.getLogger();

        if (revenue < 0.0) {
            logger.error("Invalid amount %f", revenue);
            return null;
        }

        if (currency == null) {
            logger.error("Currency must be set with revenue");
            return null;
        }

        event.revenue = revenue;
        event.currency = currency;

        return event;
    }

    public void addCallbackParameter(String key, String value) {
        if (callbackParameters == null) {
            callbackParameters = new HashMap<String, String>();
        }

        callbackParameters.put(key, value);
    }

    public void addPartnerParameter(String key, String value) {
        if (partnerParameters == null) {
            partnerParameters = new HashMap<String, String>();
        }

        partnerParameters.put(key, value);
    }
}
