package com.codepath.gameswap;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
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
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.codepath.gameswap.fragments.DetailGameFragment;
import com.codepath.gameswap.fragments.DetailPuzzleFragment;
import com.codepath.gameswap.fragments.EditGameFragment;
import com.codepath.gameswap.fragments.EditPuzzleFragment;
import com.codepath.gameswap.fragments.ProfileFragment;
import com.codepath.gameswap.models.Post;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.DeleteCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

    public static final String TAG = PostsAdapter.class.getSimpleName();

    protected final Context context;
    protected final List<Post> posts;

    public PostsAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view  = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
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
            implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {

        protected LinearLayout llHeader;
        protected ImageView ivProfile;
        protected TextView tvUsername;
        protected ImageButton ibMore;
        protected LinearLayout llContent;
        protected TextView tvTitle;
        protected ViewPager viewPager;
        protected RatingBar rbCondition;

        protected Post post;
        protected List<ParseFile> images;
        protected ImagePagerAdapter<ParseFile> adapter;

        public ViewHolder(@NonNull View view) {
            super(view);

            llHeader = view.findViewById(R.id.llHeader);
            ivProfile = view.findViewById(R.id.ivProfile);
            tvUsername = view.findViewById(R.id.tvUsername);
            ibMore = view.findViewById(R.id.ibMore);
            llContent = view.findViewById(R.id.llContent);
            tvTitle = view.findViewById(R.id.tvTitle);
            viewPager = view.findViewById(R.id.viewPager);
            rbCondition = view.findViewById(R.id.rbCondition);

            images = new ArrayList<>();
            adapter = new ImagePagerAdapter<>(context, images);
            viewPager.setAdapter(adapter);
            ViewTreeObserver viewTreeObserver = viewPager.getViewTreeObserver();
            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        viewPager.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        ViewGroup.LayoutParams params = viewPager.getLayoutParams();
                        params.height = viewPager.getWidth();
                        viewPager.setLayoutParams(params);
                    }
                });
            }
            llHeader.setOnClickListener(this);
            llContent.setOnClickListener(this);
            ibMore.setOnClickListener(this);
        }

        public void bind(Post post) {
            // Set dimensions of viewPager
            this.post = post;
            ParseUser user = post.getUser();
            tvUsername.setText(user.getUsername());
            tvTitle.setText(post.getTitle());
            rbCondition.setRating((float) post.getCondition() / 10);
            adapter.setPost(post);
            adapter.clear();
            adapter.addAll(post.getImages());
            ParseFile profileImage = (ParseFile) user.get("image");
            if (profileImage != null) {
                Glide.with(context)
                        .load(profileImage.getUrl())
                        .placeholder(R.drawable.ic_profile)
                        .into(ivProfile);
            }
        }

        @Override
        public void onClick(View view) {
            if (view == llHeader) {
                FragmentActivity activity = (FragmentActivity) context;
                if (post.getUser().getUsername().equals(ParseUser.getCurrentUser().getUsername())) {
                    ((BottomNavigationView) activity.findViewById(R.id.bottomNavigation)).setSelectedItemId(R.id.actionProfile);
                }
                FragmentManager fragmentManager = activity.getSupportFragmentManager();
                Fragment fragment = new ProfileFragment();
                Bundle bundle = new Bundle();
                bundle.putParcelable(Post.KEY_USER, post.getUser());
                fragment.setArguments(bundle);
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).addToBackStack(null).commit();
            } else if (view == llContent) {
                FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();
                Fragment fragment;
                if (post.getType().equals(Post.GAME)) {
                    fragment = new DetailGameFragment();
                } else if (post.getType().equals(Post.PUZZLE)) {
                    fragment = new DetailPuzzleFragment();
                } else {
                    Toast.makeText(context, "Try again later", Toast.LENGTH_SHORT).show();
                    return;
                }
                Bundle bundle = new Bundle();
                bundle.putParcelable(Post.TAG, post);
                fragment.setArguments(bundle);
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).addToBackStack(null).commit();
            } else if (view == ibMore) {
                PopupMenu popup = new PopupMenu(context, view);
                MenuInflater inflater = popup.getMenuInflater();
                if (post.getUser().getUsername().equals(ParseUser.getCurrentUser().getUsername())) {
                    inflater.inflate(R.menu.menu_profile_post_options, popup.getMenu());
                } else {
                    inflater.inflate(R.menu.menu_stream_post_options, popup.getMenu());
                }
                popup.setOnMenuItemClickListener(this);
                popup.show();
            }
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            int id = item.getItemId();
            if (id == R.id.actionReport) {
                post.addReportBy(ParseUser.getCurrentUser());
                post.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.e(TAG, "Error sending report", e);
                            Toast.makeText(context, "Error sending report", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else if (id == R.id.actionDelete) {
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
            return false;
        }
    }
}
