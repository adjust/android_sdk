package com.adjust.sdk;

import android.content.Context;

import java.util.Map;

public class AdjustSigner {

    // https://www.cs.umd.edu/~pugh/java/memoryModel/DoubleCheckedLocking.html
    private static volatile Object signer = null;

    private AdjustSigner() {
    }

    public static void enableSigning(ILogger logger) {
        getSignerInstance();

        if (signer == null) {
            return;
        }

        try {
            Reflection.invokeInstanceMethod(signer, "enableSigning", null);
        } catch (Exception e) {
            logger.warn("Invoking Signer enableSigning() received an error [%s]", e.getMessage());
        }
    }

    public static void disableSigning(ILogger logger) {
        getSignerInstance();

        if (signer == null) {
            return;
        }

        try {
            Reflection.invokeInstanceMethod(signer, "disableSigning", null);
        } catch (Exception e) {
            logger.warn("Invoking Signer disableSigning() received an error [%s]", e.getMessage());
        }
    }

    public static void onResume(ILogger logger){
        getSignerInstance();

        if (signer == null) {
            return;
        }

        try {
            Reflection.invokeInstanceMethod(signer, "onResume", null);
        } catch (Exception e) {
            logger.warn("Invoking Signer onResume() received an error [%s]", e.getMessage());
        }
    }

    public static void sign(Map<String, String> parameters, String activityKind, String clientSdk,
                      Context context, ILogger logger) {
        getSignerInstance();

        if (signer == null) {
            return;
        }

        try {
            Reflection.invokeInstanceMethod(signer, "sign",
                            new Class[]{Context.class, Map.class, String.class, String.class},
                            context, parameters, activityKind, clientSdk);

        } catch (Exception e) {
            logger.warn("Invoking Signer sign() for %s received an error [%s]", activityKind, e.getMessage());
        }
    }

    private static void getSignerInstance() {
        if (signer == null) {
            synchronized (AdjustSigner.class) {
                if (signer == null) {
                    signer = Reflection.createDefaultInstance("com.adjust.sdk.sig.Signer");
                }
            }
        }
    }


}
