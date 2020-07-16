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
import com.codepath.gameswap.models.Post;
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

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private LinearLayout llContent;
        private TextView tvTitle;
        private ImageView ivImage;
        private RatingBar rbCondition;

        private Post post;

        public ViewHolder(@NonNull View view) {
            super(view);

            llContent = view.findViewById(R.id.llContent);
            tvTitle = view.findViewById(R.id.tvTitle);
            ivImage = view.findViewById(R.id.ivImage);
            rbCondition = view.findViewById(R.id.rbCondition);

            llContent.setOnClickListener(this);
        }

        public void bind(Post post) {
            this.post = post;
            ParseUser user = post.getUser();
            tvTitle.setText(post.getTitle());
            rbCondition.setRating((float) post.getCondition() / 2);
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
            }
        }
    }
}
