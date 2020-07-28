package com.codepath.gameswap.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.codepath.gameswap.EndlessRecyclerViewScrollListener;
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

    public interface PostsFragmentInterface {
        void onLoadMore();
        void onRefresh();
    }

    public static final String TAG = PostsFragment.class.getSimpleName();

    protected Context context;
    protected int lastPosition;
    private PostsFragmentInterface callback;

    protected List<Post> allPosts;
    protected LinearLayoutManager layoutManager;
    protected PostsAdapter adapter;
    protected RecyclerView rvPosts;
    protected SwipeRefreshLayout swipeContainer;
    protected EndlessRecyclerViewScrollListener scrollListener;


    public PostsFragment(Fragment fragment) {
        callback = (PostsFragmentInterface) fragment;
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
        swipeContainer = view.findViewById(R.id.swipeContainer);

        layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(RecyclerView.HORIZONTAL);
        allPosts = new ArrayList<>();
        adapter = new PostsAdapter(context, allPosts);
        rvPosts.setAdapter(adapter);
        rvPosts.setLayoutManager(layoutManager);

        setScrollAndRefreshListeners();
    }

    protected void setScrollAndRefreshListeners() {
        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                callback.onLoadMore();
            }
        };
        // Adds the scroll listener to RecyclerView
        rvPosts.addOnScrollListener(scrollListener);

        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                callback.onRefresh();
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    public void addPosts(List<Post> posts) {
        adapter.addAll(posts);
    }

    public void clear() {
        adapter.clear();
        scrollListener.resetState();
        swipeContainer.setRefreshing(false);
        if (lastPosition >= 0) {
            rvPosts.scrollToPosition(lastPosition);
            lastPosition = -1;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        lastPosition = layoutManager.findFirstVisibleItemPosition();
    }
}