package com.adjust.sdk;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.content.SharedPreferences;

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
    private static final String PREFS_KEY_REFERRERS = "referrers";

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

    /**
     * Save referrer information to shared preferences.
     *
     * @param referrer  Referrer string
     * @param clickTime Referrer click time
     */
    public synchronized void saveReferrerToSharedPreferences(final String referrer, final long clickTime) {
        // Check if referrer is null or empty string already done before calling this method.
        try {
            JSONArray referrerQueue = getReferrersFromSharedPreferences();

            // If referrer is already contained in shared preferences, skip adding it.
            if (getReferrerIndex(referrer, clickTime, referrerQueue) >= 0) {
                return;
            }

            // Add new referrer JSONArray entry to the queue.
            JSONArray newReferrerPair = new JSONArray();

            newReferrerPair.put(0, referrer);
            newReferrerPair.put(1, clickTime);

            referrerQueue.put(newReferrerPair);

            // Save JSON array as string back to shared preferences.
            saveStringToSharedPreferences(PREFS_KEY_REFERRERS, referrerQueue.toString());
        } catch (JSONException e) {

        }
    }

    /**
     * Mark referrer entry in referrer queue as currently being sent from sdk_click handler.
     *
     * @param referrer  Referrer string
     * @param clickTime Referrer click time
     */
    public synchronized void markReferrerForSendingInSharedPreferences(final String referrer, final long clickTime) {
        // Don't even try to alter null or empty referrers since they shouldn't exist in shared preferences.
        if (referrer == null || referrer.length() == 0) {
            return;
        }

        try {
            // Try to locate position in queue of the referrer that should be altered.
            JSONArray referrerQueue = getReferrersFromSharedPreferences();
            int index = getReferrerIndex(referrer, clickTime, referrerQueue);

            // If referrer is not found in the queue, skip the rest.
            if (index == -1) {
                return;
            }

            JSONArray referrerPair = referrerQueue.getJSONArray(index);

            // Rebuild queue and alter the aimed referrer info entry.
            JSONArray newReferrerQueue = new JSONArray();

            for (int i = 0; i < referrerQueue.length(); i += 1) {
                if (i == index) {
                    JSONArray alteredReferrerPair = new JSONArray();

                    alteredReferrerPair.put(0, referrerPair.get(0));
                    alteredReferrerPair.put(1, referrerPair.get(1));
                    alteredReferrerPair.put(2, true);

                    newReferrerQueue.put(alteredReferrerPair);

                    continue;
                }

                newReferrerQueue.put(referrerQueue.getJSONArray(i));
            }

            // Save new referrer queue JSON array as string back to shared preferences.
            saveStringToSharedPreferences(PREFS_KEY_REFERRERS, newReferrerQueue.toString());
        } catch (JSONException e) {

        }
    }

    /**
     * Remove referrer information from shared preferences.
     *
     * @param referrer  Referrer string
     * @param clickTime Referrer click time
     */
    public synchronized void removeReferrerFromSharedPreferences(final String referrer, final long clickTime) {
        // Don't even try to remove null or empty referrers since they shouldn't exist in shared preferences.
        if (referrer == null || referrer.length() == 0) {
            return;
        }

        try {
            // Try to locate position in queue of the referrer that should be deleted.
            JSONArray referrerQueue = getReferrersFromSharedPreferences();
            int index = getReferrerIndex(referrer, clickTime, referrerQueue);

            // If referrer is not found in the queue, skip the rest.
            if (index == -1) {
                return;
            }

            // Rebuild queue without referrer that should be removed.
            JSONArray newReferrerQueue = new JSONArray();

            for (int i = 0; i < referrerQueue.length(); i += 1) {
                if (i == index) {
                    continue;
                }

                newReferrerQueue.put(referrerQueue.getJSONArray(i));
            }

            // Save new referrer queue JSON array as string back to shared preferences.
            saveStringToSharedPreferences(PREFS_KEY_REFERRERS, newReferrerQueue.toString());
        } catch (JSONException e) {

        }
    }

    /**
     * Get all currently saved referrers from shared preferences.
     *
     * @return JSONArray with all the saved referrers from shared preferences. Empty array if none available.
     */
    public synchronized JSONArray getReferrersFromSharedPreferences() {
        try {
            String referrerQueueString = getStringFromSharedPreferences(PREFS_KEY_REFERRERS);

            if (referrerQueueString == null) {
                return new JSONArray();
            } else {
                return new JSONArray(referrerQueueString);
            }
        } catch (JSONException e) {
            return new JSONArray();
        }
    }

    /**
     * Remove all key-value pairs from shared preferences.
     */
    public synchronized void clearSharedPreferences() {
        this.sharedPreferences.edit().clear().apply();
    }

    /**
     * Write a string value to shared preferences.
     *
     * @param key   Key to be written to shared preferences
     * @param value Value to be written to shared preferences
     */
    private synchronized void saveStringToSharedPreferences(final String key, final String value) {
        this.sharedPreferences.edit().putString(key, value).apply();
    }

    /**
     * Get a string value from shared preferences.
     *
     * @param key Key for which string value should be retrieved
     * @return String value for given key saved in shared preferences (null if not found)
     */
    private synchronized String getStringFromSharedPreferences(final String key) {
        try {
            return this.sharedPreferences.getString(key, null);
        } catch (ClassCastException e) {
            return null;
        }
    }

    /**
     * Check if referrer information is contained in referrer queue in shared preferences and get it's index.
     *
     * @param referrer      Referrer string
     * @param clickTime     Referrer click time
     * @param referrerQueue Referrer queue from shared preferences
     * @return Index of referrer information inside of the referrer queue. -1 if not found.
     * @throws JSONException JSON exception
     */
    private int getReferrerIndex(final String referrer,
                                 final long clickTime,
                                 final JSONArray referrerQueue) throws JSONException {
        for (int i = 0; i < referrerQueue.length(); i += 1) {
            JSONArray referrerPair = referrerQueue.getJSONArray(i);

            String savedReferrer = referrerPair.getString(0);
            long savedClickTime = referrerPair.getLong(1);

            if (savedReferrer.equals(referrer) && savedClickTime == clickTime) {
                return i;
            }
        }

        return -1;
    }
}
