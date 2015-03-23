package com.adjust.sdk.plugin;

import com.adjust.sdk.AdjustEvent;
import com.adjust.sdk.AdjustFactory;
import com.adjust.sdk.ILogger;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * Created by pfms on 24/02/15.
 */
public class AdjustCriteo {
    private static ILogger logger = AdjustFactory.getLogger();
    private static int MAX_VIEW_LISTING_PRODUCTS = 3;

    public static void injectViewSearchIntoEvent(AdjustEvent event, String checkInDate, String checkOutDate) {
        event.addPartnerParameter("din", checkInDate);
        event.addPartnerParameter("dout", checkOutDate);
    }

    public static void injectViewListingIntoEvent(AdjustEvent event, List<String> productIds, String customerId) {
        String jsonProducts = createCriteoVLFromProducts(productIds);

        if (jsonProducts == null) {
            logger.error("Missing products from Criteo View Listing");
            return;
        }

        event.addPartnerParameter("customer_id", customerId);
        event.addPartnerParameter("criteo_p", jsonProducts);
    }

    public static void injectViewProductIntoEvent(AdjustEvent event, String productId, String customerId) {
        event.addPartnerParameter("customer_id", customerId);
        event.addPartnerParameter("criteo_p", productId);
    }

    public static void injectCartIntoEvent(AdjustEvent event, List<CriteoProduct> products, String customerId) {
        String jsonProducts = createCriteoVBFromProducts(products);
        if (jsonProducts == null) {
            logger.error("Missing products from Criteo Cart");
            return;
        }

        event.addPartnerParameter("customer_id", customerId);
        event.addPartnerParameter("criteo_p", jsonProducts);
    }

    public static void injectTransactionConfirmedIntoEvent(AdjustEvent event, List<CriteoProduct> products, String customerId) {
        String jsonProducts = createCriteoVBFromProducts(products);
        if (jsonProducts == null) {
            logger.error("Missing products from Criteo Transaction Confirmed");
            return;
        }

        event.addPartnerParameter("customer_id", customerId);
        event.addPartnerParameter("criteo_p", jsonProducts);
    }

    private static String createCriteoVLFromProducts(List<String> productIds) {
        if (productIds == null) {
            return null;
        }
        StringBuffer criteoVLValue = new StringBuffer("[");
        int productIdsSize = productIds.size();

        if (productIdsSize > MAX_VIEW_LISTING_PRODUCTS) {
            logger.warn("Criteo View Listing should only have at most 3 product ids. The rest will be discarded.");
        }
        for (int i = 0; i < productIdsSize; ) {
            String productID = productIds.get(i);
            String productString = String.format("\"%s\"", productID);
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
        if (logger == null) {
            return null;
        }
        StringBuffer criteoVBValue = new StringBuffer("[");
        int productsSize = products.size();
        for (int i = 0; i < productsSize; ) {
            CriteoProduct criteoProduct = products.get(i);
            String productString = String.format("{\"i\":\"%s,\"pr\":%f,\"q\":%d}",
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
