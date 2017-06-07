package com.dev.amazonadvisor;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionMenu;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by daniele on 28/03/2017.
 */

public class ProductActivity extends AppCompatActivity implements AmazonAWSDetails {
    private FloatingActionMenu menuFab;
    private ImageView chart;
    private String Srange;
    //Variables for graph slide show
    private ViewPager mPager;
    private static int currentPage = 0;
    private static int NUM_PAGES = 0;
    private static Integer[] IMAGES = {R.drawable.one, R.drawable.two, R.drawable.three};
    private ArrayList<Integer> ImagesArray = new ArrayList<Integer>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_activity);

        /*listView = (ExpandableListView) findViewById(R.id.dropDownList);
        initData();
        listAdapter = new ExpandableList(this, listDataHeader, listHash);
        listView.setAdapter(listAdapter);*/

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);
        final ViewGroup tab = (ViewGroup) findViewById(R.id.tab);

        int range = 31; //var range defines how many days are shown in the graph
        Srange = String.valueOf(range);
        TextView priceInterval = (TextView) findViewById(R.id.price_time);
        priceInterval.setText("Graph shows past " + Srange + " days");

        final View rootView = getWindow().getDecorView().getRootView();
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {
                        rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        tab.addView(LayoutInflater.from(ProductActivity.this).inflate(R.layout.tab_indicator, tab, false));
                        byte[] productImage = getIntent().getByteArrayExtra("ImageByte");
                        ((ImageView) findViewById(R.id.product_image))
                                .setImageDrawable(new BitmapDrawable(getResources(),
                                        BitmapFactory.decodeByteArray(productImage, 0, productImage.length)));
                        ((TextView) findViewById(R.id.product_title)).setText(getIntent().getStringExtra("Title"));
                        ((TextView) findViewById(R.id.price)).setText(getIntent().getStringExtra("Price"));
                        ((TextView) findViewById(R.id.delivery_date)).setText(getIntent().getStringExtra("Availability"));
                        ((TextView) findViewById(R.id.seller)).setText(getIntent().getStringExtra("Seller"));
                        ((TextView) findViewById(R.id.price_variation)).setText(getIntent().getStringExtra("PriceDrop"));
                        ((TextView) findViewById(R.id.rating)).setText(getIntent().getStringExtra("Rating"));
                        ((TextView) findViewById(R.id.prime)).setText(getIntent().getBooleanExtra("Prime", false) ? getString(R.string.prime_available) :
                                getString(R.string.prime_not_available));
                        ((TextView) findViewById(R.id.warranty)).setText(getIntent().getStringExtra("Warranty"));
                        menuFab = (FloatingActionMenu) findViewById(R.id.product_menu);
                        menuFab.findViewById(R.id.add_to_cart).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                new AsyncTask<Void, Void, Void>() {
                                    @Override
                                    protected Void doInBackground(Void... voids) {
                                        String cartID = getSharedPreferences("AMAZON_ADVISOR", MODE_PRIVATE).getString("CART_ID", null);
                                        String HMAC = getSharedPreferences("AMAZON_ADVISOR", MODE_PRIVATE).getString("HMAC", null);
                                        AmazonLocaleUtils.setLocale(ProductActivity.this);
                                        if (HMAC == null || cartID == null) {
                                            String cartURL = createCartURL(getIntent().getStringExtra("ASIN"));
                                            String[] data = getCartDetails(cartURL);
                                            cartID = data[0];
                                            HMAC = data[1];
                                        }
                                        addProductToCart(cartID, getIntent().getStringExtra("ASIN"), HMAC);
                                        uploadCartContent(cartID, HMAC, "amazonadvis06-20");
                                        return null;
                                    }

                                    @Override
                                    protected void onPostExecute(Void aVoid) {
                                        super.onPostExecute(aVoid);
                                        final ConstraintLayout mainLayout = (ConstraintLayout) findViewById(R.id.main_constraint_layout);
                                        Snackbar.make(mainLayout, "Successful added to cart", Snackbar.LENGTH_LONG).show();
                                    }
                                }.execute();
                            }
                        });
                        menuFab.findViewById(R.id.share_link).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                shareLinkURL(getIntent().getStringExtra("Title"), getIntent().getStringExtra("URL"));
                            }
                        });
                        menuFab.findViewById(R.id.open_link).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                openURL(getIntent().getStringExtra("URL"));
                            }
                        });
                        new LoadPriceChart().execute();
                    }
                });
    }

    private class LoadPriceChart extends AsyncTask<Void, Integer, ArrayList<Bitmap>> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected ArrayList<Bitmap> doInBackground(Void... voids) {
            ArrayList<Bitmap> imageG = new ArrayList<>();
            String languageDomainCode = AmazonLocaleUtils.getLocalizedCode();
            Integer[] days = {90, 31, 7};
            for (int i : days) {
                Srange = String.valueOf(i);
                imageG.add(ImageUtils.getBitmapFromURL("https://dyn.keepa.com/pricehistory.png?domain=" + languageDomainCode + "&asin=" +
                        getIntent().getStringExtra("ASIN") + "&width=1000&height=500&amazon=1&new=0&used=0&salesrank=0&range=" + Srange));
            }
            return imageG;
        }

        @Override
        protected void onPostExecute(ArrayList<Bitmap> b) {
            mPager = (ViewPager) findViewById(R.id.slider);


            mPager.setAdapter(new sliderGraphAdapter(ProductActivity.this, b));

            // Auto start of viewpager
            final Handler handler = new Handler();
            final Runnable Update = new Runnable() {
                public void run() {
                    mPager.setCurrentItem(currentPage++, true);
                }
            };
        }
    }

    private void shareLinkURL(String title, String url) {
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setType("text/plain");
        share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        share.putExtra(Intent.EXTRA_SUBJECT, title);
        share.putExtra(Intent.EXTRA_TEXT, url);
        startActivity(Intent.createChooser(share, "Share link!"));
    }

    private void openURL(String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://"))
            url = "http://" + url;
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    private void addProductToCart(String cartID, String ASIN, String HMAC) {
        String listAddress = getSharedPreferences("AMAZON_ADVISOR", Activity.MODE_PRIVATE).getString("listAddress", "");
        String temp = listAddress.substring(listAddress.indexOf("wishlist/"));
        temp = temp.replace("wishlist/", "");
        String listID = temp.substring(0, temp.indexOf("/"));
        System.out.println(listID);
        Map<String, String> params = new HashMap<String, String>();
        params.put("Service", "AWSECommerceService");
        params.put("AssociateTag", "amazonadvis06-20");
        params.put("Version", "2009-03-31");
        params.put("CartId", cartID);
        params.put("HMAC", HMAC);
        params.put("Operation", "CartAdd");
        params.put("Item.1.ASIN", ASIN);
        params.put("Item.1.ListItemId", listID);
        params.put("Item.1.Quantity", "1");
        SignedRequestsHelper helper;
        try {
            helper = new SignedRequestsHelper(AmazonLocaleUtils.getLocalizedAWSURL());
            String requestUrl = helper.sign(params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String createCartURL(String ASIN) {
        String listAddress = getSharedPreferences("AMAZON_ADVISOR", Activity.MODE_PRIVATE).getString("listAddress", "");
        String temp = listAddress.substring(listAddress.indexOf("wishlist/"));
        temp = temp.replace("wishlist/", "");
        String listID = temp.substring(0, temp.indexOf("/"));
        Map<String, String> params = new HashMap<String, String>();
        params.put("Service", "AWSECommerceService");
        params.put("AssociateTag", "amazonadvis06-20");
        params.put("Version", "2009-03-31");
        params.put("Operation", "CartCreate");
        params.put("Item.1.ASIN", ASIN);
        params.put("Item.1.ListItemId", listID);
        params.put("Item.1.Quantity", "1");
        SignedRequestsHelper helper;
        try {
            helper = new SignedRequestsHelper(AmazonLocaleUtils.getLocalizedAWSURL());
            return helper.sign(params);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String[] getCartDetails(String requestUrl) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = null;
            int done = 0;
            while (done == 0)
                try {
                    doc = db.parse(requestUrl);
                    done = 1;
                } catch (FileNotFoundException exc) {
                    exc.printStackTrace();
                }
            String[] container = new String[2];
            System.out.println(requestUrl);
            Node cartID = doc.getElementsByTagName("CartId").item(0);
            Node HMAC = doc.getElementsByTagName("HMAC").item(0);
            NodeList response = doc.getElementsByTagName("Request");
            if (response.item(0).getChildNodes().item(0).getTextContent().equals("True")) {
                container[0] = cartID.getTextContent();
                container[1] = HMAC.getTextContent();
                getSharedPreferences("AMAZON_ADVISOR", MODE_PRIVATE).edit().putString("CART_ID", container[0]).apply();
                getSharedPreferences("AMAZON_ADVISOR", MODE_PRIVATE).edit().putString("HMAC", container[1]).apply();
                return container;
            } else
                return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void uploadCartContent(String cartID, String HMAC, String associateID) {
        StringBuilder url_string = new StringBuilder();
        url_string.append("https://" + AmazonLocaleUtils.getLocalizedURL() + "/gp/cart/aws-merge.html?");
        url_string.append("cart-id=" + cartID + "&");
        url_string.append("hmac=" + HMAC + "&");
        url_string.append("associate-id=" + associateID + "&");
        url_string.append("AWSAccessKeyId=" + AWS_ACCESS_KEY_ID);
        openURL(url_string.toString());
    }
}
