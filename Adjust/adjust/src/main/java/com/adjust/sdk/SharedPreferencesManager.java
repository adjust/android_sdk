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
     * @param clickTime Referrer click time
     * @param content   Referrer string
     */
    public synchronized void saveReferrer(final long clickTime, final String content) {
        // Check if referrer is null or empty string already done before calling this method.
        try {
            ArrayList<Referrer> referrers = getReferrers();
            Referrer referrerToSave = new Referrer(clickTime, content);

            // If referrer is already contained in shared preferences, skip adding it.
            if (getReferrerIndex(referrerToSave, referrers) >= 0) {
                return;
            }

            // Recreate new referrers (updated) array.
            JSONArray updatedReferrers = new JSONArray();

            if (referrers != null) {
                for (int i = 0; i < referrers.size(); i++) {
                    updatedReferrers.put(referrers.get(i).asJSONArray());
                }
            }

            // Add new referrer JSONArray entry to the queue.
            JSONArray newReferrerEntry = new JSONArray();

            newReferrerEntry.put(0, referrerToSave.getClickTime());
            newReferrerEntry.put(1, referrerToSave.getContent());
            newReferrerEntry.put(2, referrerToSave.getIsBeingSent());

            updatedReferrers.put(newReferrerEntry);

            // Save JSON array as string back to shared preferences.
            saveString(PREFS_KEY_REFERRERS, updatedReferrers.toString());
        } catch (JSONException e) {
        }
    }

    /**
     * Remove referrer information from shared preferences.
     *
     * @param clickTime Referrer click time
     * @param content   Referrer string
     */
    public synchronized void removeReferrer(final long clickTime, final String content) {
        // Don't even try to remove null or empty referrers since they shouldn't exist in shared preferences.
        if (content == null || content.length() == 0) {
            return;
        }

        Referrer referrerToCheck = new Referrer(clickTime, content);

        // Try to locate position in queue of the referrer that should be deleted.
        ArrayList<Referrer> referrers = getReferrers();
        int index = getReferrerIndex(referrerToCheck, referrers);

        // If referrer is not found in the queue, skip the rest.
        if (index == -1) {
            return;
        }

        // Rebuild queue without referrer that should be removed.
        JSONArray updatedReferrers = new JSONArray();

        for (int i = 0; i < referrers.size(); i += 1) {
            if (i == index) {
                continue;
            }

            updatedReferrers.put(referrers.get(i).asJSONArray());
        }

        // Save new referrer queue JSON array as string back to shared preferences.
        saveString(PREFS_KEY_REFERRERS, updatedReferrers.toString());
    }

    /**
     * Check if give referrer is saved in shared preferences.
     *
     * @param clickTime Referrer click time
     * @param content   Referrer string
     * @return boolean indicating whether given referrer exist in shared preferences or not.
     * In case of exception, return false.
     */
    public synchronized boolean doesReferrerExist(final String content, final long clickTime) {
        ArrayList<Referrer> referrers = getReferrers();
        Referrer referrerToCheck = new Referrer(clickTime, content);

        if (referrers == null) {
            return false;
        }

        for (int i = 0; i < referrers.size(); i += 1) {
            Referrer referrerEntry = referrers.get(i);

            if (referrerEntry.isEqual(referrerToCheck)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Mark given referrer as the one being sent by SdkClickHandler.
     *
     * @param referrer Referrer which is being sent.
     */
    public synchronized void markReferrerForSending(final Referrer referrer) {
        ArrayList<Referrer> referrers = getReferrers();

        if (referrers == null) {
            return;
        }

        // If referrer doesn't exist in shared preferences, do nothing.
        int index = getReferrerIndex(referrer, referrers);

        if (index < 0) {
            return;
        }

        // Mark referrer for sending.
        referrers.get(index).setIsBeingSent(true);

        // Recreate new referrers (updated) array.
        JSONArray updatedReferrers = new JSONArray();

        for (int i = 0; i < referrers.size(); i++) {
            updatedReferrers.put(referrers.get(i).asJSONArray());
        }

        // Save JSON array as string back to shared preferences.
        saveString(PREFS_KEY_REFERRERS, updatedReferrers.toString());
    }

    /**
     * Get all currently saved referrers from shared preferences.
     *
     * @return JSONArray with all the saved referrers from shared preferences. Empty array if none available.
     */
    public synchronized ArrayList<Referrer> getReferrers() {
        ArrayList<Referrer> referrersArray = new ArrayList<Referrer>();

        try {
            JSONArray arrayFromPrefs;
            String referrerQueueString = getString(PREFS_KEY_REFERRERS);

            if (referrerQueueString == null) {
                arrayFromPrefs = new JSONArray();
            } else {
                arrayFromPrefs = new JSONArray(referrerQueueString);
            }

            if (arrayFromPrefs.length() == 0) {
                return null;
            }

            for (int i = 0; i < arrayFromPrefs.length(); i += 1) {
                JSONArray referrerEntry = arrayFromPrefs.getJSONArray(i);

                if (referrerEntry.length() != Referrer.REFERRER_FIELDS_NUMBER) {
                    continue;
                }

                long clickTime = referrerEntry.getLong(0);
                String content = referrerEntry.getString(1);
                boolean isBeingSent = referrerEntry.getBoolean(2);

                Referrer referrer = new Referrer(clickTime, content);
                referrer.setIsBeingSent(isBeingSent);

                referrersArray.add(referrer);
            }
        } catch (JSONException e) {
        }

        return referrersArray;
    }

    /**
     * Initially called upon ActivityHandler initialisation.
     * Used to check if any of the still existing referrers was unsuccessfully being sent before app got killed.
     * If such found - switch it's isBeingSent flag back to "false".
     */
    public synchronized void scanForSavedReferrers() {
        ArrayList<Referrer> referrers = new ArrayList<Referrer>();

        try {
            JSONArray arrayFromPrefs;
            String referrerQueueString = getString(PREFS_KEY_REFERRERS);

            if (referrerQueueString == null) {
                arrayFromPrefs = new JSONArray();
            } else {
                arrayFromPrefs = new JSONArray(referrerQueueString);
            }

            if (arrayFromPrefs.length() == 0) {
                return;
            }

            for (int i = 0; i < arrayFromPrefs.length(); i += 1) {
                JSONArray referrerEntry = arrayFromPrefs.getJSONArray(i);

                if (referrerEntry.length() != Referrer.REFERRER_FIELDS_NUMBER) {
                    continue;
                }

                long clickTime = referrerEntry.getLong(0);
                String content = referrerEntry.getString(1);

                Referrer referrer = new Referrer(clickTime, content);
                referrer.setIsBeingSent(false);

                referrers.add(referrer);
            }

            // Rebuild queue without referrer that should be removed.
            JSONArray updatedReferrers = new JSONArray();

            for (int i = 0; i < referrers.size(); i += 1) {
                updatedReferrers.put(referrers.get(i).asJSONArray());
            }

            // Save new referrer queue JSON array as string back to shared preferences.
            saveString(PREFS_KEY_REFERRERS, updatedReferrers.toString());
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
     * @param referrerToCheck Referrer to be checked
     * @param referrers       Referrer queue from shared preferences
     * @return Index of referrer information inside of the referrer queue. -1 if not found.
     */
    private int getReferrerIndex(final Referrer referrerToCheck, final ArrayList<Referrer> referrers) {
        if (referrerToCheck == null) {
            return -1;
        }

        if (referrers == null) {
            return -1;
        }

        for (int i = 0; i < referrers.size(); i += 1) {
            Referrer referrer = referrers.get(i);

            if (referrer.isEqual(referrerToCheck)) {
                return i;
            }
        }

        return -1;
    }
}
