## OAIDプラグイン

MSA (Mobile Security Alliance) は、中国で製造される全ての端末が、広告IDであるOAIDを提供するよう推奨しています。OAIDを利用することで、Google Play Servicesが提供されていない市場でも、Android端末の流入元を計測することが可能です。

OAIDプラグインをAdjustのAndroid SDKに追加することで、デフォルトで取得する他のデバイスIDに加えて、端末のOAID値を読み取ることができるようになります。MSA SDKを使用している全ての端末、またはHuaweiの端末でHSM（Huawei Mobile Service）を使用している場合にOAIDの読み取りが可能です。

始めに、公式の[Android SDK README][readme] をご一読いただき、Adjust SDKがアプリに正常に実装されていることを確認してください。

Adjust SDKがOAID値を収集し計測するための設定手順は、以下のとおりです。HuaweiデバイスのOAIDを読み取る目的のためだけにプラグインを使用する場合は、「アプリにMSA SDKを追加する」のステップを省略できます。

### OAIDプラグインをアプリに追加する

Mavenを使用している場合は、以下のOAIDプラグイン dependencyを、既存のAdjust SDKのdependencyの隣にあるbuild.gradle` file に追加します：

```
implementation 'com.adjust.sdk:adjust-android:4.27.0'
implementation 'com.adjust.sdk:adjust-android-oaid:4.27.0'
```

Adjust OAIDプラグインを JARファイルとして追加することもできます。JARファイルは、[releases page][releases] からダウンロードすることができます。

### アプリにMSA SDKを追加する

注：Huawei端末からOAIDを読み取る場合は、MSA SDKを追加する必要はありません。この場合、OAIDプラグインはHuawei Mobile Service（バージョン2.6.2以降）を使用します。

OAIDプラグインによりMSA SDKでOAID値を読み取ることができるようにするには、MSA SDK（AARファイル）をプロジェクトのlibsディレクトリにコピーし、dependencyを設定します。また、supplierconfig.jsonをプロジェクトのassetsディレクトリにコピーする必要があります。

MSA SDKと詳細な手順については[こちら](msasdk)をご覧ください。


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
[msasdk]:  http://www.msa-alliance.cn/col.jsp?id=120
