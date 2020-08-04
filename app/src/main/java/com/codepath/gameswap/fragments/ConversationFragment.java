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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
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

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class ConversationFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = Conversation.class.getSimpleName();

    private Context context;
    private FragmentActivity activity;
    private FragmentManager fragmentManager;
    private Conversation conversation;
    private ParseUser currentUser;
    private ParseUser otherUser;
    private Post fromPost;

    private ImageView ivProfile;
    private TextView tvName;
    private TextView tvUsername;
    private LinearLayout llPost;
    private ImageView ivPost;
    private TextView tvPost;
    private EditText etMessage;
    private ImageButton ibSend;
    private ProgressBar progressBar;

    private RecyclerView rvMessages;
    private List<Message> messages;
    private LinearLayoutManager layoutManager;
    private MessagesAdapter adapter;
    private EndlessRecyclerViewScrollListener scrollListener;

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
        activity = (FragmentActivity) context;
        if (activity != null) {
            fragmentManager = activity.getSupportFragmentManager();
        }

        Bundle bundle = getArguments();
        if (bundle == null) {
            Log.e(TAG, "Error getting conversation");
            Toast.makeText(context, "Error getting conversation", Toast.LENGTH_SHORT).show();
            FragmentManager manager = getFragmentManager();
            if (manager != null) {
                manager.popBackStackImmediate();
            }
            return;
        }
        conversation = bundle.getParcelable(Conversation.TAG);
        currentUser = ParseUser.getCurrentUser();
        otherUser = getOtherUser(conversation);

        rvMessages = view.findViewById(R.id.rvMessages);
        ivProfile = view.findViewById(R.id.ivProfile);
        tvName = view.findViewById(R.id.tvName);
        tvUsername = view.findViewById(R.id.tvUsername);
        llPost = view.findViewById(R.id.llPost);
        ivPost = view.findViewById(R.id.ivPost);
        tvPost = view.findViewById(R.id.tvPost);
        etMessage = view.findViewById(R.id.etMessage);
        ibSend = view.findViewById(R.id.ibSend);
        progressBar = view.findViewById(R.id.progressBar);

        messages = new ArrayList<>();
        layoutManager = new LinearLayoutManager(context);
        adapter = new MessagesAdapter(context, messages);
        rvMessages.setAdapter(adapter);
        layoutManager.setReverseLayout(true);
        rvMessages.setLayoutManager(layoutManager);

        ParseFile profileImage = (ParseFile) otherUser.get("image");
        if (profileImage != null) {
            Glide.with(context)
                    .load(profileImage.getUrl())
                    .placeholder(R.drawable.ic_profile)
                    .into(ivProfile);
        }

        String firstName = otherUser.getString("firstName");
        String lastName = otherUser.getString("lastName");
        if (lastName != null) {
            tvName.setText(String.format("%s %s.", firstName, lastName.charAt(0)));
        } else {
            tvName.setText(String.format("%s", firstName));
        }

        tvUsername.setText(otherUser.getUsername());

        if (conversation.getFromPost() != null) {
            llPost.setVisibility(View.VISIBLE);
            fromPost = conversation.getFromPost();
            ParseFile postImage = fromPost.getImageOne();
            if (postImage != null) {
                Glide.with(context)
                        .load(postImage.getUrl())
                        .placeholder(R.drawable.ic_profile)
                        .into(ivPost);
            }
            llPost.setOnClickListener(this);
        }

        ibSend.setOnClickListener(this);
        ivProfile.setOnClickListener(this);
        tvName.setOnClickListener(this);
        tvUsername.setOnClickListener(this);

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


    @Override
    public void onClick(View view) {
        if (view == ibSend) {
            String messageText = etMessage.getText().toString();
            if (messageText.isEmpty()) {
                return;
            }
            sendMessage(messageText);
        } else if (view == llPost || view == ivPost || view == tvPost) {
            Fragment fragment;
            if (fromPost.getType().equals(Post.GAME)) {
                fragment = new DetailGameFragment();
            } else if (fromPost.getType().equals(Post.PUZZLE)) {
                fragment = new DetailPuzzleFragment();
            } else {
                Toast.makeText(context, "Try again later", Toast.LENGTH_SHORT).show();
                return;
            }
            Bundle bundle = new Bundle();
            bundle.putParcelable(Post.TAG, fromPost);
            fragment.setArguments(bundle);
            fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).addToBackStack(null).commit();
        } else if (view == ivProfile || view == tvName || view == tvUsername) {
            Fragment fragment = new ProfileFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelable(Post.KEY_USER, otherUser);
            fragment.setArguments(bundle);
            fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).addToBackStack(null).commit();
        }
    }

    /**
     * Get all of the messages from this conversations
     */
    private void queryMessages() {
        ParseQuery<Message> query = ParseQuery.getQuery(Message.class);
        query.whereEqualTo(Message.KEY_CONVERSATION, conversation);
        query.setLimit(HomeFragment.MAX_QUERY_SIZE);
        query.addDescendingOrder(Message.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<Message>() {
            @Override
            public void done(List<Message> messages, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting messages", e);
                    return;
                }
                adapter.clear();
                scrollListener.resetState();
                adapter.addAll(messages);
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
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
                            Log.e(TAG, "event not implemented yet");
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
        query.setLimit(15);
        query.addDescendingOrder(Message.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<Message>() {
            @Override
            public void done(List<Message> messages, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting messages", e);
                    return;
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