# Adjustストア&プレインストールトラッカーツール

Adjustストア&プレインストールトラッカーツール(`adjust-dtt`)は、プレインストールのadjustトラッカー情報をAPKファイルに挿入できるRubyコマンドラインツールです。この情報は後からadjust SDKに読み込まれ、特定のadjustトラッカーとプレインストールされたアプリのユーザーとを結びつけることができます。

`adjust-dtt`ツールを使用するには、LinuxまたはMac OS Xマシン上で以下の3つのツールがインストールされている必要があります。

1. `ruby` – `adjust-dtt`ツールの実行に使われます
2. `apktool` – APKファイルの展開や再圧縮に使われます
3. `jarsigner` – APKファイルの署名に使われます

RubyはMac OS Xではデフォルトでインストールされています。Linuxをお使いの場合でこれがデフォルトでインストールされていなければ、こちらを参考にインストールしてください。https://www.ruby-lang.org/en/documentation/installation/

apktoolのインストール方法はこちらでご確認いただけます。 https://ibotpeaches.github.io/Apktool/install/

Mac OS Xに`brew`パッケージマネジャー(http://brew.sh/)を設定済みでしたら、以下のコマンドでapktoolをインストールできます。

```
brew install apktool
```

もしくは、Java Development Kit (JDK)の中に`jarsigner`が含まれており、これも開発マシンにインストールされている必要があります。たとえば、SDKバージョン1.8がMac OS Xにインストールされていれば、以下の場所に`jarsigner`があるはずです。

```
/Library/Java/JavaVirtualMachines/jdk1.8.0_102.jdk/
```

JDKの中には`bin`フォルダが含まれており、これは例えば上記と同じパスにインストールされた場合、以下の場所にあります。

```
/Library/Java/JavaVirtualMachines/jdk1.8.0_102.jdk/Contents/Home/bin/
```

このフォルダ内に`jarsigner`ツールがあり、これは`adjust-dtt`ツールを使用するのに必要です。

`adjust-dtt`ツールは`apktool`と`jarsigner`の両方のツールのパスが`PATH`変数に追加されていることを想定しています。これらが追加されていない場合、`PATH`変数にこれらのツールを含むフォルダを加える必要があります。

**注意** 公式インストールガイドに従って、または`brew`を使って`apktool`を追加すると、そのパスは自動的に`PATH`変数に追加されますので、ターミナル上でどこからでも`apktool`を実行することができるようになります。しかし別の方法で`apktool`を追加し、その置き場所が`PATH`変数に含まれていない場合、それを含むフォルダを`PATH`変数に追加する必要があります。

ここで`apktool`(1)と`jarsigner`(2)の場所を以下のように仮定します。

1. /User/dummy/some/random/folder/
2. /Library/Java/JavaVirtualMachines/jdk1.8.0_102.jdk/Contents/Home/bin/

これらの場所を`PATH`変数に追加するには、`.bash_profile`ファイルを編集してください。これは通常`$HOME`ディレクトリのルートにあるはずです。

```
$HOME/.bash_profile
```

任意のエディタでこのファイルを開き、末尾に以下の行を加えてください。

```
export PATH="/Users/dummy/some/random/folder:/Library/Java/JavaVirtualMachines/jdk 1.8.0_102.jdk/Contents/Home/bin:$PATH"
```

`apktool`が`PATH`に自動的に追加されている場合は、そのパスを`PATH`変数に追加する必要はありません。上の行の代わりに、以下の行を追加してください。

```
export PATH="/Library/Java/JavaVirtualMachines/jdk1.8.0_102.jdk/Contents/Home/bin: $PATH"
```

変更を保存し、以下のコマンドを実行して変更を反映させてください。

```
source $HOME/.bash_profile
```

**注意** このガイドは`bash` shellの使用を前提としています。`zsh`など他のshellをご利用の場合は、そのshellの設定に合わせて上記の手順を行ってください。

これで`adjust-dtt`ツールの使用に必要な設定が済みました。

配布される予定のAPKは署名されています。APKの"署名なし"バージョンを生成していた場合でも、Android Studioはデフォルトで入っているデバッグ署名用のキーストアを使って署名をします。これは多くの場合以下の場所にあります。

```
$HOME/.android/debug.keystore
```

Android Studioから署名されたAPKをエクスポートする場合は、カスタム署名用のキーストアファイルで署名をします。

`adjust-dtt`ツールを使うには、デフォルトのデバッグ署名用キーストアかカスタムキーストアのいずれかにアクセスできる必要があります。`apktool`はAPKの展開に使われた後、APKからすべての署名を完全に消去しますので、APKが再圧縮されても編集や署名は復活しません。そのため、Android端末に転送し端末上で使えるようにするためには、`jarsigner`ツールを使い署名情報を渡してAPKに署名をし直す必要があります。

すべての設定が済めば、あとはプレインストールのアプリとそのトークンのためのデフォルトとして使うトラッカーをダッシュボード上で生成するのみです。

プレインストールトラッカーの設定には、`adjust-config.yaml`コンフィグファイルを生成し、アプリを公開したいストアごとの設定を記入する必要があります。

下記に、**store_1**、**store_2**、**store_3**と仮定した3つのストア用にAPKファイルを作成する場合の`adjust-config.yaml`ファイルの例を示します。

```yaml
apk_path: /Users/uerceg/Desktop/apk/example-release.apk
keystore_path: /Users/uerceg/Desktop/apk/mykeystore.jks
keystore_pass: mykeystorepass
keystore_alias: mykeystorealias
stores:
store_1:
default_tracker: abc123
store_2:
default_tracker: abc456
store_3:
default_tracker: abc789
```

以下のパラメータを設定できます。

1. `apk_path` = Full path to your APK file (global and per store)
2. `keystore_path` = Full path to your keystore file used for APK signing (global and per store)
3. `keystore_pass` = Your keystore signing password (global and per store)
4. `keystore_alias` = Your keystore alias (global and per store)
5. `default_tracker` = Your pre-installed adjust tracker token (per store only)

上の4つのパラメータは、すべてのストアで共通して設定したい場合、ルートでグローバルに定義することができます。グローバルにパラメータを設定し、さらにストアでも同じパラメータを設定した場合、ストアに設定されたパラメータがAPKの生成に使われます。configファイルは以下のようになります。

```yaml
apk_path: /Users/uerceg/Desktop/apk/example-release.apk
keystore_path: /Users/uerceg/Desktop/apk/mykeystore.jks
keystore_pass: mykeystorepass
keystore_alias: mykeystorealias
stores:
store_1:
default_tracker: abc123
keystore_path: /Users/uerceg/Desktop/apk/differentkeystore.jks
keystore_pass: differentkeystorepass
keystore_alias: differentkeystorealias
store_2:
default_tracker: abc456
store_3:
default_tracker: abc789
```

この場合、`adjust-dtt`ツールは`store_1`用にAPKを生成する際には`differentkeystore.jks`と`differentkeystorepass`と`differentkeystorealias`を使います。**stores**パラメータはルートに置く必要があり、その中で必要な数だけストアを定義することができます。ストアごとに変更されたAPKファイルが生成されます。

`adjust-config.yaml`ファイルが正しく設定されたら、`adjust-dtt`ツールを以下のように実行してください。

```
adjust-dtt adjust-config.yaml
```

**注意** `adjust-dtt`ツールを実行できない場合、実行権限があることをご確認ください。権限がなければ、以下のコマンドを実行してください。

```
chmod +x adjust-dtt
```

これを実行したら、もう一度`adjust-dtt`ツールを実行してみてください。

`adjust-dtt`ツールを実行すると、以下が起こります。

1. アプリのAPKファイルが展開され、APKの名称をもつフォルダがAPKファイルが置かれているフォルダに生成されます。
2. デフォルトのトラッカー情報を含む`adjust_config.properties`ファイルがassetsフォルダを探します。
3. assetsフォルダもしくは`adjust_config.properties`ファイルがない場合、それらは自動的に生成され、`default_tracker`パラメータとして渡されるトラッカーがconfigファイルに書き込まれます。
4. APK内にすでに`adjust_config.properties`ファイルがあり、しかしデフォルトトラッカーの値が書き込まれていない場合、`default_tracker`パラメータの値がファイルに書き込まれます。configファイルがすでにデフォルトトラッカーの値を持っている場合、その亜大はコマンドに渡された値を比較され、これらが一致しない場合はコマンドに渡されたほうの値が使われます。
5. APKが再圧縮されます。
6. `keystore_path`、`keystore_pass`、`keystore_alias`パラメータの値として渡された情報をもとにAPKが署名されます。
7. これで終わりです。生成されたAPKはオリジナルと同じ名前の末尾に`_[store_name]`が付与された名前がついています。上記の例では、それぞれ`example-release_store_1.apk`、`example- release_store_2.apk`、`example-release_store_3.apk`の名前で3つのAPKファイルが生成されます。配布前にご自由に名称を変更してください。

ツールについてご質問があれば、お気軽に support@adjust.com までご連絡ください。
