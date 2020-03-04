package com.adjust.sdk;

import android.content.Context;

import java.util.Map;

public class AdjustSigner {

    private static Object signer = null;

    public static void enableSigning(ILogger logger) {
        Object signer = getSigner();
        if (signer == null) {
            return;
        }

        try {
            Reflection.invokeInstanceMethod(signer, "enableSigning", null);
        } catch (Exception e) {
            logger.warn("Invoking SigV2 enableSigning() received an error [%s]", e.getMessage());
        }
    }

    public static void disableSigning(ILogger logger) {
        Object signer = getSigner();
        if (signer == null) {
            return;
        }

        try {
            Reflection.invokeInstanceMethod(signer, "disableSigning", null);
        } catch (Exception e) {
            logger.warn("Invoking SigV2 disableSigning() received an error [%s]", e.getMessage());
        }
    }

    public static void onResume(ILogger logger){
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
        if (signer == null) {
            signer = Reflection.createDefaultInstance("com.adjust.sdk.sigv2.Signer");
        }

        return signer;
    }


}
