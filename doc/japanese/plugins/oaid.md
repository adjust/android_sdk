## OAIDプラグイン

OAIDは、MSA (Mobile Security Alliance) SDKを実装した端末で利用できる広告IDです。このIDを利用すると、Google Play Servicesが利用できない市場でもAndroid端末の流入元を計測することができます。 

OAIDプラグインにより、AdjustのAndroid SDKはデフォルトで取得する他のデバイスIDに加えて、デバイスのOAID値を読み取ることができるようになります。 

始めに、公式の[Android SDK README] [readme]をご一読いただき、Adjust SDKがアプリに正常に実装されていることを確認してください。

Adjust SDKがOAID値を収集し計測するための設定手順は、以下のとおりです。

### OAIDプラグインをアプリに追加する

Mavenを使用している場合は、以下のOAIDプラグイン dependencyを、既存のAdjust SDKのdependencyの隣にあるbuild.gradle` file に追加します：

```
implementation 'com.adjust.sdk:adjust-android:4.19.1'
implementation 'com.adjust.sdk:adjust-android-oaid:4.19.1'
```

Adjust OAIDプラグインを JARファイルとして追加することもできます。JARファイルは、[releases page][releases] からダウンロードすることができます。

### Proguardの設定

Proguardを使用し、かつ Google Playストアでアプリを公開しない場合には、Google Play Servicesに関連する全てのルールを削除し、[SDK README][readme proguard]でリファラーライブラリをインストールすることができます。

以下のような `com.adjust.sdk`パッケージルールを使用します：

```
-keep public class com.adjust.sdk.**{ *; }
```

### プラグインを使用する

OAID値を読み取る際には、SDKを開始する前に `AdjustOaid.readOaid（）`を呼び出します。

```java
AdjustOaid.readOaid();

// ...

Adjust.onCreate(config);
```

SDKがOAID値を読み取らないようにするためには、 `AdjustOaid.doNotReadOaid（）`を呼び出します。


[readme]:    ../../japanese/README.md
[releases]:  https://github.com/adjust/android_sdk/releases
[readme proguard]: ../../japanese/README.md#qs-proguard
