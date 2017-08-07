package com.adjust.sdk;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by uerceg on 07.08.17.
 */

public class Referrer {
    /**
     * Number of referrer fields.
     */
    public static final int REFERRER_FIELDS_NUMBER = 3;

    /**
     * Referrer click time stamp.
     */
    private long clickTime;

    /**
     * Referrer content.
     */
    private String content;

    /**
     * Flag that indicates whether referrer is currently being sent by SdkClickHandler or not.
     */
    private boolean isBeingSent;

    /**
     * Referrer object constructor.
     *
     * @param clickTime Referrer click time
     * @param content   Referrer content
     */
    public Referrer(final long clickTime, final String content) {
        this.content = content;
        this.clickTime = clickTime;

        this.isBeingSent = false;
    }

    /**
     * Referrer click time getter.
     *
     * @return Referrer click time
     */
    public long getClickTime() {
        return clickTime;
    }

    /**
     * Referrer content getter.
     *
     * @return Referrer content
     */
    public String getContent() {
        return content;
    }

    /**
     * Is referrer being sent flag getter.
     *
     * @return Is referrer being sent flag's value
     */
    public boolean getIsBeingSent() {
        return isBeingSent;
    }

    /**
     * Set info whether referrer is being sent or not.
     *
     * @param send boolean indicating whether referrer is being sent or not
     */
    public void setIsBeingSent(final boolean send) {
        isBeingSent = send;
    }

    /**
     * Convert Referrer object to JSON array.
     * [0] - clickTime
     * [1] - content
     * [2] - isBeingSent
     *
     * @return JSONArray object with referrer inside
     */
    public JSONArray asJSONArray() {
        JSONArray array = new JSONArray();

        array.put(clickTime);
        array.put(content);
        array.put(isBeingSent);

        return array;
    }

    /**
     * Convert Referrer object to JSONArray string.
     *
     * @return JSONArray as string of the current Referrer object
     */
    public String asString() {
        return asJSONArray().toString();
    }

    /**
     * Check if current referrer is equal with given one.
     * Equality criteria is equality of: clickTime and content members.
     *
     * @param referrer Referrer to check equality with
     * @return boolean indicating whether two referrers are equal
     */
    public boolean isEqual(final Referrer referrer) {
        if (referrer == null) {
            return false;
        }

        if (referrer.getClickTime() != clickTime) {
            return false;
        }

        if (!referrer.getContent().equals(content)) {
            return false;
        }

        return true;
    }

    /**
     * Get Referrer object from given JSONArray object.
     *
     * @param array JSONArray to convert to Referrer object
     * @return Referrer object made from given JSONArray object
     */
    public static Referrer getReferrerFromJSONArray(final JSONArray array) {
        if (array != null && array.length() != REFERRER_FIELDS_NUMBER) {
            return null;
        }

        try {
            long clickTime = array.getLong(0);
            String content = array.getString(1);
            boolean isBeingSent = array.getBoolean(2);

            Referrer referrer = new Referrer(clickTime, content);
            referrer.setIsBeingSent(isBeingSent);

            return referrer;
        } catch (JSONException e) {
            return null;
        }
    }

    /**
     * Get Referrer object from given JSONArray object string.
     *
     * @param arrayString JSONArray string to convert to Referrer object
     * @return Referrer object made from given JSONArray object string
     */
    public static Referrer getReferrerFromJSONArrayString(final String arrayString) {
        JSONArray array;

        try {
            if (arrayString == null) {
                return null;
            } else {
                array = new JSONArray(arrayString);
            }

            if (array != null && array.length() != REFERRER_FIELDS_NUMBER) {
                return null;
            }

            long clickTime = array.getLong(0);
            String content = array.getString(1);
            boolean isBeingSent = array.getBoolean(2);

            Referrer referrer = new Referrer(clickTime, content);
            referrer.setIsBeingSent(isBeingSent);

            return referrer;
        } catch (JSONException e) {
            return null;
        }
    }
}
