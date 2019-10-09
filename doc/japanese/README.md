これはネイティブadjust™のAndroid SDKガイドです。adjust™についての詳細は[adjust.com]をご覧ください。

Read this in other languages: [English][en-readme], [中文][zh-readme], [日本語][ja-readme], [한국어][ko-readme].

## 目次

### Quick start
   * [サンプルアプリ](#qs-example-apps)
   * [基本的な導入方法](#qs-getting-started)
      * [SDK をプロジェクトに追加する](#qs-add-sdk)
      * [Google Play サービスの追加](#qs-gps)
      * [パーミッションの追加](#qs-permissions)
      * [Proguard の設定](#qs-proguard)
      * [インストールリファラ](#qs-install-referrer)
         * [Google Play Referrer API](#qs-gpr-api)
         * [Google Play Store のインテント](#qs-gps-intent)
   * [SDK をアプリに実装する](#qs-integrate-sdk)
      * [基本設定](#qs-basic-setup)
         * [Native App SDK](#qs-basic-setup-native)
         * [WebView SDK](#qs-basic-setup-web)
      * [セッショントラッキング](#qs-session-tracking)
         * [APIレベルが14以降](#qs-session-tracking-api-14)
         * [レベル9から13のAPI](#qs-session-tracking-api-9)
      * [SDK シグネチャー](#qs-sdk-signature)
      * [Adjust ログの取得](#qs-adjust-logging)
      * [アプリのビルド](#qs-build-the-app)

### ディープリンキング

   * [ディープリンク](#dl)
   * [スタンダード・ディープリンキング](#dl-standard)
   * [ディファード・ディープリンキング](#dl-deferred)
   * [ディープリンクを介したリアトリビューション](#dl-reattribution)
   
### イベントトラッキング

   * [イベントトラッキング](#et-tracking)
   * [収益のトラッキング](#et-revenue)
   * [収益の重複排除 (deduplication)](#et-revenue-deduplication)
   * [アプリ内課金の保証](#et-purchase-verification)
   
### カスタムパラメータ

   * [カスタムパラメータ](#cp)
   * [イベントパラメータ](#cp-event-parameters)
      * [イベントコールバックパラメータ](#cp-event-callback-parameters)
      * [パートナーパラメータ](#cp-event-partner-parameters)
      * [イベントコールバックID](#cp-event-callback-id)
   * [セッションパラメータ](#cp-session-parameters)
      * [セッションコールバックパラメータ](#cp-session-callback-parameters)
      * [セッションパートナーパラメータ](#cp-session-partner-parameters)
      * [ディレイスタート](#cp-delay-start)

### 追加機能

   * [Push トークン (uninstall tracking)](#af-push-token)
   * [アトリビューションコールバック](#af-attribution-callback)
   * [広告収益の計測](#af-ad-revenue)
   * [イベントとセッションのコールバック](#af-session-event-callbacks)
   * [ユーザーアトリビューション](#af-user-attribution)
   * [デバイス ID](#af-device-ids)
      * [Google 広告 ID](#af-gps-adid)
      * [Amazon 広告 ID](#af-amazon-adid)
      * [Adjust デバイスID](#af-adid)
   * [プレインストールのトラッカー](#af-pre-installed-trackers)
   * [オフラインモード](#af-offline-mode)
   * [トラッキングの無効化](#af-disable-tracking)
   * [イベントバッファリング](#af-event-buffering)
   * [バックグラウンドでのトラッキング](#af-background-tracking)
   * [GDPR消去する権利（忘れられる権利）](#af-gdpr-forget-me)

### トラブルシューティング
   * ["Session failed (Ignoring too frequent session. ...)"というエラーが出る](#tt-session-failed)
   * [ブロードキャストレシーバーがインストールリファラを受信していない](#tt-broadcast-receiver)
   * [アプリ起動時にイベントを始動したい](#tt-event-at-launch)

### ライセンス



## Quick start

### <a id="qs-example-apps"></a>サンプルアプリ

Android サンプルアプリがexample ディレクトリ ([example-app-java][example-java]) および ([example-app-kotlin directories][example-kotlin])にあります。Webviewに使用するサンプルアプリは([example-webbridge][example-webbridge]) 、Android TVのサンプルは ([example-app-tv][example-tv])をご覧ください。をご覧ください。SDK実装の際は、Androidプロジェクトを開き、このサンプルをご参照ください。

### <a id="qs-getting-started"></a>基本的な導入方法

Adjust SDKをAndroidプロジェクトに実装する手順を説明します。ここでは、Androidアプリケーションの開発にAndroid Studioが使用されていること、対象はAndroid APIレベル **9（Gingerbread）** 以降であることを条件に説明します。
 
### <a id="qs-add-sdk"></a>SDKをプロジェクトに追加する

Mavenを使用している場合は、以下の内容を`build.gradle`ファイルに追加します。file:
 
```gradle
implementation 'com.adjust.sdk:adjust-android:4.18.3'
implementation 'com.android.installreferrer:installreferrer:1.0'
```

アプリの WebView内で Adjust SDK を使用したい場合は、以下のdependencyを追加してください。

```gradle
implementation 'com.adjust.sdk:adjust-android-webbridge:4.18.3'
```

Adjust SDK およびWebView拡張機能をJAR ファイルとして追加し、Adjust [リリースページ][releases]からダウンロードすることもできます。
 
### <a id="qs-gps"></a>Google Playサービスの追加

2014年8月1日より、Google Playストアのアプリには、端末をユニーク判別するために[Google Advertising ID][google_ad_id]の使用が義務付けられました。Adjust SDKでGoogle広告ID（gps_adid / Google Play Services Advertising ID）を使用するには、[Google Playサービス][google_play_services]を導入する必要があります。導入済みではい場合は、以下の手順に沿って設定してください。

- アプリの`build.gradle`ファイルを開き、`dependencies`ブロックに次の行を追加してください。

```gradle
implementation 'com.google.android.gms:play-services-analytics:16.0.4'
```

**注意：** Adjust SDKは、Google Playサービスの一つである`play-services-analytics`ライブラリの特定のバージョンとは紐付いていませんので、必要に応じて最新バージョンをご使用ください。

### <a id="qs-permissions"></a>パーミッションの追加
 
`AndroidManifest.xml`ファイルにAdjust SDKに必要なパーミッションが存在しない場合は、以下を追加してください。
 
```xml
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
```

**Google Playストア以外の第三者ストアからアプリをリリースする**場合は、以下のパーミッションも追加してください。

```xml
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
```
 
### <a id="qs-proguard"></a>Proguardの設定

Proguardをお使いの場合は、以下をProguardファイルに追加してください。

```
-keep class com.adjust.sdk.** { *; }
-keep class com.google.android.gms.common.ConnectionResult {
    int SUCCESS;
}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient {
    com.google.android.gms.ads.identifier.AdvertisingIdClient$Info getAdvertisingIdInfo(android.content.Context);
}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info {
    java.lang.String getId();
    boolean isLimitAdTrackingEnabled();
}
-keep public class com.android.installreferrer.** { *; }
```

**Google Playストア以外の第三者ストアからアプリをリリースする**場合は、`com.adjust.sdk`のパッケージ規定にしたがってください。

```
-keep public class com.adjust.sdk.** { *; }
```

### <a id="qs-install-referrer"></a>インストールリファラ

アプリのインストールをアトリビューションソースに正確にアトリビュートするため、Adjustは**インストールリファラ**の情報を必要とします。そのために**Google Play リファラ API**かブロードキャストレシーバーを使用して、**Google Playストアのインテント**を取得します。

**重要**：Google Play リファラ APIは、Androidのインストールリファラをより安全に提供し、またクリックインジェクションの不正に対抗する目的でGoogleが新たに導入したものです。アプリケーションでこれをサポートすることを**強く推奨**します。Google Playストアのインテントは、インストールのリファラ情報を取得する上で安全性が低い方法です。当面、新しいGoogle Play Referrer APIと並行して引き続き存在しますが、将来廃止される予定となっています。

#### <a id="qs-gpr-api"></a>Google Play リファラAPI

アプリでこのAPIをサポートするには、[SDKをプロジェクトに追加する](#qs-add-sdk) の章の手順に適切に従って、以下の行を`build.gradle`ファイルに追加していることを確認してください。

```
implementation 'com.android.installreferrer:installreferrer:1.0'
```

また、[Proguardの設定](#qs-proguard)の章をよく読んで、記載されているすべてのルール、特に、この機能に必要なルールが追加されていることを確認してください。

```
-keep public class com.android.installreferrer.** { *; }
```

この機能は、**Adjust SDK v4.12.0以降**を使用している場合にサポートされます。

#### <a id="qs-gps-intent"></a>Google Playストアのインテント

Google Play ストアの`INSTALL_REFERRER`インテントは、ブロードキャストレシーバーを使用して受信することをおすすめします。ブロードキャストレシーバーを使用せずに`INSTALL_REFERRER`インテントを取得したい場合、以下の`receiver`タグを`AndroidManifest.xml`の`application`タグ内に追加してください。

```xml
<receiver
    android:name="com.adjust.sdk.AdjustReferrerReceiver"
    android:permission="android.permission.INSTALL_PACKAGES"
    android:exported="true" >
    <intent-filter>
        <action android:name="com.android.vending.INSTALL_REFERRER" />
    </intent-filter>
</receiver>
```

Adjustはこのブロードキャストレシーバーを使用して、インストールのリファラ情報を取得し、バックエンドに転送します。

`INSTALL_REFERRER`インテントに対して既に他のブロードキャストレシーバーを使用している場合、[こちらの説明][referrer]に従って、Adjust ブロードキャストレシーバーを追加してください。

### <a id="qs-integrate-sdk"></a>SDKをアプリに実装する
 
まず最初に、基本的なセッショントラッキングを設定します。

### <a id="qs-basic-setup"></a>基本設定

ネイティブアプリにSDK を実装する場合は、以下の [ネイティブアプリSDK](#qs-basic-setup-native) に示す手順に従ってください。SDK を アプリ内のWebViewで使用する場合は、以下の [WebView SDK](#qs-basic-setup-web) に示す手順に従ってください。

#### <a id="qs-basic-setup-native"></a>ネイティブアプリSDK

SDKの初期化には、Android [アプリケーション][android-application] のグローバルクラスを使用することを推奨します。アプリ内に存在しない場合、以下の手順に従ってください。

- `Application`を継承したクラスを作成します。
- アプリの`AndroidManifest.xml`ファイルを開き、`<application>`エレメントを確認します。
- `android:name`属性を追加し、先頭にドット（.）を付けて新規アプリケーションのクラス名をセットします。

   サンプルアプリの場合、`GlobalApplication`という名前の`Application`クラスを使用しているため、マニフェストファイルの設定は以下の通りになります。

    ```xml
     <application
       android:name=".GlobalApplication"
       <!-- ...-->
    </application>
    ```
    
-  `Application`クラスの`onCreate`メソッドをご確認いただき、無い場合は作成してください。また、以下のコードを追加してAdjust SDKを初期化してください。
 
    ```java
    import com.adjust.sdk.Adjust;
    import com.adjust.sdk.AdjustConfig;

    public class GlobalApplication extends Application {
        @Override
        public void onCreate() {
            super.onCreate();

            String appToken = "{YourAppToken}";
            String environment = AdjustConfig.ENVIRONMENT_SANDBOX;
            AdjustConfig config = new AdjustConfig(this, appToken, environment);
            Adjust.onCreate(config);
        }
    }
    ```

`{YourAppToken}`にアプリトークンを代入してください。アプリトークンは[管理画面][dashboard]で確認することができます。

アプリのビルドをテスト用（Sandbox）か本番用（Production）に分けるためには、SDK内の環境`environment`をいずれかにセットする必要があります。

```java
String environment = AdjustConfig.ENVIRONMENT_SANDBOX;
String environment = AdjustConfig.ENVIRONMENT_PRODUCTION;
```

**重要：** リリース前のテスト段階では、`AdjustConfig.ENVIRONMENT_SANDBOX`に設定してください。アプリをストアに申請する前に、SDKの環境を`AdjustConfig.ENVIRONMENT_PRODUCTION`にご変更ください。再度開発やテストを行う場合は、設定を`AdjustConfig.ENVIRONMENT_SANDBOX`に戻してください。

Adjustはこの環境設定を使用して、本番用の計測数値とテスト端末からのテスト計測を区別してレポート画面に表示します。この値の設定には常に注意が必要ですが、課金イベントを計測する場合は特に気をつけてください。

#### <a id="qs-basic-setup-web"></a>Web Views SDK

`WebView` オブジェクトのリファレンスを取得後：

- `webView.getSettings().setJavaScriptEnabled(true)` を呼び出して、JavaScriptをWebViewで有効化します
- `AdjustBridge.registerAndGetInstance(getApplication(), webview)` をコールして `AdjustBridgeInstance` のデフォルトインスタンスを起動します
- これにより、Adjust ブリッジがWebViewの JavaScript Interface として登録されます

以上の手順を完了すると、アクティビティは次のようになります：

```java
public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WebView webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());

        AdjustBridge.registerAndGetInstance(getApplication(), webview);
        try {
            webView.loadUrl("file:///android_asset/AdjustExample-WebView.html");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

この手順が完了すると、アプリに Adjust ブリッジが追加されます。JavaScript ブリッジは、Adjust のネイティブAndroid SDK と、WebViewにロードされるページの間での通信が可能となります。

HTMLファイルに、アセットのルートフォルダにある Adjust JavaScript をインポートしてください。HTML ファイルが同じ場所にある場合は、以下のような形でインポートできます：

```html
<script type="text/javascript" src="adjust.js"></script>
<script type="text/javascript" src="adjust_event.js"></script>
<script type="text/javascript" src="adjust_config.js"></script>
```

JavaScript ファイルにリファレンスを追加したら、 HTML ファイル内でそれを使って Adjust SDK を初期化します。

```js
let yourAppToken = '{YourAppToken}';
let environment = AdjustConfig.EnvironmentSandbox;
let adjustConfig = new AdjustConfig(yourAppToken, environment);

Adjust.onCreate(adjustConfig);
```

`{YourAppToken} `をアプリトークンに置き換えます。これは[管理画面][dashboard]から確認できます。
次に、テスト中か本番用かに応じて`environment` の値を設定します。

```js
let environment = AdjustConfig.EnvironmentSandbox;
let environment = AdjustConfig.EnvironmentProduction;
```

**重要：** アプリのテスト中は（その場合に限り）、値を `AdjustConfig.EnvironmentSandbox` に設定してください。アプリをストアに申請する前に、対象環境が `AdjustConfig.EnvironmentProduction` と設定されていることを確認してください。開発やテストを再開する場合は、値を `AdjustConfig.EnvironmentSandbox` に戻してください。

Adjustはこの環境設定を使用して、本番用の計測数値とテスト端末からのテスト計測を区別してレポート画面に表示します。

### <a id="qs-session-tracking"></a>セッショントラッキング

**注意**：この手順は**非常に重要です**。**必ずアプリに正しく実装されていることを確認**してください。この実装を行うことにより、アプリ内のAdjust SDKで適切なセッション計測が可能になります。
 
#### <a id="qs-session-tracking-api-14"></a>APIレベルが14以降

- `ActivityLifecycleCallbacks`インターフェースを実装したプライベートクラスを追加します。このインターフェースを利用できなければ、そのアプリのAndroid APIレベルは14未満を対象としています。アクティビティをそれぞれ手動でアップデートする必要がありますので、こちらの[ガイド](#qs-session-tracking-api-9)を参照してください。以前に`Adjust.onResume`および`Adjust.onPause`のコールを使っていた場合、これらを削除する必要があります。

- `onActivityResumed(Activity activity)`メソッドを編集して、`Adjust.onResume()`のコールを追加します。`onActivityPaused(Activity activity)`メソッドを編集して、`Adjust.onPause()`のコールを追加します。

- Adjust SDKの設定で、`onCreate()`メソッドを追加します。作成した`ActivityLifecycleCallbacks`のコールと、作成した`registerActivityLifecycleCallbacks`クラスのインスタンスを追加してください。
 
    ```java
    import com.adjust.sdk.Adjust;
    import com.adjust.sdk.AdjustConfig;

    public class GlobalApplication extends Application {
        @Override
        public void onCreate() {
            super.onCreate();

            String appToken = "{YourAppToken}";
            String environment = AdjustConfig.ENVIRONMENT_SANDBOX;
            AdjustConfig config = new AdjustConfig(this, appToken, environment);
            Adjust.onCreate(config);

            registerActivityLifecycleCallbacks(new AdjustLifecycleCallbacks());

            //...
        }

         private static final class AdjustLifecycleCallbacks implements ActivityLifecycleCallbacks {
             @Override
             public void onActivityResumed(Activity activity) {
                 Adjust.onResume();
             }

             @Override
             public void onActivityPaused(Activity activity) {
                 Adjust.onPause();
             }

             //...
         }
      }
    ```
 
#### <a id="qs-session-tracking-api-9"></a>レベル9から13のAPI

Gradleの`minSdkVersion`が`9`から`13`の場合、`14`以上にアップデートすると、今後の連携の手順が容易になります。Android公式[管理画面][dashboard]にて、最新バージョン関する情報をご確認ください。

セッショントラッキングを正しく行うためには、Acticityの開始または停止ごとにAdjust SDKの該当メソッドをコールする必要があります。この設定を行わないと、SDKはセッション開始やセッション終了を見落とす可能性があります。適切にセッションをトラッキングするには、**全てのActivityに対して以下の作業を行なってください。**

- Activityの`onResume`メソッド中に`Adjust.onResume()`へのコールを追加してください。必要に応じてメソッドを作成してください。
- Activityの`onPause`メソッド中に`Adjust.onPause()`へのコールを追加してください。必要に応じてメソッドを作成してください。

これらの手順を行うと、Activityは以下のように記述されます。

```java
import com.adjust.sdk.Adjust;
// ...
public class YourActivity extends Activity {
    protected void onResume() {
        super.onResume();
        Adjust.onResume();
    }
    protected void onPause() {
        super.onPause();
        Adjust.onPause();
    }
    // ...
}
```

これと同じ手順をアプリの**すべて**のActivityに行なってください。将来新しいActivityを作成する場合、この手順を忘れないでください。コーディングスタイルの違いによって、すべてのActivityに対する共通のスーパークラスにこれを実装するという方法もあります。


### <a id="qs-sdk-signature"></a>SDKシグネチャー

Adjust SDK シグネチャーの有効化は、Adjustのアカウントマネージャーが行います。ご利用をご希望のクライアント様は、Adjust サポート (support@adjust.com) までご連絡ください。

アカウントで SDK シグネチャーが既に有効化され、Adjust 管理画面にあるアプリシークレット にアクセスする必要がある場合は、以下の方法でアプリに SDK シグネチャーを実装してください。

アプリシークレットは、設定するインスタンスで `setAppSecret` を呼び出して登録されます。

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
AdjustConfig config = new AdjustConfig(this, appToken, environment);
config.setAppSecret(secretId, info1, info2, info3, info4);
Adjust.onCreate(config);
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
let adjustConfig = new AdjustConfig(yourAppToken, environment);
adjustConfig.setAppSecret(secretId, info1, info2, info3, info4);
Adjust.onCreate(adjustConfig);
```
</td>
</tr>
</table>


### <a id="qs-adjust-logging"></a>Adjustログの取得

Configインスタンスの`setLogLevel`に設定するパラメータを変更することによって、記録するログのレベルを調節できます。
 
<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
config.setLogLevel(LogLevel.VERBOSE); // enable all logs
config.setLogLevel(LogLevel.DEBUG); // disable verbose logs
config.setLogLevel(LogLevel.INFO); // disable debug logs (default)
config.setLogLevel(LogLevel.WARN); // disable info logs
config.setLogLevel(LogLevel.ERROR); // disable warning logs
config.setLogLevel(LogLevel.ASSERT); // disable error logs
config.setLogLevel(LogLevel.SUPRESS); // disable all logs
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
adjustConfig.setLogLevel(AdjustConfig.LogLevelVerbose); // enable all logs
adjustConfig.setLogLevel(AdjustConfig.LogLevelDebug); // disable verbose logs
adjustConfig.setLogLevel(AdjustConfig.LogLevelInfo); // disable debug logs (default)
adjustConfig.setLogLevel(AdjustConfig.LogLevelWarn); // disable info logs
adjustConfig.setLogLevel(AdjustConfig.LogLevelError); // disable warning logs
adjustConfig.setLogLevel(AdjustConfig.LogLevelAssert); // disable error logs
adjustConfig.setLogLevel(AdjustConfig.LogLevelSuppress); // disable all logs
```
</td>
</tr>
</table>

すべてのログの出力を無効にする場合、ログレベルをsuppressに設定する他に、configオブジェクトのコンストラクタを使用してください (抑制されたログレベルがサポートされるべきかどうかを判定するboolean値が得られます)。

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
AdjustConfig config = new AdjustConfig(this, appToken, environment, true);
config.setLogLevel(LogLevel.SUPRESS);
Adjust.onCreate(config);
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
let adjustConfig = new AdjustConfig(yourAppToken, environment, true);
adjustConfig.setLogLevel(AdjustConfig.LogLevelSuppress);
Adjust.onCreate(adjustConfig);
```
</td>
</tr>
</table>
 
### <a id="qs-build-the-app"></a>アプリのビルド
 
Androidアプリをビルドして実行します。`LogCat`ビューアにて`tag:Adjust`フィルタを設定し、他のすべてのログを非表示にすることができます。アプリが起動された後、`Install tracked`のログが出力されるはずです。

## ディープリンキング

### <a id="dl"></a>ディープリンク

URLからアプリへのディープリンクを使ったAdjustトラッカーURLをご利用の場合、ディープリンクURLとその内容の情報を得られる可能性があります。 ユーザーがすでにアプリをインストールしている状態でそのURLに訪れた場合(スタンダード・ディープリンキング)と、 アプリをインストールしていないユーザーがURLを開いた場合(ディファード・ディープリンキング)が有り得ます。 スタンダード・ディープリンキングの場合、Androidのプラットフォームにはディープリンクの内容を取得できる仕組みがあります。 ディファード・ディープリンキングに対してはAndroidプラットフォームはサポートしていませんので、 Adjust SDKがディープリンクの内容を取得するメカニズムを提供します。


### <a id="dl-standard"></a>スタンダード・ディープリンキング

アプリをすでにインストールしているユーザーが`deep_link`パラメータのついたAdjustのトラッカーURLを叩いた後でアプリを立ち上げたい場合、 アプリのディープリンキングを有効化する必要があります。**ユニークスキーム名**を選択し、リンクがクリックされてアプリが開いた時に起動したいアクティビティを 指定することで有効化できます。これは`AndroidManifest.xml`内で設定できます。マニフェストファルの該当のアクティビティ定義に `intent-filter`セクションを追加し、該当のスキーム名に`android:scheme`プロパティを指定してください。

```xml
<activity
    android:name=".MainActivity"
    android:configChanges="orientation|keyboardHidden"
    android:label="@string/app_name"
    android:screenOrientation="portrait">

    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>

    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="adjustExample" />
    </intent-filter>
</activity>
```

この設定をすると、トラッカーURLがクリックされた時にアプリを開くには、AdjustトラッカーURLの`deep_link`パラメータにあるスキーム名を指定する必要があります。 ディープリンクに情報を追加していないトラッカーURLは次のようになります。

```
https://app.adjust.com/abc123?deep_link=adjustExample%3A%2F%2F
```

`deep_link`パラメータの値は**URLエンコード**される必要があります。

トラッカーURLをクリック後、アプリが上記の設定をされていれば、アプリは`MainActivity`インテントの通りに起動します。 `MainActivity`クラス内で`deep_link`パラメータの内容が自動的に提供されます。 届けられたこの情報は**エンコードされていません**が、URL内ではエンコードされています。

`AndroidManifest.xml`ファイルのアクティビティの`android:launchMode`設定によっては、`deep_link`パラメータの内容情報は アクティビティファイルの適切な箇所に届けられます。`android:launchMode`のとり得る値について詳しくは[Android公式資料][android-launch-modes]をご確認ください。

指定のアクティビティに`Intent`オブジェクトを介してディープリンクの内容情報を送ることができる場所は2か所あります。 アクティビティの`onCreate`メソッドか`onNewIntent`メソッドのいずれかです。アプリが起動してこれらのどちらかのメソッドが呼ばれると、 クリックURL中の`deep_link`パラメータ内の実際に渡されたディープリンクを取得することができます。 この情報はロジックを追加する際に使うことができます。

これらのメソッドからディープリンク情報を抽出する方法は以下の通りです。

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Intent intent = getIntent();
    Uri data = intent.getData();
    // data.toString() -> This is your deep_link parameter value.
}
```

```java
@Override
protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);

    Uri data = intent.getData();
    // data.toString() -> This is your deep_link parameter value.
}
```

### <a id="dl-deferred"></a>ディファード・ディープリンキング

ユーザーが`deep_link`パラメータのついたトラッカーURLをクリックし、アプリ未インストールの場合はディファード・ディープリンキングが作動します。クリックしたユーザーはアプリストアにリダイレクトされます。アプリをダウンロードし初回起動したタイミングで、`deep_link`パラメータの内容がアプリに送信されます。

Adjust SDKはデフォルトでディファード・ディープリンクを開きます。追加設定の必要はありません。

#### ディファード・ディープリンク のコールバック

コールバック関数を用いて、SDKがディファードディープリンクを開くかどうかを決めることができます。

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
AdjustConfig config = new AdjustConfig(this, appToken, environment);

// Evaluate the deeplink to be launched.
config.setOnDeeplinkResponseListener(new OnDeeplinkResponseListener() {
    @Override
    public boolean launchReceivedDeeplink(Uri deeplink) {
        // ...
        if (shouldAdjustSdkLaunchTheDeeplink(deeplink)) {
            return true;
        } else {
            return false;
        }
    }
});

Adjust.onCreate(config);
```

Adjust SDKがサーバーからディープリンク情報を受信すると、リスナ内のディープリンク内容の情報を送信しますので、`boolean`値を返してください。 この値はAdjust SDKがディープリンクから指定したスキーム名へアクティビティを起動させたいかどうかで決定してください。 スキーム名の指定はスタンダード・ディープリンキングと同様です。

`true`を返すと、[スタンダード・ディープリンキング](#dl-standard) の章で説明したものと同様に起動します。 SDKにアクティビティをスタートさせたくない場合、リスナから`false`を返してください。 ディープリンクの内容に基づいてアプリの次の挙動を決定してください。
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
let adjustConfig = new AdjustConfig(yourAppToken, environment);
adjustConfig.setDeferredDeeplinkCallback(function (deeplink) {});

Adjust.onCreate(adjustConfig);
```

このディファード・ディープリンクのシナリオでは、Configオブジェクトにもう1つの設定を追加できます。Adjust SDK がディファード・ディープリンクに関する情報を入手すると、SDK で URL を開くかどうかを選択できます。このオプションについては、Configオブジェクトで `setOpenDeferredDeeplink` メソッドをコールして設定します：

```js
// ...

function deferredDeeplinkCallback(deeplink) {}

let adjustConfig = new AdjustConfig(yourAppToken, environment);
adjustConfig.setOpenDeferredDeeplink(true);
adjustConfig.setDeferredDeeplinkCallback(deferredDeeplinkCallback);

Adjust.start(adjustConfig);

```

コールバックを設定しない場合、**Adjust SDK はデフォルトで常に URL を立ち上げようとすることにご注意ください。**
</td>
</tr>
</table>

### <a id="dl-reattribution"></a>ディープリンクを介したリアトリビューション

Adjustはディープリンクを使ったリエンゲージメントキャンペーンをサポートしています。 詳しくは[公式資料][reattribution-with-deeplinks]をご覧ください。

ディープリンクご利用の場合、ユーザーが正しくリアトリビューションされるために、Adjust SDKへのコールを追加してください。

アプリでディープリンクの内容データを受信したら、`Adjust.appWillOpenUrl(Uri, Context)`メソッドへのコールを追加してください。 このコールによって、Adjust SDKはディープリンクの中に新たなアトリビューションが存在するかを調べ、あった場合はAdjustサーバーにこれを送信します。 ディープリンクのついたAdjustトラッカーURLのクリックによってユーザーがリアトリビュートされる場合、 [アトリビューションコールバック](#af-attribution-callback) がこのユーザーの新しいアトリビューションデータで呼ばれます。

`Adjust.appWillOpenUrl(Uri, Context)`のコールは下記のようになります。

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Intent intent = getIntent();
    Uri data = intent.getData();
    Adjust.appWillOpenUrl(data, getApplicationContext());
}
```

```java
@Override
protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);

    Uri data = intent.getData();
    Adjust.appWillOpenUrl(data, getApplicationContext());
}
```

**注**: Android SDK v4.14.0より`Adjust.appWillOpenUrl(Uri)` メソッドは**deprecated**（推奨されていません） と表示されます。代わりに`Adjust.appWillOpenUrl(Uri, Context)`メソッドを使用してください。

**Web viewに関する注意**:このコールは、webviewから関数`Adjust.appWillOpenUrl`をJavascript でこのように作成することもできます:

```js
Adjust.appWillOpenUrl(deeplinkUrl);
```

## イベントトラッキング

### <a id="et-tracking"></a>イベントトラッキング

Adjustを使ってアプリ内のイベントをトラッキングすることができます。ここではあるボタンのタップを毎回トラックするケースを想定して説明します。 [管理画面][dashboard]にてイベントトークンを作成します。そのイベントトークンは仮に`abc123`とします。 タップをトラックするため、ボタンの`onClick`メソッドに以下のような記述を追加します。

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
AdjustEvent adjustEvent = new AdjustEvent("abc123");
Adjust.trackEvent(adjustEvent);
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
let adjustEvent = new AdjustEvent('abc123');
Adjust.trackEvent(adjustEvent);
```
</td>
</tr>
</table>
 
### <a id="et-revenue"></a>収益のトラッキング

広告をタップはもちろん、アプリ内課金の発生時もトラッキングできます。 1回のタップで1ユーロセントの報酬と仮定すると、報酬イベントは以下のようになります。

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
AdjustEvent adjustEvent = new AdjustEvent("abc123");
adjustEvent.setRevenue(0.01, "EUR");
Adjust.trackEvent(adjustEvent);
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
let adjustEvent = new AdjustEvent('abc123');
adjustEvent.setRevenue(0.01, 'EUR');
Adjust.trackEvent(adjustEvent);
```
</td>
</tr>
</table>

もちろんこれはコールバックパラメータと紐付けることができます。

通貨トークンを設定する場合、Adjustは自動的に収益を任意の報酬に変換します。 詳しくは[通貨の変換][currency-conversion]をご覧ください。

アプリ内課金のトラッキングをする際は、課金が完了し、アイテムが購入された後に`trackEvent`をコールするようにしてください。実際に発生しなかった収益イベントをトラッキングしてしまうのを防ぐためです。

収益とイベントトラッキングについては[イベントトラッキングガイド][event-tracking]もご参照ください。

### <a id="et-revenue-deduplication"></a>収益の重複排除

オプションとしてオーダーIDを追加することにより、収益イベントが重複してトラッキングされるのを防ぐことができます。これを実行すると、10個前までのオーダーIDが記憶され、収益イベントに紐づけられたオーダーIDが重複している場合、そのイベントを排除します。以下の例をご参照ください。

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
AdjustEvent adjustEvent = new AdjustEvent("abc123");
adjustEvent.setRevenue(0.01, "EUR");
adjustEvent.setOrderId("{OrderId}");
Adjust.trackEvent(adjustEvent);
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
let adjustEvent = new AdjustEvent('abc123');
adjustEvent.setRevenue(0.01, 'EUR');
adjustEvent.setOrderId('{OrderId}');
Adjust.trackEvent(event);
```
</td>
</tr>
</table>

### <a id="et-purchase-verification"></a>アプリ内課金の検証

Adjustのサーバーサイドのレシート検証ツール、課金検証（Purchase Verification）を使ってアプリ内で行われたアプリ内課金の妥当性を調べる際は、 Android purchase SDKをご利用ください。詳しくは[こちら][android-purchase-verification]をご覧ください。

## カスタムパラメータ

### <a id="cp"></a>カスタムパラメータ

Adjust SDK が標準仕様で取得するパラメータ（IDFAなど）に加え、Adjust SDK を使ってカスタム値（クライアント様が保有するユーザー ID、商品 ID など）をイベントまたはセッションに追加することができます。カスタムパラメータは、コールバック経由でのみ送信が可能で、Adjust 管理画面のレポートに表示されることはありません。

コールバックパラメータはクライアント様だけがご利用可能です。外部パートナーにデータが送信されるパートナーパラメータに関しては、情報を共有するパートナーとの間で利用されなければなりません。内部と外部のパートナー双方の利用目的で値をトラッキングする場合は（例えば商品 ID など）、リアルタイムコールバックとパートナーパラメータの両方にてトラッキングすることを推奨します。

### <a id="cp-event-parameters"></a>イベントパラメータ

### <a id="cp-event-callback-parameters"></a>イベントコールバックパラメータ

[管理画面][dashboard]でイベントにコールバックURLを登録することができます。イベントがトラッキングされるたびに そのURLにGETリクエストが送信されます。トラッキングする前にイベントで`addCallbackParameter`をコールすることによって、 イベントにコールバックパラメータを追加できます。そうして追加されたパラメータはコールバックURLに送られます。

例えば、コールバックURLに`http://www.example.com/callback`を登録した場合、イベントトラッキングは以下のようになります。

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
AdjustEvent adjustEvent = new AdjustEvent("abc123");
adjustEvent.addCallbackParameter("key", "value");
adjustEvent.addCallbackParameter("foo", "bar");
Adjust.trackEvent(adjustEvent);
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
let adjustEvent = new AdjustEvent('abc123');
adjustEvent.addCallbackParameter('key', 'value');
adjustEvent.addCallbackParameter('foo', 'bar');
Adjust.trackEvent(adjustEvent);
```
</td>
</tr>
</table>

この場合、Adjustはこのイベントをトラッキングし、以下にリクエストが送られます。

```
http://www.example.com/callback?key=value&foo=bar
```

パラメータの値として使われることのできるプレースホルダは、`{gps_adid}`のような様々な形に対応しています。 得られるコールバック内で、このプレースホルダは該当デバイスのGoogle PlayサービスIDに置き換えられます。 独自に設定されたパラメータには何も格納しませんが、コールバックに追加されます。 イベントにコールバックを登録していない場合は、これらのパラメータは使われません。

URLコールバックについて詳しくは[コールバックガイド][callbacks-guide]をご覧ください。 利用可能な値のリストもこちらで参照してください。
 
### <a id="cp-event-partner-parameters"></a>パートナーパラメータ

Adjustの管理画面上で連携が有効化されているネットワークパートナーに送信するパラメータを設定することができます。

これは上記のコールバックパラメータと同様に機能しますが、 eventインスタンスの`addPartnerParameter`メソッドをコールすることにより追加されます。

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
AdjustEvent adjustEvent = new AdjustEvent("abc123");
adjustEvent.addPartnerParameter("key", "value");
adjustEvent.addPartnerParameter("foo", "bar");
Adjust.trackEvent(adjustEvent);
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
let adjustEvent = new AdjustEvent('abc123');
adjustEvent.addPartnerParameter('key', 'value');
adjustEvent.addPartnerParameter('foo', 'bar');
Adjust.trackEvent(adjustEvent);
```
</td>
</tr>
</table>

スペシャルパートナーとその統合について詳しくは[連携パートナーガイド][special-partners]をご覧ください。

### <a id="cp-event-callback-id"></a>イベントコールバックID

トラッキングしたいイベントにカスタムIDを追加できます。このIDはイベントをトラッキングし、成功か失敗かの通知を受け取けとれるようコールバックを登録することができます。イベントインスタンスに`setCallbackId`メソッドをコールしてこのIDを設定してください：

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
AdjustEvent adjustEvent = new AdjustEvent("abc123");
adjustEvent.setCallbackId("Your-Custom-Id");
Adjust.trackEvent(adjustEvent);
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
let adjustEvent = new AdjustEvent('abc123');
adjustEvent.setCallbackId('Your-Custom-Id');
Adjust.trackEvent(adjustEvent);
```
</td>
</tr>
</table>

### <a id="cp-session-parameters"></a>セッションパラメータ

いくつかのパラメータは、Adjust SDKのイベントごと、セッションごとに送信するために保存されます。 このいずれかのパラメータを追加すると、これらはローカル保存されるため、毎回追加する必要はありません。 同じパラメータを再度追加しても何も起こりません。

これらのセッションパラメータはAdjust SDKが起動する前にコールすることができます。インストール時にパラメータを送信したい場合は、Adjust SDKの初回起動を[遅らせる](#cp-delay-start)ことができます。 ただし、必要なパラメータの値を得られるのは起動後となります。

### <a id="cp-session-callback-parameters"></a>セッションコールバックパラメータ

[イベント](#cp-event-callback-parameters)で設定された同じコールバックパラメータを、 Adjust SDKのイベントごとまたはセッションごとに送信するために保存することもできます。

セッションコールバックパラメータのインターフェイスとイベントコールバックパラメータは似ています。 イベントにキーと値を追加する代わりに、Adjustの`Adjust.addSessionCallbackParameter(String key, String value)`へのコールで追加されます。
 
<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
Adjust.addSessionCallbackParameter("foo", "bar");
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
Adjust.addSessionCallbackParameter('foo', 'bar');
```
</td>
</tr>
</table>

セッションコールバックパラメータは、イベントに追加されたコールバックパラメータとマージされます。 イベントに追加されたコールバックパラメータは、セッションコールバックパラメータより優先されます。 イベントに追加されたコールバックパラメータがセッションから追加されたパラメータと同じキーを持っている場合、 イベントに追加されたコールバックパラメータの値が優先されます。

`Adjust.removeSessionCallbackParameter(String key)`メソッドに指定のキーを渡すことで、 特定のセッションコールバックパラメータを削除することができます。
 
<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
Adjust.removeSessionCallbackParameter("foo");
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
Adjust.removeSessionCallbackParameter('foo');
```
</td>
</tr>
</table>

セッションコールバックパラメータからすべてのキーと値を削除したい場合は、`Adjust.resetSessionCallbackParameters()`メソッドを使ってリセットすることができます。

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
Adjust.resetSessionCallbackParameters();
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
Adjust.resetSessionCallbackParameters();
```
</td>
</tr>
</table>

### <a id="cp-session-partner-parameters"></a>セッションパートナーパラメータ

Adjust SDKのイベントごとやセッションごとに送信される[セッションコールバックパラメータ](#cp-session-callback-parameters)があるように、 セッションパートナーパラメータも用意されています。

これらはAdjustのネットワークパートナーに送信されます。Adjust[管理画面][dashboard]のパートナー設定で有効化された連携に利用されます。

セッションパートナーパラメータのインターフェイスとイベントパートナーパラメータは似ています。 イベントにキーと値を追加する代わりに、`Adjust.addSessionPartnerParameter(String key, String value)`へのコールで追加されます。

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
Adjust.addSessionPartnerParameter("foo", "bar");
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
Adjust.addSessionPartnerParameter('foo', 'bar');
```
</td>
</tr>
</table>

セッションパートナーパラメータはイベントに追加されたパートナーパラメータとマージされます。イベントに追加されたパートナーパラメータは、 セッションパートナーパラメータより優先されます。イベントに追加されたパートナーパラメータが セッションから追加されたパラメータと同じキーを持っている場合、イベントに追加されたパートナーパラメータの値が優先されます。

`Adjust.removeSessionPartnerParameter(String key)`メソッドに指定のキーを渡すことで、 特定のセッションパートナーパラメータを削除することができます。

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
Adjust.removeSessionPartnerParameter("foo");
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
Adjust.removeSessionPartnerParameter('foo');
```
</td>
</tr>
</table>

セッションパートナーパラメータからすべてのキーと値を削除したい場合は、`Adjust.resetSessionPartnerParameters()`メソッドを使ってリセットすることができます。

 
<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
Adjust.resetSessionPartnerParameters();
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
Adjust.resetSessionPartnerParameters();
```
</td>
</tr>
</table>

### <a id="cp-delay-start"></a>ディレイスタート

Adjust SDKの初回起動を遅らせると、クライアント様のユニークID（会員ID）などのセッションパラメータを取得しインストール時に送信できるようにすることができます。

Configインスタンスの`setDelayStart`メソッドで、ディレイタイムを秒単位で設定できます。

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
adjustConfig.setDelayStart(5.5);
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```java
adjustConfig.setDelayStart(5.5);
```
</td>
</tr>
</table>

この場合、Adjust SDKは最初のインストールセッションと生成されるイベントを初めの5.5秒間は送信しません。 設定された時間が過ぎるまで、もしくは`Adjust.sendFirstPackages()`がコールされるまで、 セッションパラメータはすべてディレイインストールセッションとイベントに追加され、Adjust SDKは通常通り再開します。


**Adjust SDKのディレイスタートは最大で10秒です。**

## 追加機能
 
プロジェクトにAdjust SDKを連携させると、以下の機能をご利用できるようになります。

### <a id="af-push-token"></a>Pushトークン (uninstall tracking)

プッシュトークンは、オーディエンスビルダーとコールバック送信に使用されます。また、アンインストールと再インストールのトラッキングにも必要です。

Adjust にプッシュ通知トークンを送信する際には、トークンを入手（または値に変更が発生）した後、以下のコールをAdjust に追加してください。

<table>
<tr>
<td>
<b>Native SDK</b>
</td>
</tr>
<tr>
<td>

```java
Adjust.setPushToken(pushNotificationsToken, context);
```

シグネチャーに`context`を追加しアップデートすると、SDKはより多くのシナリオに沿ってプッシュトークンを送信することができます。上記のメソッドを使用することを推奨します。

しかし、`context`が追加されない場合でも、Adjustは引き続き、同じメソッドを使用する過去のシグネチャーをサポートします。

</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
Adjust.setPushToken(pushNotificationsToken);
```
</td>
</tr>
</table>

### <a id="af-attribution-callback"></a>アトリビューションコールバック

トラッカーのアトリビューション変化の通知を受けるために、リスナを登録することができます。 アトリビューションには複数の流入元が紐づく可能性があるため、この情報は同時に送ることができません。

[アトリビューションデータに関するポリシー][attribution-data]を必ずご確認ください。

`Config`インスタンスで、SDKをスタートする前に以下のアトリビューションコールバックを追加してください。

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
AdjustConfig config = new AdjustConfig(this, appToken, environment);

config.setOnAttributionChangedListener(new OnAttributionChangedListener() {
    @Override
    public void onAttributionChanged(AdjustAttribution attribution) {}
});

Adjust.onCreate(config);
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
function attributionCallback(attribution) {}

// ...

let adjustConfig = new AdjustConfig(yourAppToken, environment);
adjustConfig.setAttributionCallback(attributionCallback);
Adjust.onCreate(adjustConfig);
```
</td>
</tr>
</table>

リスナはSDKが最後のアトリビューションデータを取得した時に呼ばれます。 リスナの機能で`attribution`パラメータを確認することができます。このパラメータのプロパティの概要は以下の通りです。


- `trackerToken` 最新アトリビューションのトラッカートークン
- `trackerName` 最新アトリビューションのトラッカー名
- `network` 最新アトリビューションの流入元名
- `campaign` 最新アトリビューションのキャンペーン名
- `adgroup` 最新アトリビューションのアドグループ名
- `creative` 最新アトリビューションのクリエイティブ名
- `clickLabel` 最新アトリビューションのクリックラベル
- `adid` AdjustユニークID（Adjust Device ID）

### <a id="af-ad-revenue"></a>広告収益の計測

**注**：この機能はネイティブのSDK v4.18.0以降のみ利用可能です。

Adjust SDKを利用して、以下のメソッドを呼び出し広告収益情報を計測することができます。

<table> 
<tr> 
<td> 
<b>ネイティブアプリSDK</b>
</td> 
</tr> 
<tr> 
<td> 

```java
Adjust.trackAdRevenue(source, payload);
```
</td> 
</tr> 
</table> 

Adjust SDKにパスするメソッドの引数は以下の通りです。
- `source` - 広告収益情報のソースを指定する`String`オブジェクト
- `payload` - `広告収益のJSONを格納する`JSONObject`オブジェクト

現在、弊社は以下の`source`パラメータの値のみ対応しています。
- `AD_REVENUE_MOPUB` - メディエーションプラットフォームのMoPubを示します。（詳細は、[統合ガイド][sdk2sdk-mopub]を参照ください）

### <a id="af-session-event-callbacks"></a>イベントとセッションのコールバック

イベントとセッションの双方もしくはどちらかをトラッキングし、成功か失敗かの通知を受け取れるようリスナを登録することができます。リスナには４種類あります。それらは、トラッキングに成功したイベント、トラッキングに失敗したイベント、トラッキングに成功したセッション、トラッキングに失敗したイベントです。 Configオブジェクトを生成すると、リスナをいくつでも追加することができます。

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
AdjustConfig config = new AdjustConfig(this, appToken, environment);

// Set event success tracking delegate.
config.setOnEventTrackingSucceededListener(new OnEventTrackingSucceededListener() {
    @Override
    public void onFinishedEventTrackingSucceeded(AdjustEventSuccess eventSuccessResponseData) {
        // ...
    }
});

// Set event failure tracking delegate.
config.setOnEventTrackingFailedListener(new OnEventTrackingFailedListener() {
    @Override
    public void onFinishedEventTrackingFailed(AdjustEventFailure eventFailureResponseData) {
        // ...
    }
});

// Set session success tracking delegate.
config.setOnSessionTrackingSucceededListener(new OnSessionTrackingSucceededListener() {
    @Override
    public void onFinishedSessionTrackingSucceeded(AdjustSessionSuccess sessionSuccessResponseData) {
        // ...
    }
});

// Set session failure tracking delegate.
config.setOnSessionTrackingFailedListener(new OnSessionTrackingFailedListener() {
    @Override
    public void onFinishedSessionTrackingFailed(AdjustSessionFailure sessionFailureResponseData) {
        // ...
    }
});

Adjust.onCreate(config);
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
function eventSuccessCallback(eventSuccessResponseData) {}
function eventFailureCallback(eventFailureResponseData) {}
function sessionSuccessCallback(sessionSuccessResponseData) {}
function sessionFailureCallback(sessionFailureResponseData) {}

// ...

let adjustConfig = new AdjustConfig(yourAppToken, environment);
adjustConfig.setEventSuccessCallback(eventSuccessCallback);
adjustConfig.setEventFailureCallback(eventFailureCallback);
adjustConfig.setSessionSuccessCallback(sessionSuccessCallback);
adjustConfig.setSessionFailureCallback(sessionFailureCallback);
Adjust.onCreate(adjustConfig);
```
</td>
</tr>
</table>

リスナ関数はSDKがサーバーにパッケージ送信を試みた後で呼ばれます。 リスナ関数内でリスナ用のレスポンスデータオブジェクトを確認することができます。 レスポンスデータのプロパティの概要は以下の通りです。
 
- `message` サーバーからのメッセージまたはSDKのエラーログ
- `timestamp` サーバーからのタイムスタンプ
- `adid` Adjustから提供されるユニークデバイスID
- `jsonResponse` サーバーからのレスポンスのJSONオブジェクト

イベントのレスポンスデータは以下を含みます。

- `eventToken` トラッキングされたパッケージがイベントだった場合、そのイベントトークン
- `callbackId` イベントオブジェクトにカスタム設定された[コールバックID](#cp-event-callback-id)

失敗したイベントとセッションは以下を含みます。

- boolean `willRetry` しばらく後に再送を試みる予定であるかどうかを示します。


### <a id="af-user-attribution"></a>ユーザーアトリビューション

[アトリビューションコールバック](#af-attribution-callback) で説明したとおり、アトリビューション情報に変更がある度に、このコールバックが起動されます。Adjust インスタンスの以下のメソッドをコールすることで、必要な時にいつでもユーザーの最新のアトリビューション情報にアクセスすることができます：

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
AdjustAttribution attribution = Adjust.getAttribution();
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
let attribution = Adjust.getAttribution();
```
</td>
</tr>
</table>

**注意**： このコールは、**Adjust SDK v4.11.0** 以上で使用が可能です。

**注意**： 最新のアトリビューション情報は、Adjust がバックエンドで行うアプリインストールのトラッキングが完了し、アトリビューションコールバックがトリガーされた後にのみ利用が可能となります。SDK が初期化され、アトリビューションコールバックがトリガーされる前には、ユーザーのアトリビューション値にアクセスすることができません。

### <a id="af-device-ids"></a>デバイスID

Adjust SDK を使って、デバイス ID を取得することもできます。

### <a id="af-gps-adid"></a>Google広告ID / Google Play Services advertising identifier(gps_adid)

Google Analyticsなどの一部のサービスでは、レポートの重複を防ぐためにデバイスIDとクライアントIDを連携させることが求められます。

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

Google 広告 ID は、バックグラウンドスレッドでのみ読み取りが可能という制限付で、取得することができます。ファンクション`getGoogleAdId` をコンテクストとインスタンスの`OnDeviceIdsRead` を呼び出すと、どんな状況でも機能させることができます：

```java
Adjust.getGoogleAdId(this, new OnDeviceIdsRead() {
    @Override
    public void onGoogleAdIdRead(String googleAdId) {}
});
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

デバイスの Google 広告デバイス ID を取得する際には、Google 広告 ID を引数として受け取る `Adjust.getGoogleAdId` に対して、以下のようなコールバック機能を渡す必要があります。：

```js
Adjust.getGoogleAdId(function(googleAdId) {
    // ...
});
```
</td>
</tr>
</table>

### <a id="af-amazon-adid"></a>Amazon広告ID / Amazon advertising identifier(fire_adid)

Amazon 広告 ID を取得する必要がある場合は、`Adjust` インスタンスで、以下のメソッドを呼び出します：

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
String amazonAdId = Adjust.getAmazonAdId(context);
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
let amazonAdId = Adjust.getAmazonAdId();
```
</td>
</tr>
</table>

### <a id="af-adid"></a>Adjust デバイスID (adid)

アプリがインストールされている各デバイスに対して、Adjust は、バックエンドでユニークな **Adujust デバイス ID (adid)** を生成します。この ID を取得するためには、`Adjust` インスタンスで、以下のメソッドを呼び出してください：

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
String adid = Adjust.getAdid();
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
let adid = Adjust.getAdid();
```
</td>
</tr>
</table>

**注意**： このコールは、**Adjust SDK v4.11.0** 以上で使用が可能です。

**注意**：adid に関する情報は、Adjust がバックエンドで行うアプリインストールのトラッキングが完了後のみ利用可能となります。SDK が初期化されてアプリインストールのトラッキングが完了しないと、adid 値にアクセスすることはできません。

### <a id="af-pre-installed-trackers"></a>プレインストールのトラッカー

すでにアプリをインストールしたことのあるユーザーをAdjust SDKを使って識別したい場合は、次の手順で設定を行ってください。

- [管理画面][dashboard]上で新しいトラッカーを作成してください。
- App Delegateを開き、Configのデフォルトトラッカーを設定してください。

  <table>
  <tr>
  <td>
  <b>Native App SDK</b>
  </td>
  </tr>
  <tr>
  <td>

  ```java
  adjustConfig.setDefaultTracker("{TrackerToken}");
  ```
  </td>
  </tr>
  <tr>
  <td>
  <b>Web View SDK</b>
  </td>
  </tr>
  <tr>
  <td>

  ```js
  adjustConfig.setDefaultTracker('{TrackerToken}');
  ```
  </td>
  </tr>
  </table>

- `{TrackerToken}`にステップ2で作成したトラッカートークンを入れてください。 管理画面には`http://app.adjust.com/`を含むトラッカーURLが表示されます。 ソースコード内にはこのURLすべてではなく、6文字のトークンを抜き出して指定してください。

- アプリをビルドしてください。LogCatで下記のような行が表示されるはずです。
    ```
    Default tracker: 'abc123'
    ```

### <a id="af-offline-mode"></a>オフラインモード

Adjustのサーバーへの送信を一時停止し、保持されているトラッキングデータを後から送信するために Adjust SDKをオフラインモードにすることができます。 オフラインモード中はすべての情報がファイルに保存されるので、イベントをたくさん発生させすぎないようにご注意ください。

`true`パラメータで`setOfflineMode`を呼び出すとオフラインモードを有効にできます。

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
Adjust.setOfflineMode(true);
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
Adjust.setOfflineMode(true);
```
</td>
</tr>
</table>

反対に、`false`パラメータで`setOfflineMode`を呼び出せばオフラインモードを解除できます。 Adjust SDKがオンラインモードに戻った時、保存されていた情報は正しいタイムスタンプでAdjustのサーバーに送られます。

トラッキングの無効化とは異なり、この設定はセッション間で**記憶されません**。 オフラインモード時にアプリを終了しても、次に起動した時にはオンラインモードとしてアプリが起動します。

### <a id="af-disable-tracking"></a>トラッキングの無効化

`setEnabled`にパラメータ`false`を渡すことで、AdjustSDKが行うデバイスのアクティビティのトラッキングをすべて無効にすることができます。**この設定はセッション間で記憶されます**
 
<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
Adjust.setEnabled(false);
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
Adjust.setEnabled(false);
```
</td>
</tr>
</table>

Adjust SDKが現在有効かどうか、`isEnabled`関数を呼び出せば確認できます。 また、`setEnabled`関数に`true`を渡せば、Adjust SDKを有効にすることができます。
 
### <a id="af-event-buffering"></a>イベントバッファリング

イベントトラッキングを酷使している場合、HTTPリクエストを遅らせて1分毎にまとめて送信したほうがいい場合があります。 その場合は、Configインスタンスでイベントバッファリングを有効にしてください。

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
adjustConfig.setEventBufferingEnabled(true);
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
adjustConfig.setEventBufferingEnabled(true);
```
</td>
</tr>
</table>

### <a id="af-background-tracking"></a>バックグラウンドでのトラッキング

デフォルトでは、アプリがバックグラウンドにある間、Adjust SDKはHTTPリクエストの送信を停止します。 この設定はConfigインスタンスで変更できます。

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
adjustConfig.setSendInBackground(true);
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
adjustConfig.setSendInBackground(true);
```
</td>
</tr>
</table>

### <a id="af-gdpr-forget-me"></a>GDPR消去する権利（忘れられる権利）

次のメソッドを呼び出すと、EUの一般データ保護規制（GDPR）第17条に従い、ユーザーが消去する権利（忘れられる権利）を行使した際にAdjust SDKがAdjustバックエンドに情報を通知します。

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
Adjust.gdprForgetMe(context);
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
Adjust.gdprForgetMe();
```
</td>
</tr>
</table>

この情報を受け取ると、Adjustはユーザーのデータを消去し、Adjust SDKはユーザーの追跡を停止します。この削除された端末からのリクエストは今後、Adjustに送信されません。

この変更はテストを行なっている場合でも恒久的で、元の設定に戻すことは*できない*ことをご留意ください。


## トラブルシューティング

### <a id="tt-session-failed"></a>"Session failed (Ignoring too frequent session. ...)"というエラーが出る

このエラーはインストールのテストの際に起こりえます。アンインストール後に再度インストールするだけでは新規インストールとして動作しません。 SDKがローカルで統計したセッションデータを失ったとサーバーは判断してエラーメッセージを無視し、 その端末に関する有効なデータのみが与えられます。

この仕様はテスト中には厄介かもしれませんが、サンドボックスと本番用の挙動をできる限り近づけるために必要です。

アプリに対して、編集者レベル（または管理人権限）のアクセス権を持っている場合には、どんなデバイスに対するアプリのセッションデータについても、[テストコンソー][testing_console]ルを使って Adjust 管理画面から直接リセットすることができます。

端末に関する記録が消去されると、テスティングコンソールは`Forgot device`と返します。 もしその端末の記録がすでに消去されていたり、値が不正だった場合は、そのリンクは`Advertising ID not found`と返します。

現在ご契約のパッケージでアクセスが可能の場合は、[開発者用API][dev_api]で設定確認と端末記録の消去を行うことができます。

### <a id="tt-broadcast-receiver"></a>ブロードキャストレシーバーがインストールリファラを受信していない

[ガイド](#qs-gps-intent)に従って設定を済ませていれば、 ブロードキャストレシーバーはAdjustのSDKとサーバーにインストールを送信するよう設定されているはずです。

手動でテスト用インストールリファラを作動させることで確認できます。`com.your.appid`にアプリIDを入力し、Android Studioの [adb](http://developer.android.com/tools/help/adb.html)ツールで以下のコマンドを実行してください。

```
adb shell am broadcast -a com.android.vending.INSTALL_REFERRER -n com.your.appid/com.adjust.sdk.AdjustReferrerReceiver --es "referrer" "adjust_reftag%3Dabc1234%26tracking_id%3D123456789%26utm_source%3Dnetwork%26utm_medium%3Dbanner%26utm_campaign%3Dcampaign"
```

INSTALL_REFERRERインテントに対してすでに別のブロードキャストリファラを使っている状態でこの[ガイド][referrer]の設定をした場合、`com.adjust.sdk.AdjustReferrerReceiver`にブロードキャストレシーバーを入力してください。

-`n com.your.appid/com.adjust.sdk.AdjustReferrerReceiver`パラメータを削除することもできます。 削除すると、デバイスに入っているすべてのアプリが`INSTALL_REFERRER`インテントを受信します。

ログレベルを`verbose`に設定していれば、リファラが読み込まれると以下のログが表示されるはずです。

```
V/Adjust: Reading query string (adjust_reftag=abc1234&tracking_id=123456789&utm_source=network&utm_medium=banner&utm_campaign=campaign) from reftag
```

SDKのパッケージハンドラーに追加されるクリックパッケージは以下のような形です。

```
V/Adjust: Path:      /sdk_click
    ClientSdk: android4.6.0
    Parameters:
      app_token        abc123abc123
      click_time       yyyy-MM-dd'T'HH:mm:ss.SSS'Z'Z
      created_at       yyyy-MM-dd'T'HH:mm:ss.SSS'Z'Z
      environment      sandbox
      gps_adid         12345678-0abc-de12-3456-7890abcdef12
      needs_attribution_data 1
      referrer         adjust_reftag=abc1234&tracking_id=123456789&utm_source=network&utm_medium=banner&utm_campaign=campaign
      reftag           abc1234
      source           reftag
      tracking_enabled 1
```

アプリの起動前にこのテストを行う場合、パッケージの送信は表示されません。パッケージはアプリの起動後に送信されます。

**重要:** この機能をテストをするために`adb`ツールを利用することは推奨しておりません。全てのリファラコンテンツを`adb`でテストするためには（`&`で分けられた複数のパラメータがある場合）、ブロードキャストリシーバーで受信するためにコンテンツをエンコードすることが必要です。もしエンコードをしないと、`adb`はレファラを最初の&サインで切り、誤ったコンテンツをブロードキャストレシーバーに伝えます。アプリがどのようにエンコードされていないリファラを受信しているかを確認したい場合は、Adjustのサンプルアプリを利用して、`MainActivity.java`ファイルの`onFireIntentClick`メソッドのインテント内に送信されたコンテンツを変更してください:


```java
public void onFireIntentClick(View v) {
    Intent intent = new Intent("com.android.vending.INSTALL_REFERRER");
    intent.setPackage("com.adjust.examples");
    intent.putExtra("referrer", "utm_source=test&utm_medium=test&utm_term=test&utm_content=test&utm_campaign=test");
    sendBroadcast(intent);
}
```

自分の選んだコンテントで`putExtra`2番目のパラメーターを自由に変更してください。

### <a id="tt-event-at-launch"></a>アプリ起動時にイベントを始動したい

直感的には分かりにくいですが、グローバル`Application`クラスの`onCreate`メソッドはアプリ起動時だけでなく、 アプリによってシステムやイベントが作動する時にも呼ばれます。

Adjust SDKはこの場合の初期化についてサポートしています。この機能はアプリが実際に起動した時でなく、 アクティビティがスタートした時、たとえばユーザーがアプリを起動させた時に起こります。

これらのコールはアプリがユーザーの操作以外の要因で起動した場合にも、Adjust SDKを起動しイベントを送信しします。これはアプリの外部要因にもよります。

このように、アプリ起動時のイベントの作動はインストールとセッションの数を正確にトラッキングできません。

インストール後にイベントを作動させたい場合は、[アトリビューション変更時用のリスナ](#attribution_changed_listener)をご利用ください。

アプリ起動時にイベントを作動させたい場合は、スタートするアクティビティの`onCreate`メソッドをご使用ください。

[dashboard]:  http://adjust.com
[adjust.com]: http://adjust.com

[en-readme]:  ../../README.md
[zh-readme]:  ../chinese/README.md
[ja-readme]:  ../japanese/README.md
[ko-readme]:  ../korean/README.md

[example-java]:                   ../../Adjust/example-app-java
[example-kotlin]:                 ../../Adjust/example-app-kotlin
[example-tv]:                     ../../Adjust/example-app-tv
[example-webbridge]:              ../../Adjust/example-app-webbridge

[maven]:                          http://maven.org
[referrer]:                       ../japanese/multiple-receivers.md
[releases]:                       https://github.com/adjust/adjust_android_sdk/releases
[google_ad_id]:                   https://support.google.com/googleplay/android-developer/answer/6048248?hl=en
[event-tracking]:                 https://docs.adjust.com/ja/event-tracking
[callbacks-guide]:                https://docs.adjust.com/ja/callbacks
[new-referrer-api]:               https://developer.android.com/google/play/installreferrer/library.html
[special-partners]:               https://docs.adjust.com/ja/special-partners
[attribution-data]:               https://github.com/adjust/sdks/blob/master/doc/attribution-data.md
[android-dashboard]:              http://developer.android.com/about/dashboards/index.html
[currency-conversion]:            https://docs.adjust.com/ja/event-tracking/#part-8
[android-application]:            http://developer.android.com/reference/android/app/Application.html
[android-launch-modes]:           https://developer.android.com/guide/topics/manifest/activity-element.html
[google_play_services]:           http://developer.android.com/google/play-services/setup.html
[reattribution-with-deeplinks]:   https://docs.adjust.com/en/deeplinking/#manually-appending-attribution-data-to-a-deep-link
[android-purchase-verification]:  https://github.com/adjust/android_purchase_sdk
[testing_console]:                https://docs.adjust.com/ja/testing-console/#part-2
[dev_api]:                        https://docs.adjust.com/ja/adjust-for-developers/

[sdk2sdk-mopub]:                  ../japanese/sdk-to-sdk/mopub.md

## <a id="license"></a>ライセンス

Adjust SDKはMITライセンスを適用しています。

Copyright (c) 2012-2019 Adjust GmbH, http://www.adjust.com

以下に定める条件に従い、本ソフトウェアおよび関連文書のファイル（以下「ソフトウェア」）の複製を取得するすべての人に対し、 ソフトウェアを無制限に扱うことを無償で許可します。これには、ソフトウェアの複製を使用、複写、変更、結合、掲載、頒布、サブライセンス、 および/または販売する権利、およびソフトウェアを提供する相手に同じことを許可する権利も無制限に含まれます。

上記の著作権表示および本許諾表示を、ソフトウェアのすべての複製または重要な部分に記載するものとします。

ソフトウェアは「現状のまま」で、明示であるか暗黙であるかを問わず、何らの保証もなく提供されます。 ここでいう保証とは、商品性、特定の目的への適合性、および権利非侵害についての保証も含みますが、それに限定されるものではありません。 作者または著作権者は、契約行為、不法行為、またはそれ以外であろうと、ソフトウェアに起因または関連し、 あるいはソフトウェアの使用またはその他の扱いによって生じる一切の請求、損害、その他の義務について何らの責任も負わないものとします。
