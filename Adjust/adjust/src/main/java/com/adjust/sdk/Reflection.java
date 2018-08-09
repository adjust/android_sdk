package com.adjust.sdk;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.LocaleList;
import android.telephony.TelephonyManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;

public class Reflection {
    private static Object getAdvertisingInfoObject(Context context) throws Exception {
        return invokeStaticMethod("com.google.android.gms.ads.identifier.AdvertisingIdClient", "getAdvertisingIdInfo", new Class[]{Context.class}, context);
    }

    public static String getPlayAdId(Context context) {
        try {
            Object AdvertisingInfoObject = getAdvertisingInfoObject(context);
            String playAdid = (String) invokeInstanceMethod(AdvertisingInfoObject, "getId", null);
            return playAdid;
        } catch (Throwable t) {
            return null;
        }
    }

    public static Boolean isPlayTrackingEnabled(Context context) {
        try {
            Object AdvertisingInfoObject = getAdvertisingInfoObject(context);
            Boolean isLimitedTrackingEnabled = (Boolean) invokeInstanceMethod(AdvertisingInfoObject, "isLimitAdTrackingEnabled", null);
            Boolean isPlayTrackingEnabled = (isLimitedTrackingEnabled == null ? null : !isLimitedTrackingEnabled);
            return isPlayTrackingEnabled;
        } catch (Throwable t) {
            return null;
        }
    }

    public static String getMacAddress(Context context) {
        try {
            // String macSha1 = (String) invokeStaticMethod("com.adjust.sdk.MacAddressUtil", "getMacAddress", new Class[]{Context.class}, context);
            // return macSha1;

            return MacAddressUtil.getMacAddress(context);
        } catch (Throwable t) {
            return null;
        }
    }

    public static String getAndroidId(Context context) {
        try {
            // String androidId = (String) invokeStaticMethod("com.adjust.sdk.AndroidIdUtil", "getAndroidId", new Class[]{Context.class}, context);
            // return androidId;

            return AndroidIdUtil.getAndroidId(context);
        } catch (Throwable t) {
            return null;
        }
    }

    public static String[] getSupportedAbis() {
        String[] supportedAbis = null;
        try {
            // supportedAbis = (String[])readField("android.os.Build", "SUPPORTED_ABIS");

            // Build.SUPPORTED_ABIS added as of API 21.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                supportedAbis = Build.SUPPORTED_ABIS;
            }
        } catch (Throwable t) {}
        return supportedAbis;
    }

    public static String getCpuAbi() {
        String cpuAbi = null;
        try {
            // cpuAbi = (String) readField("android.os.Build", "CPU_ABI");

            // Build.CPU_ABI deprecated as of API 21.
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                cpuAbi = Build.CPU_ABI;
            }
        } catch (Throwable t) {}
        return cpuAbi;
    }

    public static Locale getLocaleFromLocaleList(Configuration configuration) {
        Locale locale = null;
        try {
            // Object localesList = invokeInstanceMethod(configuration, "getLocales", null);
            // if (localesList ==  null) {
            //    return null;
            // }
            // locale = (Locale)invokeInstanceMethod(localesList, "get", new Class[]{int.class}, 0);

            // Configuration.getLocales() added as of API 24.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                LocaleList localesList = configuration.getLocales();
                if (localesList == null) {
                    return null;
                }
                return localesList.get(0);
            }
        } catch (Throwable t) {}
        return locale;
    }

    public static Locale getLocaleFromField(Configuration configuration) {
        Locale locale = null;
        try {
            // locale = (Locale)readField("android.content.res.Configuration", "locale", configuration);

            // Configuration.locale deprecated as of API 24
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                return configuration.locale;
            }
        } catch (Throwable t) {}
        return locale;
    }

    public static String getImei(TelephonyManager telephonyManager) {
        // return telephonyManager.getImei();
        try {
            return (String) invokeInstanceMethod(telephonyManager, "getImei", null);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getImei(TelephonyManager telephonyManager, int index) {
        // return telephonyManager.getImei();
        try {
            return (String) invokeInstanceMethod(telephonyManager, "getImei", new Class[]{int.class}, index);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getMeid(TelephonyManager telephonyManager) {
        // return telephonyManager.getMeid();
        try {
            return (String) invokeInstanceMethod(telephonyManager, "getMeid", null);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getMeid(TelephonyManager telephonyManager, int index) {
        // return telephonyManager.getMeid();
        try {
            return (String) invokeInstanceMethod(telephonyManager, "getMeid", new Class[]{int.class}, index);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getTelephonyId(TelephonyManager telephonyManager) {
        // return telephonyManager.getDeviceId();
        try {
            return (String) invokeInstanceMethod(telephonyManager, "getDeviceId", null);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getTelephonyId(TelephonyManager telephonyManager, int index) {
        // return telephonyManager.getDeviceId();
        try {
            return (String) invokeInstanceMethod(telephonyManager, "getDeviceId", new Class[]{int.class}, index);
        } catch (Exception e) {
            return null;
        }
    }

    public static Class forName(String className) {
        try {
            Class classObject = Class.forName(className);
            return classObject;
        } catch (Throwable t) {
            return null;
        }
    }

    public static Object createDefaultInstance(String className) {
        Class classObject = forName(className);
        Object instance = createDefaultInstance(classObject);
        return instance;
    }

    public static Object createDefaultInstance(Class classObject) {
        try {
            Object instance = classObject.newInstance();
            return instance;
        } catch (Throwable t) {
            return null;
        }
    }

    public static Object createInstance(String className, Class[] cArgs, Object... args) {
        try {
            Class classObject = Class.forName(className);
            @SuppressWarnings("unchecked")
            Constructor constructor = classObject.getConstructor(cArgs);
            Object instance = constructor.newInstance(args);
            return instance;
        } catch (Throwable t) {
            return null;
        }
    }

    public static Object invokeStaticMethod(String className, String methodName, Class[] cArgs, Object... args)
            throws Exception {
        Class classObject = Class.forName(className);

        return invokeMethod(classObject, methodName, null, cArgs, args);
    }

    public static Object invokeInstanceMethod(Object instance, String methodName, Class[] cArgs, Object... args)
            throws Exception {
        Class classObject = instance.getClass();

        return invokeMethod(classObject, methodName, instance, cArgs, args);
    }

    public static Object invokeMethod(Class classObject, String methodName, Object instance, Class[] cArgs, Object... args)
            throws Exception {
        @SuppressWarnings("unchecked")
        Method methodObject = classObject.getMethod(methodName, cArgs);
        if (methodObject == null) {
            return null;
        }
        Object resultObject = methodObject.invoke(instance, args);

        return resultObject;
    }

    public static Object readField(String className, String fieldName)
            throws Exception {
        return readField(className, fieldName, null);
    }

    public static Object readField(String className, String fieldName, Object instance)
            throws Exception {
        Class classObject = forName(className);
        if (classObject == null) {
            return null;
        }
        Field fieldObject = classObject.getField(fieldName);
        if (fieldObject == null) {
            return null;
        }
        return fieldObject.get(instance);
    }
}
