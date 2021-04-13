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
        HttpURLConnection ucon = null;
        try {
            ucon = (HttpURLConnection) convertToHttps(urlToRequest).openConnection();
            ucon.setInstanceFollowRedirects(false);

            ucon.connect();

            final String headerLocationField = ucon.getHeaderField("Location");
            if (headerLocationField != null) {
                resolvedURL = new URL(headerLocationField);
            }
        }
        catch (final Throwable ignored) {}
        finally {
            if (ucon != null) {
                ucon.disconnect();
            }

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

    private static URL convertToHttps(final URL urlToConvert) {
        if (urlToConvert == null) {
            return urlToConvert;
        }

        final String stringUrlToConvert = urlToConvert.toExternalForm();

        if (stringUrlToConvert == null) {
            return urlToConvert;
        }

        if (stringUrlToConvert.startsWith("https:")) {
            return urlToConvert;
        }

        if (! stringUrlToConvert.startsWith("http:")) {
            return urlToConvert;
        }

        URL convertedUrl = urlToConvert;
        try {
            convertedUrl = new URL("https:" + stringUrlToConvert.substring(5));
        } catch (final MalformedURLException ignored) { }

        return convertedUrl;
    }
}
