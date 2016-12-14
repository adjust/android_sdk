# Adjust Store & Pre-install Tracker Tool

Adjust Store & Pre-install Tracker Tool (`adjust-dtt`) is a small ruby command line tool that enables you to insert pre-installed adjust tracker information into your APK file. This information will later be read by the adjust SDK and enable you to tie the users of each pre-installed app to a specific adjust tracker.

In order to run the `adjust-dtt` tool, you need to have three tools installed on your Linux / Mac OS X machine:

1. `ruby` – used for running the `adjust-dtt` tool
2. `apktool` – used for APK unpacking and re-packing
3. `jarsigner` – used for APK signing purposes

Ruby comes installed by default on Mac OS X. In case your Linux distribution doesn’t have it installed by default, please check these instructions on how to install it on different Linux distributions: https://www.ruby-lang.org/en/documentation/installation/

How to install the apktool is described here: https://ibotpeaches.github.io/Apktool/install/

If you have the `brew` package manager (http://brew.sh/) for Mac OS X set up on your machine, you can easily install the apktool by running:

```
brew install apktool
```

Alternatively, `jarsigner` is the tool available as part of the Java Development Kit (JDK) which needs to be installed on your machine. For example, once JDK (in this case, version 1.8) is installed on Mac OS X, it will be found at this location:

```
/Library/Java/JavaVirtualMachines/jdk1.8.0_102.jdk/
```

The JDK installation contains a `bin` folder, which (if we use the same installation path as above) is located at this path:

```
/Library/Java/JavaVirtualMachines/jdk1.8.0_102.jdk/Contents/Home/bin/
```

Inside of this folder, you can find the `jarsigner` tool, which is needed in order to use the `adjust-dtt` tool.

The `adjust-dtt` tool assumes that the paths of both of these tools (`apktool` and `jarsigner`) are added to your system `PATH` variable. If this is not done, you will need to add the folders containing these two tools to the `PATH` variable.

**Note**: Adding the `apktool` via either of these suggested methods (official installation guide or `brew`) will enable you to run the `apktool` from any place in your terminal (its path will be added automatically to your `PATH` variable), but in case you decided to add the `apktool` in a way in which its location is not part of the `PATH` variable, you will need to add a containing folder to the `PATH` variable.

Let's assume that these are the locations of the `apktool` (1) and `jarsigner` (2):

1. /User/dummy/some/random/folder/
2. /Library/Java/JavaVirtualMachines/jdk1.8.0_102.jdk/Contents/Home/bin/

In order to add these two locations to the `PATH` variable, you need to edit your `.bash_profile` file. It is usually located in the root of your `$HOME` directory:

```
$HOME/.bash_profile
```

Open this with your preferred editor and add the following to the end of it:

```
export PATH="/Users/dummy/some/random/folder:/Library/Java/JavaVirtualMachines/jdk 1.8.0_102.jdk/Contents/Home/bin:$PATH"
```

In case  the `apktool` exists automatically in your `PATH` (as part of an installation via `brew`, for example), you don’t need to add its path to the `PATH` variable. Instead (and following the example above), add the following line, instead of the one above:

```
export PATH="/Library/Java/JavaVirtualMachines/jdk1.8.0_102.jdk/Contents/Home/bin: $PATH"
```

Save the changes and run the following command to immediately apply the changes:

```
source $HOME/.bash_profile
```

**Note**: This tutorial assumes that you are using the `bash` shell. If you are using another shell (`zsh`, for example), please perform the above steps in accordance with your shell settings.

You have now set everything up so that you can use the `adjust-dtt` tool.

The APK that you are planning to distribute is signed; even if you have generated an "unsigned" version of the APK, Android Studio will still have signed it with a default debug signing keystore which exists on your machine. The most probable location for this is:

```
$HOME/.android/debug.keystore
```

If you are exporting a signed APK from Android Studio, then you are signing the APK with your custom signing keystore file.

You will need to have access to either your default debug keystore file or your custom one in order to use the `adjust-dtt` tool. The reason for this is that the `apktool`, once used to unpack the APK, completely removes any signing from the APK, and once the APK is repacked following editing, signing is not re-enabled. Because of this, you will need to run the `jarsigner` tool and pass signing information to it in order to re-sign your APK so that it can be delivered and used on Android devices.

If everything is ready, the only thing left to do is to create a tracker on the adjust dashboard that you want to use as the default tracker for your pre-installed app and get its token.

With all this information, you should be able to generate an `adjust-config.yaml` configuration file and fill it up with settings for each store you want your app to be published in for which you need to set up pre-installed tracker settings.

Below, you can see an example `adjust-config.yaml` file which assumes that we want to build an APK for three different imaginary stores named **store_1**, **store_2** and **store_3**:

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

The first four parameters can be defined globally in root if you want those settings to be used for every store for which you do not define that parameter. If you define a parameter globally and for the store, the specific store parameter will be used for APK generation. So, for example, the config file can look like this:

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

In this case, the `adjust-dtt` tool will use `differentkeystore.jks`, `differentkeystorepass` and `differentkeystorealias` when generating the APK for `store_1`. The **stores** parameter must be in the root, and inside of it you can define as many stores as you like. A modified APK file will be generated for each store.

With the `adjust-config.yaml` file properly configured, you just need to run the `adjust-dtt` tool like this:

```
adjust-dtt adjust-config.yaml
```

**Note**: If you are unable to run the `adjust-dtt` tool, check if it has executive permissions assigned, and, if it does not, please run:

```
chmod +x adjust-dtt
```

Then try to run the `adjust-dtt` tool again.

When you run the `adjust-dtt` tool, the following happens:

1. Your app's APK file is unpacked and a folder bearing your APK's name is created in the folder where the APK file is located.
2. An assets folder is searched for followed by an `adjust_config.properties` file (which contains the default tracker info) within the assets folder.
3. If the assets folder or `adjust_config.properties` file do not exist, they are auto-created and the tracker passed as the `default_tracker` parameter is written to the config file.
4. If the APK already contains an `adjust_config.properties` file but the default tracker value isn't written in it, the `default_tracker` parameter value is written to the file. If the config file already contains a default tracker value, the existing value is compared with the one you passed to the command, and, if they differ, the one you passed to the command is used.
5. The APK is repacked.
6. The APK is signed with the information you passed as the values of the `keystore_path`, `keystore_pass` and `keystore_alias` parameters.
7. That's it. The generated APK will have the same name as your original one with a `_[store_name]` suffix appended to it. In the aobve example, we will get three APK files generated named `example-release_store_1.apk`, `example- release_store_2.apk` and `example-release_store_3.apk`. Feel free to rename them as you wish before you distribute them.

If you have any questions or issues with this tool, feel free to contact us: support@adjust.com

