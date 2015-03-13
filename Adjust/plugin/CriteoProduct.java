package com.adjust.sdk.plugin;

/**
 * Created by pfms on 24/02/15.
 */
public class CriteoProduct {
    float price;
    int quantity;
    String productID;

    public CriteoProduct(float price, int quantity, String productID) {
        this.price = price;
        this.quantity = quantity;
        this.productID = productID;
    }
}
