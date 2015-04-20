package com.adjust.sdk.plugin;

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

    public static void injectViewSearchIntoEvent(AdjustEvent event, String checkInDate, String checkOutDate) {
        event.addPartnerParameter("din", checkInDate);
        event.addPartnerParameter("dout", checkOutDate);

        injectHashEmail(event);
    }

    public static void injectViewListingIntoEvent(AdjustEvent event, List<String> productIds, String customerId) {
        String jsonProducts = createCriteoVLFromProducts(productIds);
        event.addPartnerParameter("customer_id", customerId);
        event.addPartnerParameter("criteo_p", jsonProducts);

        injectHashEmail(event);
    }

    public static void injectViewProductIntoEvent(AdjustEvent event, String productId, String customerId) {
        event.addPartnerParameter("customer_id", customerId);
        event.addPartnerParameter("criteo_p", productId);

        injectHashEmail(event);
    }

    public static void injectCartIntoEvent(AdjustEvent event, List<CriteoProduct> products, String customerId) {
        String jsonProducts = createCriteoVBFromProducts(products);

        event.addPartnerParameter("customer_id", customerId);
        event.addPartnerParameter("criteo_p", jsonProducts);

        injectHashEmail(event);
    }

    public static void injectTransactionConfirmedIntoEvent(AdjustEvent event, List<CriteoProduct> products, String customerId) {
        String jsonProducts = createCriteoVBFromProducts(products);

        event.addPartnerParameter("customer_id", customerId);
        event.addPartnerParameter("criteo_p", jsonProducts);

        injectHashEmail(event);
    }

    public static void injectUserLevelIntoEvent(AdjustEvent event, long uiLevel, String customerId) {
        event.addPartnerParameter("customer_id", customerId);
        event.addPartnerParameter("ui_level", String.valueOf(uiLevel));

        injectHashEmail(event);
    }

    public static void injectUserStatusIntoEvent(AdjustEvent event, String uiStatus, String customerId) {
        event.addPartnerParameter("customer_id", customerId);
        event.addPartnerParameter("ui_status", uiStatus);

        injectHashEmail(event);
    }

    public static void injectAchievementUnlockedIntoEvent(AdjustEvent event, String uiAchievement, String customerId) {
        event.addPartnerParameter("customer_id", customerId);
        event.addPartnerParameter("ui_achievmnt", uiAchievement);

        injectHashEmail(event);
    }

    public static void injectCustomEventIntoEvent(AdjustEvent event, String uiData, String customerId) {
        event.addPartnerParameter("customer_id", customerId);
        event.addPartnerParameter("ui_data", uiData);

        injectHashEmail(event);
    }

    public static void injectCustomEvent2IntoEvent(AdjustEvent event, String uiData2, long uiData3, String customerId) {
        event.addPartnerParameter("customer_id", customerId);
        event.addPartnerParameter("ui_data2", uiData2);
        event.addPartnerParameter("ui_data3", String.valueOf(uiData3));

        injectHashEmail(event);
    }

    public static void injectHashedEmailIntoCriteoEvents(String hashEmail) {
        hashEmailInternal = hashEmail;
    }

    private static void injectHashEmail(AdjustEvent event) {
        if (hashEmailInternal == null) {
            return;
        }

        event.addPartnerParameter("criteo_email_hash", hashEmailInternal);
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
