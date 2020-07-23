package com.codepath.gameswap;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ImagePagerAdapter extends PagerAdapter {

    private Context context;
    private List<Bitmap> bitmaps;

    public ImagePagerAdapter(Context context, List<Bitmap> bitmaps) {
        this.context = context;
        this.bitmaps = bitmaps;
    }

    public void clear() {
        bitmaps.clear();
        notifyDataSetChanged();
    }

    public void add(Bitmap newBitmap) {
        bitmaps.add(newBitmap);
        notifyDataSetChanged();
    }

    public void addAll(List<Bitmap> newBitmaps) {
        bitmaps.addAll(newBitmaps);
        notifyDataSetChanged();
    }

    @Override
    public int getItemPosition(@NotNull Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return bitmaps.size();
    }

    @Override
    public boolean isViewFromObject(@NotNull View view, @NotNull Object object) {
        return view == object;
    }

    @NotNull
    @Override
    public Object instantiateItem(@NotNull ViewGroup container, int position) {
        ImageView imageView = new ImageView(context);
        int padding = 4;
        imageView.setPadding(padding, padding, padding, padding);
        imageView.setImageBitmap(bitmaps.get(position));
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        container.addView(imageView, 0);
        return imageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, @NotNull Object object) {
        container.removeView((ImageView) object);
    }
}
