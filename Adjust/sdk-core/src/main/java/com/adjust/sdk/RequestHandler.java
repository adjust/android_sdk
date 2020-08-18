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
import java.util.List;

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

                List<String> urls;
                boolean requestProcessed = false;
                if (activityPackage.getActivityKind() == ActivityKind.GDPR) {
                    urls = UrlFactory.getGdprUrls();
                    for (int i=0; i<urls.size() && !requestProcessed; i++) {
                        String gdprUrl = urls.get(i);
                        if (gdprPath != null) {
                            gdprUrl += gdprPath;
                        }
                        gdprUrl += activityPackage.getPath();
                        boolean isLastUrl = i == urls.size()-1;
                        requestProcessed = sendI(activityPackage, queueSize, gdprUrl, isLastUrl);
                        if (requestProcessed && i > 0) {
                            UrlFactory.prioritiseGdprUrl(urls.get(i));
                        }
                    }
                } else if (activityPackage.getActivityKind() == ActivityKind.SUBSCRIPTION) {
                    urls = UrlFactory.getSubscriptionUrls();
                    for (int i=0; i<urls.size() && !requestProcessed; i++) {
                        String subscriptionUrl = urls.get(i);
                        if (subscriptionPath != null) {
                            subscriptionUrl += subscriptionPath;
                        }
                        subscriptionUrl += activityPackage.getPath();
                        boolean isLastUrl = i == urls.size()-1;
                        requestProcessed = sendI(activityPackage, queueSize, subscriptionUrl, isLastUrl);
                        if (requestProcessed && i > 0) {
                            UrlFactory.prioritiseSubscriptionUrl(urls.get(i));
                        }
                    }
                } else {
                    urls = UrlFactory.getBaseUrls();
                    for (int i=0; i<urls.size() && !requestProcessed; i++) {
                        String baseUrl = urls.get(i);
                        if (basePath != null) {
                            baseUrl += basePath;
                        }
                        baseUrl += activityPackage.getPath();
                        boolean isLastUrl = i == urls.size()-1;
                        requestProcessed = sendI(activityPackage, queueSize, baseUrl, isLastUrl);
                        if (requestProcessed && i > 0) {
                            UrlFactory.prioritiseBaseUrl(urls.get(i));
                        }
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

    private boolean sendI(ActivityPackage activityPackage, int queueSize, String targetURL, boolean isLastUrl) {

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
            if (isLastUrl) {
                closePackageI(activityPackage, "Request timed out", e);
            }
            return false;
        } catch (IOException e) {
            if (isLastUrl) {
                closePackageI(activityPackage, "Request failed", e);
            }
            return false;
        } catch (Throwable e) {
            sendNextPackageI(activityPackage, "Runtime exception", e);
            return true;
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
