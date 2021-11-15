## Android用adjust SDKのv3.6.2からv4.28.7への移行

### アプリケーションクラス

adjust SDKの初期化方法が大きく変わりました。今後はマニフェストファイルでなく
グローバルAndroid[Application][android_application]クラスをご使用ください。

まだご利用されていない場合は、[Readme][basic-setup]に記載の手順に従って統合を進めてください。

adjust SDKの設定方法も大きく変わりました。初期設定はすべて新しいconfigオブジェクトで行わるようになります。
`Application`クラスの`onCreate`メゾッドで以下の作業を行ってください。

1. configオブジェクト`AdjustConfig`を作成し、アプリトークン、環境と`this`を指定してください。
2. 任意の設定を加えてください。
3. configオブジェクトで`Adjust.onCreate`を呼び出し、SDKをローンチしてください。


移行前のマニフェストファイルの設定および移行後の`Application`クラスでの設定がどのように行われるか例を示します。

##### 移行前

```xml
<meta-data android:name="AdjustAppToken"    android:value="{YourAppToken}" />
<meta-data android:name="AdjustLogLevel"    android:value="info" />
<meta-data android:name="AdjustEnvironment" android:value="sandbox" />
```

##### 移行後

```java
import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustConfig;

public class YourApplicationClass extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // configure Adjust
        String appToken = "{YourAppToken}";
        String environment = AdjustConfig.ENVIRONMENT_SANDBOX;
        AdjustConfig config = new AdjustConfig(this, appToken, environment);
        config.setLogLevel(LogLevel.INFO); // if not configured, INFO is used by default
        Adjust.onCreate(config);
    }
}
```

### イベントトラッキング

トラッキングされる前に設定することのできるイベントオブジェクトを導入しました。
導入前後で設定がどのように行われるか例を示します。

##### 導入前

```java
Map<String, String> parameters = new HashMap<String, String>();
parameters.put("key", "value");
parameters.put("foo", "bar");
Adjust.trackEvent("abc123", parameters);
```

##### 導入後

```java
AdjustEvent event = new AdjustEvent("abc123");
event.addCallbackParameter("key", "value");
event.addCallbackParameter("foo", "bar");
Adjust.trackEvent(event);
```

### 収益トラッキング

収益は通常のイベントとして扱えるようになりました。報酬と通過をトラッキングするよう設定するだけです。
イベントトークンなしでは収益のトラッキングはできなくなりましたので、ご注意ください。
ダッシュボードでイベントトークンを追加作成する必要がある場合があります。

*注意* 金額のフォーマットがセント単位から通過単位に変わりました。
現在の収益トラッキングの金額は通過単位に調整されているはずです。(100で割った値になります)

##### 変更前

```java
Adjust.trackRevenue(1.0, "abc123");
```

##### 変更後

```java
AdjustEvent event = new AdjustEvent("abc123");
event.setRevenue(0.01, "EUR");
Adjust.trackEvent(event);
```

## v2.1.xから移行する場合の追加手順

メインクラスの名前を`com.adeven.adjustio.AdjustIo`から`com.adjust.sdk.Adjust`に変更しました。
すべてのadjust SDKのコールを更新するには、次のステップに進んでください。

1. `Package Explorer`から旧`AdjustIo`プロジェクトを右クリックし、`Delete`を選択してください。
   `Delete project contents on disk`にチェックを入れ、`OK`をクリックしてください。

2. Eclipseメニューから`Search → File...`と進み、`File Search`タブを選んでください。
   検索欄に`AdjustIo`と入力し、`Case sensitive`にチェックを入れてください。
   ファイル名のパターンが`*`で、スコープが`Workspace`になっていることをご確認ください。

   ![][search]

3. `Replace...`をクリックし、置換欄に`Adjust`を入力して`Preview >`をクリックしてください。
   javaファイル中のすべてのadjustコールとマニフェストファイル中のすべてのadjustの設定が置換されるはずです。
   プレビューで変更を確認いただいた上、`OK`をクリックしてください。

   ![][replace]

4. 同様に、`ReferrerReceiver`のパッケージ名を更新できるよう、
   すべてのマニフェストファイルで`adeven.adjustio`を`adjust.sdk`に置換してください。

5. バージョン3.6.2をダウンロードし、`Adjust`フォルダから新しいAndroidプロジェクトを作成してください。

    ![][import]

6. アプリのAndroidプロパティを開き、新しい`Adjust`ライブラリが選択されていることを確認してください。

7. `Package Explorer`からアプリを右クリックし、`Source → Organize Imports`を選択してください。

8. プロジェクトをビルドし、問題なく差し替えられたことを確認してください。

