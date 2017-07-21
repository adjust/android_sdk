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
     * Maximal size of referrer entry JSON array in shared preferences.
     */
    private static final int PREFS_REFERRER_ENTRY_MAX_SIZE = 3;

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
    public synchronized void saveReferrer(final String referrer, final long clickTime) {
        // Check if referrer is null or empty string already done before calling this method.
        try {
            JSONArray referrerQueue = getReferrers();

            // If referrer is already contained in shared preferences, skip adding it.
            if (getReferrerIndex(referrer, clickTime, referrerQueue) >= 0) {
                return;
            }

            // Add new referrer JSONArray entry to the queue.
            JSONArray newreferrerEntry = new JSONArray();

            newreferrerEntry.put(0, referrer);
            newreferrerEntry.put(1, clickTime);

            referrerQueue.put(newreferrerEntry);

            // Save JSON array as string back to shared preferences.
            saveString(PREFS_KEY_REFERRERS, referrerQueue.toString());
        } catch (JSONException e) {

        }
    }

    /**
     * Mark referrer entry in referrer queue as currently being sent from sdk_click handler.
     *
     * @param referrer  Referrer string
     * @param clickTime Referrer click time
     */
    public synchronized void markReferrerForSending(final String referrer, final long clickTime) {
        // Don't even try to alter null or empty referrers since they shouldn't exist in shared preferences.
        if (referrer == null || referrer.length() == 0) {
            return;
        }

        try {
            // Try to locate position in queue of the referrer that should be altered.
            JSONArray referrerQueue = getReferrers();
            int index = getReferrerIndex(referrer, clickTime, referrerQueue);

            // If referrer is not found in the queue, skip the rest.
            if (index == -1) {
                return;
            }

            JSONArray referrerEntry = referrerQueue.getJSONArray(index);

            // Rebuild queue and alter the aimed referrer info entry.
            JSONArray newReferrerQueue = new JSONArray();

            for (int i = 0; i < referrerQueue.length(); i += 1) {
                if (i == index) {
                    JSONArray alteredreferrerEntry = new JSONArray();

                    alteredreferrerEntry.put(0, referrerEntry.get(0));
                    alteredreferrerEntry.put(1, referrerEntry.get(1));
                    alteredreferrerEntry.put(2, true);

                    newReferrerQueue.put(alteredreferrerEntry);

                    continue;
                }

                newReferrerQueue.put(referrerQueue.getJSONArray(i));
            }

            // Save new referrer queue JSON array as string back to shared preferences.
            saveString(PREFS_KEY_REFERRERS, newReferrerQueue.toString());
        } catch (JSONException e) {

        }
    }

    /**
     * Check if referrer entry in shared preferences is already marked for sending.
     *
     * @param referrer  Referrer string
     * @param clickTime Referrer click time
     * @return Boolean indicating whether referrer is marked for sending or not. If no entry, default to false.
     */
    public synchronized boolean isReferrerMarkedForSending(final String referrer, final long clickTime) {
        // Don't even try to alter null or empty referrers since they shouldn't exist in shared preferences.
        if (referrer == null || referrer.length() == 0) {
            return false;
        }

        try {
            // Try to locate position in queue of the referrer that should be altered.
            JSONArray referrerQueue = getReferrers();
            int index = getReferrerIndex(referrer, clickTime, referrerQueue);

            // If referrer is not found in the queue, skip the rest.
            if (index == -1) {
                return false;
            }

            JSONArray referrerEntry = referrerQueue.getJSONArray(index);

            return isReferrerMarkedForSending(referrerEntry);
        } catch (JSONException e) {
            return false;
        }
    }

    /**
     * Check if referrer entry in shared preferences is already marked for sending.
     * @param referrerEntry Referrer entry
     * @return Boolean indicating whether referrer entry is marked for sending or not
     */
    public synchronized boolean isReferrerMarkedForSending(final JSONArray referrerEntry) {
        try {
            if (referrerEntry.length() != PREFS_REFERRER_ENTRY_MAX_SIZE) {
                return false;
            }

            return referrerEntry.getBoolean(2);
        } catch (JSONException e) {
            return false;
        }
    }

    /**
     * Remove referrer information from shared preferences.
     *
     * @param referrer  Referrer string
     * @param clickTime Referrer click time
     */
    public synchronized void removeReferrer(final String referrer, final long clickTime) {
        // Don't even try to remove null or empty referrers since they shouldn't exist in shared preferences.
        if (referrer == null || referrer.length() == 0) {
            return;
        }

        try {
            // Try to locate position in queue of the referrer that should be deleted.
            JSONArray referrerQueue = getReferrers();
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
            saveString(PREFS_KEY_REFERRERS, newReferrerQueue.toString());
        } catch (JSONException e) {

        }
    }

    /**
     * Get all currently saved referrers from shared preferences.
     *
     * @return JSONArray with all the saved referrers from shared preferences. Empty array if none available.
     */
    public synchronized JSONArray getReferrers() {
        try {
            String referrerQueueString = getString(PREFS_KEY_REFERRERS);

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
     * Helper method to print all the referrers from shared preferences.
     */
    public synchronized void printAllTheReferrers() {
        try {
            JSONArray referrerQueue = getReferrers();

            AdjustFactory.getLogger().debug("List of referrers in shared preferences: ");

            for (int i = 0; i < referrerQueue.length(); i += 1) {
                JSONArray referrerEntry = referrerQueue.getJSONArray(i);

                String isBeingSent;
                String referrer = referrerEntry.getString(0);
                String clickTime = String.valueOf(referrerEntry.getLong(1));

                if (referrerEntry.length() == PREFS_REFERRER_ENTRY_MAX_SIZE) {
                    isBeingSent = String.valueOf(referrerEntry.getBoolean(2));
                } else {
                    isBeingSent = "NA";
                }

                AdjustFactory.getLogger().debug("Referrer: " + referrer);
                AdjustFactory.getLogger().debug("Click time: " + clickTime);
                AdjustFactory.getLogger().debug("Is being sent: " + isBeingSent);
            }
        } catch (JSONException e) {

        }
    }

    /**
     * Remove all key-value pairs from shared preferences.
     */
    public synchronized void clear() {
        this.sharedPreferences.edit().clear().commit();
    }

    /**
     * Write a string value to shared preferences.
     *
     * @param key   Key to be written to shared preferences
     * @param value Value to be written to shared preferences
     */
    private synchronized void saveString(final String key, final String value) {
        this.sharedPreferences.edit().putString(key, value).commit();
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
            JSONArray referrerEntry = referrerQueue.getJSONArray(i);

            String savedReferrer = referrerEntry.getString(0);
            long savedClickTime = referrerEntry.getLong(1);

            if (savedReferrer.equals(referrer) && savedClickTime == clickTime) {
                return i;
            }
        }

        return -1;
    }
}
