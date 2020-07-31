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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.codepath.gameswap.EndlessRecyclerViewScrollListener;
import com.codepath.gameswap.PostsAdapter;
import com.codepath.gameswap.R;
import com.codepath.gameswap.SnapOnScrollListener;
import com.codepath.gameswap.models.Post;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class PostsFragment extends Fragment implements OnSnapPositionChangeListener {

    public interface PostsFragmentInterface {
        void onLoadMore();
        void onRefresh();
        void onSnapPositionChange(Post position, int i);
    }

    public static final String TAG = PostsFragment.class.getSimpleName();

    protected Context context;
    protected int lastPosition;
    private PostsFragmentInterface callback;

    protected List<Post> allPosts;
    protected LinearLayoutManager layoutManager;
    protected PostsAdapter adapter;
    protected RecyclerView rvPosts;
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

        layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(RecyclerView.HORIZONTAL);
        allPosts = new ArrayList<>();
        adapter = new PostsAdapter(context, allPosts);
        rvPosts.setAdapter(adapter);
        rvPosts.setLayoutManager(layoutManager);
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(rvPosts);
        SnapOnScrollListener snapOnScrollListener = new SnapOnScrollListener(snapHelper, this);
        rvPosts.addOnScrollListener(snapOnScrollListener);
        setScrollAndRefreshListeners();
        Bundle bundle = getArguments();
        if (bundle != null) {
            lastPosition = bundle.getInt("lastPosition");
            rvPosts.scrollToPosition(lastPosition);
        }
    }

    @Override
    public void onSnapPositionChange(int position) {
        Post post = allPosts.get(position);
        if (post != null) {
            callback.onSnapPositionChange(post, position);
        }
    }

    protected void setScrollAndRefreshListeners() {
        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                if (allPosts.size() >= HomeFragment.MAX_QUERY_SIZE) {
                    callback.onLoadMore();
                }
            }
        };
        // Adds the scroll listener to RecyclerView
        rvPosts.addOnScrollListener(scrollListener);
    }

    public void addPosts(List<Post> posts) {
        adapter.addAll(posts);
    }

    public void clear() {
        adapter.clear();
        scrollListener.resetState();
        if (lastPosition >= 0) {
            rvPosts.smoothScrollToPosition(lastPosition);
            lastPosition = -1;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        lastPosition = layoutManager.findFirstVisibleItemPosition();
    }

    public void smoothScrollTo(int position) {
        rvPosts.smoothScrollToPosition(position);
    }

    public void scrollTo(int position) {
        rvPosts.scrollToPosition(position);
    }
}