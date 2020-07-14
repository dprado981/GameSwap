package com.codepath.gameswap.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codepath.gameswap.PostsAdapter;
import com.codepath.gameswap.R;
import com.codepath.gameswap.models.Post;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class PostsFragment extends Fragment {

    private Context context;

    private List<Post> allPosts;
    private LinearLayoutManager layoutManager;
    protected PostsAdapter adapter;
    protected RecyclerView rvPosts;

    public PostsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_posts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context = getContext();

        rvPosts = view.findViewById(R.id.rvPosts);

        layoutManager = new LinearLayoutManager(context);
        allPosts = new ArrayList<>();

        adapter = new PostsAdapter(context, allPosts);

        rvPosts.setAdapter(adapter);
        rvPosts.setLayoutManager(layoutManager);

        List<Post> fakeData = new ArrayList<>();
        for (int i = 0; i < 400; i++) {
            fakeData.add(new Post());
        }
        adapter.addAll(fakeData);
    }

}