package com.adjust.sdk.plugin;

import com.adjust.sdk.AdjustEvent;
import com.adjust.sdk.AdjustFactory;
import com.adjust.sdk.ILogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * AdjustSociomantic Class
 * Created by Nicolas Brugneaux <nicolas.brugneaux@sociomantic.com> on 01/04/15.
 */
public abstract class AdjustSociomantic {

    public final static String SCMCategory = "category";
    public final static String SCMProductName = "fn";
    public final static String SCMSalePrice = "price";
    public final static String SCMAmount = "amount";
    public final static String SCMCurrency = "currency";
    public final static String SCMProductURL = "url";
    public final static String SCMProductImageURL = "photo";
    public final static String SCMBrand = "brand";
    public final static String SCMDescription = "description";
    public final static String SCMTimestamp = "date";
    public final static String SCMValidityTimestamp = "valid";
    public final static String SCMQuantity = "quantity";
    public final static String SCMScore = "score";
    public final static String SCMProductID = "identifier";
    public final static String SCMActionConfirmed = "confirmed";
    public final static String SCMCustomerAgeGroup = "agegroup";
    public final static String SCMCustomerEducation = "education";
    public final static String SCMCustomerGender = "gender";
    public final static String SCMCustomerID = "identifier";
    public final static String SCMCustomerMHash = "mhash";
    public final static String SCMCustomerSegment = "segment";
    public final static String SCMCustomerTargeting = "targeting";
    public final static String SCMTransaction = "transaction";

    private final static List<String> productAliases = Arrays.asList(
        SCMCategory,
        SCMProductName,
        SCMSalePrice,
        SCMAmount,
        SCMCurrency,
        SCMProductURL,
        SCMProductImageURL,
        SCMBrand,
        SCMDescription,
        SCMTimestamp,
        SCMValidityTimestamp,
        SCMQuantity,
        SCMScore
    );

    private final static List<String> basketAliases = Arrays.asList(
        SCMProductID,
        SCMAmount,
        SCMCurrency,
        SCMQuantity
    );

    private final static  List<String> saleAliases = Arrays.asList(
        SCMAmount,
        SCMCurrency
    );

    private final static  List<String> customerAliases = Arrays.asList(
        SCMCustomerAgeGroup,
        SCMCustomerEducation,
        SCMCustomerGender,
        SCMCustomerID,
        SCMCustomerMHash,
        SCMCustomerSegment,
        SCMCustomerTargeting
    );

    private static ILogger logger = AdjustFactory.getLogger();

    public static void injectCustomerDataIntoEvent(AdjustEvent event, Map<String, String> customerData) {
        if (null == event) {
            logger.error("Event object is required.");
            return;
        }
        if (null == customerData) {
            logger.error("Customer data is required.");
            return;
        }

        Map<String, String> data = new HashMap<>();

        for (Entry<String, String> entry: customerData.entrySet()) {
            if (!customerAliases.contains(entry.getKey())) {
                logger.warn("Key must belong to the customer Aliases, entry: %s was discarded", entry.getKey());
            }
            else {
                data.put(entry.getKey(), entry.getValue());
            }
        }

        String dob = stringify(data);

        event.addPartnerParameter("dob", dob);
    }

    public static void injectHomePageIntoEvent(AdjustEvent event) {
        if (null == event) {
            logger.error("Event object is required.");
        }
        // do nothing
    }

    public static void injectViewListingIntoEvent(AdjustEvent event, List<String> categories) {
        injectViewListingIntoEvent(event, categories, null);
    }

    public static void injectViewListingIntoEvent(AdjustEvent event, List<String> categories, String date) {
        if (null == event) {
            logger.error("Event object is required.");
            return;
        }
        if (null == categories) {
            logger.error("Categories list is required.");
            return;
        }

        Map<String, Object> co = new HashMap<>();

        if (null != date) {
            co.put(SCMTimestamp, date);
        }

        co.put(SCMCategory, categories);

        event.addPartnerParameter("co", stringify(co));
    }

    public static void injectProductIntoEvent(AdjustEvent event, String productId) {
        injectProductIntoEvent(event, productId, null);
    }

    public static void injectProductIntoEvent(AdjustEvent event, String productId, Map<String, Object> parameters) {
        if (null == event) {
            logger.error("Event object is required.");
            return;
        }
        if (null == productId || "".equals(productId)) {
            logger.error("Product ID is required.");
            return;
        }

        Map<String, Object> po;

        if (null != parameters) {
            po = filter(parameters, productAliases);
        }
        else {
            po = new HashMap<>();
        }

        po.put(SCMProductID, productId);

        event.addPartnerParameter("po", stringify(po));
    }

    public static void injectCartIntoEvent(AdjustEvent event, List products) {
        if (null == event) {
            logger.error("Event object is required.");
            return;
        }
        if (null == products) {
            logger.error("Products list is required.");
            return;
        }

        List<Map<String, Object>> po = new ArrayList<>();

        for (Object product: products) {
            Map<String, Object> _product = new HashMap<>();

            if (product instanceof String) {
                _product.put(SCMProductID, product);
            }
            else if (product instanceof Map) {
                _product = filter((Map<String, Object>) product, basketAliases);
            }

            if (!_product.isEmpty()) {
                po.add(_product);
            }
        }

        if (!po.isEmpty()) {
            event.addPartnerParameter("po", stringify(po));
        }
    }

