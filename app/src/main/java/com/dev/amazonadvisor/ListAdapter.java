package com.dev.amazonadvisor;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
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
        final String discountInPercent = String.format( "%.2f", dataset.get(position).discount) + " %";
        double priceChange = dataset.get(position).priceIncrement;
        final String priceChangeAbs = priceChange >= 0 ? "+ " + dataset.get(position).currency + " " + priceChange : "- " + dataset.get(position).currency + " " + priceChange*-1;
        if(priceChange >= 0)
            ((ImageView)holder.layout.findViewById(R.id.trend)).setImageResource(R.drawable.price_down);
        else
            ((ImageView)holder.layout.findViewById(R.id.trend)).setImageResource(R.drawable.price_up);
        ((TextView)holder.layout.findViewById(R.id.discount)).setText(priceChangeAbs + " " + discountInPercent);
        holder.layout.findViewById(R.id.card_view).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, ProductActivity.class);

                View sharedView = holder.layout.findViewById(R.id.card_view);
                String transitionName = activity.getString(R.string.product_transition);
                intent.putExtra("Title", dataset.get(position).title);
                intent.putExtra("Description", dataset.get(position).description);
                intent.putExtra("Price", dataset.get(position).price);
                intent.putExtra("Discount", discountInPercent);
                intent.putExtra("PriceDrop", priceChangeAbs);
                intent.putExtra("SuggestedPrice", dataset.get(position).suggestedPrice);
                intent.putExtra("Availability", dataset.get(position).availability);
                intent.putExtra("Prime", dataset.get(position).prime);
                intent.putExtra("Seller", dataset.get(position).seller);
                intent.putExtra("Rating", dataset.get(position).rating);
                intent.putExtra("Warranty", dataset.get(position).warranty);
                intent.putExtra("ASIN", dataset.get(position).productId);
                intent.putExtra("URL", dataset.get(position).url);
                Bitmap bitmap = ImageUtils.convertByteArrayToBitmap(dataset.get(position).image);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                intent.putExtra("ImageByte", stream.toByteArray());
                if(Build.VERSION.SDK_INT >=
                        Build.VERSION_CODES.LOLLIPOP) {
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
