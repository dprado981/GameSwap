package com.codepath.gameswap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.codepath.gameswap.models.Post;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.parse.ParseFile;

public class CustomWindowAdapter implements GoogleMap.InfoWindowAdapter {

    public static final String TAG = CustomWindowAdapter.class.getSimpleName();
    private LayoutInflater inflater;
    private Context context;

    public CustomWindowAdapter(LayoutInflater inflater, Context context){
        this.inflater = inflater;
        this.context = context;
    }

    // This defines the contents within the info window based on the marker
    @Override
    public View getInfoContents(final Marker marker) {
        // Getting view from the layout file
        @SuppressLint("InflateParams")
        View v = inflater.inflate(R.layout.custom_info_window, null);
        // Populate fields
        TextView title = v.findViewById(R.id.tvTitle);
        title.setText(marker.getTitle());

        Post post = (Post) marker.getTag();
        if (post != null) {
            RatingBar condition = v.findViewById(R.id.rbCondition);
            condition.setRating(post.getCondition() / 10.0f);

            final ImageView ivImage = v.findViewById(R.id.ivImage);
            ParseFile image = post.getImageOne();
            if (image != null) {
                Glide.with(context)
                     .asBitmap()
                     .override(100, 100)
                     .listener(new RequestListener<Bitmap>() {
                         @Override
                         public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                             return false;
                         }

                         @Override
                         public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                             if (!dataSource.equals(DataSource.MEMORY_CACHE)) {
                                 marker.showInfoWindow();
                             }
                             return false;
                         }
                     })
                     .load(image.getUrl()).placeholder(R.drawable.ic_image).into(ivImage);
            }
        }


        // Return info window contents
        return v;
    }

    // This changes the frame of the info window; returning null uses the default frame.
    // This is just the border and arrow surrounding the contents specified above
    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }
}
