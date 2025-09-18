# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep all classes in the LVL licensing AIDL package
-keep class com.android.vending.licensing.** { *; }

# Keep interface method signatures
-keep interface com.android.vending.licensing.ILicensingService
-keep interface com.android.vending.licensing.ILicenseResultListener

# Prevent obfuscation of the stub classes used for IPC
-keepclassmembers class * implements android.os.IInterface {
    <methods>;
}