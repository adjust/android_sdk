package com.adjust.sdk.network;

import static com.adjust.sdk.Constants.BASE_URL;
import static com.adjust.sdk.Constants.BASE_URL_FORMAT;
import static com.adjust.sdk.Constants.BASE_URL_NO_SUB_DOMAIN_FORMAT;
import static com.adjust.sdk.Constants.GDPR_URL;
import static com.adjust.sdk.Constants.GDPR_URL_FORMAT;
import static com.adjust.sdk.Constants.PURCHASE_VERIFICATION_URL;
import static com.adjust.sdk.Constants.PURCHASE_VERIFICATION_URL_FORMAT;
import static com.adjust.sdk.Constants.SUBSCRIPTION_URL;
import static com.adjust.sdk.Constants.SUBSCRIPTION_URL_FORMAT;

import com.adjust.sdk.ActivityKind;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UrlStrategy {
    private static final String BASE_URL_IO = "https://app.adjust.io";
    private static final String GDPR_URL_IO = "https://gdpr.adjust.io";
    private static final String SUBSCRIPTION_URL_IO = "https://subscription.adjust.io";
    private static final String PURCHASE_VERIFICATION_URL_IO = "https://ssrv.adjust.io";

    private final List<String> urlStrategyDomains;
    private final boolean useSubdomains;

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
                       final List<String> urlStrategyDomains,
                       final boolean useSubdomains)
    {
        this.urlStrategyDomains = urlStrategyDomains;
        this.useSubdomains = useSubdomains;

        this.baseUrlOverwrite = baseUrlOverwrite;
        this.gdprUrlOverwrite = gdprUrlOverwrite;
        this.subscriptionUrlOverwrite = subscriptionUrlOverwrite;
        this.purchaseVerificationUrlOverwrite = purchaseVerificationUrlOverwrite;

        baseUrlChoicesList = baseUrlChoices();
        gdprUrlChoicesList = gdprUrlChoices();
        subscriptionUrlChoicesList = subscriptionUrlChoices();
        purchaseVerificationUrlChoicesList = purchaseVerificationUrlChoices();

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

    private List<String> baseUrlChoices() {

        if (urlStrategyDomains == null || urlStrategyDomains.isEmpty()) {
            return Arrays.asList(BASE_URL, BASE_URL_IO);
        }
        if (useSubdomains) {
            List<String> baseUrls = new ArrayList<>();
            for (String url : urlStrategyDomains) {
                baseUrls.add(String.format(BASE_URL_FORMAT, url));
            }
            return baseUrls;
        }else {
            List<String> baseUrls = new ArrayList<>();
            for (String url : urlStrategyDomains) {
                baseUrls.add(String.format(BASE_URL_NO_SUB_DOMAIN_FORMAT, url));
            }
            return baseUrls;
        }
    }
    private List<String> gdprUrlChoices() {
        if (urlStrategyDomains == null || urlStrategyDomains.isEmpty()) {
            return Arrays.asList(GDPR_URL, GDPR_URL_IO);
        }
        List<String> baseUrls = new ArrayList<>();
        if (useSubdomains) {
            for (String url : urlStrategyDomains) {
                baseUrls.add(String.format(GDPR_URL_FORMAT, url));
            }
        }else {
            for (String url : urlStrategyDomains) {
                baseUrls.add(String.format(BASE_URL_NO_SUB_DOMAIN_FORMAT, url));
            }
        }
        return baseUrls;
    }
    private List<String> subscriptionUrlChoices() {
        if (urlStrategyDomains == null || urlStrategyDomains.isEmpty()) {
            return Arrays.asList(SUBSCRIPTION_URL, SUBSCRIPTION_URL_IO);
        }
        List<String> baseUrls = new ArrayList<>();
        if (useSubdomains) {
            for (String url : urlStrategyDomains) {
                baseUrls.add(String.format(SUBSCRIPTION_URL_FORMAT, url));
            }
        }else {
            for (String url : urlStrategyDomains) {
                baseUrls.add(String.format(BASE_URL_NO_SUB_DOMAIN_FORMAT, url));
            }
        }
        return baseUrls;
    }
    private List<String> purchaseVerificationUrlChoices() {
        if (urlStrategyDomains == null || urlStrategyDomains.isEmpty()) {
            return Arrays.asList(PURCHASE_VERIFICATION_URL, PURCHASE_VERIFICATION_URL_IO);
        }
        List<String> baseUrls = new ArrayList<>();
        if (useSubdomains){
            for (String url : urlStrategyDomains) {
                baseUrls.add(String.format(PURCHASE_VERIFICATION_URL_FORMAT, url));
            }
        }else {
            for (String url : urlStrategyDomains) {
                baseUrls.add(String.format(BASE_URL_NO_SUB_DOMAIN_FORMAT, url));
            }
        }
        return baseUrls;
    }
}
