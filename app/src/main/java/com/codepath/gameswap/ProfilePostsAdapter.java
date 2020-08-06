package com.codepath.gameswap;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.codepath.gameswap.models.Post;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

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
            ivFavorite.setOnClickListener(this);

            RelativeLayout.LayoutParams llDetailsLayoutParams = (RelativeLayout.LayoutParams)llDetails.getLayoutParams();
            llDetailsLayoutParams.addRule(RelativeLayout.START_OF, R.id.ivFavorite);
            llDetails.setLayoutParams(llDetailsLayoutParams);

            ViewGroup.LayoutParams cvParams = cvContent.getLayoutParams();
            cvParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            cvParams.height = 320;
            cvContent.setLayoutParams(cvParams);
        }

        @Override
        public void onClick(View view) {
            super.onClick(view);
            if (view == ivFavorite) {
                final ParseRelation<ParseUser> relation = post.getRelation(Post.KEY_FAVORITED_BY);
                ParseQuery<ParseUser> query = relation.getQuery();
                query.findInBackground(new FindCallback<ParseUser>() {
                    @Override
                    public void done(List<ParseUser> users, ParseException e) {
                        if (e != null) {
                            Log.e(TAG, "Error getting favorites");
                            return;
                        }
                        ParseUser currentUser = ParseUser.getCurrentUser();
                        boolean isFavorited = false;
                        for (ParseUser user : users) {
                            if (user.getUsername().equals(currentUser.getUsername())) {
                                isFavorited = true;
                                break;
                            }
                        }
                        if (isFavorited) {
                            ivFavorite.setImageResource(R.drawable.ic_favorite_outline);
                            relation.remove(currentUser);
                            currentUser.getRelation("favorites").remove(post);
                        } else {
                            ivFavorite.setImageResource(R.drawable.ic_favorite_filled);
                            relation.add(currentUser);
                            currentUser.getRelation("favorites").add(post);
                        }
                        post.saveInBackground();
                        currentUser.saveInBackground();
                    }
                });
            }
        }
    }
}
