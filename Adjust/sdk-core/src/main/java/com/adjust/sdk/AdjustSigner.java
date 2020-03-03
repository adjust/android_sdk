package com.adjust.sdk;

import android.content.Context;

import java.util.Map;

public class AdjustSigner {

    private static boolean isSigningEnabled = true;  // by default enabled
    private static Object signer = null;

    public static void enableSigning() {
        isSigningEnabled = true;
    }

    public static void disableSigning() {
        isSigningEnabled = false;
    }

    public static void onResume(ILogger logger){
        if (!isSigningEnabled) {
            return;
        }

        Object signer = getSigner();
        if (signer == null) {
            return;
        }

        try {
            Reflection.invokeInstanceMethod(signer, "onResume", null);
        } catch (Exception e) {
            logger.warn("Invoking SigV2 onResume() received an error [%s]", e.getMessage());
        }
    }

    public static void sign(Map<String, String> parameters, String activityKind, String clientSdk,
                      Context context, ILogger logger) {
        if (!isSigningEnabled) {
            return;
        }

        Object signer = getSigner();
        if (signer == null) {
            return;
        }

        try {
            Reflection.invokeInstanceMethod(signer, "sign",
                            new Class[]{Context.class, Map.class, String.class, String.class},
                            context, parameters, activityKind, clientSdk);

        } catch (Exception e) {
            logger.warn("Invoking SigV2 sign() for %s received an error [%s]", activityKind, e.getMessage());
        }
    }

    private static Object getSigner() {
        if (!isSigningEnabled) {
            return null;
        }

        if (signer == null) {
            signer = Reflection.createDefaultInstance("com.adjust.sdk.sigv2.Signer");
        }

        if (signer == null) {
            isSigningEnabled = false;
        }

        return signer;
    }


}
