package com.dev.amazonadvisor;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by daniele on 28/03/2017.
 */

public class ProductActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);
        ViewGroup tab = (ViewGroup) findViewById(R.id.tab);
        tab.addView(LayoutInflater.from(this).inflate(R.layout.tab_indicator, tab, false));
        ((ImageView)findViewById(R.id.product_image)).setImageDrawable(getResources().getDrawable(R.drawable.product_demo));
        ((TextView)findViewById(R.id.product_title)).setText(getIntent().getStringExtra("Title"));
    }
}
