## Summary

This is the Android SDK of AdjustIo. You ca read more about it at [adjust.io][].

## Basic Installation

These are the minimal steps required to integrate the AdjustIo SDK into your Android project. We are going to assume that you use Eclipse for your Android development.

### 1. Get the SDK
Download the latest version [here][download]. Extract the archive in a folder of your liking.

### 2. Add it to your project
In the Eclipse menu select `File|New|Project...`.
![New Project][project]
From the Wizard expand the `Android` group and select `Android Project from Existing Code` and click `Next`.
![Android Project][android]
On the top of the next screen click the `Browse...` button and locate the folder you extracted in step 1. Select the AdjustIo subfolder and click `Open`. In the `Projects:` group make sure the AdjustIo project is selected. Also tick the option `Copy projects into workspace` and click `Finish`.
![Import Projects][import]

### 3. Integrate AdjustIo into your app
In the Package Explorer right click on your Android project and select `Properties`.
![Project Properties][properties]
In the left pane select `Android`. In the bottom right group `Library` click the `Add...` button. From the list select the AdjustIo library project and click `OK`. Save your changed project properties by clicking `OK` again.
![Add Library][library]
In the Package Explorer open the `AndroidManifest.xml` of your Android project. Add the `uses-permission` tags for `INTERNET` and `ACCESS_WIFI_STATE` if they are not present already.
![Add Permissions][permissions]
In the Package Explorer open the launch activity of your Android App. Add the line `import com.adeven.adjustio.AdjustIo;` to the top of the source file. In the onCreate method of your activity add the line `AdjustIo.appDidLaunch(getApplication());`. This tells AdjustIo about the launch of your Application.
![Adjust Activity][activity]

### 4. Build your app
Build and run your Android app. In your LogCat viewer you can set the filter `tag:AdjustIo` to hide all other logs. After your app has launched you should see the following AdjustIo log: `Tracked session start.`

[adjust.io]: http://www.adjust.io
[download]: https://github.com/adeven/adjust_android_sdk/zipball/master
[project]: https://raw.github.com/adeven/adjust_sdk/master/Resources/android/project.png
[android]: https://raw.github.com/adeven/adjust_sdk/master/Resources/android/android.png
[import]: https://raw.github.com/adeven/adjust_sdk/master/Resources/android/import.png
[properties]: https://raw.github.com/adeven/adjust_sdk/master/Resources/android/properties.png
[library]: https://raw.github.com/adeven/adjust_sdk/master/Resources/android/library.png
[permissions]: https://raw.github.com/adeven/adjust_sdk/master/Resources/android/permissions.png
[activity]: https://raw.github.com/adeven/adjust_sdk/master/Resources/android/activity.png
