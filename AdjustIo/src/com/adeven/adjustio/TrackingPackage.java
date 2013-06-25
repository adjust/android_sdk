//
//  TrackingPackage.java
//  AdjustIo
//
//  Created by Benjamin Weiss on 17.4.13
//  Copyright (c) 2012 adeven. All rights reserved.
//  See the file MIT-LICENSE for copying permission.
//

package com.adeven.adjustio;

import java.io.Serializable;

// TODO: remove this comment? clean up comments!
/**
 * Holds information of one tracking package.
 *
 * @author keyboardsurfer
 * @since 17.4.13
 */
public class TrackingPackage implements Serializable {
    private static final long serialVersionUID = -5435782033488813179L;

    final String path;
    final String successMessage;
    final String failureMessage;
    final String userAgent;
    final String parameters;

    public TrackingPackage(String path, String successMessage, String failureMessage, String userAgent, String parameters) {
        this.path = path;
        this.successMessage = successMessage;
        this.failureMessage = failureMessage;
        this.userAgent = userAgent;
        this.parameters = parameters;
    }
}
