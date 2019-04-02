##adjustとMixpanel SDKの連携

Mixpanel APIは共通のプロパティを`super properties`としてすべてのアクティビティで送信できるよう登録できます。
詳しくは[Mixpanelのページ][mixpanel_android]でご確認いただけます。
Mixpanelのトラッキングされるすべてのイベントとadjustを連携させるには、
アトリビューションデータを受け取った後に`super properties`を設定する必要があります。
Android SDKガイドの[リスナ][listener]の項目を参考に実装してください。
Mixpanel APIを使うためのデリゲート関数は次のようになります。

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

このインターフェイスを実装する前に、[データの取り扱い][attribution_data]についてご確認ください。

[mixpanel_android]: https://mixpanel.com/help/reference/android#superproperties
[attribution_data]: https://github.com/adjust/sdks/blob/master/doc/attribution-data.md
[listener]: https://github.com/adjust/android_sdk/tree/master#13-set-listener-for-delegate-notifications
