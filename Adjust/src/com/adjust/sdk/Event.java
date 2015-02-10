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

    public Event(String eventToken) {
        this.eventToken = eventToken;
    }

    public void setRevenue(double amount, String currency) {
        this.revenue = amount;
        this.currency = currency;
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
