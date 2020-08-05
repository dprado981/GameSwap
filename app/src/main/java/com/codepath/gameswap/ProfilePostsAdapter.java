package com.codepath.gameswap;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.codepath.gameswap.fragments.EditGameFragment;
import com.codepath.gameswap.fragments.EditPuzzleFragment;
import com.codepath.gameswap.models.Post;
import com.parse.DeleteCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ProfilePostsAdapter extends PostsAdapter {

    public static final String TAG = ProfilePostsAdapter.class.getSimpleName();

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view  = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }

    public ProfilePostsAdapter(Context context, List<Post> posts) {
        super(context, posts);
    }

    public class ViewHolder extends PostsAdapter.ViewHolder {

        public ViewHolder(@NonNull View view) {
            super(view);
            CardView cvContent = view.findViewById(R.id.cvContent);
            LinearLayout llDetails = view.findViewById(R.id.llDetails);

            RelativeLayout.LayoutParams ivFavoriteLayoutParams = (RelativeLayout.LayoutParams)ivFavorite.getLayoutParams();
            ivFavoriteLayoutParams.removeRule(RelativeLayout.END_OF);
            ivFavorite.setLayoutParams(ivFavoriteLayoutParams);

            RelativeLayout.LayoutParams llDetailsLayoutParams = (RelativeLayout.LayoutParams)llDetails.getLayoutParams();
            llDetailsLayoutParams.addRule(RelativeLayout.START_OF, R.id.ivFavorite);
            llDetails.setLayoutParams(llDetailsLayoutParams);

            llHeader.setVisibility(View.GONE);
            ViewGroup.LayoutParams cvParams = cvContent.getLayoutParams();
            cvParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            cvParams.height = 320;
            cvContent.setLayoutParams(cvParams);
        }
    }
}
