## Support for multiple broadcast receivers

If multiple sources need to register a broadcast receiver for the `INSTALL_REFERRER` intent in your app, you will have to implement your own
`BroadcastReceiver`, which calls all the other receivers you want to support. If you have your own broadcast receiver defined in your manifest file (similar to the one stated below):

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

please make sure to inform the Adjust broadcast receiver (and any other that might need the information) of your intent content like so:

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
