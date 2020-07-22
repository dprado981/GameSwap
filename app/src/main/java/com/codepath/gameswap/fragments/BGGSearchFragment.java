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

import com.codepath.gameswap.BGGGameAdapter;
import com.codepath.gameswap.EndlessRecyclerViewScrollListener;
import com.codepath.gameswap.PostsAdapter;
import com.codepath.gameswap.R;
import com.codepath.gameswap.models.BGGGame;
import com.codepath.gameswap.models.Post;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class BGGSearchFragment extends Fragment {

    public static final String TAG = PostsFragment.class.getSimpleName();

    private Context context;

    private List<BGGGame> games;
    private LinearLayoutManager layoutManager;
    private BGGGameAdapter adapter;
    private RecyclerView rvResults;
    private SwipeRefreshLayout swipeContainer;
    private EndlessRecyclerViewScrollListener scrollListener;

    public BGGSearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bgg_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context = getContext();

        rvResults = view.findViewById(R.id.rvResults);
        swipeContainer = view.findViewById(R.id.swipeContainer);

        layoutManager = new LinearLayoutManager(context);
        games = new ArrayList<>();

        adapter = new BGGGameAdapter(context, games);

        rvResults.setAdapter(adapter);
        rvResults.setLayoutManager(layoutManager);

        for (int i = 0; i < 40; i++) {
            BGGGame game = new BGGGame("title");
            games.add(game);
        }
        adapter.notifyDataSetChanged();

        setHasOptionsMenu(true);
/*
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
                android.R.color.holo_red_light);*/
    }

    /*private void queryPosts(final boolean loadNext) {
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
            public void done(List<Post> posts, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }
                if (!loadNext) {
                    adapter.clear();
                    scrollListener.resetState();
                    swipeContainer.setRefreshing(false);
                }
                adapter.addAll(posts);
                adapter.notifyDataSetChanged();
            }
        });
    }*/

    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search_bar, menu);
        final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setIconified(false);
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
                //querySearch(queryString);
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void querySearch(String queryString) {
        Log.d(TAG, "Searching for " + queryString);
    }

}