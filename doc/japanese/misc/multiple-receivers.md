## 複数のブロードキャストレシーバーへのサポート

アプリ内にインテント `INSTALL_REFERRER` に対するブロードキャストレシーバーを複数のサービス（Google Analyticsなど）に登録する必要がある場合は、
サポート対象となる他のすべてのレシーバーにコールする、独自の `BroadcastReceiver` を実装する必要があります。マニフェストファイルに、独自のブロードキャストレシーバーを設定している場合（以下のような内容になります）：

```xml
<receiver
    android:name="com.your.app.InstallReceiver"
    android:permission="android.permission.INSTALL_PACKAGES"
    android:exported="true" >
    <intent-filter>
        <action android:name="com.android.vending.INSTALL_REFERRER" />
    </intent-filter>
</receiver>
```

Adjustのブロードキャストレシーバー（およびその情報を必要する他のサービス）に対して、インテントをこのように作成してください：

```java
public class InstallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Adjust receiver.
        new AdjustReferrerReceiver().onReceive(context, intent);
        // Google Analytics receiver.
        new CampaignTrackingReceiver().onReceive(context,intent);
        // And any other receiver which needs the intent.
    }
}
```
