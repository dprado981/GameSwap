package com.codepath.gameswap.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.codepath.gameswap.EndlessRecyclerViewScrollListener;
import com.codepath.gameswap.PostsAdapter;
import com.codepath.gameswap.R;
import com.codepath.gameswap.models.Block;
import com.codepath.gameswap.models.Post;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class PostsFragment extends Fragment {

    public static final String TAG = PostsFragment.class.getSimpleName();

    protected Context context;
    protected int lastPosition;

    protected List<Post> allPosts;
    protected LinearLayoutManager layoutManager;
    protected PostsAdapter adapter;
    protected RecyclerView rvPosts;
    protected SwipeRefreshLayout swipeContainer;
    protected EndlessRecyclerViewScrollListener scrollListener;

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
        swipeContainer = view.findViewById(R.id.swipeContainer);

        layoutManager = new LinearLayoutManager(context);
        allPosts = new ArrayList<>();
        setAdapter();
        rvPosts.setAdapter(adapter);
        rvPosts.setLayoutManager(layoutManager);

        queryPosts(false);
        setHasOptionsMenu(true);

        setScrollAndRefreshListeners();
    }

    protected void setAdapter() {
        adapter = new PostsAdapter(context, allPosts);
    }

    protected void setScrollAndRefreshListeners() {
        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                queryPosts(true);
            }
        };
        // Adds the scroll listener to RecyclerView
        rvPosts.addOnScrollListener(scrollListener);

        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                queryPosts(false);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }


    protected void queryPosts(final boolean loadNext) {
        // Specify which class to query
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        // Find all posts
        query.include(Post.KEY_USER);
        query.setLimit(20);
        query.addDescendingOrder(Post.KEY_CREATED_AT);
        if (loadNext) {
            Date olderThanDate = allPosts.get(allPosts.size()-1).getCreatedAt();
            query.whereLessThan(Post.KEY_CREATED_AT, olderThanDate);
        }
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(final List<Post> posts, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }
                ParseRelation<Block> blockRelation = ParseUser.getCurrentUser().getRelation("blocks");
                ParseQuery<Block> blockQuery = blockRelation.getQuery();
                blockQuery.include(Block.KEY_BLOCKING);
                blockQuery.include(Block.KEY_BLOCKED);
                blockQuery.findInBackground(new FindCallback<Block>() {
                    @Override
                    public void done(List<Block> blocks, ParseException e) {
                        List<Post> notBlocked = new ArrayList<>();
                        for (Post post : posts) {
                            boolean blockedPost = false;
                            for (Block block : blocks) {
                                Log.d(TAG, block.getBlocked().getUsername());
                                if (block.getBlocked().getUsername().equals(post.getUser().getUsername())) {
                                    blockedPost = true;
                                    break;
                                }
                            }
                            if (!blockedPost) {
                                notBlocked.add(post);
                            }
                        }
                        if (!loadNext) {
                            adapter.clear();
                            scrollListener.resetState();
                            swipeContainer.setRefreshing(false);
                        }
                        adapter.addAll(notBlocked);
                        adapter.notifyDataSetChanged();
                        if (lastPosition >= 0) {
                            rvPosts.scrollToPosition(lastPosition);
                            lastPosition = -1;
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search_bar, menu);
        final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setMaxWidth( Integer.MAX_VALUE );
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String queryString) {
                // perform query here
                querySearch(queryString);
                // workaround to avoid issues with some emulators and keyboard devices firing twice if a keyboard enter is used
                // see https://code.google.com/p/android/issues/detail?id=24599
                searchView.clearFocus();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String queryString) {
                querySearch(queryString);
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void querySearch(String queryString) {
        // Specify which class to query
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        // Find all posts
        query.include(Post.KEY_USER);
        query.setLimit(2);
        if (!queryString.isEmpty()) {
            query.whereContains(Post.KEY_TITLE, queryString);
        }
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

    @Override
    public void onPause() {
        super.onPause();
        lastPosition = layoutManager.findFirstVisibleItemPosition();
    }
}