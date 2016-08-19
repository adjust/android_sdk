# Google Advertising Id

The default behavior of the Android SDK of adjust is to send the [Google Advertising ID][google_ad_id] when it's
available. Only if the app does not use [Google Play Services][ensure], 
we try to obtain the Android Id and Mac Address of the device.

Although we have this protection to prevent the use of Mac Address or Android Id for a Google Play Services app,
it's possible to remove from the source the files that access this device functions. Follow the steps to do so:

1. Get the Android SDK of adjust by following the first step of our [guide][get_sdk].

2. Find the folder `adjust/src/main/java/com/adjust/sdk/plugin`. 
It contains both the files `MacAddressUtil.java` and `AndroidIdUtil.java`.

3. Delete one or both files, that you don't want to be used in any case from the project.

[google_ad_id]:https://developer.android.com/google/play-services/id.html
[ensure]:http://developer.android.com/google/play-services/setup.html#ensure
[get_sdk]:https://github.com/adjust/android_sdk#1-get-the-sdk
