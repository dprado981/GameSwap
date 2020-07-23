package com.codepath.gameswap;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.gameswap.fragments.DetailFragment;
import com.codepath.gameswap.fragments.ProfileFragment;
import com.codepath.gameswap.models.Post;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.List;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder>{

    public static final String TAG = PostsAdapter.class.getSimpleName();

    private final Context context;
    private final List<Post> posts;

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

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private LinearLayout llHeader;
        private ImageView ivProfile;
        private TextView tvUsername;
        private LinearLayout llContent;
        private TextView tvTitle;
        private ImageView ivImage;
        private RatingBar rbCondition;

        private Post post;

        public ViewHolder(@NonNull View view) {
            super(view);

            llHeader = view.findViewById(R.id.llHeader);
            ivProfile = view.findViewById(R.id.ivProfile);
            tvUsername = view.findViewById(R.id.tvUsername);
            llContent = view.findViewById(R.id.llContent);
            tvTitle = view.findViewById(R.id.tvTitle);
            ivImage = view.findViewById(R.id.ivImage);
            rbCondition = view.findViewById(R.id.rbCondition);

            llHeader.setOnClickListener(this);
            llContent.setOnClickListener(this);
        }

        public void bind(Post post) {
            this.post = post;
            ParseUser user = post.getUser();
            tvUsername.setText(user.getUsername());
            tvTitle.setText(post.getTitle());
            rbCondition.setRating((float) post.getCondition() / 10);
            ParseFile image = post.getImageOne();
            if (image != null) {
                Glide.with(context)
                        .load(image.getUrl())
                        .placeholder(R.drawable.ic_image)
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
                FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();
                Fragment fragment = new ProfileFragment();
                Bundle bundle = new Bundle();
                bundle.putParcelable(Post.KEY_USER, post.getUser());
                fragment.setArguments(bundle);
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).addToBackStack(null).commit();
            } else if (view == llContent) {
                FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();
                Fragment fragment = new DetailFragment();
                Bundle bundle = new Bundle();
                bundle.putParcelable(Post.TAG, post);
                fragment.setArguments(bundle);
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).addToBackStack(null).commit();
            }
        }
    }
}
