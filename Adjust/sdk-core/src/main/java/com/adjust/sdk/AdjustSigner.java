package com.adjust.sdk;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

public class AdjustSigner {

    // https://www.cs.umd.edu/~pugh/java/memoryModel/DoubleCheckedLocking.html
    private static volatile Object signerInstance = null;

    private AdjustSigner() {
    }

    public static boolean isPresent() {
        getSignerInstance();

        if (signerInstance != null) {
            return true;
        }

        return false;
    }

    public static void onResume(ILogger logger){
        getSignerInstance();

        if (signerInstance == null) {
            return;
        }

        try {
            Reflection.invokeInstanceMethod(signerInstance, "onResume", null);
        } catch (Exception e) {
            logger.warn("Invoking Signer onResume() received an error [%s]", e.getMessage());
        }
    }

    public static Map<String, String> sign(final Map<String, String> packageParams,
                                           final Map<String, String> extraParams,
                                           final Context context, ILogger logger) {
        getSignerInstance();

        Map<String, String> outputParams = new HashMap<>();

        if (signerInstance != null) {
            try {
                logger.debug("Signing all the parameters");
                Reflection.invokeInstanceMethod(signerInstance, "sign",
                        new Class[]{Context.class, Map.class, Map.class, Map.class},
                        context, packageParams, extraParams, outputParams);

            } catch (Exception e) {
                logger.warn("Invoking Signer sign() for %s received an error [%s]", e.getMessage());
            }
        }

        return outputParams;
    }

    private static void getSignerInstance() {
        if (signerInstance == null) {
            synchronized (AdjustSigner.class) {
                if (signerInstance == null) {
                    signerInstance = Reflection.createDefaultInstance("com.adjust.sdk.sig.Signer");
                }
            }
        }
    }
}
