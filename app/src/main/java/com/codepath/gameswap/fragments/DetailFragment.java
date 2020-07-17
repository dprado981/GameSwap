package com.codepath.gameswap.fragments;

import android.content.Context;
import android.graphics.drawable.Drawable;
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
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
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
public class DetailFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = DetailFragment.class.getSimpleName();

    private Context context;

    private Post post;
    private ParseUser user;

    private Conversation targetConversation;

    private ImageView ivProfile;
    private TextView tvUsername;
    private TextView tvTitle;
    private ImageView ivImage;
    private TextView tvNotesContent;
    private RatingBar rbCondition;
    private RatingBar rbDifficulty;
    private TextView tvAgeRatingValue;
    private Button btnMessage;

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

        ivProfile = view.findViewById(R.id.ivProfile);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvTitle = view.findViewById(R.id.tvTitle);
        ivImage = view.findViewById(R.id.ivImage);
        tvNotesContent = view.findViewById(R.id.tvNotesContent);
        rbCondition = view.findViewById(R.id.rbCondition);
        rbDifficulty = view.findViewById(R.id.rbDifficulty);
        tvAgeRatingValue = view.findViewById(R.id.tvAgeRatingValue);
        btnMessage = view.findViewById(R.id.btnMessage);

        Bundle bundle = getArguments();
        if (bundle == null) {
            return;
        }
        post = bundle.getParcelable(Post.TAG);
        user = post.getUser();
        tvUsername.setText(user.getUsername());
        tvTitle.setText(post.getTitle());
        rbCondition.setRating((float) post.getCondition() / 2);
        rbDifficulty.setRating((float) post.getDifficulty() / 2);
        int ageRating = post.getAgeRating();
        if (ageRating == 0) {
            tvAgeRatingValue.setText(R.string.not_specified);
        } else {
            tvAgeRatingValue.setText(post.getAgeRating() +  "+");
        }
        String notes = post.getNotes();
        if (notes.isEmpty()) {
            tvNotesContent.setText(R.string.not_specified);
        } else {
            tvNotesContent.setText(notes);
        }

        ParseFile image = post.getImage();
        if (image != null) {
            Glide.with(context)
                    .load(post.getImage().getUrl())
                    .placeholder(R.drawable.ic_image)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            Log.e(TAG, "Glide failed to load image");
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            // Make all images square
                            ivImage.getLayoutParams().height = ((View) ivImage.getParent()).getWidth();
                            return false;
                        }
                    })
                    .into(ivImage);

            ParseFile profileImage = (ParseFile) post.getUser().get("image");
            if (profileImage != null) {
                Glide.with(context)
                        .load(profileImage.getUrl())
                        .placeholder(R.drawable.ic_profile)
                        .into(ivProfile);
            }
        }

        ivProfile.setOnClickListener(this);
        tvUsername.setOnClickListener(this);
        btnMessage.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        if (view == btnMessage) {
            setTargetConversation();
        } else if (view == ivProfile || view == tvUsername) {
            FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();
            Fragment fragment = new ProfileFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelable(Post.KEY_USER, user);
            fragment.setArguments(bundle);
            fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).addToBackStack(null).commit();
        }
    }

    private void setTargetConversation() {
        // Specify which class to query
        ParseQuery<Conversation> userOneQuery = ParseQuery.getQuery(Conversation.class);
        ParseQuery<Conversation> userTwoQuery = ParseQuery.getQuery(Conversation.class);

        // Find the Conversation that include the current user and the other user
        userOneQuery.whereEqualTo(Conversation.KEY_USER_ONE, ParseUser.getCurrentUser());
        userOneQuery.whereEqualTo(Conversation.KEY_USER_TWO, post.getUser());

        userTwoQuery.whereEqualTo(Conversation.KEY_USER_TWO, ParseUser.getCurrentUser());
        userTwoQuery.whereEqualTo(Conversation.KEY_USER_ONE, post.getUser());

        // Combine queries into a compound query
        List<ParseQuery<Conversation>> queries = new ArrayList<>();
        queries.add(userOneQuery);
        queries.add(userTwoQuery);
        ParseQuery<Conversation> query = ParseQuery.or(queries);

        // Include Users and sort by most recent
        query.include(Conversation.KEY_USER_ONE);
        query.include(Conversation.KEY_USER_TWO);
        query.include(Conversation.KEY_LAST_MESSAGE);
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
                    goToConversationFragment(targetConversation);
                } else {
                    targetConversation = new Conversation();
                    targetConversation.setUserOne(ParseUser.getCurrentUser());
                    targetConversation.setUserTwo(post.getUser());
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