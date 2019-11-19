//
//  RequestHandler.java
//  Adjust
//
//  Created by Christian Wellenbrock on 2013-06-25.
//  Copyright (c) 2013 adjust GmbH. All rights reserved.
//  See the file MIT-LICENSE for copying permission.
//

package com.adjust.sdk;

import com.adjust.sdk.scheduler.SingleThreadCachedScheduler;
import com.adjust.sdk.scheduler.ThreadExecutor;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;

public class RequestHandler implements IRequestHandler {
    private ThreadExecutor executor;
    private WeakReference<IPackageHandler> packageHandlerWeakRef;
    private WeakReference<IActivityHandler> activityHandlerWeakRef;
    private ILogger logger;
    private String basePath;
    private String gdprPath;

    public RequestHandler(IActivityHandler activityHandler, IPackageHandler packageHandler) {
        this.logger = AdjustFactory.getLogger();
        this.executor = new SingleThreadCachedScheduler("RequestHandler");
        init(activityHandler, packageHandler);
        this.basePath = packageHandler.getBasePath();
        this.gdprPath = packageHandler.getGdprPath();
    }

    @Override
    public void init(IActivityHandler activityHandler, IPackageHandler packageHandler) {
        this.packageHandlerWeakRef = new WeakReference<IPackageHandler>(packageHandler);
        this.activityHandlerWeakRef = new WeakReference<IActivityHandler>(activityHandler);
    }

    @Override
    public void sendPackage(final ActivityPackage activityPackage, final int queueSize) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                sendI(activityPackage, queueSize);
            }
        });
    }

    @Override
    public void teardown() {
        logger.verbose("RequestHandler teardown");
        if (executor != null) {
            executor.teardown();
        }
        if (packageHandlerWeakRef != null) {
            packageHandlerWeakRef.clear();
        }
        if (activityHandlerWeakRef != null) {
            activityHandlerWeakRef.clear();
        }
        executor = null;
        packageHandlerWeakRef = null;
        activityHandlerWeakRef = null;
        logger = null;
    }

    private void sendI(ActivityPackage activityPackage, int queueSize) {
        String url;

        if (activityPackage.getActivityKind() == ActivityKind.GDPR) {
            url = AdjustFactory.getGdprUrl();
            if (gdprPath != null) {
                url += gdprPath;
            }
        } else {
            url = AdjustFactory.getBaseUrl();
            if (basePath != null) {
                url += basePath;
            }
        }

        String targetURL = url + activityPackage.getPath();

        try {
            ResponseData responseData = UtilNetworking.createPOSTHttpsURLConnection(targetURL, activityPackage, queueSize);

            IPackageHandler packageHandler = packageHandlerWeakRef.get();
            if (packageHandler == null) {
                return;
            }
            IActivityHandler activityHandler = activityHandlerWeakRef.get();
            if (activityHandler == null) {
                return;
            }

            if (responseData.trackingState == TrackingState.OPTED_OUT) {
                activityHandler.gotOptOutResponse();
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
        String finalMessage = Util.formatString("%s. (%s) Will retry later", packageMessage, reasonString);
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
        String finalMessage = Util.formatString("%s. (%s)", failureMessage, reasonString);
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
