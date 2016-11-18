package com.adjust.sdk;

import android.content.Context;
import android.content.res.Configuration;

import com.adjust.sdk.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.adjust.sdk.Constants.PLUGINS;

public class Reflection {

    public static Object getVMRuntimeObject() {
        try {
            return invokeStaticMethod("dalvik.system.VMRuntime", "getRuntime", null);
        } catch (Throwable t) {
            return null;
        }
    }

    public static String getVmInstructionSet() {
        try {
            Object VMRuntimeObject = getVMRuntimeObject();

            String vmInstructionSet = (String)invokeInstanceMethod(VMRuntimeObject, "vmInstructionSet", null);
            return vmInstructionSet;
        } catch (Throwable t) {
            return null;
        }
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
            String macSha1 = (String) invokeStaticMethod(
                    "com.adjust.sdk.plugin.MacAddressUtil",
                    "getMacAddress",
                    new Class[]{Context.class}, context
            );

            return macSha1;
        } catch (Throwable t) {
            return null;
        }
    }

    public static String getAndroidId(Context context) {
        try {
            String androidId = (String) invokeStaticMethod("com.adjust.sdk.plugin.AndroidIdUtil", "getAndroidId"
                    , new Class[]{Context.class}, context);

            return androidId;
        } catch (Throwable t) {
            return null;
        }
    }

    private static Object getAdvertisingInfoObject(Context context)
            throws Exception {
        return invokeStaticMethod("com.google.android.gms.ads.identifier.AdvertisingIdClient",
                "getAdvertisingIdInfo",
                new Class[]{Context.class}, context
        );
    }

    public static String[] getSupportedAbis() {
        String[] supportedAbis = null;
        try {
            supportedAbis = (String[])readField("android.os.Build", "SUPPORTED_ABIS");
        } catch (Throwable t) {}
        return supportedAbis;
    }

    public static String getCpuAbi() {
        String cpuAbi = null;
        try {
            cpuAbi = (String) readField("android.os.Build", "CPU_ABI");
        }catch (Throwable t) {}
        return cpuAbi;
    }

    public static Locale getLocaleFromLocaleList(Configuration configuration) {
        Locale locale = null;
        try {
            Object localesList = invokeInstanceMethod(configuration, "getLocales", null);
            if (localesList ==  null) {
                return null;
            }
            locale = (Locale)invokeInstanceMethod(localesList, "get", new Class[]{int.class}, 0);

        }catch (Throwable t) {}
        return locale;
    }

    public static Locale getLocaleFromField(Configuration configuration) {
        Locale locale = null;
        try {
            locale = (Locale)readField("android.content.res.Configuration", "locale", configuration);
        }catch (Throwable t) {}
        return locale;
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

    public static Map<String, String> getPluginKeys(Context context) {
        Map<String, String> pluginKeys = new HashMap<String, String>();

        for (Plugin plugin : getPlugins()) {
            Map.Entry<String, String> pluginEntry = plugin.getParameter(context);
            if (pluginEntry != null) {
                pluginKeys.put(pluginEntry.getKey(), pluginEntry.getValue());
            }
        }

        if (pluginKeys.size() == 0) {
            return null;
        } else {
            return pluginKeys;
        }
    }

    private static List<Plugin> getPlugins() {
        List<Plugin> plugins = new ArrayList<Plugin>(PLUGINS.size());

        for (String pluginName : PLUGINS) {
            Object pluginObject = Reflection.createDefaultInstance(pluginName);
            if (pluginObject != null && pluginObject instanceof Plugin) {
                plugins.add((Plugin) pluginObject);
            }
        }

        return plugins;
    }
}
