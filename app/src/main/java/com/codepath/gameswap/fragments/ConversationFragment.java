package com.codepath.gameswap.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codepath.gameswap.MessagesAdapter;
import com.codepath.gameswap.R;
import com.codepath.gameswap.models.Conversation;
import com.codepath.gameswap.models.Message;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
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

    private RecyclerView rvMessages;
    private List<Message> messages;
    private LinearLayoutManager layoutManager;
    private MessagesAdapter adapter;

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

        TextView tvUsername = view.findViewById(R.id.tvUsername);
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

        queryMessages();

    }

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
                messages.add(0, message);
                adapter.notifyItemInserted(0);
                conversation.setLastMessage(message);
                conversation.saveInBackground();
            }
        });
    }

    private void queryMessages() {
        // Specify which class to query
        ParseQuery<Message> fromQuery = ParseQuery.getQuery(Message.class);
        ParseQuery<Message> toQuery = ParseQuery.getQuery(Message.class);

        // Find all Messages from current user to other user
        fromQuery.whereEqualTo(Message.KEY_FROM, currentUser);
        fromQuery.whereEqualTo(Message.KEY_TO, otherUser);

        // Find all Messages to current user from other user
        toQuery.whereEqualTo(Message.KEY_TO, currentUser);
        toQuery.whereEqualTo(Message.KEY_FROM, otherUser);

        // Combine queries into a compound query
        List<ParseQuery<Message>> queries = new ArrayList<>();
        queries.add(fromQuery);
        queries.add(toQuery);
        ParseQuery<Message> query = ParseQuery.or(queries);

        // Include Users and sort by most recent
        query.include(Message.KEY_FROM);
        query.include(Message.KEY_TO);
        query.addDescendingOrder(Conversation.KEY_CREATED_AT);

        query.findInBackground(new FindCallback<Message>() {
            @Override
            public void done(List<Message> messages, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting messages", e);
                    return;
                }
                adapter.clear();
                adapter.addAll(messages);
                adapter.notifyDataSetChanged();
            }
        });
    }

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