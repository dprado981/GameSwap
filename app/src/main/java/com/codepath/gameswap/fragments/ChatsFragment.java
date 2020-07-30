package com.codepath.gameswap.fragments;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

import com.codepath.gameswap.ConversationsAdapter;
import com.codepath.gameswap.EndlessRecyclerViewScrollListener;
import com.codepath.gameswap.R;
import com.codepath.gameswap.models.Conversation;
import com.google.android.material.snackbar.Snackbar;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class ChatsFragment extends Fragment {

    public static final String TAG = ChatsFragment.class.getSimpleName();

    private Context context;

    private RecyclerView rvConversations;
    private SwipeRefreshLayout swipeContainer;
    private EndlessRecyclerViewScrollListener scrollListener;

    private List<Conversation> conversations;
    private LinearLayoutManager layoutManager;
    private ConversationsAdapter adapter;

    private Conversation deletedConversation;

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            final int position = viewHolder.getAdapterPosition();
            deletedConversation = conversations.get(position);
            conversations.remove(position);
            adapter.notifyItemRemoved(position);

            final ParseUser userOne = deletedConversation.getUserOne();
            if (userOne.getUsername().equals(ParseUser.getCurrentUser().getUsername())) {
                deletedConversation.setDeletedByOne(true);
            } else {
                deletedConversation.setDeletedByTwo(true);
            }
            deletedConversation.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        Log.e(TAG, "Error while deleting", e);
                    }
                }
            });
            Snackbar.make(rvConversations, "Conversation deleted", Snackbar.LENGTH_SHORT)
                    .setAction("Undo", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            conversations.add(position, deletedConversation);
                            adapter.notifyItemInserted(position);
                            if (userOne.getUsername().equals(ParseUser.getCurrentUser().getUsername())) {
                                deletedConversation.setDeletedByOne(false);
                            } else {
                                deletedConversation.setDeletedByTwo(false);
                            }
                            deletedConversation.saveInBackground();
                        }
                    }).show();
        }

        @Override
        public void onChildDraw (@NotNull Canvas c, @NotNull RecyclerView recyclerView, @NotNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive){
            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addSwipeLeftBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent))
                    .addSwipeLeftActionIcon(R.drawable.ic_delete)
                    .create()
                    .decorate();
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    };

    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = getContext();

        rvConversations = view.findViewById(R.id.rvConversations);
        swipeContainer = view.findViewById(R.id.swipeContainer);


        Toolbar toolbar = view.findViewById(R.id.toolbar);
        final TextView tvTitle = view.findViewById(R.id.tvTitle);
        SearchView searchView = view.findViewById(R.id.searchView);
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    tvTitle.setVisibility(View.GONE);
                } else {
                    tvTitle.setVisibility(View.VISIBLE);
                }
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String searchQuery) {
                queryConversations(false, searchQuery);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                tvTitle.setVisibility(View.GONE);
                return false;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                tvTitle.setVisibility(View.VISIBLE);
                queryConversations(false);
                return false;
            }
        });

        conversations = new ArrayList<>();
        layoutManager = new LinearLayoutManager(context);
        adapter = new ConversationsAdapter(context, conversations);
        rvConversations.setAdapter(adapter);
        rvConversations.setLayoutManager(layoutManager);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(rvConversations);

        queryConversations(false);

        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                queryConversations(true);
            }
        };
        // Adds the scroll listener to RecyclerView
        rvConversations.addOnScrollListener(scrollListener);

        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                queryConversations(false);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    private void queryConversations(final boolean loadNext) {
        queryConversations(loadNext, null);
    }

    private void queryConversations(final boolean loadNext, final String searchString) {
        // Specify which class to query
        ParseQuery<Conversation> userOneQuery = ParseQuery.getQuery(Conversation.class);
        ParseQuery<Conversation> userTwoQuery = ParseQuery.getQuery(Conversation.class);

        // Find all Conversations that include the current user
        userOneQuery.whereEqualTo(Conversation.KEY_USER_ONE, ParseUser.getCurrentUser());
        userTwoQuery.whereEqualTo(Conversation.KEY_USER_TWO, ParseUser.getCurrentUser());

        // Combine queries into a compound query
        List<ParseQuery<Conversation>> queries = new ArrayList<>();
        queries.add(userOneQuery);
        queries.add(userTwoQuery);
        ParseQuery<Conversation> query = ParseQuery.or(queries);

        // Include Users and sort by most recent
        query.include(Conversation.KEY_USER_ONE);
        query.include(Conversation.KEY_USER_TWO);
        query.include(Conversation.KEY_LAST_MESSAGE);
        query.include(Conversation.KEY_FROM_POST);
        query.setLimit(20);
        query.addDescendingOrder(Conversation.KEY_UPDATED_AT);
        if (loadNext) {
            Date olderThanDate = conversations.get(conversations.size()-1).getCreatedAt();
            query.whereLessThan(Conversation.KEY_UPDATED_AT, olderThanDate);
        }

        query.findInBackground(new FindCallback<Conversation>() {
            @Override
            public void done(List<Conversation> conversations, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting conversations", e);
                    return;
                }
                List<Conversation> relevantConversations = new ArrayList<>();
                for (Conversation conversation : conversations) {
                    String usernameOne = conversation.getUserOne().getUsername();
                    String usernameTwo = conversation.getUserTwo().getUsername();
                    // If the current user hasn't deleted the conversation, add it
                    boolean isUserOne = usernameOne.equals(ParseUser.getCurrentUser().getUsername());
                    if (!((isUserOne && conversation.getDeletedByOne())
                            || (!isUserOne && conversation.getDeletedByTwo()))) {
                        if (searchString != null) {
                            if ((isUserOne && usernameTwo.contains(searchString))
                            || (!isUserOne && usernameOne.contains(searchString))) {
                                relevantConversations.add(conversation);
                            }
                        } else {
                            relevantConversations.add(conversation);
                        }
                    }

                }

                if (!loadNext) {
                    adapter.clear();
                    scrollListener.resetState();
                    swipeContainer.setRefreshing(false);
                }
                adapter.addAll(relevantConversations);
                adapter.notifyDataSetChanged();
            }
        });
    }
}