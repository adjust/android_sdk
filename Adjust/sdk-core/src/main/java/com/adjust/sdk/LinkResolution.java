package com.adjust.sdk;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class LinkResolution {
    public interface LinkResolutionCallback {
        void resolvedLinkCallback(URL resolvedLink);
    }

    // https://www.cs.umd.edu/~pugh/java/memoryModel/DoubleCheckedLocking.html
    private static volatile ExecutorService executor;

    private static final int maxRecursions = 3;
    private static final String[] expectedUrlHostSuffixArray = {
            "adjust.com",
            "adj.st",
            "go.link"
    };

    private LinkResolution() { }

    public static void resolveLink(final String encodedURL,
                                   final LinkResolutionCallback linkResolutionCallback)
    {
        if (linkResolutionCallback == null) {
            return;
        }

        if (executor == null) {
            synchronized (expectedUrlHostSuffixArray) {
                if (executor == null) {
                    executor = Executors.newSingleThreadExecutor();
                }
            }
        }

        executor.execute(new Runnable() {
            @Override
            public void run() {
                URL originalURL = null;
                try {
                    originalURL = new URL(encodedURL);
                } catch (final MalformedURLException ignored) {
                }

                if (originalURL == null) {
                    linkResolutionCallback.resolvedLinkCallback(null);
                    return;
                }

                requestAndResolve(originalURL, 0, linkResolutionCallback);
            }
        });
    }
    private static void resolveLink(
            final URL responseUrl,
            final URL previousUrl,
            final int recursionNumber,
            final LinkResolutionCallback linkResolutionCallback)
    {
        // return (possible null) previous url when the current one does not exist
        if (responseUrl == null) {
            linkResolutionCallback.resolvedLinkCallback(previousUrl);
            return;
        }

        // return found url with expected host
        if (isExpectedUrl(responseUrl.getHost())) {
            linkResolutionCallback.resolvedLinkCallback(responseUrl);
            return;
        }

        // return previous (non-null) url when it reached the max number of recursive tries
        if (recursionNumber > maxRecursions) {
            linkResolutionCallback.resolvedLinkCallback(responseUrl);
            return;
        }

        requestAndResolve(responseUrl, recursionNumber, linkResolutionCallback);
    }

    private static void requestAndResolve(final URL urlToRequest,
                                          final int recursionNumber,
                                          final LinkResolutionCallback linkResolutionCallback)
    {
        URL resolvedURL = null;
        try {
            final HttpURLConnection ucon =
                    (HttpURLConnection) urlToRequest.openConnection();
            ucon.setInstanceFollowRedirects(false);
            resolvedURL = new URL(ucon.getHeaderField("Location"));
        }
        catch (final Throwable ignored) { }
        finally {
            resolveLink(resolvedURL,
                    urlToRequest,
                    recursionNumber + 1,
                    linkResolutionCallback);
        }
    }

    private static boolean isExpectedUrl(final String urlHost) {
        if (urlHost == null) {
            return false;
        }

        for (final String expectedUrlHostSuffix : expectedUrlHostSuffixArray) {
            if (urlHost.endsWith(expectedUrlHostSuffix)) {
                return true;
            }
        }

        return false;
    }
}
