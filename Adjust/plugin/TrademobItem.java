package com.adjust.sdk.plugin;

public class TrademobItem {
     float price;
     int quantity;
     String itemId;

    public TrademobItem(String itemId, int quantity, float price) {
        this.price = price;
        this.quantity = quantity;
        this.itemId = itemId;
    }
}
