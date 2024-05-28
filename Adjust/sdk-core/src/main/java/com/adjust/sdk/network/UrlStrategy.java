package com.adjust.sdk.network;

import com.adjust.sdk.ActivityKind;
import com.adjust.sdk.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UrlStrategy {
    private static final String BASE_URL_WORLD = "https://app.adjust.world";
    private static final String GDPR_URL_WORLD = "https://gdpr.adjust.world";
    private static final String SUBSCRIPTION_URL_WORLD = "https://subscription.adjust.world";
    private static final String PURCHASE_VERIFICATION_URL_WORLD = "https://ssrv.adjust.world";

    private final String baseUrlOverwrite;
    private final String gdprUrlOverwrite;
    private final String subscriptionUrlOverwrite;
    private final String purchaseVerificationUrlOverwrite;

    final List<String> baseUrlChoicesList;
    final List<String> gdprUrlChoicesList;
    final List<String> subscriptionUrlChoicesList;
    final List<String> purchaseVerificationUrlChoicesList;
    boolean wasLastAttemptSuccess;
    int choiceIndex;
    int startingChoiceIndex;
    boolean wasLastAttemptWithOverwrittenUrl;

    public UrlStrategy(final String baseUrlOverwrite,
                       final String gdprUrlOverwrite,
                       final String subscriptionUrlOverwrite,
                       final String purchaseVerificationUrlOverwrite,
                       final List<String> adjustUrlStrategy,
                       final Boolean useSubdomains)
    {
        this.baseUrlOverwrite = baseUrlOverwrite;
        this.gdprUrlOverwrite = gdprUrlOverwrite;
        this.subscriptionUrlOverwrite = subscriptionUrlOverwrite;
        this.purchaseVerificationUrlOverwrite = purchaseVerificationUrlOverwrite;

        baseUrlChoicesList = baseUrlChoices(adjustUrlStrategy, useSubdomains);
        gdprUrlChoicesList = gdprUrlChoices(adjustUrlStrategy, useSubdomains);
        subscriptionUrlChoicesList = subscriptionUrlChoices(adjustUrlStrategy, useSubdomains);
        purchaseVerificationUrlChoicesList = purchaseVerificationUrlChoices(adjustUrlStrategy, useSubdomains);

        wasLastAttemptSuccess = false;
        choiceIndex = 0;
        startingChoiceIndex = 0;
        wasLastAttemptWithOverwrittenUrl = false;
    }

    public void resetAfterSuccess() {
        startingChoiceIndex = choiceIndex;
        wasLastAttemptSuccess = true;
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    public boolean shouldRetryAfterFailure(final ActivityKind activityKind) {
        wasLastAttemptSuccess = false;

        // does not need to "rotate" choice index
        //  since it will use the same overwritten url
        //  might as well stop retrying in the same sending "session"
        //  and let the backoff strategy pick it up
        if (wasLastAttemptWithOverwrittenUrl) {
            return false;
        }

        int choiceListSize;

        if (activityKind == ActivityKind.GDPR) {
            choiceListSize = gdprUrlChoicesList.size();
        } else if (activityKind == ActivityKind.SUBSCRIPTION) {
            choiceListSize = subscriptionUrlChoicesList.size();
        } else if (activityKind == ActivityKind.PURCHASE_VERIFICATION) {
            choiceListSize = purchaseVerificationUrlChoicesList.size();
        } else {
            choiceListSize = baseUrlChoicesList.size();
        }

        final int nextChoiceIndex = (choiceIndex + 1) % choiceListSize;
        choiceIndex = nextChoiceIndex;
        final boolean nextChoiceHasNotReturnedToStartingChoice = choiceIndex != startingChoiceIndex;

        return nextChoiceHasNotReturnedToStartingChoice;
    }

    public String targetUrlByActivityKind(final ActivityKind activityKind) {
        if (activityKind == ActivityKind.GDPR) {
            if (gdprUrlOverwrite != null) {
                wasLastAttemptWithOverwrittenUrl = true;
                return gdprUrlOverwrite;
            } else {
                wasLastAttemptWithOverwrittenUrl = false;
                return gdprUrlChoicesList.get(choiceIndex);
            }
        } else if (activityKind == ActivityKind.SUBSCRIPTION) {
            if (subscriptionUrlOverwrite != null) {
                wasLastAttemptWithOverwrittenUrl = true;
                return subscriptionUrlOverwrite;
            } else {
                wasLastAttemptWithOverwrittenUrl = false;
                return subscriptionUrlChoicesList.get(choiceIndex);
            }
        } else if (activityKind == ActivityKind.PURCHASE_VERIFICATION) {
            if (purchaseVerificationUrlOverwrite != null) {
                wasLastAttemptWithOverwrittenUrl = true;
                return purchaseVerificationUrlOverwrite;
            } else {
                wasLastAttemptWithOverwrittenUrl = false;
                return purchaseVerificationUrlChoicesList.get(choiceIndex);
            }
        } else {
            if (baseUrlOverwrite != null) {
                wasLastAttemptWithOverwrittenUrl = true;
                return baseUrlOverwrite;
            } else {
                wasLastAttemptWithOverwrittenUrl = false;
                return baseUrlChoicesList.get(choiceIndex);
            }
        }
    }

    private static List<String> baseUrlChoices(final List<String> urlStrategy,final Boolean useSubdomains) {

        if (urlStrategy == null || urlStrategy.isEmpty()) {
            return Arrays.asList(Constants.BASE_URL,  BASE_URL_WORLD);
        }
        if (useSubdomains != null && useSubdomains){
            List<String> baseUrls = new ArrayList<>();
            for (String url : urlStrategy) {
                baseUrls.add(String.format(Constants.BASE_URL_FORMAT, url));
            }
            return baseUrls;
        }else {
            List<String> baseUrls = new ArrayList<>();
            for (String url : urlStrategy) {
                baseUrls.add(String.format("https://%s", url));
            }
            return baseUrls;
        }
    }
    private static List<String> gdprUrlChoices(final List<String> urlStrategy,final Boolean useSubdomains) {
        if (urlStrategy == null || urlStrategy.isEmpty()) {
            return Arrays.asList(Constants.GDPR_URL,  GDPR_URL_WORLD);
        }
        if (useSubdomains != null && useSubdomains){
            List<String> baseUrls = new ArrayList<>();
            for (String url : urlStrategy) {
                baseUrls.add(String.format(Constants.GDPR_URL_FORMAT, url));
            }
            return baseUrls;
        }else {
            List<String> baseUrls = new ArrayList<>();
            for (String url : urlStrategy) {
                baseUrls.add(String.format("https://%s", url));
            }
            return baseUrls;
        }
    }
    private static List<String> subscriptionUrlChoices(final List<String> urlStrategy,final Boolean useSubdomains) {
        if (urlStrategy == null || urlStrategy.isEmpty()) {
            return Arrays.asList(Constants.SUBSCRIPTION_URL,  SUBSCRIPTION_URL_WORLD);
        }
        if (useSubdomains != null && useSubdomains){
            List<String> baseUrls = new ArrayList<>();
            for (String url : urlStrategy) {
                baseUrls.add(String.format(Constants.SUBSCRIPTION_URL_FORMAT, url));
            }
            return baseUrls;
        }else {
            List<String> baseUrls = new ArrayList<>();
            for (String url : urlStrategy) {
                baseUrls.add(String.format("https://%s", url));
            }
            return baseUrls;
        }
    }
    private static List<String> purchaseVerificationUrlChoices(final List<String> urlStrategy,final Boolean useSubdomains) {
        if (urlStrategy == null || urlStrategy.isEmpty()) {
            return Arrays.asList(Constants.PURCHASE_VERIFICATION_URL, PURCHASE_VERIFICATION_URL_WORLD);
        }
        if (useSubdomains != null && useSubdomains){
            List<String> baseUrls = new ArrayList<>();
            for (String url : urlStrategy) {
                baseUrls.add(String.format(Constants.PURCHASE_VERIFICATION_URL_FORMAT, url));
            }
            return baseUrls;
        }else {
            List<String> baseUrls = new ArrayList<>();
            for (String url : urlStrategy) {
                baseUrls.add(String.format("https://%s", url));
            }
            return baseUrls;
        }
    }
}
