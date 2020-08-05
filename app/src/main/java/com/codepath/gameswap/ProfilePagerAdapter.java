package com.codepath.gameswap;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.codepath.gameswap.fragments.ProfilePostsFragment;

public class ProfilePagerAdapter extends FragmentPagerAdapter {

    public static final String TAG = ProfilePagerAdapter.class.getSimpleName();

    private Context context;
    private ProfilePostsFragment profilePostsFragment;
    private ProfilePostsFragment favoritesFragment;

    public ProfilePagerAdapter(Context context, FragmentManager fragmentManager,
                               ProfilePostsFragment profilePostsFragment, ProfilePostsFragment favoritesFragment) {
        super(fragmentManager);
        this.context = context;
        this.profilePostsFragment = profilePostsFragment;
        this.favoritesFragment = favoritesFragment;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return profilePostsFragment;
        } else {
            return favoritesFragment;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Posts";
            case 1:
                return "Favorites";
            default:
                return null;
        }
    }
}
