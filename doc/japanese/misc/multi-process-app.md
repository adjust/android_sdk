# マルチプロセスアプリとadjustの連携

Androidアプリは1つもしくはそれ以上のプロセスで構成されます。サービスやアクティビティをメインプロセス以外のプロセスで実行することもできます。
プロセス内に ```android:process``` プロパティを設定するか、Androidマニフェストファイルでアクティビティの定義を記入し、これを実装してください。

```xml
<activity
    android:name=".YourActivity"
    android:process=":YourProcessName">
</activity>
```

```xml
<service
    android:name=".YourService"
    android:process=":YourProcessName">
</service>
```

このようなサービスやアクティビティを定義すると、メインプロセスでないプロセスでこれらを実行させることになります。

デフォルトでは、メインプロセスの名称はアプリのパッケージ名と同じになっています。アプリのパッケージ名が```com.example.myapp```の場合、メインプロセスの名前も同じです。この場合、```YourActivity```と```YourService```は```com.example.myapp:YourProcessName```という名前のプロセスで実行されます。

adjust SDKは1つのアプリ内での複数のプロセスからのトラッキングを__現在サポートしていません__。アプリで複数のプロセスをお使いの場合、
```AdjustConfig```オブジェクトにメインプロセスの名前を設定してください。

```java
String appToken = "{YourAppToken}";
String environment = AdjustConfig.ENVIRONMENT_SANDBOX;  // or AdjustConfig.ENVIRONMENT_PRODUCTION
AdjustConfig config = new AdjustConfig(this, appToken, environment);

config.setProcessName("com.example.myapp");

Adjust.onCreate(config);
```

Androidマニフェストファイルで```application```の```android:process```プロパティを変更すると、メインプロセスの名称を変えることができます。

```xml
<application
  android:name=".YourApp"
  android:icon="@drawable/ic_launcher"
  android:label="@string/app_name"
  android:theme="@style/AppTheme"
  android:process=":YourMainProcessName">
</application>
```

メインプロセスの名称をこのように記述した場合、```AdjustConfig```オブジェクトでメインプロセスの名前を次のように設定してください。

```java
config.setProcessName("com.example.myapp:YourMainProcessName");
```

これでメインプロセス名をadjust SDKに知らせることができ、SDKはその他のプロセスを一切初期化しません。他のプロセスからSDKを使おうと試みた場合、次のlogメッセージが表示されます。

```
05-06 17:15:06.885    8743-8743/com.example.myapp:YourProcessName I/Adjust﹕ Skipping initialization in background process (com.example.myapp:YourProcessName)
```

```AdjustConfig```オブジェクトでメインプロセス名を設定せずに複数のプロセスでSDKをコールしようとすると、Androidの異なるプロセス間ではメモリ領域をシェアしませんので、SDKの様々なインスタンスを初期化することになります。これは予測できない結果につながる恐れがありますので、複数のプロセスをご使用の場合は常にメインプロセス名を設定するか、アプリ内で2つ以上のプロセスでadjust SDKを使わないようにご注意ください。
