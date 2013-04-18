//
//  Logger.java
//  AdjustIo
//
//  Created by Benjamin Weiss on 17.4.13
//  Copyright (c) 2012 adeven. All rights reserved.
//  See the file MIT-LICENSE for copying permission.
//

package com.adeven.adjustio;

import android.util.Log;

// TODO: add log levels
/**
 * A Wrapper that allows easy toggles of Logging.
 *
 * @author keyboardsurfer
 * @since 17.4.13
 */
public class Logger {

    private static boolean isLoggingEnabled = true;

    public static void setLoggingEnabled(boolean isLoggingEnabled) {
        Logger.isLoggingEnabled = isLoggingEnabled;
    }


    public static void d(String logTag, String message) {
        if (isLoggingEnabled) {
            Log.d(logTag, message);
        }
    }

    public static void d(String logTag, String message, Throwable throwable) {
        if (isLoggingEnabled) {
            Log.d(logTag, message, throwable);
        }
    }

    public static void w(String logTag, String message) {
        if (isLoggingEnabled) {
            Log.w(logTag, message);
        }
    }

    public static void e(String logTag, String message) {
        if (isLoggingEnabled) {
            Log.e(logTag, message);
        }
    }
}
