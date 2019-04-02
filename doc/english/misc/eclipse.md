## Integrate Adjust using Eclipse

Since SDK version 4.0.0 we recommend using Android Studio. Follow these steps to integrate the Adjust SDK using Eclipse.

Please, have in mind that this document contains only integration information which differs from the ones stated in official [README][readme]. Once you do Eclipse specific integration described in this document, please refer to main `README` for any additional integration and/or SDK usage instructions.

## Basic Installation

The most straightforward way to integrate the Adjust SDK in an Eclipse project is by linking the compiled JAR.

### Get the JAR

You can get the Adjust SDK JAR from our [releases page][releases]. Another way is to download it from the [Maven repository][maven] by searching for [`com.adjust.sdk`][maven-search].

### Add the Adjust SDK JAR to your project

After downloading the JAR file, drag it into the `libs` folder inside your project. This will make the Adjust SDK available in your app.

### Add Google Play Services

Since the 1st of August of 2014, apps in the Google Play Store must use the [Google Advertising ID][google-ad-id] to uniquely identify devices. To allow the Adjust SDK to use the Google Advertising ID, you must integrate the [Google Play Services][google-play-services]. If you haven't done this yet, follow these steps:

- Copy the library project from `<android-sdk>/extras/google/google-play-services/libproject/google-play-services_lib/` to the location where you maintain your Android app projects.

- Import the library project into your Eclipse workspace. Click `File > Import`, select `Android > Existing Android Code into Workspace`, and browse to the copy of the library project to import it.

- In your app project, reference Google Play services library project. See [Referencing a Library Project for Eclipse][eclipse-library] for more information on how to do this. You should be referencing a copy of the library that you copied to your development workspace. You should not reference the library directly from the Android SDK directory.

- After you've added the Google Play services library as a dependency for your app project, open your app's manifest file and add the following tag as a child of the [<application>][application] element:

    ```xml
    <meta-data android:name="com.google.android.gms.version"
          android:value="@integer/google-play-services_version" />
    ```


[maven]:                http://maven.org
[readme]:               ../../../README.md
[releases]:             https://github.com/adjust/android_sdk/releases
[application]:          http://developer.android.com/guide/topics/manifest/application-element.html
[google-ad-id]:         https://developer.android.com/google/play-services/id.html
[maven-search]:         http://search.maven.org/#search%7Cga%7C1%7Ccom.adjust.sdk
[eclipse-library]:      http://developer.android.com/tools/projects/projects-eclipse.html#ReferencingLibraryProject
[google-play-services]: http://developer.android.com/google/play-services/setup.html
