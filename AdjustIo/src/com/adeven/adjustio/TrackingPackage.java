//
//  TrackingPackage.java
//  AdjustIo
//
//  Created by Benjamin Weiss on 17.4.13
//  Copyright (c) 2012 adeven. All rights reserved.
//  See the file MIT-LICENSE for copying permission.
//

package com.adeven.adjustio;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

/**
 * Holds information of one tracking package.
 *
 * @author keyboardsurfer
 * @since 17.4.13
 */
public class TrackingPackage {
    final String path;
    final String successMessage;
    final String failureMessage;
    final String userAgent;
    final List<NameValuePair> parameters;

    public TrackingPackage(String path, String successMessage, String failureMessage, String userAgent, List<NameValuePair> parameters) {
        this.path = path;
        this.successMessage = successMessage;
        this.failureMessage = failureMessage;
        this.userAgent = userAgent;
        this.parameters = parameters;
    }

    /**
     * A builder to enable chained building of a RequestThread.
     */
    static class Builder {
        private String path;
        private String successMessage;
        private String failureMessage;
        private String userAgent;
        private List<NameValuePair> parameters;

        Builder() {
            parameters = new ArrayList<NameValuePair>();
        }

        Builder setPath(String path) {
            this.path = path;
            return this;
        }

        Builder setUserAgent(String userAgent) {
            this.userAgent = userAgent;
            Logger.verbose(path, "userAgent", userAgent);
            return this;
        }

        Builder setSuccessMessage(String successMessage) {
            this.successMessage = successMessage;
            Logger.verbose(path, "successMessage", successMessage);
            return this;
        }

        Builder setFailureMessage(String failureMessage) {
            this.failureMessage = failureMessage;
            Logger.verbose(path, "failureMessage", failureMessage);
            return this;
        }

        Builder addTrackingParameter(String key, String value) {
            if (value == null || value == "" ) {
                return this;
            }

            parameters.add(new BasicNameValuePair(key, value));
            Logger.verbose(path, key, value);
            return this;
        }

        TrackingPackage build() {
            TrackingPackage trackingPackage = new TrackingPackage(path, successMessage, failureMessage, userAgent, parameters);
            return trackingPackage;
        }
    }
}
