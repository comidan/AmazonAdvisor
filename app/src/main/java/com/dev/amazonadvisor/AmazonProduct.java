package com.dev.amazonadvisor;

import com.orm.SugarRecord;

/**
 * Created by daniele on 28/03/2017.
 */

public class AmazonProduct extends SugarRecord{

    String title, description, price, productId, seller, availability, priceDrop;
    byte[] image;
    boolean prime;

    public AmazonProduct()
    {

    }

    public AmazonProduct(String productId, String title, String description, String price, String seller, String availability, String priceDrop, boolean prime, byte[] image)
    {
        this.productId = productId;
        this.title = title;
        this.description = description;
        this.price = price;
        this.seller = seller;
        this.availability = availability;
        this.priceDrop = priceDrop;
        this.prime = prime;
        this.image = image;
    }

    public AmazonProduct(String productId, String title, String price, String seller, String availability, String priceDrop, boolean prime, byte[] image)
    {
        this.productId = productId;
        this.title = title;
        this.price = price;
        this.seller = seller;
        this.availability = availability;
        this.priceDrop = priceDrop;
        this.prime = prime;
        this.image = image;
        description = "";
    }
}
