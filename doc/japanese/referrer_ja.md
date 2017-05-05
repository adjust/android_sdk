## 複数のブロードキャストレシーバへのサポート

複数のSDKでアプリの`INSTALL_REFERRER`インテントにブロードキャストレシーバを登録する必要がある場合、
サポートしたい他のすべてのレシーバをコールする`BroadcastReceiver`を独自に実装する必要があります。
それはこのような形になります。[1]

```java
public class InstallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Adjust
        new AdjustReferrerReceiver().onReceive(context, intent);

        // Google Analytics
        new CampaignTrackingReceiver().onReceive(context, intent);
    }
}
```

対象レシーバのリストの調整とインポートの記述を必ず確認してください。
独自`InstallReceiver`を使うには、`AndroidManifest.xml`の更新も必要です。

```xml
<receiver
    android:name="com.your.app.InstallReceiver"
    android:exported="true" >
    <intent-filter>
        <action android:name="com.android.vending.INSTALL_REFERRER" />
    </intent-filter>
</receiver>
```

パッケージ名を必ず確認してください。

---

参考および関連リンク

- [1] http://stackoverflow.com/questions/14158841/android-google-analytics-v2-broadcast-receiver-for-all-sdks
- [2] http://stackoverflow.com/questions/4093150/get-referrer-after-installing-app-from-android-market
- [3] http://docs.mdotm.com/index.php/Universal_Application_Tracking
