package com.adjust.sdk.plugin;


import com.adjust.sdk.AdjustEvent;
import com.adjust.sdk.AdjustFactory;
import com.adjust.sdk.ILogger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class AdjustTrademob {

    private static int MAX_LISTING_ITEMS_COUNT = 5;

    private static ILogger logger = AdjustFactory.getLogger();

    public static void injectViewItemIntoEvent(AdjustEvent event, String itemId, Map<String, String> metadata) {
        event.addPartnerParameter("tm_item", itemId);
        String jsonMetadata = stringifyMetadata(metadata);
        event.addPartnerParameter("tm_md", jsonMetadata);
    }

    public static void injectViewListingIntoEvent(AdjustEvent event, List<String> itemIds, Map<String, String> metadata) {
        String jsonItems = stringifyItemIds(itemIds);
        event.addPartnerParameter("tm_item", jsonItems);
        String jsonMetadata = stringifyMetadata(metadata);
        event.addPartnerParameter("tm_md", jsonMetadata);
    }

    public static void injectAddToBasketIntoEvent(AdjustEvent event, List<TrademobItem> items,  Map<String, String> metadata) {
        String jsonMetadata = stringifyMetadata(metadata);
        event.addPartnerParameter("tm_md", jsonMetadata);
        String jsonItems = stringifyItems(items);
        event.addPartnerParameter("tm_item", jsonItems);
    }

    public static void injectCheckoutIntoEvent(AdjustEvent event, List<TrademobItem> items, Map<String, String> metadata) {
        String jsonMetadata = stringifyMetadata(metadata);
        event.addPartnerParameter("tm_md", jsonMetadata);
        String jsonItems = stringifyItems(items);
        event.addPartnerParameter("tm_item", jsonItems);
    }

    private static String stringifyItemIds(List<String> itemIds) {
        if (itemIds == null) {
            logger.warn("TM View Listing item ids list is null. Empty ids array will be sent.");
            itemIds = new ArrayList<String>();
        }
        StringBuffer tmViewList = new StringBuffer("[");
        int itemsSize = itemIds.size();
        int i = 0;

        while (i < itemsSize) {
            String itemId = itemIds.get(i);
            String itemString = String.format(Locale.US, "\"%s\"", itemId);
            tmViewList.append(itemString);

            i++;

            if (i == itemsSize ||  i >= MAX_LISTING_ITEMS_COUNT) {
                break;
            }

            tmViewList.append(",");
        }

        tmViewList.append("]");

        return tmViewList.toString();
    }

    private static String stringifyItems(List<TrademobItem> items) {
        if (items == null) {
            logger.warn("TM View Listing item ids list is empty. Empty items will be sent.");
            items = new ArrayList<TrademobItem>();
        }

        StringBuffer itemsStrBuffer = new StringBuffer("[");
        int itemsSize = items.size();

        for (int i = 0; i < itemsSize; ) {
            TrademobItem item = items.get(i);
            String itemString = String.format(Locale.US, "{\"id\":\"%s\",\"price\":%f,\"quantity\":%d}",
                    item.itemId,
                    item.price,
                    item.quantity);
            itemsStrBuffer.append(itemString);

            i++;

            if (i == itemsSize ||  i >= MAX_LISTING_ITEMS_COUNT) {
                break;
            }

            itemsStrBuffer.append(",");
        }

        itemsStrBuffer.append("]");

        return itemsStrBuffer.toString();
    }

    private static String stringifyMetadata(Map<String, String> metadata) {

        if(null == metadata) {
            return "{}";
        }
        String res = "{";

        Iterator<Map.Entry<String, String>> iterator = metadata.entrySet().iterator();

        while(iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            String key = "\"".concat(entry.getKey()).concat("\"");
            String value = "\"".concat(entry.getValue()).concat("\"");
            res = res.concat(key).concat(":").concat(value);

            if (iterator.hasNext()) {
                res = res.concat(",");
            }
        }

        return res.concat("}");
    }
}
