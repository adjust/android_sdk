## 多个广播接收器

如果有多个来源需要注册您应用的广播接收器（broadcast receiver) 以获取`INSTALL_REFERRER` Intent，那么您须集成自己的`BroadcastReceiver`，然后用其调用您希望支持的其他所有接收器。

如果您已在 Manifest 文件中定义了您自己的广播接收器，比如类似以下的设置：

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

那么请确保按以下方式通知 Adjust (或其他需要该信息）的广播接收器关于 Intent 的内容：

```java
public class InstallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Adjust receiver.
        new AdjustReferrerReceiver().onReceive(context, intent);
        // Google Analytics receiver.
        new CampaignTrackingReceiver().onReceive(context, intent);
        // And any other receiver which needs the intent.
    }
}
```
