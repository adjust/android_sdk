##Integrate adjust with Mixpanel SDK

The Mixpanel API allows to register common properties to be sent in all
activities as `super properties`, as it is explained in the [Mixpanel
page][mixpanel_android].  To integrate adjust with all tracked events of
Mixpanel, you must set the `super properties` after receiving the response data
of each activity.  Follow the steps of the [listener][listener] chapter in our
Android SDK guide to implement it.  The delegate function can be set as the
following, to use the Mixpanel API:

```java
public class YourApplicationClass extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // configure Adjust
        String appToken = "{YourAppToken}";
        String environment = AdjustConfig.ENVIRONMENT_SANDBOX;
        AdjustConfig config = new AdjustConfig(this, appToken, environment);

        config.setOnAttributionChangedListener(new OnAttributionChangedListener() {
            @Override
            public void onAttributionChanged(AdjustAttribution attribution) {
                MixpanelAPI mixpanel = MixpanelAPI.getInstance(context, MIXPANEL_TOKEN);

                // The adjust properties will be sent
                // with all future track calls.
                JSONObject props = new JSONObject();

                insertJsonProperty(props, "[Adjust]Network", attribution.network);

                insertJsonProperty(props, "[Adjust]Campaign", attribution.campaign);

                insertJsonProperty(props, "[Adjust]Adgroup", attribution.adgroup);

                insertJsonProperty(props, "[Adjust]Creative", attribution.creative);

                if (props.length() > 0)
                    mixpanel.registerSuperProperties(props);
            }

            private void insertJsonProperty(JSONObject props, String name, String value) {
                try {
                    if (value != null) {
                        props.put(name, value);
                    }
                } catch(JSONException e) { }
            }
        });

        Adjust.onCreate(config);
    }
}
```

Before you implement this interface, please take care to consider [possible
conditions for usage of some of your data][attribution_data].

[mixpanel_android]: https://mixpanel.com/help/reference/android#superproperties
[attribution_data]: https://github.com/adjust/sdks/blob/master/doc/attribution-data.md
[listener]: https://github.com/adjust/android_sdk/tree/master#13-set-listener-for-delegate-notifications
