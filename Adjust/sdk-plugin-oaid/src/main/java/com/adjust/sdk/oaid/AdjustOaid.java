package com.adjust.sdk.oaid;

import static com.adjust.sdk.oaid.Util.readCertFromAssetFile;

import android.content.Context;

import com.adjust.sdk.AdjustFactory;
import com.adjust.sdk.ILogger;
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
        ILogger logger = AdjustFactory.getLogger();
        readOaid();

        try {
            System.loadLibrary("msaoaidsec");
            String certificate = readCertFromAssetFile(context, logger);
            isMsaSdkAvailable = MdidSdkHelper.InitCert(context, certificate);
        } catch (Throwable t) {
            isMsaSdkAvailable = false;
            logger.debug("Adjust", "Error during msa sdk initialization " + t.getMessage());
        }
    }

    public static void getOaidParameters(final Context context,final OnOaidReadListener listener) {
        ILogger logger = AdjustFactory.getLogger();
        if (listener == null) {
            logger.error("onOaidReadListener cannot be null");
            return;
        }
        if (context == null) {
            logger.error("context cannot be null");
            return;
        }

        new AsyncTaskExecutor<Context, OaidResult>(){
            @Override
            protected OaidResult doInBackground(Context[] contexts) {
                readOaid(context);
                OaidResult oaidResult = new OaidResult();
                Map<String, String> oaidParameters = Util.getOaidParameters(context, logger);
                if (oaidParameters != null && !oaidParameters.isEmpty() && oaidParameters.get("oaid") != null) {
                    oaidResult.oaid = oaidParameters.get("oaid");
                }else {
                    oaidResult.error = "OaidPlugin getOaidParameters: parameters is null";
                }
                return oaidResult;
            }

            @Override
            protected void onPostExecute(OaidResult oaidResult) {
                if (oaidResult != null) {
                    if (oaidResult.oaid != null) {
                        listener.onOaidRead(oaidResult.oaid);
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
