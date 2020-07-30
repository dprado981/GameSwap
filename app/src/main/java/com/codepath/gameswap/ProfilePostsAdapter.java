package com.codepath.gameswap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;

import com.codepath.gameswap.models.Post;

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

    public class ViewHolder extends PostsAdapter.ViewHolder implements PopupMenu.OnMenuItemClickListener {

        public ViewHolder(@NonNull View view) {
            super(view);
            llHeader.setVisibility(View.GONE);
            CardView cvContent = view.findViewById(R.id.cvContent);
            ViewGroup.LayoutParams cvParams = cvContent.getLayoutParams();
            cvParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            cvParams.height = 320;
            cvContent.setLayoutParams(cvParams);
        }
    }
}
