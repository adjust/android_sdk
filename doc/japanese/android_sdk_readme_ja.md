こちらは、adjust™のAndroid用SDKです。adjust™についての詳細は[adjust.com]をご覧ください。

Web Viewをアプリ内でご使用の場合、Javascriptコードからadjustのトラッキングをご利用いただくには、
[Android Web View SDKガイド](https://github.com/adjust/android_sdk/blob/master/doc/japanese/web_views_ja.md)をご確認ください。

<section id='toc-section'>
</section>

Read this in other languages: [English][en-readme], [中文][zh-readme], [日本語][ja-readme], [한국어][ko-readme].

### <a id="example-app"></a>アプリサンプル

サンプルアプリが[`example`ディレクトリ][example]にあります。
このAndroidプロジェクトをadjust SDK利用の参考にご活用ください。

### <a id="basic-integration"></a>基本的な連携方法

Androidプロジェクトにadjust SDKを連携させるための手順を説明します。
Android の開発に Android Studio が使用されていること、Android API レベル9(Gingerbread)以降が使用されていることを仮定します。

[Maven レポジトリ][maven]をご使用の場合、[こちらの手順](#sdk-add)から始めていただけます。

#### <a id="sdk-get"></a>SDKダウンロード

[リリースページ][releases]から最新バージョンをダウンロードしてください。任意のフォルダにアーカイブを展開してください。


#### <a id="sdk-import"></a>Adjustモジュールのインポート
 
Android Studioメニューから`File → Import Module...`と選択してください。
 
![][import_module]
 
`Source directory`フィールドに、上で展開したフォルダを置いてください。`./android_sdk/Adjust/adjust`フォルダを選択してください。
モジュール名`:adjust`が表示されていることをご確認ください。
 
![][select_module]
 
`adjust`モジュールはAndroid Studioに後からインポートします。
 
![][imported_module]
 
#### <a id="sdk-add"></a>プロジェクトへのSDKの追加
 
アプリの`build.gradle`ファイルを開き、`dependencies`ブロックに以下の行を加えてください。

 
```
compile project(":adjust")
```

![][gradle_adjust]

Mavenをご利用の場合、代わりに書きの行を加えてください。
 
```
compile 'com.adjust.sdk:adjust-android:4.11.4'
```
 
#### <a id="sdk-gps"></a>Google Playサービスの追加

2014年8月1日以降、Google Playストア内のアプリはデバイスの特定のために[Google広告ID][google_ad_id]を使うことが必須とされています。
adjust SDKでGoogle広告IDを使うためには、[Google Playサービス][google_play_services]を連携させる必要があります。
Google Playサービスの連携がお済みでない場合は、以下の手順に進んでください。

1. `build.gradle`ファイルを開き、`dependencies`ブロックを探してください。そこに、以下の行を追加してください。

    ```
        compile 'com.google.android.gms:play-services-analytics:9.2.1'
    ```
 
    ![][gradle_gps]

2. **Google Playサービスのバージョン7以降を使っている場合は、このステップは飛ばしてください。**
パッケージ・エクスプローラからAndroidプロジェクトの`AndroidManifest.xml`を開いてください。
以下の`meta-data`タグを`<application>`エレメントの中に追加してください。

    ```
        <meta-data android:name="com.google.android.gms.version"
                android:value="@integer/google_play_services_version" />
    ```

    ![][manifest_gps]

#### <a id="sdk-permissions"></a>パーミッションの追加
 
パッケージ・エクスプローラからAndroidプロジェクトの`AndroidManifest.xml`を開いてください。
`INTERNET`の`uses-permission`タグがそこになければ、これを追加してください。
 
```xml
<uses-permission android:name="android.permission.INTERNET" />
```

**Google Playストア向けでない**アプリの場合は、両方のパーミッションを追加してください。
 
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
```

![][manifest_permissions]
 
#### <a id="sdk-proguard"></a>Proguard設定

Proguardをお使いの場合は、下記の記述をProguardファイルに追加してください。

```
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep class com.adjust.sdk.plugin.MacAddressUtil {
    java.lang.String getMacAddress(android.content.Context);
 }
-keep class com.adjust.sdk.plugin.AndroidIdUtil {
    java.lang.String getAndroidId(android.content.Context);
 }
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
```

**Google Playストア向けでない**アプリの場合は、`com.google.android.gms`の記述を省略できます。
 
 ![][proguard]
 
**重要** Proguardファイルで`-overloadaggressively`フラグをご使用の場合、adjust SDKの正しい動作のために以下の2つのどちらかをご検討ください。
 
* 必要でなければ、`-overloadaggressively`を削除する
* Proguardファイルに`-useuniqueclassmembernames`フラグを追加する

#### <a id="sdk-broadcast-receiver"></a>Adjust broadcastレシーバ

`INSTALL_REFERRER`インテントの受信に**Broadcastレシーバを使わない**場合、`AndroidManifest.xml`の`application`タグの中に以下の`receiver`タグを追加してください。
 
```xml
<receiver
android:name="com.adjust.sdk.AdjustReferrerReceiver"
android:exported="true" >
<intent-filter>
<action android:name="com.android.vending.INSTALL_REFERRER" />
</intent-filter>
</receiver>
```
 
 ![][receiver]

adjustはコンバージョンをより正確にトラッキングできるよう、インストールリファラを受信するためにこのBroadcastレシーバを使用します。

`INSTALL_REFERRER`インテントに別のBroadcastレシーバを既にお使いの場合、
[こちらの手順][multibroadcast]に従ってadjustのBroadcastレシーバを追加してください。

#### <a id="sdk-integrate"></a>アプリへのSDKの連携
 
はじめに、基本的なセッションのトラッキングの設定を行います。
 
#### <a id="basic-setup"></a>基本設定

SDKの初期化にグローバルAndroid[Application][android_application]クラスのご使用をおすすめします。
まだこのクラスを実装していなければ、次の手順で実装してください。

1. `Application`を拡張するクラスを作成してください。
    ![][application_class]

2. アプリの`AndroidManifest.xml`ファイルを開き、`<application>`エレメントを置いてください。
3. `android:name`アトリビュートを追加し、先頭にドットをつけて新しいアプリケーションクラスの名前に設定してください。

サンプルアプリでは`GlobalApplication`と名付けた`Application`クラスを使用していますので、マニフェストファイルは以下のように設定されます。

```xml
 <application
   android:name=".GlobalApplication"
   ... >
  ...
 </application>
```

![][manifest_application]

4. `Application`クラスに`onCreate`メソッドがあればそこに、なければこれを作成し、adjust SDK初期化の以下のコードを追加してください。
 
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

         }
     }
```

     ![][application_config]

    `{YourAppToken}`にアプリトークンを記入してください。トークンは[dashboard]でご確認いただけます。

     `environment`に以下のどちらかを設定してください。これはテスト用アプリか本番用アプリかによって異なります。


```java
     String environment = AdjustConfig.ENVIRONMENT_SANDBOX;
     String environment = AdjustConfig.ENVIRONMENT_PRODUCTION;
```

**重要** この値はアプリのテスト中のみ`AdjustConfig.ENVIRONMENT_SANDBOX`に設定してください。
アプリを提出する前に`AdjustConfig.ENVIRONMENT_PRODUCTION`になっていることを必ず確認してください。
再度開発やテストをする際は`AdjustConfig.ENVIRONMENT_SANDBOX`に戻してください。

この変数は実際のトラフィックとテスト端末からのテストのトラフィックを区別するために利用されます。
正しく計測するために、この値の設定には常に注意してください。収益のトラッキングの際には特に重要です。
 
#### <a id="session-tracking"></a>セッショントラッキング

**重要** この作業は**非常に重要**ですので、必ず**アプリ内で正しく実装されている**ことをご確認ください。
この実装により、adjust SDKにより的確なセッションのトラッキングが有効になります。
 
##### <a id="session-tracking-api14"></a>レベル14以降のAPI

1. `ActivityLifecycleCallbacks`インターフェイスを実装したプライベートクラスを追加してください。
このインターフェイスへのアクセスができなければ、そのアプリのAndroid APIレベルは14未満です。
アクティビティをそれぞれ手動でアップデートする必要がありますので、こちらの[ガイド](#session-tracking-api9)をご参照ください。
以前にそれぞれのアクティビティで`Adjust.onResume`と`Adjust.onPause`コールを使っていた場合は、これらを削除してください。
 
     ![][activity_lifecycle_class]

2. `onActivityResumed(Activity activity)`メソッドを編集し、`Adjust.onResume()`のコールを追加してください。
`onActivityPaused(Activity activity)`メソッドを編集し、`Adjust.onPause()`のコールを追加してください。
 
     ![][activity_lifecycle_methods]

3. adjust SDKが設定されている部分に`onCreate()`メソッドを追加し、`registerActivityLifecycleCallbacks`のコールと
作成した`ActivityLifecycleCallbacks`クラスのインスタンスを追加してください。
 
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

![][activity_lifecycle_register]
 
##### <a id="session-tracking-api9"></a>レベル9から13のAPI

Gradleの`minSdkVersion`が`9`から`13`の間の場合、`14`以上にアップデートしていただくと今後の連携の手順が簡単になります。
ぜひご検討ください。Androidの[公式ページ][android-dashboard]にて最新のマーケットシェアをご確認ください。

セッショントラッキングを正しく行うためには、アクティビティの開始または停止ごとにadjust SDKの該当メソッドをコールする必要があります。
これを怠ると、SDKはセッションの開始や終了を関知できなくなる場合があります。正しいセッショントラッキングのために、
**すべてのアクティビティに対して以下の作業を行ってください**。

1. アクティビティにソースファイルを開いてください。
2. ファイル最上部に`import`の記述を加えてください。
3. アクティビティの`onResume`メソッド中に`Adjust.onResume`へのコールを追加してください。
  `onResume`メソッドがなければ作成してください。
4. アクティビティの`onPause`メソッド中に`Adjust.onPause`へのコールを追加してください。
  `onPause`メソッドがなければ作成してください。

これらの手順が済むと、アクティビティは次のようになるはずです。

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

![][activity]

**すべての**アクティビティに対してこれらの作業を行ってください。今後新しいアクティビティを作成した時にも忘れずに行ってください。
すべてのアクティビティに対する共通のスーパークラスにこれを実装するという方法もあります。

#### <a id="adjust-logging"></a>Adjustログ

`AdjustConfig`インスタンスの`setLogLevel`に設定するパラメータを変更することによって記録するログのレベルを調節できます。
パラメータは以下の種類があります。
 
```java
 config.setLogLevel(LogLevel.VERBOSE);   // すべてのログを有効にする

 config.setLogLevel(LogLevel.ASSERT);    // errorsも無効にする
```
 
#### <a id="build-the-app"></a>アプリのビルド
 
アプリをビルドして実行しましょう。`LogCat`ビューアにて`tag:Adjust`フィルターを設定し、他のログすべてを非表示にすることがでいます。
アプリが実行されたあと、`Install tracked`のログが出力されるはずです。
 
 ![][log_message]
 
### 追加機能
 
プロジェクトにadjust SDKを連携させると、以下の機能をご利用できるようになります。
 
#### <a id="event-tracking">イベントトラッキング

adjustを使ってアプリ内のイベントをトラッキングすることができます。ここではあるボタンのタップを毎回トラックしたい場合について説明します。
[dashboard]にてイベントトークンを作成し、そのイベントトークンは仮に`abc123`というイベントトークンと関連しているとします。
タップをトラックするため、ボタンの`onClick`メソッドに以下のような記述を追加します。
 
```java
 AdjustEvent event = new AdjustEvent("abc123");
 Adjust.trackEvent(event);
```
 
##### <a id="revenue-tracking">収益のトラッキング

広告をタップした時やアプリ内課金をした時などにユーザーが報酬を得る仕組みであれば、そういったイベントもトラッキングできます。
1回のタップで1ユーロセントの報酬と仮定すると、報酬イベントは以下のようになります。
 
```java
AdjustEvent event = new AdjustEvent("abc123");
event.setRevenue(0.01, "EUR");
Adjust.trackEvent(event);
```

もちろんこれはコールバックパラメータと紐付けることができます。

通貨トークンを設定する場合、adjustは自動的に収益を任意の報酬に変換します。
詳しくは[通貨の変換][currency-conversion]をご覧ください。

収益とイベントトラッキングについては[イベントトラッキングガイド][event-tracking]もご参照ください。

イベントインスタンスは、イベントがトラッキングされる前にそのイベントを設定するためにも使えます。

##### <a id="iap-verification">アプリ内課金の検証

adjustのサーバーサイドのレシート検証ツール、Purchase Verificationを使ってアプリ内で行われたアプリ内課金の妥当性を調べる際は、
Android purchase SDKをご利用ください。詳しくは[こちら][android-purchase-verification]をご覧ください。

##### <a id="callback-parameters">コールバックパラメータ

[dashboard]でイベントにコールバックURLを登録することができます。イベントがトラッキングされるたびに
そのURLにGETリクエストが送信されます。トラッキングする前にイベントで`addCallbackParameter`をコールすることによって、
イベントにコールバックパラメータを追加できます。そうして追加されたパラメータはコールバックURLに送られます。

例えば、コールバックURLに`http://www.adjust.com/callback`を登録した場合、イベントトラッキングは以下のようになります。

```java
AdjustEvent event = new AdjustEvent("abc123");

event.addCallbackParameter("key", "value");
event.addCallbackParameter("foo", "bar");

Adjust.trackEvent(event);
```
この場合、adjustはこのイベントをトラッキングし以下にリクエストが送られます。

```
http://www.adjust.com/callback?key=value&foo=bar
```

パラメータの値として使われることのできるプレースホルダーは、`{gps_adid}`のような様々な形に対応しています。
得られるコールバック内で、このプレースホルダーは該当デバイスのGoogle PlayサービスIDに置き換えられます。
独自に設定されたパラメータには何も格納しませんが、コールバックに追加されます。
イベントにコールバックを登録していない場合は、これらのパラメータは使われません。

URLコールバックについて詳しくは[コールバックガイド][callbacks-guide]をご覧ください。
利用可能な値のリストもこちらで参照してください。
 
##### <a id="partner-parameters">パートナーパラメータ

adjustのダッシュボード上で連携が有効化されているネットワークパートナーに送信するパラメータを設定することができます。

これは上記のコールバックパラメータと同様に機能しますが、
`AdjustEvent`インスタンスの`addPartnerParameter`メソッドをコールすることにより追加されます。
 
```java
 AdjustEvent event = new AdjustEvent("abc123");

 Adjust.trackEvent(event);
```

スペシャルパートナーとその統合について詳しくは[連携パートナーガイド][special-partners]をご覧ください。

### <a id="callback-id"></a>コールバック ID
トラッキングしたいイベントにカスタムIDを追加できます。このIDはイベントをトラッキングし、成功か失敗かの通知を受け取けとれるようコールバックを登録することができます。このIDは`AdjustEvent`インスタンスの`setCallbackId`メソッドと呼ぶように設定できます：

```java
AdjustEvent event = new AdjustEvent("abc123");

event.setCallbackId("Your-Custom-Id");

Adjust.trackEvent(event);
```

#### <a id="session-parameters">セッションパラメータ

いくつかのパラメータは、adjust SDKのイベントごと、セッションごとに送信するために保存されます。
このいずれかのパラメータを追加すると、これらはローカル保存されるため、毎回追加する必要はありません。
同じパラメータを再度追加しても何も起こりません。

これらのセッションパラメータはadjust SDKが立ち上がる前にコールすることができるので、インストール時に送信を確認することもできます。
インストール時に送信したい場合は、adjust SDKの初回立ち上げを[遅らせる](#delay-start)ことができます。
ただし、必要なパラメータの値を得られるのは立ち上げ後となります。

##### <a id="session-callback-parameters"> セッションコールバックパラメータ

[イベント](#callback-parameters)で設定された同じコールバックパラメータを、
adjust SDKのイベントごとまたはセッションごとに送信するために保存することもできます。

セッションコールバックパラメータのインターフェイスとイベントコールバックパラメータは似ています。
イベントにキーと値を追加する代わりに、`Adjust`の`Adjust.addSessionCallbackParameter(String key, String value)`へのコールで追加されます。
 
```java
Adjust.addSessionCallbackParameter("foo", "bar");

```

セッションコールバックパラメータは、イベントに追加されたコールバックパラメータとマージされます。
イベントに追加されたコールバックパラメータは、セッションコールバックパラメータより優先されます。
イベントに追加されたコールバックパラメータがセッションから追加されたパラメータと同じキーを持っている場合、
イベントに追加されたコールバックパラメータの値が優先されます。

`Adjust.removeSessionCallbackParameter(String key)`メソッドに指定のキーを渡すことで、
特定のセッションコールバックパラメータを削除することができます。
 
```java
Adjust.removeSessionCallbackParameter("foo");
```

セッションコールバックパラメータからすべてのキーと値を削除したい場合は、
`Adjust.resetSessionCallbackParameters()`メソッドを使ってリセットすることができます。

```java
 Adjust.resetSessionCallbackParameters();
```

##### <a id="session-partner-parameters"> セッションパートナーパラメータ

adjust SDKのイベントごとやセッションごとに送信される[セッションコールバックパラメータ](#session-callback-parameters)があるように、
セッションパートナーパラメータも用意されています。

これらはネットワークパートナーに送信され、adjust[ダッシュボード]で有効化されている連携のために利用されます。

セッションパートナーパラメータのインターフェイスとイベントパートナーパラメータは似ています。
イベントにキーと値を追加する代わりに、`Adjust.addSessionPartnerParameter(String key, String value)`へのコールで追加されます。

```java
 Adjust.addSessionPartnerParameter("foo", "bar");
```

セッションパートナーパラメータはイベントに追加されたパートナーパラメータとマージされます。イベントに追加されたパートナーパラメータは、
セッションパートナーパラメータより優先されます。イベントに追加されたパートナーパラメータが
セッションから追加されたパラメータと同じキーを持っている場合、イベントに追加されたパートナーパラメータの値が優先されます。

`Adjust.removeSessionPartnerParameter(String key)`メソッドに指定のキーを渡すことで、
特定のセッションパートナーパラメータを削除することができます。
 
```java
Adjust.removeSessionPartnerParameter("foo");
```

セッションパートナーパラメータからすべてのキーと値を削除したい場合は、
`Adjust.resetSessionPartnerParameters()`メソッドを使ってリセットすることができます。
 
```java
Adjust.resetSessionPartnerParameters();
```

##### <a id="delay-start"> ディレイスタート

adjust SDKのスタートを遅らせると、ユニークIDなどのセッションパラメータを取得しインストール時に送信できるようにすることができます。

`AdjustConfig`インスタンスの`setDelayStart`メソッドで、遅らせる時間を秒単位で設定できます。

```java
adjustConfig.setDelayStart(5.5);
```

この場合、adjust SDKは最初のインストールセッションと生成されるイベントを初めの5.5秒間は送信しません。
この時間が過ぎるまで、もしくは`Adjust.sendFirstPackages()`がコールされるまで、
セッションパラメータはすべてディレイインストールセッションとイベントに追加され、adjust SDKは通常通り再開します。

adjust SDKのディレイスタートは最大で10秒です。

#### <a id="attribution-callback"></a>アトリビューションコールバック

トラッカーのアトリビューション変化の通知を受けるために、リスナを登録することができます。
アトリビューションには複数のソースがあり得るため、この情報は同時に送ることができません。匿名リスナを作成する最も簡単な方法を以下で紹介します。

[アトリビューションデータに関するポリシー][attribution-data]を必ずご確認ください。

`AdjustConfig`インスタンスで、SDKをスタートする前に以下の匿名リスナを追加してください。

```
 Adjust.onCreate(config);
```

代わりに、`Application`クラスに`OnAttributionChangedListener`インターフェイスを実装してリスナとして設定することもできます。
 
```java
 AdjustConfig config = new AdjustConfig(this, appToken, environment);

 Adjust.onCreate(config);
```

リスナはSDKが最後のアトリビューションデータを取得した時に呼ばれます。
リスナの機能で`attribution`パラメータを確認することができます。このパラメータのプロパティの概要は以下の通りです。

 - `String trackerToken` 最新アトリビューションのトラッカートークン
 - `String trackerName` 最新アトリビューションのトラッカー名

 - `String creative` 最新アトリビューションのクリエイティブのグループ階層
 - `String clickLabel` 最新アトリビューションのクリックラベル
 
- `String trackerToken` 最新アトリビューションのトラッカートークン
- `String trackerName` 最新アトリビューションのトラッカー名
- `String network` 最新アトリビューションの流入元名
- `String campaign` 最新アトリビューションのキャンペーン名
- `String adgroup` 最新アトリビューションのアドグループ名
- `String creative` 最新アトリビューションのクリエイティブ名
- `String clickLabel` 最新アトリビューションのクリックラベル
- `String adid` adjustユニークID
 
#### <a id="session-event-callbacks"></a>イベントとセッションのコールバック

イベントとセッションの双方もしくはどちらかをトラッキングし、成功か失敗かの通知を受け取れるようリスナを登録することができます。
`AdjustConfig`オブジェクトを生成すると、リスナをいくつでも追加することができます。

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

リスナ関数はSDKがサーバーにパッケージ送信を試みた後で呼ばれます。
リスナ関数内でリスナ用のレスポンスデータオブジェクトを確認することができます。
レスポンスデータのプロパティの概要は以下の通りです。
 
- `String message` サーバーからのメッセージまたはSDKのエラーログ
- `String timestamp` サーバーからのタイムスタンプ
- `String adid` adjustから提供されるユニークデバイスID
- `JSONObject jsonResponse` サーバーからのレスポンスのJSONオブジェクト

イベントのレスポンスデータは以下を含みます。

- `String eventToken` トラッキングされたパッケージがイベントだった場合、そのイベントトークン
- `String callbackId` イベントオブジェクトにカスタム設定されたコールバックID

失敗したイベントとセッションは以下を含みます。

- `boolean willRetry` しばらく後に再送を試みる予定であるかどうかを示します。

#### <a id="disable-tracking"></a>トラッキングの無効化

`setEnabled`にパラメータ`false`を渡すことで、adjustSDKが行うデバイスのアクティビティのトラッキングをすべて無効にすることができます。
**この設定はセッション間で記憶されます**
 
 ```java
 Adjust.setEnabled(false);
 ```

adjust SDKが現在有効かどうか、`isEnabled`関数を呼び出せば確認できます。
また、`setEnabled`関数に`true`を渡せば、adjust SDKを有効にすることができます。
 
#### <a id="offline-mode"></a>オフラインモード

adjustのサーバーへの送信を一時停止し、保持されているトラッキングデータを後から送信するために
adjust SDKをオフラインモードにすることができます。
オフラインモード中はすべての情報がファイルに保存されるので、イベントをたくさん発生させすぎないようにご注意ください。

`true`パラメータで`setOfflineMode`を呼び出すとオフラインモードを有効にできます。

```
Adjust.setOfflineMode(true);
```

反対に、`false`パラメータで`setOfflineMode`を呼び出せばオフラインモードを解除できます。
adjust SDKがオンラインモードに戻った時、保存されていた情報は正しいタイムスタンプでadjustのサーバーに送られます。

トラッキングの無効化とは異なり、この設定はセッション間で**記憶されません**。
オフラインモード時にアプリを終了しても、次に起動した時にはオンラインモードとしてアプリが起動します。

#### <a id="event-buffering"></a>イベントバッファリング

イベントトラッキングを酷使している場合、HTTPリクエストを遅らせて1分毎にまとめて送信したほうがいい場合があります。
その場合は、`AdjustConfig`インスタンスでイベントバッファリングを有効にしてください。

```java
AdjustConfig config = new AdjustConfig(this, appToken, environment);

config.setEventBufferingEnabled(true);

Adjust.onCreate(config);
```

### <a id="gdpr-forget-me"></a>GDPR消去する権利（忘れられる権利）

次のメソッドを呼び出すと、EUの一般データ保護規制（GDPR）第17条に従い、ユーザーが消去する権利（忘れられる権利）を行使した際にAdjust SDKがAdjustバックエンドに情報を通知します。

```objc
[Adjust gdprForgetMe];
```

この情報を受け取ると、Adjustはユーザーのデータを消去し、Adjust SDKはユーザーの追跡を停止します。この削除された端末からのリクエストは今後、Adjustに送信されません。

#### <a id="background-tracking"></a>バックグラウンドでのトラッキング

adjust SDKはデフォルドではアプリがバックグラウンドにある時はHTTPリクエストを停止します。
この設定は`AdjustConfig`インスタンスで変更できます。

```java
AdjustConfig config = new AdjustConfig(this, appToken, environment);

config.setSendInBackground(true);

Adjust.onCreate(config);
```

#### <a id="device-ids"></a>デバイスID

Google Analyticsなどの一部のサービスでは、レポートの重複を防ぐためにデバイスIDとクライアントIDを連携させることが求められます。

Google広告IDを取得する必要がある場合、広告IDはバックグラウンドでのスレッドでしか読み込みできないという制約があります。
`getGoogleAdId`関数と`OnDeviceIdsRead`インスタンスをコールすると、この条件以外でも取得できるようになります。

```java
Adjust.getGoogleAdId(this, new OnDeviceIdsRead() {
    @Override
    public void onGoogleAdIdRead(String googleAdId) {
        // ...
    }
});
```

`OnDeviceIdsRead`インスタンスの`onGoogleAdIdReadメソッド内で、Google広告IDを`googleAdId`変数として利用できます。

#### <a id="push-token"></a>Pushトークン

プッシュ通知のトークンを送信するには、トークンを取得次第またはその値が変更され次第、adjustへの以下のコールを追加してください。

```java
Adjust.setPushToken(pushNotificationsToken);
```

#### <a id="pre-installed-trackers">プレインストールのトラッカー

すでにアプリをインストールしたことのあるユーザーをadjust SDKを使って識別したい場合は、次の手順で設定を行ってください。

1. [dashboard]上で新しいトラッカーを作成してください。
2. App Delegateを開き、`ADJConfig`のデフォルトトラッカーを設定してください。

  ```java
  AdjustConfig config = new AdjustConfig(this, appToken, environment);
  config.setDefaultTracker("{TrackerToken}");
  Adjust.onCreate(config);
  ```

`{TrackerToken}`にステップ2で作成したトラッカートークンを入れてください。
ダッシュボードには`http://app.adjust.com/`を含むトラッカーURLが表示されます。
ソースコード内にはこのURLすべてではなく、6文字のトークンを抜き出して指定してください。

3. アプリをビルドしてください。LogCatで下記のような行が表示されるはずです。

    ```
    Default tracker: 'abc123'
    ```

#### <a id="deeplinking"></a>ディープリンキング

URLからアプリへのディープリンクを使ったadjustトラッカーURLをご利用の場合、ディープリンクURLとその内容の情報を得られる可能性があります。
ユーザーがすでにアプリをインストールしている状態でそのURLに訪れた場合(スタンダード・ディープリンキング)と、
アプリをインストールしていないユーザーがURLを開いた場合(ディファード・ディープリンキング)が有り得ます。
スタンダード・ディープリンキングの場合、Androidのプラットフォームにはディープリンクの内容を取得できる仕組みがあります。
ディファード・ディープリンキングに対してはAndroidプラットフォームはサポートしていませんので、
adjust SDKがディープリンクの内容を取得するメカニズムを提供します。

##### <a id="deeplinking-standard">スタンダード・ディープリンキング

アプリをすでにインストールしているユーザーが`deep_link`パラメータのついたadjustのトラッカーURLを叩いた後でアプリを立ち上げたい場合、
アプリのディープリンキングを有効化する必要があります。**ユニークスキーム名**を選択し、リンクがクリックされてアプリが開いた時に起動したいアクティビティを
指定することで有効化できます。これは`AndroidManifest.xml`内で設定できます。マニフェストファルの該当のアクティビティ定義に
`intent-filter`セクションを追加し、該当のスキーム名に`android:scheme`プロパティを指定してください。

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

この設定をすると、トラッカーURLがクリックされた時にアプリを開くには、adjustトラッカーURLの`deep_link`パラメータにあるスキーム名を指定する必要があります。
ディープリンクに情報を追加していないトラッカーURLは次のようになります。

```
https://app.adjust.com/abc123?deep_link=adjustExample%3A%2F%2F
```

`deep_link`パラメータの値は**URLエンコード**される必要があります。

トラッカーURLをクリック後、アプリが上記の設定をされていれば、アプリは`MainActivity`インテントの通りに起動します。
`MainActivity`クラス内で`deep_link`パラメータの内容が自動的に提供されます。
届けられたこの情報は**エンコードされていません**が、URL内ではエンコードされています。

`AndroidManifest.xml`ファイルのアクティビティの`android:launchMode`設定によっては、`deep_link`パラメータの内容情報は
アクティビティファイルの適切な箇所に届けられます。`android:launchMode`のとり得る値について詳しくは[Android公式資料][android-launch-modes]をご確認ください。

指定のアクティビティに`Intent`オブジェクトを介してディープリンクの内容情報を送ることができる場所は2か所あります。
アクティビティの`onCreate`メソッドか`onNewIntent`メソッドのいずれかです。アプリが起動してこれらのどちらかのメソッドが呼ばれると、
クリックURL中の`deep_link`パラメータ内の実際に渡されたディープリンクを取得することができます。
この情報はロジックを追加する際に使うことができます。

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

##### <a id="deeplinking-deferred">ディファード・ディープリンキング

ユーザーが`deep_link`パラメータのついたトラッカーURLをクリックした時にそのユーザーがアプリをデバイスにインストールしていなかった場合、
ディファード・ディープリンキングを使用します。この場合、クリックした後ユーザーはプレイストアにリダイレクトされ、アプリをダウンロードできます。
アプリの初回起動後、`deep_link`パラメータの内容がアプリに送信されます。

ディファード・ディープリンキングを使って`deep_link`パラメータの内容情報を取得するには、`AdjustConfig`オブジェクトにリスナメソッドを
設定してください。このメソッドはadjust SDKがサーバーからディープリンク内容を取得した時に呼ばれます。

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

adjust SDKがサーバーからディープリンク情報を受信すると、リスナ内のディープリンク内容の情報を送信しますので、`boolean`値を返してください。
この値はadjust SDKがディープリンクから指定したスキーム名へアクティビティを起動させたいかどうかで決定してください。
スキーム名の指定はスタンダード・ディープリンキングと同様です。

`true`を返すと、[スタンダード・ディープリンキング](#deeplinking-standard)の章で説明したものと同様に起動します。
SDKにアクティビティをスタートさせたくない場合、リスナから`false`を返してください。
ディープリンクの内容に基づいてアプリの次の挙動を決定してください。

##### <a id="deeplinking-reattribution">ディープリンクを介したリアトリビューション

adjustはディープリンクを使ったリエンゲージメントキャンペーンをサポートしています。
詳しくは[公式資料][reattribution-with-deeplinks]をご覧ください。

この機能をご利用の場合、ユーザーが正しくリアトリビューションされるために、adjust SDKへのコールを追加してください。

アプリでディープリンクの内容データを受信したら、`Adjust.appWillOpenUrl(Uri, Context)`メソッドへのコールを追加してください。
このコールによって、adjust SDKはディープリンクの中に新たなアトリビューションが存在するかを調べ、あった場合はadjustサーバーにこれを送信します。
ディープリンクのついたadjustトラッカーURLのクリックによってユーザーがリアトリビュートされる場合、
[アトリビューションコールバック](#attribution-callback)がこのユーザーの新しいアトリビューションデータで呼ばれます。

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

### <a id="troubleshooting">トラブルシューティング

#### <a id="ts-session-failed">"Session failed (Ignoring too frequent session. ...)"というエラーが出る

このエラーはインストールのテストの際に起こりえます。アンインストール後に再度インストールするだけでは新規インストールとして動作しません。
SDKがローカルで統計したセッションデータを失ったとサーバーは判断してエラーメッセージを無視し、
その端末に関する有効なデータのみが与えられます。

この仕様はテスト中には厄介かもしれませんが、サンドボックスと本番用の挙動をできる限り近づけるために必要です。

adjustのサーバーにある端末のセッションデータをリセットすることができます。ログにあるエラーメッセージをチェックしてください。

```
Session failed (Ignoring too frequent session. Last session: YYYY-MM-DDTHH:mm:ss, this session: YYYY-MM-DDTHH:mm:ss, interval: XXs, min interval: 20m) (app_token: {yourAppToken}, adid: {adidValue})
```

`{yourAppToken}`と`{adidValue}`/`{gps_adidValue}`/`{androidIDValue}`の各値を入力し、以下のリンクを開いてください。


```
http://app.adjust.com/forget_device?app_token={yourAppToken}&adid={adidValue}
```

```
http://app.adjust.com/forget_device?app_token={yourAppToken}&gps_adid={gps_adidValue}
```

```
http://app.adjust.com/forget_device?app_token={yourAppToken}&android_id={androidIDValue}
```

端末に関する記録が消去されると、このリンクは`Forgot device`と返します。
もしその端末の記録がすでに消去されていたり、値が不正だった場合は`Device not found`が返ります。

#### <a id="ts-broadcast-receiver">Broadcastレシーバがインストールリファラを受信していない

[ガイド](#broadcast_receiver)に従って設定を済ませていれば、
BroadcastレシーバはadjustのSDKとサーバーにインストールを送信するよう設定されているはずです。

手動でテスト用インストールリファラを作動させることで確認できます。`com.your.appid`にアプリIDを入力し、Android Studioの
[adb](http://developer.android.com/tools/help/adb.html)ツールで以下のコマンドを実行してください。

```
adb shell am broadcast -a com.android.vending.INSTALL_REFERRER -n com.your.appid/com.adjust.sdk.AdjustReferrerReceiver --es "referrer" "adjust_reftag%3Dabc1234%26tracking_id%3D123456789%26utm_source%3Dnetwork%26utm_medium%3Dbanner%26utm_campaign%3Dcampaign"
```

`INSTALL_REFERRER`インテントに対してすでに別のBroadcastリファラを使っている状態でこの[ガイド][referrer]の設定をした場合、
`com.adjust.sdk.AdjustReferrerReceiver`にBroadcastレシーバを入力してください。

`-n com.your.appid/com.adjust.sdk.AdjustReferrerReceiver`パラメータを削除することもできます。
削除すると、デバイスに入っているすべてのアプリが`INSTALL_REFERRER`インテントを受信します。

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

"**重要:** この機能をテストをするために`adb`ツールを利用することは推奨しておりません。全てのリファラコンテンツを`adb`でテストするためには（`&`で分けられた複数のパラメータがある場合）、ブロードキャストリシーバーで受信するためにコンテンツをエンコードすることが必要です。もしエンコードをしないと、`adb`はレファラを最初の`&`サインで切り、誤ったコンテンツをブロードキャストレシーバーに伝えます。アプリがどのようにエンコードされていないリファラを受信しているかを確認したい場合は、Adjustのサンプルアプリを利用して、`MainActivity.java`ファイルの`onFireIntentClick`メソッドのインテント内に送信されたコンテンツを変更してください:

```java
public void onFireIntentClick(View v) {
    Intent intent = new Intent("com.android.vending.INSTALL_REFERRER");
    intent.setPackage("com.adjust.examples");
    intent.putExtra("referrer", "utm_source=test&utm_medium=test&utm_term=test&utm_content=test&utm_campaign=test");
    sendBroadcast(intent);
}
```
自分の選んだコンテントで`putExtra`2番目のパラメーターを自由に変更してください。

#### <a id="ts-event-at-launch">アプリ起動時にイベントを始動したい

直感的には分かりにくいですが、グローバル`Application`クラスの`onCreate`メソッドはアプリ起動時だけでなく、
アプリによってシステムやイベントが作動する時にも呼ばれます。

adjust SDKはこの場合の初期化についてサポートしています。この機能はアプリが実際に起動した時でなく、
アクティビティがスタートした時、たとえばユーザーがアプリを起動させた時に起こります。

これらのコールはアプリがユーザーの操作以外の要因で起動した場合にも、adjust SDKを起動しイベントを送信しします。これはアプリの外部要因にもよります。

このように、アプリ起動時のイベントの作動はインストールとセッションの数を正確にトラッキングできません。

インストール後にイベントを作動させたい場合は、[アトリビューション変更時用のリスナ](#attribution_changed_listener)をご利用ください。

アプリ起動時にイベントを作動させたい場合は、スタートするアクティビティの`onCreate`メソッドをご使用ください。

[dashboard]:http://adjust.com
[adjust.com]:http://adjust.com
[en-readme]:  ../../README.md
[zh-readme]: ../chinese/android_sdk_readme_zh.md
[ja-readme]: ../japanese/android_sdk_readme_ja.md
[ko-readme]: ../korean/android_sdk_readme_ko.md

[maven]:http://maven.org
[example]:https://github.com/adjust/android_sdk/tree/master/Adjust/example
[releases]:https://github.com/adjust/adjust_android_sdk/releases
[referrer]:doc/english/referrer.md
[google_ad_id]:https://support.google.com/googleplay/android-developer/answer/6048248?hl=en
[event-tracking]:https://docs.adjust.com/en/event-tracking
[callbacks-guide]:https://docs.adjust.com/en/callbacks
[application_name]:http://developer.android.com/guide/topics/manifest/application-element.html#nm
[special-partners]:https://docs.adjust.com/en/special-partners
[attribution-data]:https://github.com/adjust/sdks/blob/master/doc/attribution-data.md
[android-dashboard]:http://developer.android.com/about/dashboards/index.html
[currency-conversion]:https://docs.adjust.com/en/event-tracking/#tracking-purchases-in-different-currencies
[android_application]:http://developer.android.com/reference/android/app/Application.html
[android-launch-modes]:https://developer.android.com/guide/topics/manifest/activity-element.html
[google_play_services]:http://developer.android.com/google/play-services/setup.html
[activity_resume_pause]:doc/activity_resume_pause.md
[reattribution-with-deeplinks]:https://docs.adjust.com/en/deeplinking/#manually-appending-attribution-data-to-a-deep-link
[android-purchase-verification]:https://github.com/adjust/android_purchase_sdk

[activity]:https://raw.github.com/adjust/sdks/master/Resources/android/v4/14_activity.png
[proguard]:https://raw.github.com/adjust/sdks/master/Resources/android/v4/08_proguard_new.png
[receiver]:https://raw.github.com/adjust/sdks/master/Resources/android/v4/09_receiver.png
[gradle_gps]:https://raw.github.com/adjust/sdks/master/Resources/android/v4/05_gradle_gps.png
[log_message]:https://raw.github.com/adjust/sdks/master/Resources/android/v4/15_log_message.png
[manifest_gps]:https://raw.github.com/adjust/sdks/master/Resources/android/v4/06_manifest_gps.png
[gradle_adjust]:https://raw.github.com/adjust/sdks/master/Resources/android/v4/04_gradle_adjust.png
[import_module]:https://raw.github.com/adjust/sdks/master/Resources/android/v4/01_import_module.png
[select_module]:https://raw.github.com/adjust/sdks/master/Resources/android/v4/02_select_module.png
[imported_module]:https://raw.github.com/adjust/sdks/master/Resources/android/v4/03_imported_module.png
[application_class]:https://raw.github.com/adjust/sdks/master/Resources/android/v4/11_application_class.png
[application_config]:https://raw.github.com/adjust/sdks/master/Resources/android/v4/13_application_config.png
[manifest_permissions]:https://raw.github.com/adjust/sdks/master/Resources/android/v4/07_manifest_permissions.png
[manifest_application]:https://raw.github.com/adjust/sdks/master/Resources/android/v4/12_manifest_application.png
[activity_lifecycle_class]:https://raw.github.com/adjust/sdks/master/Resources/android/v4/16_activity_lifecycle_class.png
[activity_lifecycle_methods]:https://raw.github.com/adjust/sdks/master/Resources/android/v4/17_activity_lifecycle_methods.png
[activity_lifecycle_register]:https://raw.github.com/adjust/sdks/master/Resources/android/v4/18_activity_lifecycle_register.png


### <a id="license"></a>ライセンス

adjust SDKはMITライセンスを適用しています。

Copyright (c) 2012-2018 Adjust GmbH,
http://www.adjust.com

以下に定める条件に従い、本ソフトウェアおよび関連文書のファイル（以下「ソフトウェア」）の複製を取得するすべての人に対し、
ソフトウェアを無制限に扱うことを無償で許可します。これには、ソフトウェアの複製を使用、複写、変更、結合、掲載、頒布、サブライセンス、
および/または販売する権利、およびソフトウェアを提供する相手に同じことを許可する権利も無制限に含まれます。

上記の著作権表示および本許諾表示を、ソフトウェアのすべての複製または重要な部分に記載するものとします。

ソフトウェアは「現状のまま」で、明示であるか暗黙であるかを問わず、何らの保証もなく提供されます。
ここでいう保証とは、商品性、特定の目的への適合性、および権利非侵害についての保証も含みますが、それに限定されるものではありません。
作者または著作権者は、契約行為、不法行為、またはそれ以外であろうと、ソフトウェアに起因または関連し、
あるいはソフトウェアの使用またはその他の扱いによって生じる一切の請求、損害、その他の義務について何らの責任も負わないものとします。
