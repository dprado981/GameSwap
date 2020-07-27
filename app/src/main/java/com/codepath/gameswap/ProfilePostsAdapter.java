package com.codepath.gameswap;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.codepath.gameswap.fragments.EditGameFragment;
import com.codepath.gameswap.fragments.EditPuzzleFragment;
import com.codepath.gameswap.models.Post;
import com.parse.DeleteCallback;
import com.parse.ParseException;

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
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            int id = item.getItemId();
            if (id == R.id.actionDelete) {
                post.deleteInBackground(new DeleteCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.e(TAG, "Issue with deleting post", e);
                            return;
                        }
                        Toast.makeText(context, "Post deleted", Toast.LENGTH_SHORT).show();
                        int index = posts.indexOf(post);
                        posts.remove(index);
                        notifyItemRemoved(index);
                    }
                });
                return true;
            } else if (id == R.id.actionEdit) {

                FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();
                Fragment fragment;
                if (post.getType().equals(Post.GAME)) {
                    fragment = new EditGameFragment();
                } else if (post.getType().equals(Post.PUZZLE)) {
                    fragment = new EditPuzzleFragment();
                } else {
                    return false;
                }
                Bundle bundle = new Bundle();
                bundle.putParcelable(Post.TAG, post);
                fragment.setArguments(bundle);
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).addToBackStack(null).commit();
                return true;
            } else {
                return false;
            }
        }
    }
}
