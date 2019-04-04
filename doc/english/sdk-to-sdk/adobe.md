## Integrate Adjust with the Adobe SDK

To integrate Adjust with all Adobe SDK tracked events, you must send your Adjust attribution data to the Adobe SDK after receiving the attribution response from our backend. Follow the steps in the [attribution callback][attribution-callback] chapter of our Android SDK guide to implement this. To use the Adobe SDK API, the callback method can be set as the following:

```java
public class YourApplicationClass extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize Adobe SDK.
        Config.setContext(this.getApplicationContext());
        Config.setDebugLogging(true);
        
        // Configure Adjust
        AdjustConfig config = new AdjustConfig(this, appToken, environment);

        config.setOnAttributionChangedListener(new OnAttributionChangedListener() {
            @Override
            public void onAttributionChanged(AdjustAttribution attribution) {
                Map<String,Object> adjustData = new HashMap<String, Object>();
                
                // Do not change the key "Adjust Network". This key is being used in the Data Connector Processing Rule.
                if (attribution.network != null) {
                    adjustData.put("Adjust Network", attribution.network);
                }
                // Do not change the key "Adjust Campaign". This key is being used in the Data Connector Processing Rule.
                if (attribution.campaign != null) {
                    adjustData.put("Adjust Campaign", attribution.campaign);
                }
                // Do not change the key "Adjust Adgroup". This key is being used in the Data Connector Processing Rule.
                if (attribution.adgroup != null) {
                    adjustData.put("Adjust Adgroup", attribution.adgroup);
                }
                // Do not change the key "Adjust Creative". This key is being used in the Data Connector Processing Rule.
                if (attribution.creative != null) {
                    adjustData.put("Adjust Creative", attribution.creative);
                }

                // Send Data to Adobe using Track Action.
                Analytics.trackAction("Adjust Campaign Data Received", adjustData);
            }
        });

        Adjust.onCreate(config);
    }
}
```

Before you implement this interface, please take care to consider the [possible conditions for usage of some of your data][attribution-data].

[attribution-data]:     https://github.com/adjust/sdks/blob/master/doc/attribution-data.md
[attribution-callback]: ../../../README.md#af-attribution-callback
