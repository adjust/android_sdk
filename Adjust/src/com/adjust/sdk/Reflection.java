package com.adjust.sdk;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.content.Context;

public class Reflection {

    public static String getPlayAdId(Context context) {
        try {
            Object AdvertisingInfoObject = getAdvertisingInfoObject(context);

            String playAdid = (String) invokeInstanceMethod(AdvertisingInfoObject, "getId", null);

            return playAdid;
        }
        catch (Throwable t) {
            return null;
        }
    }

    public static boolean isPlayTrackingEnabled(Context context) {
        try {
            Object AdvertisingInfoObject = getAdvertisingInfoObject(context);

            Boolean isLimitedTrackingEnabled = (Boolean) invokeInstanceMethod(AdvertisingInfoObject, "isLimitAdTrackingEnabled", null);

            return !isLimitedTrackingEnabled;
        }
        catch (Throwable t) {
            return false;
        }
    }

    public static boolean isGooglePlayServicesAvailable(Context context) {
        try {
            Integer isGooglePlayServicesAvailableStatusCode = (Integer) invokeStaticMethod(
                    "com.google.android.gms.common.GooglePlayServicesUtil",
                    "isGooglePlayServicesAvailable",
                    new Class[] {Context.class}, context
            );

            boolean isGooglePlayServicesAvailable = (Boolean) isConnectionResultSuccess(isGooglePlayServicesAvailableStatusCode);

            return isGooglePlayServicesAvailable;
        }
        catch (Throwable t) {
            return false;
        }
    }

    public static String getMacAddress(Context context) {
        try {
            String macSha1 = (String) invokeStaticMethod(
                    "com.adjust.sdk.deviceIds.MacAddressUtil",
                    "getMacAddress",
                    new Class[] {Context.class}, context
            );

            return macSha1;
        }
        catch (Throwable t) {
            return null;
        }
    }

    public static String getAndroidId(Context context) {
        try {
            String androidId = (String) invokeStaticMethod("com.adjust.sdk.deviceIds.AndroidIdUtil", "getAndroidId"
                    ,new Class[] {Context.class}, context);

            return androidId;
        }
        catch (Throwable t) {
            return null;
        }
    }

    private static Object getAdvertisingInfoObject(Context context)
            throws Exception {
        return invokeStaticMethod("com.google.android.gms.ads.identifier.AdvertisingIdClient",
                "getAdvertisingIdInfo",
                new Class[] {Context.class} , context
        );
    }

    private static boolean isConnectionResultSuccess(Integer statusCode) {
        if (statusCode == null) {
            return false;
        }

        try {
            Class ConnectionResultClass = Class.forName("com.google.android.gms.common.ConnectionResult");

            Field SuccessField = ConnectionResultClass.getField("SUCCESS");

            int successStatusCode = SuccessField.getInt(null);

            return successStatusCode == statusCode;
        }
        catch (Throwable t) {
            return false;
        }
    }

    private static Object invokeStaticMethod(String className, String methodName, Class[] cArgs, Object... args)
            throws Exception {
        Class classObject = Class.forName(className);

        return invokeMethod(classObject, methodName, null, cArgs, args);
    }

    private static Object invokeInstanceMethod(Object instance, String methodName, Class[] cArgs, Object... args)
            throws Exception {
        Class classObject = instance.getClass();

        return invokeMethod(classObject, methodName, instance, cArgs, args);
    }

    private static Object invokeMethod(Class classObject, String methodName, Object instance, Class[] cArgs, Object... args)
            throws Exception {
        Method methodObject = classObject.getMethod(methodName, cArgs);

        Object resultObject = methodObject.invoke(instance, args);

        return resultObject;
    }
}
