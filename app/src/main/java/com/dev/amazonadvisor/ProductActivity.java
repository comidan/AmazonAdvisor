package com.dev.amazonadvisor;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.ExpandableListAdapter;

import com.github.clans.fab.FloatingActionMenu;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by daniele on 28/03/2017.
 */

public class ProductActivity extends AppCompatActivity {
    private ExpandableListView listView;
    private ExpandableListAdapter listAdapter;
    private List<String> listDataHeader;
    private HashMap<String, List<String>> listHash;
    //private FloatingActionMenu menuFab;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_activity);

        listView = (ExpandableListView) findViewById(R.id.dropDownList);
        initData();
        listAdapter = new ExpandableList(this, listDataHeader, listHash);
        listView.setAdapter(listAdapter);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);
        ViewGroup tab = (ViewGroup) findViewById(R.id.tab);
        tab.addView(LayoutInflater.from(this).inflate(R.layout.tab_indicator, tab, false));
        ((ImageView)findViewById(R.id.product_image)).setImageDrawable(getResources().getDrawable(R.drawable.product_demo));
        ((TextView)findViewById(R.id.product_title)).setText(getIntent().getStringExtra("Title"));
        /*menuFab = (FloatingActionMenu) findViewById(R.id.menu_yellow);
        menuFab.bringToFront();*/ //Should bring view to the top
    }

    private void initData() {
        listDataHeader = new ArrayList<>();
        listHash = new HashMap<>();

        listDataHeader.add("Product details");

        List<String> productDetails = new ArrayList<>();
        productDetails.add("Seller");
        productDetails.add("Prime");
        productDetails.add("Estimated delivery");

        listHash.put(listDataHeader.get(0), productDetails);
    }
}
