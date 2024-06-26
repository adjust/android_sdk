package com.adjust.test;

import static com.adjust.test.Constants.LOGTAG;

import android.util.Log;

import java.util.Arrays;
import java.util.Locale;

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
}
