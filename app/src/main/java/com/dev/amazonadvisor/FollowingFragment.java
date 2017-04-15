package com.dev.amazonadvisor;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
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
import android.widget.EditText;
import android.widget.Toast;

import com.amazon.webservices.awsecommerceservice._2011_08_01.Errors;
import com.amazon.webservices.awsecommerceservice._2011_08_01.ImageSet;
import com.amazon.webservices.awsecommerceservice._2011_08_01.Item;
import com.amazon.webservices.awsecommerceservice._2011_08_01.ItemSearch;
import com.amazon.webservices.awsecommerceservice._2011_08_01.ItemSearchRequest;
import com.amazon.webservices.awsecommerceservice._2011_08_01.ItemSearchResponse;
import com.amazon.webservices.awsecommerceservice._2011_08_01.Items;
import com.amazon.webservices.awsecommerceservice._2011_08_01.client.AWSECommerceServicePortType_SOAPClient;
import com.amazon.webservices.awsecommerceservice._2011_08_01.item.ImageSets;
import com.github.clans.fab.FloatingActionMenu;
import com.leansoft.nano.ws.SOAPServiceCallback;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FollowingFragment extends Fragment {

    private FloatingActionMenu menuFab;
    private Handler uiHandler = new Handler();
    private RecyclerView recyclerView;
    private ListAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View rootView;

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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
                        initiateAmazonService();
                    }
                }
        );
        menuFab.setClosedOnTouchOutside(true);
        menuFab.hideMenuButton(false);
        initiateAmazonService();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
            int delay = 400;
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

        AnimatorSet set = new AnimatorSet();

        ObjectAnimator scaleOutX = ObjectAnimator.ofFloat(menuFab.getMenuIconView(), "scaleX", 1.0f, 0.2f);
        ObjectAnimator scaleOutY = ObjectAnimator.ofFloat(menuFab.getMenuIconView(), "scaleY", 1.0f, 0.2f);

        ObjectAnimator scaleInX = ObjectAnimator.ofFloat(menuFab.getMenuIconView(), "scaleX", 0.2f, 1.0f);
        ObjectAnimator scaleInY = ObjectAnimator.ofFloat(menuFab.getMenuIconView(), "scaleY", 0.2f, 1.0f);

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

        menuFab.setIconToggleAnimatorSet(set);
    }

    private void initiateAmazonService()
    {
        swipeRefreshLayout.setRefreshing(true);
        ArrayList<AmazonProduct> products = new ArrayList<>(AmazonProduct.listAll(AmazonProduct.class, "title"));
        if(products.size() > 0)
        {
            adapter = new ListAdapter(products, getActivity());
            recyclerView.setAdapter(adapter);
            swipeRefreshLayout.setRefreshing(false);
            return;
        }
        new DownloadProductsData().execute();
        /*AWSECommerceServicePortType_SOAPClient client = AWSECommerceClient.getSharedClient();
        client.setDebug(true);
        ItemSearch request = new ItemSearch();
        request.associateTag = "teg"; // seems any tag is ok
        request.shared = new ItemSearchRequest();
        request.shared.searchIndex = "Electronics";
        request.shared.responseGroup = new ArrayList<String>();
        request.shared.responseGroup.add("Images");
        request.shared.responseGroup.add("Small");
        ItemSearchRequest itemSearchRequest = new ItemSearchRequest();
        itemSearchRequest.title = "Intel Core i7-7700K";
        request.request = new ArrayList<ItemSearchRequest>();
        request.request.add(itemSearchRequest);
        AWSECommerceClient.authenticateRequest("ItemSearch");
        client.itemSearch(request, new SOAPServiceCallback<ItemSearchResponse>() {

            @Override
            public void onSuccess(ItemSearchResponse responseObject) {
                if (responseObject.items != null && responseObject.items.size() > 0) {

                    new DownloadProductsData().execute(responseObject.items);

                } else {
                    if (responseObject.operationRequest != null && responseObject.operationRequest.errors != null) {
                        Errors errors = responseObject.operationRequest.errors;
                        if (errors.error != null && errors.error.size() > 0) {
                            com.amazon.webservices.awsecommerceservice._2011_08_01.errors.Error error = errors.error.get(0);
                            Log.v("Error", error.message);
                        } else {

                        }
                    } else {

                    }
                }

            }

            @Override
            public void onFailure(Throwable error, String errorMessage) { // http or parsing error

            }

            @Override
            public void onSOAPFault(Object soapFault) { // soap fault
                com.leansoft.nano.soap11.Fault fault = (com.leansoft.nano.soap11.Fault)soapFault;
                Log.v("Fault", fault.faultstring);
            }

        });*/



    }

    private class DownloadProductsData extends AsyncTask<Void, Void, ArrayList<AmazonProduct>>
    {

        private URL url;
        private HttpURLConnection conn;
        private BufferedReader reader;
        private ArrayList<String> productIDs = new ArrayList<>();
        private Element body;

        @Override
        protected ArrayList<AmazonProduct> doInBackground(Void... lists) {

            try
            {
                url = new URL("https://www.amazon.it/gp/registry/search");
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
                String finalResult = result.substring(result.indexOf("Advisor"), result.substring(result.indexOf("Advisor"))
                        .indexOf("<") + result.indexOf("Advisor"));
                String listAddress = finalResult.substring(finalResult.indexOf("href=\"")).replace("href=\"", "")
                        .replace("\">", "");
                System.out.println(listAddress + "\n\n\n");
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
                Document doc = Jsoup.parseBodyFragment(result.toString());
                body = doc.body();
                Elements elements = body.getElementsByClass("a-spacing-large a-divider-normal");
                for(int i = 0; i < elements.size(); i++)
                    System.out.println("VALUE : " + elements.get(i).id());

                for(int i = 0; i < elements.size(); i++)
                {
                    result = new StringBuilder(result.substring(result.indexOf("<div id=\"item_")));
                    String[] id = result.toString().split("\"");
                    productIDs.add(id[1]);
                    result = new StringBuilder(result.substring(16, result.length()));
                }

                //System.out.println(body.text());

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

            ArrayList<AmazonProduct> products = new ArrayList<>();
            for(int i = 0; i < productIDs.size(); i++)
            {
                Element name = body.getElementById("itemName"+productIDs.get(i).substring(productIDs.get(i).indexOf("_")));
                Element price = body.getElementById("itemPrice"+productIDs.get(i).substring(productIDs.get(i).indexOf("_")));
                Element image = body.getElementById("itemImage"+productIDs.get(i).substring(productIDs.get(i).indexOf("_")));
                String imageContainer = image.html();
                //Log.v("Seller", body.getElementById(productIDs.get(i)).getElementsByClass("itemAvailOfferedBy").html());
                AmazonProduct product =
                        new AmazonProduct(name.html(), price.html(),
                                         ImageUtils.getByteArrayFromURL(imageContainer
                                                   .substring(imageContainer.indexOf("src=\"")).split("\"")[1]));
                product.save();
                products.add(product);
            }
            return products;
        }

        @Override
        protected void onPostExecute(ArrayList<AmazonProduct> amazonProducts) {
            super.onPostExecute(amazonProducts);
            adapter = new ListAdapter(amazonProducts, getActivity());
            recyclerView.setAdapter(adapter);
            swipeRefreshLayout.setRefreshing(false);
        }

        private void initConnection(String listAddress) throws IOException
        {
            url = new URL("https://www.amazon.it" + listAddress);
            conn = (HttpURLConnection)url.openConnection();
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        }
    }
}
