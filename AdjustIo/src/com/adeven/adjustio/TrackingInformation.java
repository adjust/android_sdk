//  Created by Benjamin Weiss on 17.4.13
//  Copyright (c) 2012 adeven. All rights reserved.
//  See the file MIT-LICENSE for copying permission.
package com.adeven.adjustio;

/**
 * Holds tracking information. 
 *
 * @author keyboardsurfer
 * @since 17.4.13
 */
class TrackingInformation {
  final String path;
  final String successMessage;
  final String failureMessage;
  final String userAgent;
  final String[] trackingParameters;

  TrackingInformation(String path, String successMessage,
                      String failureMessage, String userAgent, String... trackingParameters) {
    this.path = path;
    this.successMessage = successMessage;
    this.failureMessage = failureMessage;
    this.userAgent = userAgent;
    this.trackingParameters = trackingParameters;
  }

  /**
   * A builder to enable chained building of a {@link RequestThread}.
   */
  static class Builder {
    private String path;
    private String successMessage;
    private String failureMessage;
    private String userAgent;
    private String[] trackingParameters;

    Builder setPath(String path) {
      this.path = path;
      return this;
    }

    Builder setUserAgent(String userAgent) {
      this.userAgent = userAgent;
      return this;
    }

    Builder setSuccessMessage(String successMessage) {
      this.successMessage = successMessage;
      return this;
    }

    Builder setFailureMessage(String failureMessage) {
      this.failureMessage = failureMessage;
      return this;
    }


    Builder setTrackingParameters(String... trackingParameters) {
      this.trackingParameters = trackingParameters;
      return this;
    }

    TrackingInformation build() {
      return new TrackingInformation(path, userAgent, successMessage, failureMessage, trackingParameters);
    }
  }
}
