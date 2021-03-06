package com.codepath.gameswap.fragments;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.codepath.gameswap.ConversationsAdapter;
import com.codepath.gameswap.EndlessRecyclerViewScrollListener;
import com.codepath.gameswap.R;
import com.codepath.gameswap.models.Block;
import com.codepath.gameswap.models.Conversation;
import com.google.android.material.snackbar.Snackbar;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
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
    private FragmentActivity activity;

    private RecyclerView rvConversations;
    private SwipeRefreshLayout swipeContainer;
    private EndlessRecyclerViewScrollListener scrollListener;
    private TextView tvNoConversations;

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
                    .addSwipeLeftBackgroundColor(ContextCompat.getColor(context, R.color.colorDelete))
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
    public void onViewCreated(@NonNull View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = getContext();
        activity = (FragmentActivity) context;

        rvConversations = view.findViewById(R.id.rvConversations);
        swipeContainer = view.findViewById(R.id.swipeContainer);
        tvNoConversations = view.findViewById(R.id.tvNoConversations);

        final TextView tvTitle = view.findViewById(R.id.tvTitle);
        final SearchView searchView = view.findViewById(R.id.searchView);

        int searchIconId = ((LinearLayout)searchView.getChildAt(0)).getChildAt(1).getId();
        ImageView searchIcon = searchView.findViewById(searchIconId);
        searchIcon.setColorFilter(android.R.color.white);

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
                queryConversations(false, searchQuery.trim());
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
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
                R.color.colorAccent,
                R.color.colorPrimary,
                R.color.colorDelete);
    }

    private void queryConversations(final boolean forLoadMore) { queryConversations(forLoadMore, null); }

    private void queryConversations(final boolean forLoadMore, final String searchString) {
        tvNoConversations.setVisibility(View.INVISIBLE);
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
        query.setLimit(HomeFragment.MAX_QUERY_SIZE);
        query.addDescendingOrder(Conversation.KEY_UPDATED_AT);
        if (forLoadMore) {
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
                    String usernameOne = conversation.getUserOne().getUsername().toLowerCase();
                    String usernameTwo = conversation.getUserTwo().getUsername().toLowerCase();
                    String firstNameOne = conversation.getUserOne().getString("firstName");
                    String lastNameOne = conversation.getUserOne().getString("lastName");
                    String firstNameTwo = conversation.getUserOne().getString("firstName");
                    String lastNameTwo = conversation.getUserOne().getString("lastName");
                    String fullNameOne = (firstNameOne + lastNameOne).toLowerCase();
                    String fullNameTwo = (firstNameTwo + lastNameTwo).toLowerCase();
                    // If the current user hasn't deleted the conversation, add it
                    boolean isUserOne = usernameOne.equals(ParseUser.getCurrentUser().getUsername());
                    if (!((isUserOne && conversation.getDeletedByOne())
                            || (!isUserOne && conversation.getDeletedByTwo()))) {
                        if (searchString != null) {
                            String modifiedString = searchString.trim().toLowerCase();
                            if ((isUserOne && usernameTwo.contains(modifiedString))
                            || (!isUserOne && usernameOne.contains(modifiedString))
                            || (isUserOne && fullNameTwo.contains(modifiedString))
                            || (!isUserOne && fullNameOne.contains(modifiedString))) {
                                relevantConversations.add(conversation);
                            }
                        } else {
                            relevantConversations.add(conversation);
                        }
                    }
                }

                filterUnblocked(relevantConversations, forLoadMore);
            }
        });
    }

    private void filterUnblocked(final List<Conversation> relevantConversations, final boolean forLoadMore) {
        ParseRelation<Block> blockRelation = ParseUser.getCurrentUser().getRelation("blocks");
        ParseQuery<Block> blockQuery = blockRelation.getQuery();
        blockQuery.include(Block.KEY_USER);
        blockQuery.include(Block.KEY_BLOCKED_BY);
        blockQuery.findInBackground(new FindCallback<Block>() {
            @Override
            public void done(List<Block> blocks, ParseException e) {
                if (!forLoadMore) {
                    adapter.clear();
                    scrollListener.resetState();
                    swipeContainer.setRefreshing(false);
                }
                List<Conversation> newConversations = new ArrayList<>();
                for (Conversation conversation : relevantConversations) {
                    boolean blockedConversation = false;
                    for (Block block : blocks) {
                        String usernameOne = conversation.getUserOne().getUsername();
                        String usernameTwo = conversation.getUserTwo().getUsername();
                        boolean isUserOne = usernameOne.equals(ParseUser.getCurrentUser().getUsername());
                        if ((isUserOne && block.getBlockedBy().getUsername().equals(usernameTwo))
                                || (!isUserOne && block.getBlockedBy().getUsername().equals(usernameOne))) {
                            blockedConversation = true;
                            break;
                        }
                    }
                    if (!blockedConversation) {
                        newConversations.add(conversation);
                    }
                }
                adapter.addAll(newConversations);
                adapter.notifyDataSetChanged();
                if (adapter.isEmpty()) {
                    tvNoConversations.setVisibility(View.VISIBLE);
                }
            }
        });

    }
}