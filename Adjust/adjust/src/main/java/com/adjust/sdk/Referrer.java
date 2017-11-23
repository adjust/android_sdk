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
     * Raw referrer content.
     */
    private String rawReferrer;

    /**
     * Flag that indicates whether referrer is currently being sent by SdkClickHandler or not.
     */
    private boolean isBeingSent;

    /**
     * Referrer object constructor.
     *
     * @param clickTime   Referrer click time
     * @param rawReferrer Raw referrer content
     */
    public Referrer(final long clickTime, final String rawReferrer) {
        this.rawReferrer = rawReferrer;
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
     * Raw referrer getter.
     *
     * @return Raw referrer
     */
    public String getRawReferrer() {
        return rawReferrer;
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
     * [1] - rawReferrer
     * [2] - isBeingSent
     *
     * @return JSONArray object with referrer inside
     */
    public JSONArray asJSONArray() {
        JSONArray array = new JSONArray();

        array.put(clickTime);
        array.put(rawReferrer);
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
     * Equality criteria is equality of: clickTime and referrer members.
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

        if (!referrer.getRawReferrer().equals(this.rawReferrer)) {
            return false;
        }

        return true;
    }
}