package com.adjust.sdk.plugin;

import android.net.Uri;

import com.adjust.sdk.AdjustEvent;
import com.adjust.sdk.AdjustFactory;
import com.adjust.sdk.ILogger;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by pfms on 24/02/15.
 */
public class AdjustCriteo {
    private static ILogger logger = AdjustFactory.getLogger();
    private static int MAX_VIEW_LISTING_PRODUCTS = 3;
    private static String hashEmailInternal;
    private static String checkInDateInternal;
    private static String checkOutDateInternal;
    private static String partnerIdInternal;

    public static void injectViewListingIntoEvent(AdjustEvent event, List<String> productIds, String customerId) {
        String jsonProducts = createCriteoVLFromProducts(productIds);
        event.addPartnerParameter("customer_id", customerId);
        event.addPartnerParameter("criteo_p", jsonProducts);

        injectOptionalParams(event);
    }

    public static void injectViewProductIntoEvent(AdjustEvent event, String productId, String customerId) {
        event.addPartnerParameter("customer_id", customerId);
        event.addPartnerParameter("criteo_p", productId);

        injectOptionalParams(event);
    }

    public static void injectCartIntoEvent(AdjustEvent event, List<CriteoProduct> products, String customerId) {
        String jsonProducts = createCriteoVBFromProducts(products);

        event.addPartnerParameter("customer_id", customerId);
        event.addPartnerParameter("criteo_p", jsonProducts);

        injectOptionalParams(event);
    }

    public static void injectTransactionConfirmedIntoEvent(AdjustEvent event, List<CriteoProduct> products, String transactionId, String customerId) {
        String jsonProducts = createCriteoVBFromProducts(products);

        event.addPartnerParameter("customer_id", customerId);
        event.addPartnerParameter("transaction_id", transactionId);
        event.addPartnerParameter("criteo_p", jsonProducts);

        injectOptionalParams(event);
    }

    public static void injectUserLevelIntoEvent(AdjustEvent event, long uiLevel, String customerId) {
        event.addPartnerParameter("customer_id", customerId);
        event.addPartnerParameter("ui_level", String.valueOf(uiLevel));

        injectOptionalParams(event);
    }

    public static void injectUserStatusIntoEvent(AdjustEvent event, String uiStatus, String customerId) {
        event.addPartnerParameter("customer_id", customerId);
        event.addPartnerParameter("ui_status", uiStatus);

        injectOptionalParams(event);
    }

    public static void injectAchievementUnlockedIntoEvent(AdjustEvent event, String uiAchievement, String customerId) {
        event.addPartnerParameter("customer_id", customerId);
        event.addPartnerParameter("ui_achievmnt", uiAchievement);

        injectOptionalParams(event);
    }

    public static void injectCustomEventIntoEvent(AdjustEvent event, String uiData, String customerId) {
        event.addPartnerParameter("customer_id", customerId);
        event.addPartnerParameter("ui_data", uiData);

        injectOptionalParams(event);
    }

    public static void injectCustomEvent2IntoEvent(AdjustEvent event, String uiData2, long uiData3, String customerId) {
        event.addPartnerParameter("customer_id", customerId);
        event.addPartnerParameter("ui_data2", uiData2);
        event.addPartnerParameter("ui_data3", String.valueOf(uiData3));

        injectOptionalParams(event);
    }

    public static void injectHashedEmailIntoCriteoEvents(String hashEmail) {
        hashEmailInternal = hashEmail;
    }

    public static void injectViewSearchDatesIntoCriteoEvents(String checkInDate, String checkOutDate) {
        checkInDateInternal = checkInDate;
        checkOutDateInternal = checkOutDate;
    }

    public static void injectPartnerIdIntoCriteoEvents(String partnerId) {
        partnerIdInternal = partnerId;
    }

    public static void injectDeeplinkIntoEvent(AdjustEvent event, Uri url) {
        if (url == null) {
            return;
        }

        event.addPartnerParameter("criteo_deeplink", url.toString());

        injectOptionalParams(event);
    }

    private static void injectOptionalParams(AdjustEvent event) {
        injectHashEmail(event);
        injectSearchDates(event);
        injectPartnerId(event);
    }

    private static void injectHashEmail(AdjustEvent event) {
        if (hashEmailInternal == null || hashEmailInternal.isEmpty()) {
            return;
        }

        event.addPartnerParameter("criteo_email_hash", hashEmailInternal);
    }

    private static void injectSearchDates(AdjustEvent event) {
        if (checkInDateInternal == null || checkInDateInternal.isEmpty() ||
                checkOutDateInternal == null || checkOutDateInternal.isEmpty()) {
            return;
        }

        event.addPartnerParameter("din", checkInDateInternal);
        event.addPartnerParameter("dout", checkOutDateInternal);
    }

    private static void injectPartnerId(AdjustEvent event) {
        if (partnerIdInternal == null || partnerIdInternal.isEmpty()) {
            return;
        }

        event.addPartnerParameter("criteo_partner_id", partnerIdInternal);
    }

    private static String createCriteoVLFromProducts(List<String> productIds) {
        if (productIds == null) {
            logger.warn("Criteo View Listing product ids list is null. It will sent as empty.");
            productIds = new ArrayList<String>();
        }
        StringBuffer criteoVLValue = new StringBuffer("[");
        int productIdsSize = productIds.size();

        if (productIdsSize > MAX_VIEW_LISTING_PRODUCTS) {
            logger.warn("Criteo View Listing should only have at most 3 product ids. The rest will be discarded.");
        }
        for (int i = 0; i < productIdsSize; ) {
            String productID = productIds.get(i);
            String productString = String.format(Locale.US, "\"%s\"", productID);
            criteoVLValue.append(productString);

            i++;

            if (i == productIdsSize || i >= MAX_VIEW_LISTING_PRODUCTS) {
                break;
            }

            criteoVLValue.append(",");
        }
        criteoVLValue.append("]");
        String result = null;
        try {
            result = URLEncoder.encode(criteoVLValue.toString(),"UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error("error converting criteo product ids (%s)", e.getMessage());
        }
        return result;
    }

    private static String createCriteoVBFromProducts(List<CriteoProduct> products) {
        if (products == null) {
            logger.warn("Criteo Event product list is empty. It will sent as empty.");
            products = new ArrayList<CriteoProduct>();
        }
        StringBuffer criteoVBValue = new StringBuffer("[");
        int productsSize = products.size();
        for (int i = 0; i < productsSize; ) {
            CriteoProduct criteoProduct = products.get(i);
            String productString = String.format(Locale.US, "{\"i\":\"%s\",\"pr\":%f,\"q\":%d}",
                    criteoProduct.productID,
                    criteoProduct.price,
                    criteoProduct.quantity);
            criteoVBValue.append(productString);

            i++;

            if (i == productsSize) {
                break;
            }

            criteoVBValue.append(",");
        }
        criteoVBValue.append("]");
        String result = null;
        try {
            result = URLEncoder.encode(criteoVBValue.toString(),"UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error("error converting criteo products (%s)", e.getMessage());
        }
        return result;
    }
}
