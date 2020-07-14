package com.codepath.gameswap.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.gameswap.R;
import com.codepath.gameswap.models.Post;
import com.parse.ParseFile;
import com.parse.ParseUser;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class DetailFragment extends Fragment {

    private Context context;

    private Post post;

    public DetailFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context = getContext();

        View itemPost = view.findViewById(R.id.itemPost);
        TextView tvUsername = itemPost.findViewById(R.id.tvUsername);
        TextView tvTitle = itemPost.findViewById(R.id.tvTitle);
        ImageView ivImage = itemPost.findViewById(R.id.ivImage);
        RatingBar rbCondition = itemPost.findViewById(R.id.rbCondition);

        Bundle bundle = getArguments();
        if (bundle != null) {
            post = bundle.getParcelable("post");
            ParseUser user = post.getUser();
            tvUsername.setText(user.getUsername());
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
    }

}