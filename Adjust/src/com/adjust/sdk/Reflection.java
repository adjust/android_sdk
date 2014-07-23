package com.adjust.sdk;

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

    private static Object getAdvertisingInfoObject(Context context)
            throws Exception {
        return invokeStaticMethod("com.google.android.gms.ads.identifier.AdvertisingIdClient",
                "getAdvertisingIdInfo",
                new Class[] {Context.class} , context
        );

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
