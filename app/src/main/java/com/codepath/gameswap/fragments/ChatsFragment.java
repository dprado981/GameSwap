package com.codepath.gameswap.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.codepath.gameswap.ConversationsAdapter;
import com.codepath.gameswap.PostsAdapter;
import com.codepath.gameswap.R;
import com.codepath.gameswap.models.Conversation;
import com.codepath.gameswap.models.Post;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class ChatsFragment extends Fragment {

    public static final String TAG = ChatsFragment.class.getSimpleName();

    private Context context;

    private LinearLayoutManager layoutManager;
    private List<Conversation> conversations;
    private RecyclerView rvConversations;
    private ConversationsAdapter adapter;

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

        conversations = new ArrayList<>();
        rvConversations = view.findViewById(R.id.rvConversations);
        layoutManager = new LinearLayoutManager(context);
        adapter = new ConversationsAdapter(context, conversations);
        rvConversations.setAdapter(adapter);
        rvConversations.setLayoutManager(layoutManager);

        queryConversations(false);
        /*
        List<Conversation> fakeData = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            fakeData.add(new Conversation());
        }
        adapter.addAll(fakeData);*/
    }

    protected void queryConversations(final boolean loadNext) {
        // Specify which class to query
        ParseQuery<Conversation> query = ParseQuery.getQuery(Conversation.class);
        // Find all posts
        query.include(Conversation.KEY_USERONE);
        query.include(Conversation.KEY_USERTWO);
        query.addDescendingOrder(Conversation.KEY_UPDATED_AT);
        if (loadNext) {
            Date olderThanDate = conversations.get(conversations.size()-1).getCreatedAt();
            Log.i(TAG, "Loading posts older than " + olderThanDate);
            query.whereLessThan(Post.KEY_CREATED_AT, olderThanDate);
        }
        query.findInBackground(new FindCallback<Conversation>() {
            @Override
            public void done(List<Conversation> conversations, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }
                if (!loadNext) {
                    adapter.clear();
                }
                adapter.addAll(conversations);
                adapter.notifyDataSetChanged();
            }
        });
    }

}