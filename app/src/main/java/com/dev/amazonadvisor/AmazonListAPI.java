package com.dev.amazonadvisor;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by daniele on 14/06/2017.
 */

public class AmazonListAPI {

    public static String fetchListLink(Context context) throws IOException
    {
        String listAddress = "";
        URL url = new URL("https://" + AmazonLocaleUtils.getLocalizedURL() + "/gp/registry/search");
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("sortby", "");
        params.put("index", "it-xml-wishlist");
        params.put("field-name", context.getSharedPreferences("ACCOUNT_INFO", Context.MODE_PRIVATE)
                .getString("EMAIL", ""));
        params.put("field-firstname", "");
        params.put("field-lastname", "");
        params.put("nameOrEmail", "");
        params.put("submit.search", "");
        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String, Object> param : params.entrySet()) {
            if (postData.length() != 0) postData.append('&');
            postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
            postData.append('=');
            postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
        }
        byte[] postDataBytes = postData.toString().getBytes("UTF-8");

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
        conn.setDoOutput(true);
        conn.getOutputStream().write(postDataBytes);

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null)
            result.append(line);
        System.out.println(result);

        org.jsoup.nodes.Document doc = Jsoup.parseBodyFragment(result.toString());
        Element body = doc.body();
        Elements found = body.getElementsByClass("a-box-inner a-padding-small");
        if (found.size() == 0)
            found = body.getElementsByClass("a-expander-content a-expander-section-content a-section-expander-inner");
        for (int i = 0; i < found.size(); i++) {
            Elements links = found.get(i).getElementsByClass("a-link-normal a-declarative");
            String title = links.get(0).attr("title").toString();
            System.out.println(title);
            if (title.contains("Advisor")) {
                listAddress = links.get(0).attr("href").toString();
                break;
            }
        }
        System.out.println("ListAddress :" + listAddress + "\n\n\n");
        context.getSharedPreferences("LIST_DATA", Activity.MODE_PRIVATE).edit().putString("LIST_LINK", listAddress).apply();
        reader.close();
        return listAddress;
    }

    public static BufferedReader initConnection(String listAddress) throws IOException
    {
        URL url = new URL("https://" + AmazonLocaleUtils.getLocalizedURL() + listAddress);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        return new BufferedReader(new InputStreamReader(conn.getInputStream()));
    }

    public static ArrayList<String> fetchListProductsASIN(BufferedReader reader) throws IOException
    {
        StringBuilder result = new StringBuilder();
        String line;
        ArrayList<String> productIDs = new ArrayList<>();
        while((line = reader.readLine()) != null)
            result.append(line);
        org.jsoup.nodes.Document doc = Jsoup.parseBodyFragment(result.toString());
        Element body = doc.body();
        Elements found = body.getElementsByClass("a-text-center a-fixed-left-grid-col g-itemImage a-col-left");
        System.out.println(found.size());
        for(int i = 0; i < found.size(); i++)
        {
            Elements links = found.get(i).getElementsByClass("a-link-normal a-declarative");
            try {
                String href = links.get(0).attr("href").toString();
                href = href.replace("/dp/", "");
                String temp = href.substring(0, href.indexOf("/"));
                Log.v("ASIN", temp);
                productIDs.add(temp);
            }
            catch (Exception exc) //general excpetion for product code not found, e.g. product removed by vendor from amazon
            {
                exc.printStackTrace();
            }
        }
        return productIDs;
    }

    public static void hiddenUpdateListContent(ArrayList<String> productIDs, Context context)
    {
        DatabaseHandler databaseHandler = new DatabaseHandler(context);
        for(int i = 0; i < productIDs.size(); i++)
        {

            SignedRequestsHelper helper;
            try {
                String ENDPOINT = AmazonLocaleUtils.getLocalizedAWSURL();
                helper = new SignedRequestsHelper(ENDPOINT);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }

            String requestUrl = null;

            Map<String, String> params = new HashMap<String, String>();
            params.put("Service", "AWSECommerceService");
            params.put("AssociateTag", "amazonadvis06-20");
            params.put("Version", "2009-03-31");
            params.put("Operation", "ItemLookup");
            params.put("ItemId", productIDs.get(i));
            params.put("ResponseGroup", "Large");
            requestUrl = helper.sign(params);

            AmazonProductContainer currentProduct = fetchProductData(new String[]{requestUrl, productIDs.get(i)}, context, databaseHandler);
            AmazonProduct product = new AmazonProduct(productIDs.get(i), currentProduct.title, currentProduct.price,currentProduct.seller,
                    currentProduct.availability, "", currentProduct.prime,
                    ImageUtils.getByteArrayFromURL(currentProduct.mediumImagesURL), currentProduct.rating,
                    currentProduct.warranty, currentProduct.url, currentProduct.currency,
                    currentProduct.discount, currentProduct.priceIncrement, currentProduct.suggestedPrice);
            databaseHandler.addProduct(product);
        }
    }

    private static AmazonProductContainer fetchProductData(String[] requestData, Context context, DatabaseHandler databaseHandler) {

        AmazonProductContainer container = new AmazonProductContainer();

        ArrayList<AmazonProduct> productsBackup = new ArrayList<>(databaseHandler.getAllProducts());
        try
        {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = null;
            int done = 0;
            String requestUrl = requestData[0];
            Log.v("URL_Request", requestUrl.toString());
            while(done == 0)
                try
                {
                    doc = db.parse(requestUrl);
                    done = 1;
                }
                catch (FileNotFoundException exc)
                {
                    System.out.println("Waiting for 1.5 seconds");
                    Thread.sleep(1500);
                    exc.printStackTrace();
                }
            Node title = doc.getElementsByTagName("Title").item(0);
            Node smallImageURL = doc.getElementsByTagName("SmallImage").item(0).getFirstChild();
            Node mediumImageURL = doc.getElementsByTagName("MediumImage").item(0).getFirstChild();
            Node largeImageURL = doc.getElementsByTagName("LargeImage").item(0).getFirstChild();
            Node manufacturer = doc.getElementsByTagName("Manufacturer").item(0);
            Node seller = doc.getElementsByTagName("Publisher").item(0);
            Node model = doc.getElementsByTagName("Model").item(0);
            Node price = doc.getElementsByTagName("Price").item(0).getLastChild();
            Node suggestedPrice = doc.getElementsByTagName("ListPrice").item(0).getLastChild();
            Node itemsAsNew = doc.getElementsByTagName("TotalNew").item(0);
            Node itemsAsUsed = doc.getElementsByTagName("TotalUsed").item(0);
            Node hasPrime = doc.getElementsByTagName("IsEligibleForPrime").item(0);
            Node availability = doc.getElementsByTagName("Availability").item(0);
            Node warranty = doc.getElementsByTagName("Warranty").item(0);
            Node url = doc.getElementsByTagName("DetailPageURL").item(0);
            NodeList features = doc.getElementsByTagName("Feature");
            NodeList itemDimension = doc.getElementsByTagName("ItemDimensions");
            container.title = title.getTextContent();
            container.smallImagesURL = smallImageURL.getTextContent();
            container.mediumImagesURL = mediumImageURL.getTextContent();
            container.largeImageURL = largeImageURL.getTextContent();
            if(manufacturer != null)
                container.manufacturer = manufacturer.getTextContent();
            if(seller != null)
                container.seller = seller.getTextContent();
            if(model != null)
                container.model = model.getTextContent();
            if(price != null)
                container.price = price.getTextContent();
            if(suggestedPrice != null)
            {
                AmazonProduct temp;
                if((temp = searchByASIN(productsBackup, requestData[1])) != null)
                    container.priceIncrement = Double.parseDouble(temp.price.replace(".", "").replace(",",".").replaceAll("[^\\d.]", "")) -
                            Double.parseDouble(container.price.replace(".", "").replace(",",".").replaceAll("[^\\d.]", ""));
                else
                    container.priceIncrement = 0;
                double mainPrice = Double.parseDouble(suggestedPrice.getTextContent().replace(".", "").replace(",",".").replaceAll("[^\\d.]", ""));
                double actualPrice = Double.parseDouble(container.price.replace(".", "").replace(",",".").replaceAll("[^\\d.]", ""));
                container.discount = 100 - actualPrice/mainPrice * 100;
                container.currency = suggestedPrice.getTextContent().subSequence(0, suggestedPrice.getTextContent().indexOf(" ")).toString();
                container.suggestedPrice =  suggestedPrice.getTextContent();
            }
            if(itemsAsNew != null)
                container.itemsAsNew = Integer.parseInt(itemsAsNew.getTextContent());
            if(itemsAsUsed != null)
                container.itemAsUsed = Integer.parseInt(itemsAsUsed.getTextContent());
            if(hasPrime != null)
                container.prime = hasPrime.getTextContent().equals("1");
            if(warranty != null)
                container.warranty = warranty.getTextContent();
            if(url != null)
                container.url = url.getTextContent();
            container.availability = availability != null ? availability.getTextContent() : "";
            for(int i = 0; i < features.getLength(); i++)
                container.features.add(features.item(i).getTextContent());
            if(itemDimension.item(0) != null) //checking if dimensions data is present
                for(int i = 0; i < itemDimension.item(0).getChildNodes().getLength(); i++)
                    container.itemDimension.add(Integer.parseInt(itemDimension.item(0).getChildNodes().item(i).getTextContent()));
            container.rating = getRatingFromASIN(doc.getElementsByTagName("Item").item(0).getFirstChild().getTextContent());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        productsBackup.clear();
        return container;
    }

    private static AmazonProduct searchByASIN(ArrayList<AmazonProduct> products, String ASIN)
    {
        for(int i = 0; i < products.size(); i++)
            if(products.get(i).productId.equals(ASIN))
                return products.get(i);
        return null;
    }

    private static String getRatingFromASIN(String ASIN) throws IOException
    {
        URL url = new URL("https://" + AmazonLocaleUtils.getLocalizedURL() + "/gp/customer-reviews/widgets/average-customer-review/popover/ref=dpx_acr_pop_?contextId=dpx&asin=" + ASIN);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder result = new StringBuilder();
        String line;
        while((line = reader.readLine()) != null)
            result.append(line);
        org.jsoup.nodes.Document doc = Jsoup.parseBodyFragment(result.toString());
        Element body = doc.body();
        return body.getElementsByClass("a-size-base a-color-secondary").html();
    }
}
