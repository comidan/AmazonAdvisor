package com.dev.amazonadvisor;

import android.graphics.drawable.Drawable;

/**
 * Created by daniele on 28/03/2017.
 */

public class AmazonProduct {

    String title, description, price;
    Drawable image;

    public AmazonProduct()
    {

    }

    public AmazonProduct(String title, String description, String price, Drawable image)
    {
        this.title = title;
        this.description = description;
        this.price = price;
        this.image = image;
    }
}
