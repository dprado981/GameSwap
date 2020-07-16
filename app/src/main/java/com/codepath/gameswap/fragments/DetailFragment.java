package com.codepath.gameswap.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.gameswap.R;
import com.codepath.gameswap.models.Conversation;
import com.codepath.gameswap.models.Post;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class DetailFragment extends Fragment {

    public static final String TAG = DetailFragment.class.getSimpleName();

    private Context context;

    private Post post;

    private Conversation targetConversation;

    public DetailFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context = getContext();

        TextView tvUsername = view.findViewById(R.id.tvUsername);
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        ImageView ivImage = view.findViewById(R.id.ivImage);
        RatingBar rbCondition = view.findViewById(R.id.rbCondition);
        TextView tvNotes = view.findViewById(R.id.tvNotes);
        Button btnMessage = view.findViewById(R.id.btnMessage);

        Bundle bundle = getArguments();
        if (bundle != null) {
            post = bundle.getParcelable(Post.TAG);
            ParseUser user = post.getUser();
            tvUsername.setText(user.getUsername());
            tvTitle.setText(post.getTitle());
            rbCondition.setRating((float) post.getCondition() / 2);
            tvNotes.setText(post.getNotes());
            ParseFile image = post.getImage();
            if (image != null) {
                Glide.with(context)
                        .load(post.getImage().getUrl())
                        .placeholder(R.drawable.ic_image)
                        .into(ivImage);
            }
        }

        btnMessage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                setTargetConversation();
            }
        });

    }

    private void setTargetConversation() {
        // Specify which class to query
        ParseQuery<Conversation> userOneQuery = ParseQuery.getQuery(Conversation.class);
        ParseQuery<Conversation> userTwoQuery = ParseQuery.getQuery(Conversation.class);

        // Find the Conversation that include the current user and the other user
        userOneQuery.whereEqualTo(Conversation.KEY_USERONE, ParseUser.getCurrentUser());
        userOneQuery.whereEqualTo(Conversation.KEY_USERTWO, post.getUser());

        userTwoQuery.whereEqualTo(Conversation.KEY_USERTWO, ParseUser.getCurrentUser());
        userTwoQuery.whereEqualTo(Conversation.KEY_USERONE, post.getUser());

        // Combine queries into a compound query
        List<ParseQuery<Conversation>> queries = new ArrayList<>();
        queries.add(userOneQuery);
        queries.add(userTwoQuery);
        ParseQuery<Conversation> query = ParseQuery.or(queries);

        // Include Users and sort by most recent
        query.include(Conversation.KEY_USERONE);
        query.include(Conversation.KEY_USERTWO);
        query.include(Conversation.KEY_LASTMESSAGE);
        query.addDescendingOrder(Conversation.KEY_UPDATED_AT);

        query.findInBackground(new FindCallback<Conversation>() {
            @Override
            public void done(List<Conversation> conversations, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }
                if (!conversations.isEmpty()) {
                    targetConversation = conversations.get(0);
                    Log.d(TAG, targetConversation.getLastMessage().getText());
                    goToConversationFragment(targetConversation);
                } else {
                    targetConversation = new Conversation();
                    targetConversation.setUserOne(ParseUser.getCurrentUser());
                    targetConversation.setUserTwo(post.getUser());
                    //targetConversation.setLastMessage(null);
                    targetConversation.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            goToConversationFragment(targetConversation);
                        }
                    });
                }

            }
        });
    }

    private void goToConversationFragment(Conversation targetConversation) {
        // Go to conversation fragment
        FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();
        Fragment fragment = new ConversationFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(Conversation.TAG, targetConversation);
        fragment.setArguments(bundle);
        fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).addToBackStack(null).commit();
    }

}