package com.adjust.sdk.oaid;

import static com.adjust.sdk.oaid.Util.*;

import android.content.Context;
import android.util.Log;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustFactory;
import com.adjust.sdk.ILogger;
import com.adjust.sdk.Logger;
import com.adjust.sdk.scheduler.AsyncTaskExecutor;
import com.bun.miitmdid.core.MdidSdkHelper;

import java.util.Map;

public class AdjustOaid {
    static boolean isOaidToBeRead = false;
    static boolean isMsaSdkAvailable = false;

    public static void readOaid() {
        isOaidToBeRead = true;
    }

    public static void readOaid(Context context) {
        readOaid();

        try {
            System.loadLibrary("msaoaidsec");
            String certificate = readCertFromAssetFile(context);
            isMsaSdkAvailable = MdidSdkHelper.InitCert(context, certificate);
        } catch (Throwable t) {
            isMsaSdkAvailable = false;
            AdjustFactory.getLogger().debug("Adjust", "Error during msa sdk initialization " + t.getMessage());
        }
    }

    public static void getOaidParameters(final Context context,final OnOaidReadListener listener) {
        if (listener == null) {
            AdjustFactory.getLogger().error("onOaidReadListener cannot be null");
            return;
        }
        if (context == null) {
            AdjustFactory.getLogger().error("context cannot be null");
            return;
        }
        readOaid(context);

        if (!isMsaSdkAvailable){
            listener.onFail("MSA SDK not available");
            return;
        }

        new AsyncTaskExecutor<Context, OaidResult>(){

            @Override
            protected OaidResult doInBackground(Context[] contexts) {
                ILogger logger = AdjustFactory.getLogger();
                OaidResult oaidResult;

                if (Util.isManufacturerHuawei(logger)) {
                    oaidResult = getOaidParametersUsingHms(context, logger);
                    if (oaidResult.oaidParameters != null) {
                        return oaidResult;
                    }
                    oaidResult = getOaidParametersUsingMsa(context, logger);
                    return oaidResult;
                } else {
                    oaidResult = getOaidParametersUsingMsa(context, logger);
                    if (oaidResult.oaidParameters != null) {
                        return oaidResult;
                    }
                    return getOaidParametersUsingHms(context, logger);
                }

            }

            @Override
            protected void onPostExecute(OaidResult oaidResult) {
                if (oaidResult != null) {
                    if (oaidResult.oaidParameters != null) {
                        listener.onOaidRead(oaidResult.oaidParameters);
                    } else if (oaidResult.error != null) {
                        listener.onFail(oaidResult.error);
                    }else {
                        listener.onFail("OaidPlugin getOaidParameters: parameters is null");
                    }
                }else {
                    listener.onFail("OaidPlugin getOaidParameters: OaidResults is null");
                }
            }
        }.execute(context);
    }


    public static void doNotReadOaid() {
        isOaidToBeRead = false;
    }
}
