package com.dev.amazonadvisor;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.orm.SugarRecord;

/**
 * Created by daniele on 28/03/2017.
 */

public class AmazonProduct extends SugarRecord{

    String title, description, price;
    byte[] image;

    public AmazonProduct()
    {

    }

    public AmazonProduct(String title, String description, String price, byte[] image)
    {
        this.title = title;
        this.description = description;
        this.price = price;
        this.image = image;
    }

    public AmazonProduct(String title, String price, byte[] image)
    {
        this.title = title;
        this.price = price;
        this.image = image;

        description = "";
    }
}
