# adjust with multi-process apps

Android applications can consist of one or more processes. You can choose to run your services or activities in
a process other than the main one. You implement this by setting ```android:process``` property
inside your process or activity definition in the Android manifest file.

```xml
<activity
    android:name=".YourActivity"
    android:process=":YourProcessName">
</activity>
```

```xml
<service
    android:name=".YourService"
    android:process=":YourProcessName">
</service>
```

By defining your services or activities like this, you are forcing them to run in a different process than the
main one.

By default, the name of your main process is the same as your app package name. If your app package name is ```com.example.myapp```, that will also be the name of your main process. In that case, ```YourActivity``` and ```YourService``` will run in a process named ```com.example.myapp:YourProcessName```.

Adjust SDK at this moment __does not support__ tracking from more than one process in app. If you are using
multiple processes in your app, you should set the main process name in the ```AdjustConfig``` object.

```java
String appToken = "{YourAppToken}";
String environment = AdjustConfig.ENVIRONMENT_SANDBOX;  // or AdjustConfig.ENVIRONMENT_PRODUCTION
AdjustConfig config = new AdjustConfig(this, appToken, environment);

config.setMainProcessName("com.example.myapp");

Adjust.onCreate(config);
```

You can also change the name of your main process by changing the ```android:process``` property of your ```application``` in the Android manifest file.

```xml
<application
  android:name=".YourApp"
  android:icon="@drawable/ic_launcher"
  android:label="@string/app_name"
  android:theme="@style/AppTheme"
  android:process=":YourMainProcessName">
</application>
```

If you name your main process like this, then you should set your main process name in ```AdjustConfig```
object like this:

```java
config.setMainProcessName("com.example.myapp:YourMainProcessName");
```

This will inform our SDK of your main process name and our SDK will not initialize
in any other process where you might try to use it. If you try to use the Adjust SDK from some other process, you will
get the following log messages:

```
05-06 13:20:02.880    3442-3442/com.example.myapp:YourProcessName E/Adjust﹕ You can't initialize Adjust in process which is not the main one
05-06 13:20:02.885    3442-3442/com.example.myapp:YourProcessName E/Adjust﹕ AdjustConfig not initialized correctly
```

If you do not set your main process name in the ```AdjustConfig``` object and try to call the SDK in multiple processes, you
will initialize several different Adjust SDK instances, because different processes in Android do not share the same memory
space. This can lead to some unpredictability, so we strongly advise you to always set your main process name if you 
have a multi-process app, or ensure that you do not use the Adjust SDK in more than one process in your app. 