adjust SDK v3.4.0はデリゲート通知を追加しました。詳細は[README]をご確認ください。


## v2.0.xから移行する場合の追加手順

`AndroidManifest.xml`にadjustの設定を加えてください。
以下の`meta-data`タグを`application`タグの中に追加してください。

```xml
<meta-data android:name="AdjustAppToken"    android:value="{YourAppToken}" />
<meta-data android:name="AdjustLogLevel"    android:value="info" />
<meta-data android:name="AdjustEnvironment" android:value="sandbox" /> <!-- TODO: change to 'production' -->
```

![][settings]

`{YourAppToken}`にアプリトークンを記入してください。これは[dashboard]でご確認いただけます。

ログのレベルは`AdjustLogLevel`でグローバルに設定できます。
値には以下の種類があります。

- `verbose` - すべてのログを有効にする
- `debug` - より詳細なログを記録する
- `info` - デフォルト
- `warn` - infoのログを無効にする
- `error` - warningも無効にする
- `assert` - errorも無効にする

テスト用か本番用かによって、`AdjustEnvironment`のパラメータは以下のいずれかに設定する必要があります。

- `sandbox` - for testing
- `production` - before publishing

**重要** この値はテスト中のみ`sandbox`に設定してください。
アプリを提出する前に`production`になっていることを必ず確認してください。
再度開発やテストをする際は`sandbox`に戻してください。

この変数は実際のトラフィックとテスト端末からのテストのトラフィックを区別するために利用されます。
正しく計測するために、この値の設定には常に注意してください。収益のトラッキングの際には特に重要です。

## v2.0.xから移行する場合の追加手順

1. すべてのアクティビティは`onResume`メソッドで`Adjust.onResume`をコールする必要があります。
   これらのすべてのコールから`appToken`パラメータを削除してください。
   すると、この関数は以下のようになるはずです。

    ```java
    protected void onResume() {
        super.onResume();
        Adjust.onResume(this);
    }
    ```

2. `Adjust.setLogLevel`へのコールをすべて削除してください。

## v1.xから移行する場合の追加手順

3. 初期化にはもう`Adjust.appDidLaunch()`メソッドを使いません。
   ローンチアクティビティの`onCreate`メソッドからこのコールを削除してください。

4. 代わりに、セッションのトラッキングには、アクティビティが終了したり停止するたびに
   新しいadjustメソッドをコールする必要があります。これをしなければ、
   SDKはセッションの開始や終了を検知できなくなる場合があります。
   正しく設定するために、**すべての**アクティビティで以下の作業を行ってください。

   - アクティビティのソースファイルを開いてください。
   - ファイル最上部に`import`の記述を加えてください。
   - アクティビティの`onResume`メソッドで`Adjust.onResume`をコールしてください。
     必要であればこのメソッドを作成してください。
   - アクティビティの`orPause`メソッドで`Adjust.onPause`をコールしてください。
     必要であればこのメソッドを作成してください。

    この手順を終えると、アクティビティは以下のようになっているはずです。

    ```java
    import com.adjust.sdk.Adjust;
    // ...
    public class YourActivity extends Activity {
        protected void onResume() {
            super.onResume();
            Adjust.onResume(this);
        }
        protected void onPause() {
            super.onPause();
            Adjust.onPause();
        }
        // ...
    }
    ```

    ![][activity]

    この作業はアプリの**すべての**アクティビティにて行われる必要があります。
    今後新しいアクティビティを作成する際に、これを加えることを忘れないようご注意ください。
    コーディング方法によりますが、すべてのアクティビティ共通のスーパークラスで実装したほうが良い場合もあります。

5. The `amount` parameter of the `trackRevenue` methods is now of type
   `double`, so you can drop the `f` suffixes in number literals (`12.3f`
   becomes `12.3`).
5. `trackRevenue`メソッドの`amount`パラメータは`double`型になりました。
   末尾の`f`を外してください(たとえば、`12.3f`は`12.3`になります)。

[README]: ../README.md
[dashboard]: http://adjust.com
[search]: https://raw.github.com/adjust/adjust_sdk/master/Resources/android/search.png
[replace]: https://raw.github.com/adjust/adjust_sdk/master/Resources/android/replace.png
[import]: https://raw.github.com/adjust/adjust_sdk/master/Resources/android/import2.png
[activity]: https://raw.github.com/adjust/adjust_sdk/master/Resources/android/activity4.png
[settings]: https://raw.github.com/adjust/adjust_sdk/master/Resources/android/settings.png
[android_application]:  http://developer.android.com/reference/android/app/Application.html
[application_name]:     http://developer.android.com/guide/topics/manifest/application-element.html#nm
[basic-setup]:          https://github.com/adjust/android_sdk/tree/master#basic-setup

