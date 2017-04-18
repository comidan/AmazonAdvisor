package com.dev.amazonadvisor;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
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
        final ViewGroup tab = (ViewGroup) findViewById(R.id.tab);

        final View rootView = getWindow().getDecorView().getRootView();
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {
                        rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        tab.addView(LayoutInflater.from(ProductActivity.this).inflate(R.layout.tab_indicator, tab, false));
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
                        ((TextView)findViewById(R.id.prime)).setText(getIntent().getBooleanExtra("Prime", false) ? getString(R.string.prime_available) :
                                getString(R.string.prime_not_available));
                        new LoadPriceChart().execute();
                    }
                });
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

    private class LoadPriceChart extends AsyncTask<Void, Integer, Void>
    {
            private static final int INIT_X_AXIS = 0;
            private static final int INIT_Y_AXIS = 1;
            private static final int SET_RIGHT_AXIS = 2;
            private static final int GET_CHART_DATA = 3;

            private XAxis xAxis;
            private YAxis yAxis;
            private List<ILineDataSet> sets;

            @Override
            protected void onPreExecute() {
                mChart = (LineChart) findViewById(R.id.line_chart);
                mChart.setDrawGridBackground(false);
                mChart.getDescription().setEnabled(false);
                mChart.setTouchEnabled(true);
                mChart.setDragEnabled(true);
                mChart.setScaleEnabled(true);
                mChart.setPinchZoom(true);
            }

            @Override
            protected Void doInBackground(Void... voids) {
                LimitLine ll1 = new LimitLine(150f, "Price to notify");
                ll1.setLineWidth(4f);
                ll1.enableDashedLine(10f, 10f, 0f);
                ll1.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
                ll1.setTextSize(10f);
                publishProgress(INIT_Y_AXIS);
                while(yAxis == null);
                yAxis.removeAllLimitLines();
                yAxis.addLimitLine(ll1);
                yAxis.setAxisMaximum(250f);
                yAxis.setAxisMinimum(125f);
                //leftAxis.enableGridDashedLine(10f, 10f, 0f);
                yAxis.setDrawZeroLine(false);
                yAxis.setDrawLimitLinesBehindData(true);
                setData(20, 50, sets);
                publishProgress(SET_RIGHT_AXIS);
                publishProgress(GET_CHART_DATA);
                while(sets == null);
                //mChart.animateX(1500);
                for (ILineDataSet iSet : sets) {
                    LineDataSet set = (LineDataSet) iSet;
                    set.setMode(set.getMode() == LineDataSet.Mode.LINEAR ? LineDataSet.Mode.LINEAR : LineDataSet.Mode.LINEAR);
                }

                return null;
            }

            @Override
            protected void onProgressUpdate(Integer... progressType) {
                switch(progressType[0])
                {
                    //case INIT_X_AXIS : xAxis = mChart.getXAxis(); break;
                    case INIT_Y_AXIS : yAxis = mChart.getAxisLeft(); break;
                    case SET_RIGHT_AXIS : mChart.getAxisRight().setEnabled(true); break;
                    case GET_CHART_DATA : sets = mChart.getData().getDataSets(); break;
                }
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                mChart.invalidate();
            }
    }

    private void setData(int count, float range, List<ILineDataSet> sets) {

        ArrayList<Entry> values = new ArrayList<Entry>();

        for (int i = 0; i < count; i++) {

            float val = (float) (Math.random() * range) + 153;
            values.add(new Entry(i, val, null));
        }

        LineDataSet set1;
        LineData chartData = null;
        runOnUiThread(new Runnable() {
            private LineData chartData;

            Runnable setChartData(LineData chartData)
            {
                this.chartData = chartData;
                return this;
            }

            @Override
            public void run() {
                chartData = mChart.getData();
            }
        }.setChartData(chartData));

         if (chartData!= null &&
                 chartData.getDataSetCount() > 0) {
                set1 = (LineDataSet)chartData.getDataSetByIndex(0);
                set1.setValues(values);
                chartData.notifyDataChanged();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mChart.notifyDataSetChanged();
                    }
                });

            }
            else
            {
                set1 = new LineDataSet(values, "Price");

                set1.setDrawIcons(false);

                //set1.enableDashedLine(10f, 5f, 0f);
                set1.enableDashedHighlightLine(10f, 5f, 0f);
                set1.setColor(Color.rgb(255, 153, 0));
                set1.setCircleColor(Color.rgb(35, 47, 62));
                set1.setLineWidth(2f);
                set1.setCircleRadius(3f);
                set1.setDrawCircleHole(false);
                set1.setDrawValues(false);
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

                final LineData data = new LineData(dataSets);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mChart.setData(data);
                    }
                });
            }
        }
}
