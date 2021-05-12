package com.adjust.sdk;

import android.net.Uri;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class LinkResolution {
    public interface LinkResolutionCallback {
        void resolvedLinkCallback(Uri resolvedLink);
    }

    // https://www.cs.umd.edu/~pugh/java/memoryModel/DoubleCheckedLocking.html
    private static volatile ExecutorService executor;

    private static final int maxRecursions = 10;
    private static final String[] expectedUrlHostSuffixArray = {
            "adjust.com",
            "adj.st",
            "go.link"
    };

    private LinkResolution() { }

    public static void resolveLink(final String url,
                                   final String[] resolveUrlSuffixArray,
                                   final LinkResolutionCallback linkResolutionCallback)
    {
        if (linkResolutionCallback == null) {
            return;
        }

        if (url == null) {
            linkResolutionCallback.resolvedLinkCallback(null);
            return;
        }

        URL originalURL = null;
        try {
            originalURL = new URL(url);
        } catch (final MalformedURLException ignored) {
        }

        if (originalURL == null) {
            linkResolutionCallback.resolvedLinkCallback(null);
            return;
        }

        if (! urlMatchesSuffix(originalURL.getHost(), resolveUrlSuffixArray)) {
            linkResolutionCallback.resolvedLinkCallback(Uri.parse(originalURL.toString()));
            return;
        }

        if (executor == null) {
            synchronized (expectedUrlHostSuffixArray) {
                if (executor == null) {
                    executor = Executors.newSingleThreadExecutor();
                }
            }
        }

        final URL finalOriginalURL = originalURL;
        executor.execute(new Runnable() {
            @Override
            public void run() {
                requestAndResolve(finalOriginalURL, 0, linkResolutionCallback);
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
            linkResolutionCallback.resolvedLinkCallback(Uri.parse(previousUrl.toString()));
            return;
        }

        // return found url with expected host
        if (isTerminalUrl(responseUrl.getHost())) {
            linkResolutionCallback.resolvedLinkCallback(Uri.parse(responseUrl.toString()));
            return;
        }

        // return previous (non-null) url when it reached the max number of recursive tries
        if (recursionNumber > maxRecursions) {
            linkResolutionCallback.resolvedLinkCallback(Uri.parse(responseUrl.toString()));
            return;
        }

        requestAndResolve(responseUrl, recursionNumber, linkResolutionCallback);
    }

    private static void requestAndResolve(final URL urlToRequest,
                                          final int recursionNumber,
                                          final LinkResolutionCallback linkResolutionCallback)
    {
        final URL httpsUrl = convertToHttps(urlToRequest);
        URL resolvedURL = null;
        HttpURLConnection ucon = null;
        try {
            ucon = (HttpURLConnection) httpsUrl.openConnection();
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
                    httpsUrl,
                    recursionNumber + 1,
                    linkResolutionCallback);
        }
    }

    private static boolean isTerminalUrl(final String urlHost) {
        return urlMatchesSuffix(urlHost, expectedUrlHostSuffixArray);
    }

    private static boolean urlMatchesSuffix(final String urlHost, final String[] suffixArray) {
        if (urlHost == null) {
            return false;
        }

        if (suffixArray == null) {
            return false;
        }

        for (final String expectedUrlHostSuffix : suffixArray) {
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
