## 다수의 브로드캐스트 리시버(broadcast receiver)를 위한 지원

다수의 소스를 사용하여 앱의 `INSTALL_REFERRER` 인텐트에 브로드캐스트 리시버를 등록해야 하는 경우,
자체 `BroadcastReceiver`를 구현하여 원하시는 모든 리시버를 호출할 수 있습니다. 아래에 나열된 것과 유사하게 manifest 파일에 정의된 브로드캐스트 리시버를 보유한 경우,

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

intent content의 Adjust 브로드캐스트 리시버 및 이를 필요로 하는 기타 수신기에 아래와 같이 정보를 전달해 주시기 바랍니다.

```java
public class InstallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Adjust receiver 
        new AdjustReferrerReceiver().onReceive(context, intent);
        // Google Analytics receiver 
        new CampaignTrackingReceiver().onReceive(context, intent);
        // And any other receiver which needs the intent.
    }
}
```

