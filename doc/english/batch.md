## Integrate Adjust with the Batch.com SDK

To integrate Adjust with the Batch.com SDK, you must send your Adjust attribution data to the Batch SDK after receiving the attribution response from the Adjust backend. Follow the steps in the [attribution callback][attribution-callback] chapter of our Android SDK guide to implement this. To use the Batch.com SDK API, the callback method can be set as the following:

```java
AdjustConfig config = new AdjustConfig(this, appToken, environment);

config.setOnAttributionChangedListener(new OnAttributionChangedListener() {
    @Override
    public void onAttributionChanged(AdjustAttribution attribution) {
        // initiate Batch user editor to set new attributes
        BatchUserDataEditor editor = Batch.User.editor();

        if (attribution.network != null)
            editor.setAttribute("adjust_network", attribution.network);
        if (attribution.campaign != null)
            editor.setAttribute("adjust_campaign", attribution.campaign);
        if (attribution.adgroup != null)
            editor.setAttribute("adjust_adgroup", attribution.adgroup);
        if (attribution.creative != null)
            editor.setAttribute("adjust_creative", attribution.creative);

        // send new attributes to Batch servers
        editor.save();
    }
});

Adjust.onCreate(config);
```

Before you implement this interface, please consider the [possible conditions for usage of some of your data][attribution-data].

[attribution-data]:     https://github.com/adjust/sdks/blob/master/doc/attribution-data.md
[attribution-callback]: https://github.com/adjust/android_sdk#attribution-callback