    public static void injectConfirmedTransactionIntoEvent(AdjustEvent event, String transactionID, List products) {
        injectTransactionIntoEvent(event, transactionID, products, null, Boolean.TRUE);
    }

    public static void injectConfirmedTransactionIntoEvent(AdjustEvent event, String transactionID, List products, Map<String, Object> parameters) {
        injectTransactionIntoEvent(event, transactionID, products, parameters, Boolean.TRUE);
    }

    public static void injectTransactionIntoEvent(AdjustEvent event, String transactionID, List products) {
        injectTransactionIntoEvent(event, transactionID, products, null, Boolean.FALSE);
    }
    
    public static void injectTransactionIntoEvent(AdjustEvent event, String transactionID, List products, Map<String, Object> parameters) {
        injectTransactionIntoEvent(event, transactionID, products, parameters, Boolean.FALSE);
    }
    
    private static void injectTransactionIntoEvent(AdjustEvent event, String transactionID, List products, Map<String, Object> parameters, Boolean confirmed) {
        if (null == event) {
            logger.error("Event object is required.");
            return;
        }
        if (null == transactionID || "".equals(transactionID)) {
            logger.error("Transaction ID is required.");
            return;
        }
        if (null == products) {
            logger.error("Products list is required.");
            return;
        }

        Map<String, Map<String, Object>> to = new HashMap<>(1);
        List<Map<String, Object>> po = new ArrayList<>();

        for (Object product: products) {
            Map<String, Object> _product = new HashMap<>();

            if (product instanceof String) {
                _product.put(SCMProductID, product);
            }
            else if (product instanceof Map) {
                _product = filter((Map<String, Object>) product, basketAliases);
            }

            if (!_product.isEmpty()) {
                po.add(_product);
            }
        }

        if (!po.isEmpty()) {
            event.addPartnerParameter("po", stringify(po));
        }

        if (null != parameters) {
            to.put(SCMTransaction, filter(parameters, saleAliases));
        }
        else {
            to.put(SCMTransaction, new HashMap<String, Object>());
        }

        if (confirmed) {
            to.get(SCMTransaction).put(SCMActionConfirmed, "true");
        }

        to.get(SCMTransaction).put(SCMTransaction, transactionID);

        event.addPartnerParameter("to", stringify(to));
    }

    public static void injectLeadIntoEvent(AdjustEvent event, String leadID) {
        injectLeadIntoEvent(event, leadID, Boolean.FALSE);
    }

    public static void injectLeadIntoEvent(AdjustEvent event, String leadID, Boolean confirmed) {

        if (null == event) {
            logger.error("Event object is required.");
            return;
        }
        if (null == leadID || "".equals(leadID)) {
            logger.error("Lead ID is required.");
            return;
        }

        Map<String, Map<String, String>> to = new HashMap<>(1);
        to.put(SCMTransaction, new HashMap<String, String>());

        if (confirmed) {
            to.get(SCMTransaction).put(SCMActionConfirmed, "true");
        }

        to.get(SCMTransaction).put(SCMTransaction, leadID);

        event.addPartnerParameter("to", stringify(to));
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // private methods used internally
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private static Map<String, Object> filter(Map<String, Object> parameters, List<String> aliases) {

        Map<String, Object> filtered = new HashMap<>();

        for(Entry<String, Object> entry: parameters.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (!aliases.contains(key)) {
                logger.error("Key must correspond to as Sociomantic alias => ".concat(key));
            }
            else {
                if (key.equals(SCMCategory)) {
                    if (value instanceof String || value instanceof String[]) {
                        filtered.put(key, Arrays.asList(value));
                    } else if (value instanceof List) {
                        filtered.put(key, filterStringArray((List) value));
                    }
                }
                else {
                    filtered.put(key, value);
                }
            }
        }
        return filtered;
    }

    private static List<String> filterStringArray(List list) {

        List<String> filtered = new ArrayList<>();
        for (Object item: list) {
            if (item instanceof String) {
                filtered.add((String) item);
            }
            else {
                logger.error("All values should be strings");
            }
        }
        return filtered;
    }

    private static String stringify(Object o) {
        if (o == null) {
            return "null";
        }

        if (o instanceof String) {
            return "\"".concat((String) o).concat("\"");
        }

        if (o instanceof Integer || o instanceof Long || o instanceof Double) {
            return o.toString();
        }

        if (o instanceof Boolean) {
            return (Boolean) o ? "true" : "false";
        }

        if (o instanceof List) {
            String res = "[";
            List<Object> list = (List) o;
            Iterator<Object> iterator = list.iterator();

            while (iterator.hasNext()) {
                Object item = iterator.next();
                res = res.concat(stringify(item));

                if (iterator.hasNext()) {
                    res = res.concat(",");
                }
            }

            return res.concat("]");
        }

        if (o instanceof Map) {
            String res = "{";
            Map<Object, Object> map = (Map) o;
            Iterator<Entry<Object, Object>> iterator = map.entrySet().iterator();

            while(iterator.hasNext()) {
                Entry<Object, Object> entry = iterator.next();

                res = res.concat(stringify(entry.getKey()))
                        .concat(":")
                        .concat(stringify(entry.getValue()));

                if (iterator.hasNext()) {
                    res = res.concat(",");
                }
            }

            return res.concat("}");
        }

        logger.error("Could not parse the object ".concat(o.toString().concat(" into a JSON string, returned empty string.")));
        return "";

    }
}
