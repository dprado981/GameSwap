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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

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
            implements View.OnClickListener {

        protected LinearLayout llHeader;
        protected ImageView ivProfile;
        protected TextView tvName;
        protected RelativeLayout rlContent;
        protected TextView tvTitle;
        protected ImageView ivImage;
        protected RatingBar rbCondition;

        protected Post post;

        public ViewHolder(@NonNull View view) {
            super(view);

            llHeader = view.findViewById(R.id.llHeader);
            ivProfile = view.findViewById(R.id.ivProfile);
            tvName = view.findViewById(R.id.tvName);
            rlContent = view.findViewById(R.id.rlContent);
            tvTitle = view.findViewById(R.id.tvTitle);
            ivImage = view.findViewById(R.id.ivImage);
            rbCondition = view.findViewById(R.id.rbCondition);

            llHeader.setOnClickListener(this);
            rlContent.setOnClickListener(this);
        }

        public void bind(Post post) {
            this.post = post;
            ParseUser user = post.getUser();
            String firstName = user.getString("firstName");
            String lastName = user.getString("lastName");
            if (lastName != null) {
                tvName.setText(String.format("%s %s.", firstName, lastName.charAt(0)));
            } else {
                tvName.setText(String.format("%s", firstName));
            }
            tvTitle.setText(post.getTitle());
            rbCondition.setRating((float) post.getCondition() / 10);
            ParseFile image = post.getImageOne();
            if (image != null) {
                Glide.with(context)
                        .load(image.getUrl())
                        .placeholder(R.drawable.ic_profile)
                        .into(ivImage);
            }
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
            } else if (view == rlContent) {
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
                fragmentManager.beginTransaction()
                        .replace(R.id.flContainer, fragment)
                        .addToBackStack(null).commit();

            }
        }
    }
}
