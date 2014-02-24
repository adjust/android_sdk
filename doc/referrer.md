## Support multiple broadcast receivers

If multiple SDKs need to register a broadcast receiver for the
`INSTALL_REFERRER` intent in your app, you will have to implement your own
`BroadcastReceiver` that calls all the other receivers that you want to
support. It should look like this [1]:
```java
public class InstallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Adjust
        new ReferrerReceiver().onReceive(context, intent);

        // Google Analytics
        new CampaignTrackingReceiver().onReceive(context, intent);
    }
}
```

Make sure to adjust the list of supported receviers and fix the imports. You
also need to update your `AndroidManifest.xml` to use your own
`InstallReceiver`:
```xml
<receiver
    android:name="com.your.app.InstallReceiver"
    android:exported="true" >
    <intent-filter>
        <action android:name="com.android.vending.INSTALL_REFERRER" />
    </intent-filter>
</receiver>
```

Make sure to adjust your package name.

---

References and related links:

- [1] http://stackoverflow.com/questions/14158841/android-google-analytics-v2-broadcast-receiver-for-all-sdks
- [2] http://stackoverflow.com/questions/4093150/get-referrer-after-installing-app-from-android-market
- [3] http://docs.mdotm.com/index.php/Universal_Application_Tracking
