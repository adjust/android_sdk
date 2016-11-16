package com.adjust.sdk;

import android.content.Context;

import com.adjust.sdk.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.adjust.sdk.Constants.PLUGINS;

public class Reflection {

    public static String getPlayAdId(final Context context) {
        try {
            Object advertisingInfoObject = getAdvertisingInfoObject(context);

            return (String) invokeInstanceMethod(advertisingInfoObject, "getId", null);
        } catch (Throwable t) {
            return null;
        }
    }

    public static Boolean isPlayTrackingEnabled(final Context context) {
        try {
            Object advertisingInfoObject = getAdvertisingInfoObject(context);

            Boolean isLimitedTrackingEnabled = (Boolean) invokeInstanceMethod(advertisingInfoObject, "isLimitAdTrackingEnabled", null);

            return (isLimitedTrackingEnabled == null ? null : !isLimitedTrackingEnabled);
        } catch (Throwable t) {
            return false;
        }
    }

    public static String getMacAddress(final Context context) {
        try {
            return (String) invokeStaticMethod(
                    "com.adjust.sdk.plugin.MacAddressUtil",
                    "getMacAddress",
                    new Class[]{Context.class}, context
            );
        } catch (Throwable t) {
            return null;
        }
    }

    public static String getAndroidId(final Context context) {
        try {
            return (String) invokeStaticMethod("com.adjust.sdk.plugin.AndroidIdUtil", "getAndroidId"
                    , new Class[]{Context.class}, context);
        } catch (Throwable t) {
            return null;
        }
    }

    private static Object getAdvertisingInfoObject(final Context context)
            throws Exception {
        return invokeStaticMethod("com.google.android.gms.ads.identifier.AdvertisingIdClient",
                "getAdvertisingIdInfo",
                new Class[]{Context.class}, context
        );
    }

    private static boolean isConnectionResultSuccess(final Integer statusCode) {
        if (statusCode == null) {
            return false;
        }

        try {
            Class connectionResultClass = Class.forName("com.google.android.gms.common.ConnectionResult");
            Field successField = connectionResultClass.getField("SUCCESS");

            int successStatusCode = successField.getInt(null);
            return successStatusCode == statusCode;
        } catch (Throwable t) {
            return false;
        }
    }

    public static String[] getSupportedAbis() {
        String[] supportedAbis = null;
        Class buildClass = forName("android.os.Build");

        try {
            Field supportedAbisField = null;
            if (buildClass != null) {
                supportedAbisField = buildClass.getField("SUPPORTED_ABIS");
            }

            assert supportedAbisField != null;
            Object supportedAbisObject = supportedAbisField.get(null);

            if (supportedAbisObject instanceof String[]) {
                supportedAbis = (String[]) supportedAbisObject;
            }
        } catch (Exception ignored) {
        }

        return supportedAbis;
    }

    public static String getCpuAbi() {
        String cpuAbi = null;
        Class buildClass = forName("android.os.Build");

        try {
            Field cpuAbiField = null;
            if (buildClass != null) {
                cpuAbiField = buildClass.getField("CPU_ABI");
            }

            Object cpuAbiObject = null;
            if (cpuAbiField != null) {
                cpuAbiObject = cpuAbiField.get(null);
            }

            if (cpuAbiObject instanceof String) {
                cpuAbi = (String) cpuAbiObject;
            }
        } catch (Exception ignored) {
        }
        return cpuAbi;
    }

    public static Class forName(final String className) {
        try {
            return (Class) Class.forName(className);
        } catch (Throwable t) {
            return null;
        }
    }

    public static Object createDefaultInstance(final String className) {
        Class classObject = forName(className);
        return createDefaultInstance(classObject);
    }

    public static Object createDefaultInstance(final Class classObject) {
        try {
            return classObject.newInstance();
        } catch (Throwable t) {
            return null;
        }
    }

    public static Object createInstance(final String className,
                                        final Class[] cArgs,
                                        final Object... args) {
        try {
            Class classObject = Class.forName(className);
            @SuppressWarnings("unchecked")
            Constructor constructor = classObject.getConstructor(cArgs);
            return constructor.newInstance(args);
        } catch (Throwable t) {
            return null;
        }
    }

    public static Object invokeStaticMethod(final String className,
                                            final String methodName,
                                            final Class[] cArgs,
                                            final Object... args)
            throws Exception {
        Class classObject = Class.forName(className);

        return invokeMethod(classObject, methodName, null, cArgs, args);
    }

    public static Object invokeInstanceMethod(final Object instance,
                                              final String methodName,
                                              final Class[] cArgs,
                                              final Object... args)
            throws Exception {
        Class classObject = instance.getClass();

        return invokeMethod(classObject, methodName, instance, cArgs, args);
    }

    public static Object invokeMethod(final Class classObject,
                                      final String methodName,
                                      final Object instance,
                                      final Class[] cArgs,
                                      final Object... args)
            throws Exception {
        @SuppressWarnings("unchecked")
        Method methodObject = classObject.getMethod(methodName, cArgs);

        return methodObject.invoke(instance, args);
    }

    public static Map<String, String> getPluginKeys(final Context context) {
        Map<String, String> pluginKeys = new HashMap<>();

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
        List<Plugin> plugins = new ArrayList<>(PLUGINS.size());

        for (String pluginName : PLUGINS) {
            Object pluginObject = Reflection.createDefaultInstance(pluginName);
            if (pluginObject != null && pluginObject instanceof Plugin) {
                plugins.add((Plugin) pluginObject);
            }
        }

        return plugins;
    }
}
