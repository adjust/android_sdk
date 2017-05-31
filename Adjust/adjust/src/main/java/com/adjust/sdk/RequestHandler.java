//
//  RequestHandler.java
//  Adjust
//
//  Created by Christian Wellenbrock on 2013-06-25.
//  Copyright (c) 2013 adjust GmbH. All rights reserved.
//  See the file MIT-LICENSE for copying permission.
//

package com.adjust.sdk;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;

import javax.net.ssl.HttpsURLConnection;

public class RequestHandler implements IRequestHandler {
    private CustomScheduledExecutor scheduledExecutor;
    private WeakReference<IPackageHandler> packageHandlerWeakRef;
    private ILogger logger;
    private String basePath;

    public RequestHandler(IPackageHandler packageHandler) {
        this.logger = AdjustFactory.getLogger();
        this.scheduledExecutor = new CustomScheduledExecutor("RequestHandler", false);
        init(packageHandler);
        this.basePath = packageHandler.getBasePath();
    }

    @Override
    public void init(IPackageHandler packageHandler) {
        this.packageHandlerWeakRef = new WeakReference<IPackageHandler>(packageHandler);
    }

    @Override
    public void sendPackage(final ActivityPackage activityPackage, final int queueSize) {
        scheduledExecutor.submit(new Runnable() {
            @Override
            public void run() {
                sendI(activityPackage, queueSize);
            }
        });
    }

    @Override
    public void teardown() {
        logger.verbose("RequestHandler teardown");
        if (scheduledExecutor != null) {
            try {
                scheduledExecutor.shutdownNow();
            } catch(SecurityException se) {}
        }
        if (packageHandlerWeakRef != null) {
            packageHandlerWeakRef.clear();
        }
        scheduledExecutor = null;
        packageHandlerWeakRef = null;
        logger = null;
    }

    private void sendI(ActivityPackage activityPackage, int queueSize) {
        String url = AdjustFactory.getBaseUrl();
        if (basePath != null) {
            url += basePath;
        }
        String targetURL = url + activityPackage.getPath();

        try {
            ResponseData responseData = UtilNetworking.createPOSTHttpsURLConnection(targetURL, activityPackage, queueSize);

            IPackageHandler packageHandler = packageHandlerWeakRef.get();
            if (packageHandler == null) {
                return;
            }

            if (responseData.jsonResponse == null) {
                packageHandler.closeFirstPackage(responseData, activityPackage);
                return;
            }

            packageHandler.sendNextPackage(responseData);
        } catch (UnsupportedEncodingException e) {
            sendNextPackageI(activityPackage, "Failed to encode parameters", e);
        } catch (SocketTimeoutException e) {
            closePackageI(activityPackage, "Request timed out", e);
        } catch (IOException e) {
            closePackageI(activityPackage, "Request failed", e);
        } catch (Throwable e) {
            sendNextPackageI(activityPackage, "Runtime exception", e);
        }
    }

    // close current package because it failed
    private void closePackageI(ActivityPackage activityPackage, String message, Throwable throwable) {
        final String packageMessage = activityPackage.getFailureMessage();
        final String reasonString = Util.getReasonString(message, throwable);
        String finalMessage = String.format("%s. (%s) Will retry later", packageMessage, reasonString);
        logger.error(finalMessage);

        ResponseData responseData = ResponseData.buildResponseData(activityPackage);
        responseData.message = finalMessage;

        IPackageHandler packageHandler = packageHandlerWeakRef.get();
        if (packageHandler == null) {
            return;
        }

        packageHandler.closeFirstPackage(responseData, activityPackage);
    }

    // send next package because the current package failed
    private void sendNextPackageI(ActivityPackage activityPackage, String message, Throwable throwable) {
        final String failureMessage = activityPackage.getFailureMessage();
        final String reasonString = Util.getReasonString(message, throwable);
        String finalMessage = String.format("%s. (%s)", failureMessage, reasonString);
        logger.error(finalMessage);

        ResponseData responseData = ResponseData.buildResponseData(activityPackage);
        responseData.message = finalMessage;

        IPackageHandler packageHandler = packageHandlerWeakRef.get();
        if (packageHandler == null) {
            return;
        }

        packageHandler.sendNextPackage(responseData);
    }
}
