package com.example.akshit.imagesearch;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private List<String> list = new ArrayList<>();
    static final String TRANSITION_STRING = "image_transition";
    private static final String LOG_TAG = ImageAdapter.class.getSimpleName();

    ImageAdapter(Context c) {
        this.mContext = c;
    }

    public int getCount() {
        return list.size();
    }

    public String getItem(int position) {
        return list.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {

        final ImageView imageView;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        int num_col = prefs.getInt(MainActivity.NUM_COLUMNS_PREF, 2);

        if (convertView == null)
            imageView = new ImageView(mContext);
        else
            imageView = (ImageView) convertView;

        imageView.setLayoutParams(new GridView.LayoutParams(width / num_col, width / num_col));
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setPadding(8, 8, 4, 4);
        imageView.setTransitionName(TRANSITION_STRING);

        final String url = getItem(position);

        Picasso.with(mContext)
                .load(url)
                .placeholder(R.drawable.ic_placeholder)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        //nothing to do
                    }

                    @Override
                    public void onError() {
                        //Try again online if cache failed
                        Picasso.with(mContext)
                                .load(url)
                                .placeholder(R.drawable.ic_placeholder)
                                .error(R.drawable.ic_placeholder)
                                .into(imageView, new Callback() {
                                    @Override
                                    public void onSuccess() {
                                        //nothing to do
                                    }

                                    @Override
                                    public void onError() {
                                        Log.v(LOG_TAG,"Could not fetch image");
                                    }
                                });
                    }
                });

        return imageView;
    }

    void add(String url) {
        list.add(url);
    }

    void clear() {
        list.clear();
    }

}
