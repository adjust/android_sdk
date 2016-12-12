# Adjust Store & Pre-Install Tracker Tool

Adjust Store & Pre-Install Tracker Tool (`adjust-dtt`) is the small ruby command line tool which enables you to inject the pre-installed adjust tracker info into your APK file which will later be read by the adjust SDK and enable you to tie your users of pre-installed app to specific adjust tracker.

In order to run `adjust-dtt` tool, you need to have three tools installed on your Linux / Mac OS X machine:

1. `ruby` – used for running the `adjust-dtt` tool
2. `apktool` – used for APK unpacking and re-packing 
3. `jarsigner` – used for APK signing purposes

Ruby is installed by default on Mac OS X. In case your Linux distribution doesn’t have it installed by default, please check these instructions on how to install it on different Linux distributions: https://www.ruby-lang.org/en/documentation/installation/

How to install apktool is described in here: https://ibotpeaches.github.io/Apktool/install/ 
In addition to that, if you have `brew` package manager (http://brew.sh/) for Mac OS X set up on your machine, you can easily install apktool by running:

```
brew install apktool
```

On the other side, `jarsigner` is the tool available as part of the Java Development Kit (JDK) which needs to be installed on your machine. For example, once JDK (version 1.8 in this case) is installed on Mac OS X, it is being installed on this location:

```
/Library/Java/JavaVirtualMachines/jdk1.8.0_102.jdk/
```

JDK installation contains `bin` folder, which (if we consider installation path from example above) is located at this path:

```
/Library/Java/JavaVirtualMachines/jdk1.8.0_102.jdk/Contents/Home/bin/
```

Inside of this folder, you can find `jarsigner` tool which is needed in order to use `adjust-dtt` tool.

`adjust-dtt` tool assumes that paths of both of these tools (`apktool` and `jarsigner`) are added to your system `PATH` variable. If this is not done, you should add folders which are containing these two tools to `PATH` variable.

**Note**: Adding `apktool` with both suggested ways (official installation guide or `brew`) will enable you to run `apktool` from any place in your terminal (it's path will be added automatically to your `PATH` variable), but in case you decided to add `apktool` in some other way where it's location is not part of the `PATH` variable, you need to add containing folder to `PATH` variable.
    
Let's assume that these are locations of `apktool` (1) and `jarsigner` (2): 

1. /User/dummy/some/random/folder/
2. /Library/Java/JavaVirtualMachines/jdk1.8.0_102.jdk/Contents/Home/bin/ 

In order to add these two locations to `PATH` variable, you need to edit your `.bash_profile` file. It is usually located in the root of your `$HOME` directory: 

```
$HOME/.bash_profile
```

Open it with editor of your choice and add following to the end of it:

```
export PATH="/Users/dummy/some/random/folder:/Library/Java/JavaVirtualMachines/jdk 1.8.0_102.jdk/Contents/Home/bin:$PATH"
```

In case that `apktool` exists already automatically in your `PATH` (as part of install via `brew`, for example), you don’t need to put it’s path to `PATH` variable. In that case (and following the example from above), add following line instead of the one from above:

```
export PATH="/Library/Java/JavaVirtualMachines/jdk1.8.0_102.jdk/Contents/Home/bin: $PATH"
```

Save the changes and run following command to apply changes immediately:

```
source $HOME/.bash_profile
```

**Note**: This tutorial assumes that you are using `bash` shell. If you are using some other shells (`zsh`, for example), please perform steps from above in accordance to your shell settings. 

By completing this, you have set up everything so that `adjust-dtt` tool can be used.

Your APK which you are planning to distribute is signed. Even if you have generated "unsigned" version of APK, Android Studio has still signed it with a default debug signing keystore which exists on your machine. Most probable location of it is:

```
$HOME/.android/debug.keystore
```

If you are exporting signed APK from Android Studio, then you are signing the APK with your custom signing keystore file. In both cases, you should have access to either default debug keystore file or your custom one in order to use `adjust-dtt` tool. Reason for this is that `apktool`, once used to unpack APK, completely removes any signing from APK and once APK gets repacked after being edited, signing is not magically re-enabled with this tool. Because of this, we need to run `jarsigner` tool and pass signing information to it in order to re-sign your APK so that it can be delivered and used on Android devices.

If everything is ready, the only thing which you need to do now is to create a tracker on the adjust dashboard which you want to use as the default tracker for your pre-installed app and get it's token. With all these infos, you should be able to generate `adjust-config.yaml` configuration file and fill it up with settings for each store you want your app to be published in for which you need to set up pre-installed tracker setting.

Below you can see the example of `adjust-config.yaml` file which assumes that we want to build our APK for three different imaginary stores named **store_1**, **store_2** and **store_3**:

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

You can set up following parameters:

1. `apk_path` = Full path to your APK file (global and per store)
2. `keystore_path` = Full path to your keystore file used for APK signing (global and per store)
3. `keystore_pass` = Your keystore signing password (global and per store)
4. `keystore_alias` = Your keystore alias (global and per store)
5. `default_tracker` = Your pre-installed adjust tracker token (per store only)

First four parameters are possible to be defined globally in root if you want those settings to be used for each store for which you didn’t define that parameter. If you define same parameter globally and for the store, specific store parameter will be used for APK generation. So, for example, config file can look like this:

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

In this case, `adjust-dtt` tool will use `differentkeystore.jks`, `differentkeystorepass` and `differentkeystorealias` once generating the APK for `store_1`. **stores** parameter is obligatory one to be in the root and inside of it, you can define as much stores as you like for which modified APK file should be generated.

With `adjust-config.yaml` file properly set, you should just run the `adjust-dtt` tool like this:

```
adjust-dtt adjust-config.yaml
```

**Note**: If you are unable to run `adjust-dtt` tool, please check if it has executive permissions assigned and if not, please run:

```
chmod +x adjust-dtt
```

and retry.

Once `adjust-dtt` tool is started, it does following:

1. Unpacks your app's APK file and creates the folder of your APK's name in the same folder where APK is located.
2. Checks for assets folder in it and `adjust_config.properties` file (which contains default tracker info) in the assets folder.
3. If by any chance assets folder or `adjust_config.properties` do not exist, everything gets auto-created and tracker passed as `default_tracker` parameter gets written to config file.
4. If by some chance your APK already contained `adjust_config.properties` file and default tracker value wasn't written in it, we will take `default_tracker` parameter value and write it to the file. If config file already contained default tracker value, we'll compare existing value with the one you passed to the command and if they differ, we'll use the one you passed to the command.
5. After all these actions, APK will get re-packed.
6. APK will get signed with the information you passed as values of `keystore_path`, `keystore_pass` and `keystore_alias` parameters.
7. That's it. Generated APK will have the same name as your original one with `_[store_name]` suffix appended to it. In the example from above, we will get three APK files generated named like `example-release_store_1.apk`, `example- release_store_2.apk` and `example-release_store_3.apk`. Feel free to rename it as you wish and distribute.

If you have any questions or issues with this tool, feel free to contact us: support@adjust.com
 
