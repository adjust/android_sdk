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

import static com.adjust.sdk.Constants.PACKAGE_SENDING_MAX_ATTEMPT;

public class RequestHandler implements IRequestHandler {
    private ThreadExecutor executor;
    private WeakReference<IPackageHandler> packageHandlerWeakRef;
    private WeakReference<IActivityHandler> activityHandlerWeakRef;
    private ILogger logger;
    private String basePath;
    private String gdprPath;
    private String subscriptionPath;

    public RequestHandler(IActivityHandler activityHandler, IPackageHandler packageHandler) {
        this.logger = AdjustFactory.getLogger();
        this.executor = new SingleThreadCachedScheduler("RequestHandler");
        init(activityHandler, packageHandler);
        this.basePath = packageHandler.getBasePath();
        this.gdprPath = packageHandler.getGdprPath();
        this.subscriptionPath = packageHandler.getSubscriptionPath();
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
                boolean packageProcessed = false;
                for (int attemptCount = 1; !packageProcessed && (attemptCount <= PACKAGE_SENDING_MAX_ATTEMPT); attemptCount++) {
                    UrlStrategy urlStrategy = UrlStrategy.getStrategy(attemptCount);
                    packageProcessed = sendI(activityPackage, queueSize, urlStrategy);
                    // update strategy when changed
                    if (packageProcessed && attemptCount > 1) {
                        UrlStrategy.updateWorkingStrategy(urlStrategy);
                    }
                }
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

    private boolean sendI(ActivityPackage activityPackage, int queueSize, UrlStrategy urlStrategy) {
        String url;

        if (activityPackage.getActivityKind() == ActivityKind.GDPR) {
            url = Util.getGdprBaseUrl(urlStrategy);
            if (gdprPath != null) {
                url += gdprPath;
            }
        } else if (activityPackage.getActivityKind() == ActivityKind.SUBSCRIPTION) {
            url = Util.getSubscriptionBaseUrl(urlStrategy);
            if (subscriptionPath != null) {
                url += subscriptionPath;
            }
        } else {
            url = Util.getBaseUrl(urlStrategy);
            if (basePath != null) {
                url += basePath;
            }
        }

        String targetURL = url + activityPackage.getPath();

        logger.info("POST url: %s", targetURL);

        try {
            ResponseData responseData = UtilNetworking.createPOSTHttpsURLConnection(targetURL, activityPackage, queueSize);

            IPackageHandler packageHandler = packageHandlerWeakRef.get();
            if (packageHandler == null) {
                return true;
            }
            IActivityHandler activityHandler = activityHandlerWeakRef.get();
            if (activityHandler == null) {
                return true;
            }

            if (responseData.trackingState == TrackingState.OPTED_OUT) {
                activityHandler.gotOptOutResponse();
                return true;
            }

            if (responseData.jsonResponse == null) {
                packageHandler.closeFirstPackage(responseData, activityPackage);
                return true;
            }

            packageHandler.sendNextPackage(responseData);
            return true;
        } catch (UnsupportedEncodingException e) {
            sendNextPackageI(activityPackage, "Failed to encode parameters", e);
            return true;
        } catch (SocketTimeoutException e) {
            return handlePackageSendFailure(activityPackage, "Request timed out", e, urlStrategy);
        } catch (IOException e) {
            return handlePackageSendFailure(activityPackage, "Request failed", e, urlStrategy);
        } catch (Throwable e) {
            sendNextPackageI(activityPackage, "Runtime exception", e);
            return true;
        }
    }

    private boolean handlePackageSendFailure(ActivityPackage activityPackage, String message,
                                             Throwable throwable, UrlStrategy urlStrategy) {
        if (urlStrategy == UrlStrategy.FALLBACK_IP) {
            closePackageI(activityPackage, message, throwable);
            return true;
        }

        return false;
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
