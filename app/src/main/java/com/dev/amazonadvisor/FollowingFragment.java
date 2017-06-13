package com.dev.amazonadvisor;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

import com.github.clans.fab.FloatingActionMenu;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class FollowingFragment extends Fragment {

    private static DatabaseHandler databaseHandler;
    private static ArrayList<AmazonProduct> products;

    private FloatingActionMenu menuFab;
    private Handler uiHandler;
    private RecyclerView recyclerView;
    private ListAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View rootView;
    private boolean alreadyLoaded = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_following, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.my_recycler_view);
        menuFab = (FloatingActionMenu) rootView.findViewById(R.id.menu_yellow);
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swiperefresh);
        return rootView;
    }


    @Override
    public void onViewCreated(View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(products == null)
            products = new ArrayList<>();
        databaseHandler = new DatabaseHandler(getContext());
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(dy <= -15)
                    menuFab.showMenuButton(true);
                else if(dy >= 15)
                    menuFab.hideMenuButton(true);
            }
        });
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int position = viewHolder.getAdapterPosition();
                adapter.getDataset().remove(position);
                adapter.notifyItemRemoved(position);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        initiateAmazonService(true, savedInstanceState);
                    }
                }
        );
        menuFab.setClosedOnTouchOutside(true);
        menuFab.hideMenuButton(false);
        AmazonLocaleUtils.setLocale(getActivity());
        initiateAmazonService(false, savedInstanceState);
        Log.v("Test", "Test");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
            int delay = 400;
            uiHandler = new Handler();
            uiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    menuFab.showMenuButton(true);
                }
            }, delay);
            delay += 150;

            menuFab.setOnMenuButtonClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (menuFab.isOpened()) {
                        //do something
                    }

                    menuFab.toggle(true);
                }
            });

            createCustomAnimation();
    }


    private void createCustomAnimation() {

        final ImageView fabIcon = menuFab.getMenuIconView();

        new AsyncTask<ImageView, Void, AnimatorSet>()
        {
            @Override
            protected AnimatorSet doInBackground(ImageView... imageViews) {
                AnimatorSet set = new AnimatorSet();

                ObjectAnimator scaleOutX = ObjectAnimator.ofFloat(imageViews[0], "scaleX", 1.0f, 0.2f);
                ObjectAnimator scaleOutY = ObjectAnimator.ofFloat(imageViews[0], "scaleY", 1.0f, 0.2f);

                ObjectAnimator scaleInX = ObjectAnimator.ofFloat(imageViews[0], "scaleX", 0.2f, 1.0f);
                ObjectAnimator scaleInY = ObjectAnimator.ofFloat(imageViews[0], "scaleY", 0.2f, 1.0f);

                scaleOutX.setDuration(50);
                scaleOutY.setDuration(50);

                scaleInX.setDuration(150);
                scaleInY.setDuration(150);

                scaleInX.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        Animation rotation = AnimationUtils.loadAnimation(getContext(), R.anim.button_rotation);
                        rotation.setRepeatMode(Animation.RELATIVE_TO_SELF);
                        menuFab.getMenuIconView().startAnimation(rotation);
                        if(menuFab.isOpened())
                            menuFab.getMenuIconView().setRotation(0f);
                        else
                            menuFab.getMenuIconView().setRotation(45f);
                    }
                });

                set.play(scaleOutX).with(scaleOutY);
                set.play(scaleInX).with(scaleInY).after(scaleOutX);
                set.setInterpolator(new OvershootInterpolator(2));


                return set;
            }

            @Override
            protected void onPostExecute(AnimatorSet animatorSet) {
                menuFab.setIconToggleAnimatorSet(animatorSet);
            }
        }.execute(fabIcon);


    }

    private void initiateAmazonService(boolean forced, Bundle savedFragmentInstance)
    {
        swipeRefreshLayout.setRefreshing(true);
        new FetchProductsData(savedFragmentInstance).execute(forced);
    }

    private class FetchProductsData extends AsyncTask<Boolean, Void, ArrayList<AmazonProduct>> implements AmazonAWSDetails
    {

        private final String ENDPOINT = AmazonLocaleUtils.getLocalizedAWSURL();

        private Bundle savedFragmentIstance;
        private URL url;
        private HttpURLConnection conn;
        private BufferedReader reader;
        private ArrayList<String> productIDs = new ArrayList<>();
        private ArrayList<AmazonProduct> productsBackup;

        public FetchProductsData(Bundle savedFragmentIstance)
        {
            super();
            this.savedFragmentIstance = savedFragmentIstance;
        }

        @Override
        protected ArrayList<AmazonProduct> doInBackground(Boolean... values) {

            if((products == null || products.isEmpty()) && savedFragmentIstance != null && alreadyLoaded)
                products = new ArrayList<>(databaseHandler.getAllProducts());
            else
                alreadyLoaded = true;
            if(!values[0] && products.size() > 0)
            {
                adapter = new ListAdapter(products, getActivity());
                return products;
            }
            else if(values[0])
                databaseHandler.erase();
            productsBackup = products;
            try
            {
                url = new URL("https://" + AmazonLocaleUtils.getLocalizedURL() + "/gp/registry/search");
                Map<String,Object> params = new LinkedHashMap<>();
                params.put("sortby", "");
                params.put("index", "it-xml-wishlist");
                params.put("field-name", getActivity().getSharedPreferences("ACCOUNT_INFO", Context.MODE_PRIVATE)
                                                      .getString("EMAIL", ""));
                params.put("field-firstname", "");
                params.put("field-lastname", "");
                params.put("nameOrEmail", "");
                params.put("submit.search", "");
                StringBuilder postData = new StringBuilder();
                for (Map.Entry<String,Object> param : params.entrySet()) {
                    if (postData.length() != 0) postData.append('&');
                    postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                    postData.append('=');
                    postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
                }
                byte[] postDataBytes = postData.toString().getBytes("UTF-8");

                conn = (HttpURLConnection)url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
                conn.setDoOutput(true);
                conn.getOutputStream().write(postDataBytes);

                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while((line = reader.readLine()) != null)
                    result.append(line);
                System.out.println(result);

                org.jsoup.nodes.Document doc = Jsoup.parseBodyFragment(result.toString());
                Element body = doc.body();
                Elements found = body.getElementsByClass("a-box-inner a-padding-small");
                if(found.size() == 0)
                    found = body.getElementsByClass("a-expander-content a-expander-section-content a-section-expander-inner");
                String listAddress = "";
                for(int i = 0; i < found.size(); i++)
                {
                    Elements links = found.get(i).getElementsByClass("a-link-normal a-declarative");
                    String title = links.get(0).attr("title").toString();
                    System.out.println(title);
                    if(title.contains("Advisor"))
                    {
                        listAddress = links.get(0).attr("href").toString();
                        break;
                    }
                }
                System.out.println("ListAddress :" + listAddress + "\n\n\n");
                reader.close();


                boolean done = false;
                while(!done)
                    try
                    {
                        initConnection(listAddress);
                        done = true;
                    }
                    catch(IOException exc)
                    {
                        exc.printStackTrace();
                    }
                result = new StringBuilder();
                while((line = reader.readLine()) != null)
                    result.append(line);
                writeToFile(result.toString(), getContext());
                doc = Jsoup.parseBodyFragment(result.toString());
                body = doc.body();
                found = body.getElementsByClass("a-text-center a-fixed-left-grid-col g-itemImage a-col-left");
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
            }
            catch(MalformedURLException exc)
            {
                exc.printStackTrace();
            }
            catch(UnsupportedEncodingException exc)
            {
                exc.printStackTrace();
            }
            catch(IOException exc)
            {
                exc.printStackTrace();
            }

            products = new ArrayList<>();
            for(int i = 0; i < productIDs.size(); i++)
            {

                SignedRequestsHelper helper;
                try {
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

                AmazonProductContainer currentProduct = fetchProductData(new String[]{requestUrl, productIDs.get(i)});
                AmazonProduct product = new AmazonProduct(productIDs.get(i), currentProduct.title, currentProduct.price,currentProduct.seller,
                                                          currentProduct.availability, "", currentProduct.prime,
                                                          ImageUtils.getByteArrayFromURL(currentProduct.mediumImagesURL), currentProduct.rating,
                                                          currentProduct.warranty, currentProduct.url, currentProduct.currency,
                                                          currentProduct.discount, currentProduct.priceIncrement, currentProduct.suggestedPrice);
                databaseHandler.addProduct(product);
                products.add(product);
                adapter = new ListAdapter(products, getActivity());
                publishProgress();
            }
            adapter = new ListAdapter(products, getActivity());
            return products;
        }

        private void writeToFile(String data,Context context) {
            try {
                /*OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(context.getExternalFilesDir(null).getAbsolutePath()+"/HTML", Context.MODE_PRIVATE));
                outputStreamWriter.write(data);
                outputStreamWriter.close();*/
                File file = new File(context.getExternalFilesDir(null), "HTML");
                FileOutputStream fileOutput = new FileOutputStream(file);
                OutputStreamWriter outputStreamWriter=new OutputStreamWriter(fileOutput);
                outputStreamWriter.write(data);
                outputStreamWriter.flush();
                fileOutput.getFD().sync();
                outputStreamWriter.close();
            }
            catch (IOException exc) {
                exc.printStackTrace();
            }

        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            recyclerView.setAdapter(adapter);
        }

        @Override
        protected void onPostExecute(ArrayList<AmazonProduct> amazonProducts) {
            super.onPostExecute(amazonProducts);
            recyclerView.setAdapter(adapter);
            swipeRefreshLayout.setRefreshing(false);
        }

        private void initConnection(String listAddress) throws IOException
        {
            url = new URL("https://" + AmazonLocaleUtils.getLocalizedURL() + listAddress);
            conn = (HttpURLConnection)url.openConnection();
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        }

        private AmazonProductContainer fetchProductData(String[] requestData) {

            AmazonProductContainer container = new AmazonProductContainer();
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
                if(suggestedPrice != null) {
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

        private AmazonProduct searchByASIN(ArrayList<AmazonProduct> products, String ASIN)
        {
            for(int i = 0; i < products.size(); i++)
                if(products.get(i).productId.equals(ASIN))
                    return products.get(i);
            return null;
        }

        private String getRatingFromASIN(String ASIN) throws IOException
        {
            url = new URL("https://" + AmazonLocaleUtils.getLocalizedURL() + "/gp/customer-reviews/widgets/average-customer-review/popover/ref=dpx_acr_pop_?contextId=dpx&asin=" + ASIN);
            conn = (HttpURLConnection)url.openConnection();
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder result = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null)
                result.append(line);
            org.jsoup.nodes.Document doc = Jsoup.parseBodyFragment(result.toString());
            Element body = doc.body();
            return body.getElementsByClass("a-size-base a-color-secondary").html();
        }
    }
}
