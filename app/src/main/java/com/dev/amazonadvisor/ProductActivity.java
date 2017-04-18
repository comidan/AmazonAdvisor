package com.dev.amazonadvisor;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.Utils;

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

    private LineChart mChart;

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
        ViewGroup tab = (ViewGroup) findViewById(R.id.tab);
        tab.addView(LayoutInflater.from(this).inflate(R.layout.tab_indicator, tab, false));
        byte[] productImage = getIntent().getByteArrayExtra("ImageByte");
        ((ImageView)findViewById(R.id.product_image))
                    .setImageDrawable(new BitmapDrawable(getResources(),
                                                         BitmapFactory.decodeByteArray(productImage, 0, productImage.length)));
        ((TextView)findViewById(R.id.product_title)).setText(getIntent().getStringExtra("Title"));
        ((TextView)findViewById(R.id.price)).setText(getIntent().getStringExtra("Price"));
        ((TextView)findViewById(R.id.delivery_date)).setText(getIntent().getStringExtra("Availability"));
        ((TextView)findViewById(R.id.seller)).setText(getIntent().getStringExtra("Seller"));
        ((TextView)findViewById(R.id.price_variation)).setText(getIntent().getStringExtra("PriceDrop"));
        ((TextView)findViewById(R.id.rating)).setText(getIntent().getStringExtra("Rating"));
        mChart = (LineChart) findViewById(R.id.line_chart);
        mChart.setDrawGridBackground(false);
        mChart.getDescription().setEnabled(false);
        mChart.setTouchEnabled(true);
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setPinchZoom(true);
        /*LimitLine llXAxis = new LimitLine(10f, "Time");
        llXAxis.setLineWidth(4f);
        llXAxis.enableDashedLine(10f, 10f, 0f);
        llXAxis.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        llXAxis.setTextSize(10f);*/
        XAxis xAxis = mChart.getXAxis();
        //xAxis.enableGridDashedLine(10f, 10f, 0f);
        LimitLine ll1 = new LimitLine(150f, "Price to notify");
        ll1.setLineWidth(4f);
        ll1.enableDashedLine(10f, 10f, 0f);
        ll1.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        ll1.setTextSize(10f);
        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.removeAllLimitLines();
        leftAxis.addLimitLine(ll1);
        leftAxis.setAxisMaximum(250f);
        leftAxis.setAxisMinimum(125f);
        //leftAxis.enableGridDashedLine(10f, 10f, 0f);
        leftAxis.setDrawZeroLine(false);
        leftAxis.setDrawLimitLinesBehindData(true);
        mChart.getAxisRight().setEnabled(false);
        setData(20, 50);
        //mChart.animateX(1500);
        List<ILineDataSet> sets = mChart.getData().getDataSets();
        for (ILineDataSet iSet : sets) {
            LineDataSet set = (LineDataSet) iSet;
            set.setMode(set.getMode() == LineDataSet.Mode.LINEAR ? LineDataSet.Mode.LINEAR : LineDataSet.Mode.LINEAR);
        }
        mChart.invalidate();
    }

    private void initData() {
        listDataHeader = new ArrayList<>();
        listHash = new HashMap<>();
        listDataHeader.add("Other details");
        List<String> productDetails = new ArrayList<>();
        productDetails.add("Date");
        productDetails.add("Notification");

        listHash.put(listDataHeader.get(0), productDetails);
    }

    private void setData(int count, float range) {

        ArrayList<Entry> values = new ArrayList<Entry>();

        for (int i = 0; i < count; i++) {

            float val = (float) (Math.random() * range) + 153;
            values.add(new Entry(i, val, null));
        }

        LineDataSet set1;

        if (mChart.getData() != null &&
                mChart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet)mChart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
        } else {
            set1 = new LineDataSet(values, "Price");

            set1.setDrawIcons(false);

            //set1.enableDashedLine(10f, 5f, 0f);
            set1.enableDashedHighlightLine(10f, 5f, 0f);
            set1.setColor(Color.rgb(255, 153, 0));
            set1.setCircleColor(Color.rgb(35, 47, 62));
            set1.setLineWidth(2f);
            set1.setCircleRadius(3f);
            set1.setDrawCircleHole(false);
            set1.setValueTextSize(9f);
            set1.setDrawFilled(true);
            set1.setFormLineWidth(1f);
            set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            set1.setFormSize(15.f);

            if (Utils.getSDKInt() >= 18) {
                // fill drawable only supported on api level 18 and above
                Drawable drawable = ContextCompat.getDrawable(this, R.drawable.graph_fade);
                set1.setFillDrawable(drawable);
            }
            else {
                set1.setFillColor(Color.BLACK);
            }

            ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
            dataSets.add(set1);

            LineData data = new LineData(dataSets);

            mChart.setData(data);
        }
    }
}
