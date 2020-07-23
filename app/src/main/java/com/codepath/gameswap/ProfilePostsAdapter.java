package com.codepath.gameswap;

import android.content.Context;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.gameswap.fragments.DetailFragment;
import com.codepath.gameswap.fragments.EditFragment;
import com.codepath.gameswap.models.Post;
import com.parse.DeleteCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.List;

public class ProfilePostsAdapter extends RecyclerView.Adapter<ProfilePostsAdapter.ViewHolder>{

    public static final String TAG = ProfilePostsAdapter.class.getSimpleName();

    private final Context context;
    private final List<Post> posts;

    public ProfilePostsAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view  = LayoutInflater.from(context).inflate(R.layout.item_profile_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public void clear() {
        posts.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Post> list) {
        posts.addAll(list);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, PopupMenu.OnMenuItemClickListener{

        private LinearLayout llContent;
        private TextView tvTitle;
        private ImageButton ibMore;
        private ImageView ivImage;
        private RatingBar rbCondition;

        private Post post;

        public ViewHolder(@NonNull View view) {
            super(view);

            llContent = view.findViewById(R.id.llContent);
            tvTitle = view.findViewById(R.id.tvTitle);
            ibMore = view.findViewById(R.id.ibMore);
            ivImage = view.findViewById(R.id.ivImage);
            rbCondition = view.findViewById(R.id.rbCondition);

            llContent.setOnClickListener(this);
            ibMore.setOnClickListener(this);
        }

        public void bind(Post post) {
            this.post = post;
            ParseUser user = post.getUser();
            if (!user.getUsername().equals(ParseUser.getCurrentUser().getUsername())) {
                ibMore.setVisibility(View.GONE);
            }
            tvTitle.setText(post.getTitle());
            rbCondition.setRating((float) post.getCondition() / 10);
            ParseFile image = post.getImage();
            if (image != null) {
                Glide.with(context)
                        .load(post.getImage().getUrl())
                        .placeholder(R.drawable.ic_image)
                        .into(ivImage);
            }
        }

        @Override
        public void onClick(View view) {
            if (view == llContent) {
                FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();
                Fragment fragment = new DetailFragment();
                Bundle bundle = new Bundle();
                bundle.putParcelable(Post.TAG, post);
                fragment.setArguments(bundle);
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).addToBackStack(null).commit();
            } else if (view == ibMore) {
                PopupMenu popup = new PopupMenu(context, view);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.menu_post_options, popup.getMenu());
                popup.setOnMenuItemClickListener(this);
                popup.show();
            }
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
                Fragment fragment = new EditFragment();
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
