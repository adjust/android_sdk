# Vulcun

To add the `Vulcun` plugin, first move the file `plugin/Vulcun.java` to the folder `Adjust/src/com/adjust/sdk/plugin/`.

The `Vulcun` plugin allows to collect the sha-1 of the device's primary email. To access this information, you need to add the following permission to the `AndroidManifest.xml` of your app:

````
<uses-permission android:name="android.permission.GET_ACCOUNTS" />
````
Optionally, you can add a string to be concatenated with the email before is hashed with the `sha-1` algorithm. Add the string in a `meta-data` tag inside the `application` tag, as follows:

```
<meta-data android:name="AdjustVulcunSalt" android:value="vulcun salt example" />
```
