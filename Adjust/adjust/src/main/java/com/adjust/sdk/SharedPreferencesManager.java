package com.adjust.sdk;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;

/**
 * Class used for shared preferences manipulation.
 *
 * @author Ugljesa Erceg (uerceg)
 * @since 7th July 2017
 */

public class SharedPreferencesManager {
    /**
     * Name of Adjust preferences.
     */
    private static final String PREFS_NAME = "adjust_preferences";

    /**
     * Key name for referrers.
     */
    private static final String PREFS_KEY_RAW_REFERRERS = "raw_referrers";

    private static final String PREFS_KEY_INSTALL_REFERRERS = "install_referrers";
    /**
     * Key name for push token.
     */
    private static final String PREFS_KEY_PUSH_TOKEN = "push_token";

    /**
     * Key name for info about whether install has been tracked or not.
     */
    private static final String PREFS_KEY_INSTALL_TRACKED = "install_tracked";

    /**
     * Shared preferences of the app.
     */
    private final SharedPreferences sharedPreferences;

    /**
     * Default constructor.
     *
     * @param context Application context
     */
    public SharedPreferencesManager(final Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public synchronized void saveRawReferrer(String rawReferrer, long clickTime) {
        try {
            JSONArray rawReferrerArray = getRawReferrerArray();

            if (getRawReferrer(rawReferrer, clickTime) != null) {
                return;
            }

            JSONArray newInstallReferrer = new JSONArray();

            newInstallReferrer.put(0, rawReferrer);
            newInstallReferrer.put(1, clickTime);
            newInstallReferrer.put(2, 0);

            rawReferrerArray.put(newInstallReferrer);

            saveRawReferrerArray(rawReferrerArray);

        } catch (JSONException e) {
        }
    }

    public synchronized void saveInstallReferrer(String installReferrer, long clickTime, long installBeginTime) {
        try {
            JSONArray installReferrerArray = getInstallReferrerArray();

            if (getInstallReferrer(installReferrer, clickTime, installBeginTime) != null) {
                return;
            }

            JSONArray newInstallReferrer = new JSONArray();

            newInstallReferrer.put(0, installReferrer);
            newInstallReferrer.put(1, clickTime);
            newInstallReferrer.put(2, installBeginTime);
            newInstallReferrer.put(3, 0);

            installReferrerArray.put(newInstallReferrer);

            saveInstallReferrerArray(installReferrerArray);

        } catch (JSONException e) {
        }
    }

    public synchronized void saveRawReferrerArray(JSONArray rawReferrerArray) {
        saveString(PREFS_KEY_RAW_REFERRERS, rawReferrerArray.toString());
    }

    public synchronized void saveInstallReferrerArray(JSONArray installReferrerArray) {
        saveString(PREFS_KEY_INSTALL_REFERRERS, installReferrerArray.toString());
    }

    /**
     * Remove referrer information from shared preferences.
     *
     * @param clickTime   Referrer click time
     * @param rawReferrer Referrer string
     */
    public synchronized void removeRawReferrer(String rawReferrer, long clickTime) {
        // Don't even try to remove null or empty referrers since they shouldn't exist in shared preferences.
        if (rawReferrer == null || rawReferrer.length() == 0) {
            return;
        }

        int rawReferrerIndex = getRawReferrerIndex(rawReferrer, clickTime);

        if (rawReferrerIndex < 0) {
            return;
        }

        JSONArray rawReferrerArray = getRawReferrerArray();

        // Rebuild queue without referrer that should be removed.
        JSONArray updatedReferrers = new JSONArray();

        for (int i = 0; i < rawReferrerArray.length(); i += 1) {
            if (i == rawReferrerIndex) {
                continue;
            }

            try {
                updatedReferrers.put(rawReferrerArray.getJSONArray(i));
            } catch (JSONException e) {
            }
        }

        // Save new referrer queue JSON array as string back to shared preferences.
        saveString(PREFS_KEY_RAW_REFERRERS, updatedReferrers.toString());
    }

    public synchronized int getRawReferrerIndex(String rawReferrer,
                                                long clickTime) {
        try {
            JSONArray rawReferrers = getRawReferrerArray();

            for (int i = 0; i < rawReferrers.length(); i++) {
                JSONArray savedRawReferrer = rawReferrers.getJSONArray(i);
                // check if raw referrer is already saved
                String savedRawReferrerString = savedRawReferrer.optString(0, null);
                if (savedRawReferrerString == null || !savedRawReferrerString.equals(rawReferrer)) {
                    continue;
                }
                long savedClickTime = savedRawReferrer.optLong(1, -1);
                if (savedClickTime != clickTime) {
                    continue;
                }
                // install referrer found, skip adding it
                return i;
            }
        } catch (JSONException e) {
        }

        return -1;
    }

    public synchronized JSONArray getRawReferrer(String rawReferrer,
                                                 long clickTime) {
        int rawReferrerIndex = getRawReferrerIndex(rawReferrer, clickTime);
        if (rawReferrerIndex >= 0) {
            try {
                getRawReferrerArray().getJSONArray(rawReferrerIndex);
            } catch (JSONException e) {
            }
        }
        return null;
    }

    public synchronized JSONArray getInstallReferrer(String installReferrer,
                                                     long clickTime,
                                                     long installBeginTime) {
        try {
            JSONArray installReferrers = getInstallReferrerArray();

            for (int i = 0; i < installReferrers.length(); i++) {
                JSONArray savedInstallReferrer = installReferrers.getJSONArray(i);
                // check if install referrer is already saved
                String savedInstallReferrerString = savedInstallReferrer.optString(0, null);
                if (savedInstallReferrerString == null || !savedInstallReferrerString.equals(installReferrer)) {
                    continue;
                }
                long savedClickTime = savedInstallReferrer.optLong(1, -1);
                if (savedClickTime != clickTime) {
                    continue;
                }
                long savedInstalBeginTime = savedInstallReferrer.optLong(2, -1);
                if (savedInstalBeginTime != installBeginTime) {
                    continue;
                }
                // install referrer found, skip adding it
                return savedInstallReferrer;
            }
        } catch (JSONException e) {
        }

        return null;
    }

    public synchronized JSONArray getRawReferrerArray() {
        try {
            String referrerQueueString = getString(PREFS_KEY_RAW_REFERRERS);

            if (referrerQueueString != null) {
                return new JSONArray(referrerQueueString);
            }
        } catch (JSONException e) {
        }

        return new JSONArray();
    }

    public synchronized JSONArray getInstallReferrerArray() {
        try {
            String referrerQueueString = getString(PREFS_KEY_INSTALL_REFERRERS);

            if (referrerQueueString != null) {
                return new JSONArray(referrerQueueString);
            }
        } catch (JSONException e) {
        }

        return new JSONArray();
    }

    /**
     * Initially called upon ActivityHandler initialisation.
     * Used to check if any of the still existing referrers was unsuccessfully being sent before app got killed.
     * If such found - switch it's isBeingSent flag back to "false".
     */
    public synchronized void setSendingReferrersAsUnsend() {
        try {
            JSONArray rawReferrerArray = getRawReferrerArray();
            boolean hasRawReferrersBeenChanged = false;
            for (int i = 0; i < rawReferrerArray.length(); i++) {
                    JSONArray rawReferrer = rawReferrerArray.getJSONArray(i);
                    int sendingStatus = rawReferrer.optInt(2, -1);
                    if (sendingStatus == 1) {
                        rawReferrer.put(2, 0);
                        hasRawReferrersBeenChanged = true;
                    }
            }
            if (hasRawReferrersBeenChanged) {
                saveRawReferrerArray(rawReferrerArray);
            }
        } catch (JSONException e) {
        }

        try {
            JSONArray installReferrerArray = getInstallReferrerArray();
            boolean hasInstallReferrersBeenChanged = false;
            for (int i = 0; i < installReferrerArray.length(); i++) {
                JSONArray installReferrer = installReferrerArray.getJSONArray(i);
                int sendingStatus = installReferrer.optInt(3, -1);
                if (sendingStatus == 1) {
                    installReferrer.put(3, 0);
                    hasInstallReferrersBeenChanged = true;
                }
            }
            if (hasInstallReferrersBeenChanged) {
                saveInstallReferrerArray(installReferrerArray);
            }
        } catch (JSONException e) {
        }
    }

    /**
     * Save push token to shared preferences.
     *
     * @param pushToken Push notifications token
     */
    public synchronized void savePushToken(final String pushToken) {
        saveString(PREFS_KEY_PUSH_TOKEN, pushToken);
    }

    /**
     * Get push token from shared preferences.
     *
     * @return Push token value
     */
    public synchronized String getPushToken() {
        return getString(PREFS_KEY_PUSH_TOKEN);
    }

    /**
     * Save information that install has been tracked to shared preferences.
     */
    public synchronized void setInstallTracked() {
        saveBoolean(PREFS_KEY_INSTALL_TRACKED, true);
    }

    /**
     * Get information if install has been tracked from shared preferences. If no info, default to false.
     *
     * @return boolean indicating whether install has been tracked or not
     */
    public synchronized boolean getInstallTracked() {
        return getBoolean(PREFS_KEY_INSTALL_TRACKED, false);
    }

    /**
     * Remove push token from shared preferences.
     */
    public synchronized void removePushToken() {
        remove(PREFS_KEY_PUSH_TOKEN);
    }

    /**
     * Remove all key-value pairs from shared preferences.
     */
    public synchronized void clear() {
        this.sharedPreferences.edit().clear().apply();
    }

    /**
     * Write a string value to shared preferences.
     *
     * @param key   Key to be written to shared preferences
     * @param value Value to be written to shared preferences
     */
    private synchronized void saveString(final String key, final String value) {
        this.sharedPreferences.edit().putString(key, value).apply();
    }

    /**
     * Write a boolean value to shared preferences.
     *
     * @param key   Key to be written to shared preferences
     * @param value Value to be written to shared preferences
     */
    private synchronized void saveBoolean(final String key, final boolean value) {
        this.sharedPreferences.edit().putBoolean(key, value).apply();
    }

    /**
     * Get a string value from shared preferences.
     *
     * @param key Key for which string value should be retrieved
     * @return String value for given key saved in shared preferences (null if not found)
     */
    private synchronized String getString(final String key) {
        try {
            return this.sharedPreferences.getString(key, null);
        } catch (ClassCastException e) {
            return null;
        }
    }

    /**
     * Get a boolean value from shared preferences.
     *
     * @param key          Key for which boolean value should be retrieved
     * @param defaultValue Default value to be returned if nothing found in shared preferences
     * @return boolean value for given key saved in shared preferences
     */
    private synchronized boolean getBoolean(final String key, final boolean defaultValue) {
        try {
            return this.sharedPreferences.getBoolean(key, defaultValue);
        } catch (ClassCastException e) {
            return defaultValue;
        }
    }

    /**
     * Remove a value saved with given key from shared preferences.
     *
     * @param key Key to be removed
     */
    private synchronized void remove(final String key) {
        this.sharedPreferences.edit().remove(key).apply();
    }
}
