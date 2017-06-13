package com.dev.amazonadvisor;

/**
 * Created by daniele on 28/03/2017.
 */

public class AmazonProduct
{

    String title, description, price, productId, seller, availability, priceDrop, rating, warranty, url, currency, suggestedPrice;
    byte[] image;
    boolean prime;
    double discount, priceIncrement;

    public AmazonProduct()
    {

    }

    public AmazonProduct(String productId, String title, String description, String price, String seller, String availability, String priceDrop, boolean prime, byte[] image, String rating)
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
        this.rating = rating;
    }

    public AmazonProduct(String productId, String title, String price, String seller, String availability, String priceDrop, boolean prime, byte[] image, String rating, String warranty, String url, String currency, double discount, double priceIncrement, String suggestedPrice)
    {
        this.productId = productId;
        this.title = title;
        this.price = price;
        this.seller = seller;
        this.availability = availability;
        this.priceDrop = priceDrop;
        this.prime = prime;
        this.image = image;
        this.rating = rating;
        this.warranty =  warranty;
        this.url = url;
        description = "";
        this.currency = currency;
        this.discount = discount;
        this.priceIncrement = priceIncrement;
        this.suggestedPrice = suggestedPrice;
    }
}
