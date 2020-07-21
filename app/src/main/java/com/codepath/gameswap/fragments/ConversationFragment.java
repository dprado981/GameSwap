package com.codepath.gameswap.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.gameswap.EndlessRecyclerViewScrollListener;
import com.codepath.gameswap.MessagesAdapter;
import com.codepath.gameswap.R;
import com.codepath.gameswap.models.Conversation;
import com.codepath.gameswap.models.Message;
import com.codepath.gameswap.models.Post;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.livequery.ParseLiveQueryClient;
import com.parse.livequery.SubscriptionHandling;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class ConversationFragment extends Fragment {

    public static final String TAG = Conversation.class.getSimpleName();

    private Context context;
    private Conversation conversation;
    private ParseUser currentUser;
    private ParseUser otherUser;

    private ImageView ivProfile;
    private TextView tvUsername;

    private RecyclerView rvMessages;
    private List<Message> messages;
    private LinearLayoutManager layoutManager;
    private MessagesAdapter adapter;
    private EndlessRecyclerViewScrollListener scrollListener;

    private EditText etMessage;
    private ImageButton ibSend;

    public ConversationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_conversation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = getContext();

        Bundle bundle = getArguments();
        conversation = bundle.getParcelable(Conversation.TAG);
        currentUser = ParseUser.getCurrentUser();
        otherUser = getOtherUser(conversation);

        rvMessages = view.findViewById(R.id.rvMessages);
        messages = new ArrayList<>();
        layoutManager = new LinearLayoutManager(context);
        adapter = new MessagesAdapter(context, messages);
        rvMessages.setAdapter(adapter);
        layoutManager.setReverseLayout(true);
        rvMessages.setLayoutManager(layoutManager);

        ivProfile = view.findViewById(R.id.ivProfile);
        ParseFile image = (ParseFile) otherUser.get("image");
        if (image != null) {
            Glide.with(context)
                    .load(image.getUrl())
                    .placeholder(R.drawable.ic_profile)
                    .into(ivProfile);
        }

        tvUsername = view.findViewById(R.id.tvUsername);
        tvUsername.setText(otherUser.getUsername());

        etMessage = view.findViewById(R.id.etMessage);
        ibSend = view.findViewById(R.id.ibSend);
        ibSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String messageText = etMessage.getText().toString();
                if (messageText.isEmpty()) {
                    return;
                }
                sendMessage(messageText);
            }
        });

        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                queryOlder();
            }
        };
        // Adds the scroll listener to RecyclerView
        rvMessages.addOnScrollListener(scrollListener);

        queryMessages();
        liveQueryMessages();
    }

    /**
     * Get all of the messages from this conversations
     */
    private void queryMessages() {
        ParseQuery<Message> query = ParseQuery.getQuery(Message.class);
        query.whereEqualTo(Message.KEY_CONVERSATION, conversation);
        query.setLimit(5);
        query.addDescendingOrder(Message.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<Message>() {
            @Override
            public void done(List<Message> messages, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting messages", e);
                    return;
                }
                for (Message m : messages) {
                    Log.d(TAG, "text:" + m.getText());
                }
                adapter.clear();
                scrollListener.resetState();
                adapter.addAll(messages);
                adapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * Updates the view when a new message is updated
     */
    private void liveQueryMessages() {
        ParseLiveQueryClient parseLiveQueryClient = ParseLiveQueryClient.Factory.getClient();
        ParseQuery<Message> query = ParseQuery.getQuery(Message.class);
        query.whereEqualTo(Message.KEY_CONVERSATION, conversation);
        parseLiveQueryClient.subscribe(query).handleEvents(new SubscriptionHandling.HandleEventsCallback<Message>() {
            @Override
            public void onEvents(ParseQuery<Message> query, final SubscriptionHandling.Event event, final Message message) {
                Handler refresh = new Handler(Looper.getMainLooper());
                refresh.post(new Runnable() {
                    public void run() {
                        Message newLastMessage = null;
                        if (event == SubscriptionHandling.Event.CREATE) {
                            messages.add(0, message);
                            newLastMessage = message;
                        } else if (event == SubscriptionHandling.Event.DELETE) {
                            messages.remove(message);
                            newLastMessage = messages.get(0);
                        } else {
                            Log.i(TAG, "not implemented yet");
                        }
                        if (newLastMessage != null) {
                            adapter.notifyDataSetChanged();
                            conversation.setLastMessage(newLastMessage);
                            conversation.saveInBackground();
                        }
                    }
                });
            }
        });
    }

    /**
     * Creates and saves a new message with the text from messageText.
     * Adds it to the users current conversation
     */
    private void sendMessage(String messageText) {
        final Message message = new Message();
        message.setText(messageText);
        message.setFrom(currentUser);
        message.setTo(otherUser);
        message.setConversation(conversation);
        message.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error while sending: ", e);
                    Toast.makeText(context, "Error while sending", Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.d(TAG, "Sent successfully");
                etMessage.setText("");
                conversation.setLastMessage(message);
                conversation.saveInBackground();
            }
        });
    }

    /**
     * Get all of the messages from this conversations
     */
    private void queryOlder() {
        ParseQuery<Message> query = ParseQuery.getQuery(Message.class);
        query.whereEqualTo(Message.KEY_CONVERSATION, conversation);
        Date olderThanDate = messages.get(messages.size()-1).getCreatedAt();
        query.whereLessThan(Post.KEY_CREATED_AT, olderThanDate);
        query.setLimit(5);
        query.addDescendingOrder(Message.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<Message>() {
            @Override
            public void done(List<Message> messages, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting messages", e);
                    return;
                }
                for (Message m : messages) {
                    Log.d(TAG, "text:" + m.getText());
                }
                adapter.addAll(messages);
                adapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * Returns the ParseUser in the conversation that is not currently signed in
     */
    private ParseUser getOtherUser(Conversation conversation) {
        ParseUser userOne = conversation.getUserOne();
        ParseUser userTwo = conversation.getUserTwo();
        if (userOne.getUsername().equals(ParseUser.getCurrentUser().getUsername())) {
            return userTwo;
        } else {
            return userOne;
        }
    }

}