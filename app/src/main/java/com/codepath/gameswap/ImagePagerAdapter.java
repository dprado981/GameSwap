package com.codepath.gameswap;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Adapter;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.codepath.gameswap.fragments.DetailFragment;
import com.codepath.gameswap.models.Post;
import com.parse.ParseFile;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ImagePagerAdapter<T> extends PagerAdapter implements View.OnClickListener {

    private Context context;
    private List<T> images;
    private Post post;

    public ImagePagerAdapter(Context context, List<T> images) {
        this.context = context;
        this.images = images;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public void clear() {
        images.clear();
        notifyDataSetChanged();
    }

    public void add(T item) {
        images.add(item);
        notifyDataSetChanged();
    }

    public void addAll(List<T> items) {
        images.addAll(items);
        notifyDataSetChanged();
    }

    @Override
    public int getItemPosition(@NotNull Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public boolean isViewFromObject(@NotNull View view, @NotNull Object object) {
        return view == object;
    }

    @NotNull
    @Override
    public Object instantiateItem(@NotNull final ViewGroup container, int position) {
        ImageView imageView = new ImageView(context);
        int padding = 4;
        imageView.setPadding(padding, padding, padding, padding);
        T image = images.get(position);
        if (image instanceof Bitmap) {
            imageView.setImageBitmap((Bitmap) image);
        } else if (image instanceof ParseFile) {
            Glide.with(context)
                    .load(((ParseFile) image).getUrl())
                    .placeholder(R.drawable.ic_profile)
                    .into(imageView);
        }
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        /*imageView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));*/
        container.addView(imageView, 0);
        imageView.setOnClickListener(this);
        return imageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, @NotNull Object object) {
        container.removeView((ImageView) object);
    }

    @Override
    public void onClick(View view) {
        // Restrict onClick so doesn't work in detail Fragment
        if (post != null) {
            FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();
            Fragment fragment = new DetailFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelable(Post.TAG, post);
            fragment.setArguments(bundle);
            fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).addToBackStack(null).commit();
        }
    }
}
