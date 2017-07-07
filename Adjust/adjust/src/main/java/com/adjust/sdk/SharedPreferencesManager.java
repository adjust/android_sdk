package com.adjust.sdk;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Class used for shared preferences manipulation.
 */
public class SharedPreferencesManager {
    private SharedPreferences sharedPreferences;

    /**
     * Default constructor
     * @param context Application context
     * @param name Shared preferences name
     */
    public SharedPreferencesManager(Context context, String name) {
        this.sharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    /**
     * Write a string value to shared preferences
     * @param key Key to be written to shared preferences
     * @param value Value to be written to shared preferences
     */
    public synchronized void saveStringToSharedPreferences(String key, String value) {
        this.sharedPreferences.edit().putString(key, value).apply();
    }

    /**
     * Write a long value to shared preferences
     * @param key Key to be written to shared preferences
     * @param value Value to be written to shared preferences
     */
    public synchronized void saveLongToSharedPreferences(String key, long value) {
        this.sharedPreferences.edit().putLong(key, value).apply();
    }

    /**
     * Write a boolean value to shared preferences
     * @param key Key to be written to shared preferences
     * @param value Value to be written to shared preferences
     */
    public synchronized void saveBooleanToSharedPreferences(String key, boolean value) {
        this.sharedPreferences.edit().putBoolean(key, value).apply();
    }

    /**
     * Get a string value from shared preferences
     * @param key Key for which string value should be retrieved
     * @return String value for given key saved in shared preferences (null if not found)
     */
    public synchronized String getStringFromSharedPreferences(String key) {
        String defaultValue = null;

        try {
            return this.sharedPreferences.getString(key, defaultValue);
        } catch (ClassCastException e) {
            return defaultValue;
        }
    }

    /**
     * Get a long value from shared preferences
     * @param key Key for which long value should be retrieved
     * @return Long value for given key saved in shared preferences (-1 if not found)
     */
    public synchronized long getLongFromSharedPreferences(String key) {
        long defaultValue = -1;

        try {
            return this.sharedPreferences.getLong(key, defaultValue);
        } catch (ClassCastException e) {
            return defaultValue;
        }
    }

    /**
     * Get a long value from shared preferences
     * @param key Key for which boolean value should be retrieved
     * @return Boolean value for given key saved in shared preferences (false if not found)
     */
    public synchronized boolean getBooleanFromSharedPreferences(String key) {
        boolean defaultValue = false;

        try {
            return this.sharedPreferences.getBoolean(key, defaultValue);
        } catch (ClassCastException e) {
            return defaultValue;
        }
    }

    /**
     * Check if a given key is contained inside of shared preferences
     * @param key Key which is being checked
     * @return Boolean indicating whether key is present in shared preferences or not
     */
    public synchronized boolean isContainedInSharedPreferences(String key) {
        return this.sharedPreferences.contains(key);
    }

    /**
     * Remove given key from shared preferences
     * @param key Key which might be removed
     */
    public synchronized void removeFromSharedPreferences(String key) {
        this.sharedPreferences.edit().remove(key).apply();
    }

    /**
     * Remove all key-value pairs from shared preferences
     */
    public synchronized void clearSharedPreferences() {
        this.sharedPreferences.edit().clear().apply();
    }
}
