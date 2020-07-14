package com.codepath.gameswap.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codepath.gameswap.LoginActivity;
import com.codepath.gameswap.ProfilePostsAdapter;
import com.codepath.gameswap.R;
import com.codepath.gameswap.models.Post;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    public static final String TAG = ProfileFragment.class.getSimpleName();

    private Context context;

    private RecyclerView rvPosts;
    private ImageView ivProfile;
    private TextView tvUsername;
    private Button btnLogout;

    private List<Post> allPosts;
    private LinearLayoutManager layoutManager;
    private ProfilePostsAdapter adapter;

    private ParseUser user;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context = getContext();

        rvPosts = view.findViewById(R.id.rvPosts);
        ivProfile = view.findViewById(R.id.ivProfile);
        tvUsername = view.findViewById(R.id.tvUsername);
        btnLogout = view.findViewById(R.id.btnLogout);

        layoutManager = new LinearLayoutManager(context);
        allPosts = new ArrayList<>();
        adapter = new ProfilePostsAdapter(context, allPosts);
        rvPosts.setAdapter(adapter);
        rvPosts.setLayoutManager(layoutManager);

        Bundle bundle = getArguments();
        if (bundle == null) {
            user = ParseUser.getCurrentUser();
        } else {
            user = bundle.getParcelable(Post.KEY_USER);
            btnLogout.setVisibility(View.GONE);
        }
        tvUsername.setText(user.getUsername());

        queryPosts();

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ParseUser.logOutInBackground(new LogOutCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.e(TAG, "Issue with logout:", e);
                            return;
                        }
                        Intent intent = new Intent(context, LoginActivity.class);
                        startActivity(intent);
                        Activity activity = getActivity();
                        if (activity != null ){
                            activity.finishAffinity();
                        }
                    }
                });
            }
        });
    }

    private void queryPosts() {
        // Specify which class to query
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        // Find all posts
        query.include(Post.KEY_USER);
        query.whereEqualTo(Post.KEY_USER, user);
        query.addDescendingOrder(Post.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }
                adapter.clear();
                adapter.addAll(posts);
                adapter.notifyDataSetChanged();
            }
        });
    }

}