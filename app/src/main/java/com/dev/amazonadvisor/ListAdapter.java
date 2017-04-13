package com.dev.amazonadvisor;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    private ArrayList<AmazonProduct> dataset;
    private Activity activity;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout layout;
        public ViewHolder(LinearLayout layout) {
            super(layout);
            this.layout = layout;
        }
    }

    public ListAdapter(ArrayList<AmazonProduct> dataset, Activity activity) {
        this.dataset = dataset;
        this.activity =  activity;
    }

    @Override
    public ListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout layout = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.product_card, parent, false);
        ViewHolder vh = new ViewHolder(layout);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        ((ImageView)holder.layout.findViewById(R.id.product_image)).setImageDrawable(new BitmapDrawable(activity.getResources(),
                                                                                                        ImageUtils.convertByteArrayToBitmap(
                                                                                                        dataset.get(position).image)));
        ((TextView)holder.layout.findViewById(R.id.product_title)).setText(dataset.get(position).title);
        //((TextView)holder.layout.findViewById(R.id.product_description)).setText(dataset.get(position).description);
        ((TextView)holder.layout.findViewById(R.id.product_price)).setText(dataset.get(position).price);
        ((CardView)holder.layout.findViewById(R.id.card_view)).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, ProductActivity.class);

                View sharedView = ((CardView)holder.layout.findViewById(R.id.card_view));
                String transitionName = activity.getString(R.string.product_transition);
                intent.putExtra("Title", dataset.get(position).title);
                intent.putExtra("Description", dataset.get(position).description);
                intent.putExtra("Price", dataset.get(position).price);
                Bitmap bitmap = ImageUtils.convertByteArrayToBitmap(dataset.get(position).image);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                intent.putExtra("ImageByte", stream.toByteArray());
                if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions transitionActivityOptions = ActivityOptions.makeSceneTransitionAnimation(activity,
                            sharedView,
                            transitionName);
                    activity.startActivity(intent, transitionActivityOptions.toBundle());
                }
                else
                    activity.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }

    public ArrayList<AmazonProduct> getDataset()
    {
        return dataset;
    }
}
