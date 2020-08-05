package com.codepath.gameswap.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.codepath.gameswap.EndlessRecyclerViewScrollListener;
import com.codepath.gameswap.PostsAdapter;
import com.codepath.gameswap.ProfilePostsAdapter;
import com.codepath.gameswap.R;
import com.codepath.gameswap.models.Conversation;
import com.codepath.gameswap.models.Post;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class ProfilePostsFragment extends Fragment {

    public static final String TAG = ProfilePostsFragment.class.getSimpleName();

    private Context context;
    private FragmentActivity activity;
    private FragmentManager fragmentManager;
    private int lastPosition;
    private boolean forFavorite;

    private ParseUser user;
    private ParseUser currentUser;
    private Conversation targetConversation;

    private List<Post> allPosts;
    private LinearLayoutManager layoutManager;
    private PostsAdapter adapter;
    private RecyclerView rvPosts;
    private SwipeRefreshLayout swipeContainer;
    private EndlessRecyclerViewScrollListener scrollListener;

    public ProfilePostsFragment(boolean forFavorite, ParseUser user) {
        this.forFavorite = forFavorite;
        this.user = user;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile_posts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context = getContext();
        activity = (FragmentActivity) context;
        if (activity != null) {
            fragmentManager = activity.getSupportFragmentManager();
        }
        currentUser = ParseUser.getCurrentUser();

        rvPosts = view.findViewById(R.id.rvPosts);
        swipeContainer = view.findViewById(R.id.swipeContainer);

        layoutManager = new LinearLayoutManager(context);
        allPosts = new ArrayList<>();
        adapter = new ProfilePostsAdapter(context, allPosts);
        rvPosts.setAdapter(adapter);
        rvPosts.setLayoutManager(layoutManager);

        if (forFavorite) {
            queryFavorites(false);
        } else {
            queryPosts(false);
        }

        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                if (forFavorite) {
                    queryFavorites(true);
                } else {
                    queryPosts(true);
                }
            }
        };
        // Adds the scroll listener to RecyclerView
        rvPosts.addOnScrollListener(scrollListener);

        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (forFavorite) {
                    queryFavorites(false);
                } else {
                    queryPosts(false);
                }
            }
        });

        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(
                R.color.colorAccent,
                R.color.colorPrimary,
                R.color.colorDelete);
    }

    private void queryPosts(final boolean loadNext) {
        // Specify which class to query
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        // Find all posts
        query.include(Post.KEY_USER);
        query.setLimit(HomeFragment.MAX_QUERY_SIZE);
        query.whereEqualTo(Post.KEY_USER, user);
        query.addDescendingOrder(Post.KEY_CREATED_AT);
        if (loadNext) {
            Date olderThanDate = allPosts.get(allPosts.size() - 1).getCreatedAt();
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
                if (lastPosition >= 0) {
                    rvPosts.scrollToPosition(lastPosition);
                    lastPosition = -1;
                }
            }
        });
    }

    private void queryFavorites(final boolean loadNext) {
        // Specify which class to query
        ParseRelation<Post> relation = user.getRelation("favorites");
        ParseQuery<Post> query = relation.getQuery();
        // Find all posts
        query.include(Post.KEY_USER);
        query.setLimit(HomeFragment.MAX_QUERY_SIZE);
        if (loadNext) {
            Date olderThanDate = allPosts.get(allPosts.size() - 1).getCreatedAt();
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
                List<Post> newPosts = new ArrayList<>();
                for (Post post : posts) {
                    if (!post.containedIn(allPosts)) {
                        newPosts.add(post);
                    }
                }
                adapter.addAll(newPosts);
                adapter.notifyDataSetChanged();
                if (lastPosition >= 0) {
                    rvPosts.scrollToPosition(lastPosition);
                    lastPosition = -1;
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        lastPosition = layoutManager.findFirstVisibleItemPosition();
    }

}