# adjust 商店&预安装跟踪码工具

adjust 商店&预安装跟踪码工具（`adjust-dtt`）是一个ruby命令行工具，使您能够插入adjust预安装跟踪码信息至您的APK文件。该信息将在之后由adjust SDK读取，您可以将每个预安装应用的用户绑定到特定的adjust跟踪码中。

为了运行`adjust-dtt`工具，您需要在您的Linux / Mac OS X机器上安装以下三个工具：

1. `ruby` – 用于运行`adjust-dtt`工具
2. `apktool` – 用于APK解包和重新打包
3. `jarsigner` – 用于给APK签名

Mac OS X默认安装Ruby。如果您的Linux发行版未默认安装Ruby，请查看此说明，了解如何在不同的Linux发行版上安装Ruby： https://www.ruby-lang.org/en/documentation/installation/

参阅这里了解如何安装apktool: https://ibotpeaches.github.io/Apktool/install/

如果您已在机器上安装了用于Mac OS X的`brew`包管理器(http://brew.sh/)，您可以通过运行以下命令轻松安装apktool：

```
brew install apktool
```

或者，您可以在机器上安装`jarsigner`，该工具是Java开发工具包（JDK）的一部分。举例来说，一旦在Mac OS X上安装了JDK（在此例中为版本1.8），您可在此找到它：

```
/Library/Java/JavaVirtualMachines/jdk1.8.0_102.jdk/
```

JDK安装带一个`bin`文件夹，(如果您使用和以上相同的安装路径）该文件夹应位于此路径：

```
/Library/Java/JavaVirtualMachines/jdk1.8.0_102.jdk/Contents/Home/bin/
```

在此文件夹中，您可以找到`jarsigner`工具，该工具对于后续使用`adjust-dtt`工具是必要的。

`adjust-dtt'工具假定此两个工具（`apktool`和`jarsigner`）的路径都被添加至您的系统`PATH`变量中。 如果还未完成添加，您需要将包含这此两个工具的文件夹添加至`PATH`变量。

**注意**: 如果您使用推荐方式（官方安装向导或`brew`）来添加`apktool`，您可以从终端的任何位置运行`apktool`（它的路径将被自动添加至`PATH`变量），但是如果您决定以其它方式，即其位置不是作为`PATH`变量一部分的方式来添加`apktool`，您需要添加一个包含文件夹到`PATH`变量。

假设以下为`apktool` (1)和`jarsigner` (2)的位置：

1. /User/dummy/some/random/folder/
2. /Library/Java/JavaVirtualMachines/jdk1.8.0_102.jdk/Contents/Home/bin/

为了添加这两个位置至`PATH`变量，您需要编辑`.bash_profile`文件。该文件通常位于`$HOME`根目录：

```
$HOME/.bash_profile
```

选择您偏好的编辑器并打开该文件，在文件末端添加如下行：

```
export PATH="/Users/dummy/some/random/folder:/Library/Java/JavaVirtualMachines/jdk 1.8.0_102.jdk/Contents/Home/bin:$PATH"
```

在`apktool`自动存在于您的`PATH`（比如`brew`安装自带）的情况下，您无需再添加路径至`PATH`变量。而是添加下行（参考上例），而不是上行：

```
export PATH="/Library/Java/JavaVirtualMachines/jdk1.8.0_102.jdk/Contents/Home/bin: $PATH"
```

保存更改并运行以下命令立即应用更改：

```
source $HOME/.bash_profile
```

**注意**: 本教程假定您正在使用`bash` shell，如果您在使用其他shell(例如`zsh`)，请依据您的shell设置执行以上步骤。

现在您已经完成所有设置，可以开始使用`adjust-dtt`工具。

您计划发布的APK已经签名；即使生成的是“未签名”的APK版本，Android Studio仍会使用您计算机上的默认调试签名密钥库（keystore)对其进行签名。该密钥库应该位于：

```
$HOME/.android/debug.keystore
```

如果您正在从Android Studio导出已签名的APK，您将使用自定义签名密钥库文件对APK进行签名。

您需要访问您的默认调试密钥库文件或者自定义文件，以使用`adjust-dtt`工具。该操作的原因是`apktool`一旦用于解压APK，将完全删除APK上的所有签名，当APK在编辑后被重新打包，签名则不会被重新启用。因此，您需要运行`jarsigner`工具，并向其传递签名信息，以便重新签名APK，实现APK在安卓设备上的传送和使用。

一切准备就绪后，您最后要做的就是在adjust控制面板上创建一个跟踪链接并获取其识别码，您将使用该跟踪码作为预安装应用的默认跟踪码。

获取所有信息后，您可以生成一个`adjust-config.yaml`配置文件，填入您想要发布您应用的每个商店设置，并设置预安装跟踪码。

如下`adjust-config.yaml`文件示例，假设我们想要为**store_1**， **store_2**和**store_3**三个虚构商店构建一个APK:

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

您可以设置以下参数:

1. `apk_path` = 您的APK文件的完整路径（全局和每个商店）
2. `keystore_path` = 用于APK签名的密钥库文件的完整路径（全局和每个商店）
3. `keystore_pass` = 您的密钥库签名密码（全局和每个商店）
4. `keystore_alias` = 您的密钥库别名（全局和每个商店）
5. `default_tracker` = 您的预安装adjust跟踪码(仅每个商店)

如果您希望对未定义参数的每个商店应用这些设置，您可以在root中全局定义前四个参数。如果您不仅全局而且对每个商店定义了参数，则指定的商店参数将被用于生成APK。举例来说，配置文件将如下：

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


如上例，在为`store_1`生成APK时，`adjust-dtt`工具将使用`differentkeystore.jks`、`differentkeystorepass` 和 `differentkeystorealias`。**stores**参数必须要在root中，您可以在root中按您的需要定义多个商店。之后将为每个商店生成一个修改后的APK文件。

正确设置`adjust-config.yaml`文件后，你只需如下运行`adjust-dtt`工具：

```
adjust-dtt adjust-config.yaml
```

**注意**: 如果您无法运行`adjust-dtt`工具，请检查是否分配了执行权限，如没有，请运行：

```
chmod +x adjust-dtt
```

然后再次尝试运行`adjust-dtt`工具。

当您运行`adjust-dtt`工具时，将会发生以下情况：

1. 您的应用的APK文件被解压，带有您的APK名称的文档将被创建在原APK文件处。
2. 搜索assets文件夹,以及assets文件夹中的`adjust_config.properties`文件（包含默认跟踪码信息）。
3. 如果assets文件夹或者`adjust_config.properties`不存在，它们将被自动创建，跟踪码将以`default_tracker` （默认跟踪码）参数被写入配置文件中。
4. 如果APK已经包含了 `adjust_config.properties`文件，但是默认跟踪码未被写入， 则`default_tracker`参数值将被写入文件。如果APK文件已经包含默认跟踪码，该跟踪码将与您的传递至命令的值比较，如果两个值不同，则使用您传递给命令的值。
5. 重新打包APK。
6. APK将使用您传递的`keystore_path`、 `keystore_pass` 和`keystore_alias`参数值进行签名。
7. 生成的APK将保留和原始名相同的名称，并附加`_[store_name]`后缀。如上例，我们将生成三个APK文件，名称为 `example-release_store_1.apk`、`example- release_store_2.apk` 和 `example-release_store_3.apk`。您可以在发布它们之前随意重新命名这些文件。

如果您对此工具有任何疑问，欢迎随时电邮联系我们： support@adjust.com 。
