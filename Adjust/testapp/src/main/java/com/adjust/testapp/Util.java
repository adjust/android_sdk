package com.adjust.testapp;

/**
 * Created by nonelse on 22.02.2018
 */
public class Util {
    static Boolean strictParseStringToBoolean(String value) {
        if (value == null) {
            return null;
        }
        if (value.equalsIgnoreCase("true")) {
            return true;
        }
        if (value.equalsIgnoreCase("false")) {
            return false;
        }

        return null;
    }
}
