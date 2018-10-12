package com.adjust.test;

import android.util.Log;

import java.util.Arrays;
import java.util.Locale;

import static com.adjust.test.Constants.LOGTAG;

/**
 * Created by nonelse on 11.03.17.
 */

public class Utils {

    public static void debug(String message, Object... parameters) {
        try {
            Log.d(LOGTAG, String.format(Locale.US, message, parameters));
        } catch (Exception e) {
            Log.e(LOGTAG, String.format(Locale.US, "Error formating log message: %s, with params: %s"
                    , message, Arrays.toString(parameters)));
        }
    }

    public static void error(String message, Object... parameters) {
        try {
            Log.e(LOGTAG, String.format(Locale.US, message, parameters));
        } catch (Exception e) {
            Log.e(LOGTAG, String.format(Locale.US, "Error formating log message: %s, with params: %s"
                    , message, Arrays.toString(parameters)));
        }
    }

    public static String appendBasePath(String basePath, String path) {
        if (basePath == null) {
            return path;
        }
        return basePath + path;
    }
}
