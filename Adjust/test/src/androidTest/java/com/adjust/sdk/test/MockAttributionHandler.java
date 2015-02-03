package com.adjust.sdk.test;

import com.adjust.sdk.Attribution;
import com.adjust.sdk.IActivityHandler;
import com.adjust.sdk.IAttributionHandler;

import org.json.JSONObject;

/**
 * Created by pfms on 09/01/15.
 */
public class MockAttributionHandler implements IAttributionHandler {
    private MockLogger testLogger;
    private String prefix = "AttributionHandler ";
    public IActivityHandler activityHandler;
    public JSONObject lastJsonResponse;

    public MockAttributionHandler(MockLogger testLogger) {
        this.testLogger = testLogger;
    }

    @Override
    public void getAttribution() {
        testLogger.test(prefix +  "getAttribution");
    }

    @Override
    public void checkAttribution(JSONObject jsonResponse) {
        testLogger.test(prefix +  "checkAttribution");

        this.lastJsonResponse = jsonResponse;
        /*
        if (activityHandler == null) {
            return;
        }

        JSONObject attributionJson = jsonResponse.optJSONObject("attribution");
        Attribution attribution = Attribution.fromJson(attributionJson);

        if (activityHandler.updateAttribution(attribution)) {
            activityHandler.launchAttributionDelegate();
        }
        */
    }
}
