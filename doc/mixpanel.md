##Integrate adjust with Mixpanel SDK

The Mixpanel API allows to register common properties to be sent in all events as `super properties`, as it is explained in the [Mixpanel page][mixpanel_android]. 
To integrate adjust with all tracked events of Mixpanel, you must set the `super properties` after receiving the response data of each event. 
Follow the steps of the [delegate notifications][response_callbacks] chapter in our Android SDK guide to implement it. 
The delegate function can be set as the following, to use the Mixpanel API: 

```java
    Adjust.setOnFinishedListener(new OnFinishedListener() {
        public void onFinishedTracking(ResponseData responseData) {
            MixpanelAPI mixpanel =
                MixpanelAPI.getInstance(context, MIXPANEL_TOKEN);
    
            // The adjust properties will be sent
            // with all future track calls.
            JSONObject props = new JSONObject();
            
            if (responseData.getNetwork() != null)
                props.put("[Adjust]Network", responseData.getNetwork());
                
            if (responseData.getCampaign() != null)
                props.put("[Adjust]Campaign", responseData.getCampaign());
            
            if (responseData.getAdgroup() != null)
                props.put("[Adjust]Adgroup", responseData.getAdgroup());
            
            if (responseData.getCreative() != null)
                props.put("[Adjust]Creative", responseData.getCreative());
            
            if (props.length() > 0)
                mixpanel.registerSuperProperties(props);
    
        }
    });
```


Before you implement this interface, please take care to consider [possible conditions for usage of some of your data][attribution_data].

[mixpanel_android]: https://mixpanel.com/help/reference/android#superproperties
[attribution_data]: https://github.com/adjust/sdks/blob/master/doc/attribution-data.md
[response_callbacks]: https://github.com/adjust/android_sdk/tree/master#11-set-listener-for-delegate-notifications
