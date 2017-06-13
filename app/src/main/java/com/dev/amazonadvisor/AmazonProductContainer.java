package com.dev.amazonadvisor;

import java.util.ArrayList;

/**
 * Created by daniele on 18/04/2017.
 */

public class AmazonProductContainer {

    String smallImagesURL ,mediumImagesURL, largeImageURL, manufacturer, seller, price, model, title, availability, rating, warranty, url, currency, suggestedPrice;
    ArrayList<String> features = new ArrayList<String>();
    ArrayList<Integer> itemDimension = new ArrayList<Integer>();
    int itemsAsNew,itemAsUsed;
    boolean prime;
    double priceIncrement, discount;

}
