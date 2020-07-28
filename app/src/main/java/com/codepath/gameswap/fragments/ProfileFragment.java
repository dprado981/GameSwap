package com.codepath.gameswap.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.codepath.gameswap.EndlessRecyclerViewScrollListener;
import com.codepath.gameswap.LoginActivity;
import com.codepath.gameswap.PostsAdapter;
import com.codepath.gameswap.ProfilePostsAdapter;
import com.codepath.gameswap.R;
import com.codepath.gameswap.models.Block;
import com.codepath.gameswap.models.Conversation;
import com.codepath.gameswap.models.Post;
import com.codepath.gameswap.models.Report;
import com.codepath.gameswap.utils.CameraUtils;
import com.google.android.gms.common.internal.FallbackServiceBroker;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = ProfileFragment.class.getSimpleName();

    private Context context;
    private int lastPosition;

    private ParseUser user;
    private ParseUser currentUser;
    private File profileImageFile;
    private Conversation targetConversation;

    private List<Post> allPosts;
    private LinearLayoutManager layoutManager;
    private PostsAdapter adapter;
    private RecyclerView rvPosts;
    private SwipeRefreshLayout swipeContainer;
    private EndlessRecyclerViewScrollListener scrollListener;

    private ImageView ivProfile;
    private TextView tvUsername;
    private Button btnLogout;
    private Button btnMessage;

   public ProfileFragment() {

   }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context = getContext();
        currentUser = ParseUser.getCurrentUser();

        rvPosts = view.findViewById(R.id.rvPosts);
        swipeContainer = view.findViewById(R.id.swipeContainer);

        layoutManager = new LinearLayoutManager(context);
        allPosts = new ArrayList<>();
        adapter = new ProfilePostsAdapter(context, allPosts);
        rvPosts.setAdapter(adapter);
        rvPosts.setLayoutManager(layoutManager);

        ivProfile = view.findViewById(R.id.ivProfile);
        tvUsername = view.findViewById(R.id.tvUsername);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnMessage = view.findViewById(R.id.btnMessage);

        Bundle bundle = getArguments();
        if (bundle == null) {
            user = ParseUser.getCurrentUser();
        } else {
            user = bundle.getParcelable(Post.KEY_USER);
        }
        if (user.getUsername().equals(ParseUser.getCurrentUser().getUsername())) {
            btnMessage.setVisibility(View.GONE);
        } else {
            btnLogout.setVisibility(View.GONE);
        }
        tvUsername.setText(user.getUsername());

        ParseFile image = (ParseFile) user.get("image");
        if (image != null) {
            Glide.with(context)
                    .load(image.getUrl())
                    .placeholder(R.drawable.ic_profile)
                    .into(ivProfile);
        }

        btnLogout.setOnClickListener(this);
        btnMessage.setOnClickListener(this);
        ivProfile.setOnClickListener(this);

        setHasOptionsMenu(true);
        queryPosts(false);

        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                queryPosts(true);
            }
        };
        // Adds the scroll listener to RecyclerView
        rvPosts.addOnScrollListener(scrollListener);

        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                queryPosts(false);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }


    protected void queryPosts(final boolean loadNext) {
        // Specify which class to query
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        // Find all posts
        query.include(Post.KEY_USER);
        query.setLimit(20);
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

    @Override
    public void onPause() {
        super.onPause();
        lastPosition = layoutManager.findFirstVisibleItemPosition();
    }

    @Override
    public void onClick(View view) {
        if (view == ivProfile) {
            launchCamera();
        } else if (view == btnMessage) {
            goToConversation();
        }
    }

    /**
     * Starts the camera and sets the URI where the photo will be stored
     */
    private void launchCamera() {
        // Create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference for future access
        profileImageFile = CameraUtils.getPhotoFileUri(context, CameraUtils.getProfileFileName(), TAG);
        // Wrap File object into a content provider (required for API >= 24)
        Uri fileProvider = FileProvider.getUriForFile(context, "com.codepath.fileprovider.gameswap", profileImageFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);
        // Ensure that it's safe to use the Intent
        Activity activity = getActivity();
        if (activity != null && intent.resolveActivity(activity.getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, CameraUtils.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    /**
     * Loads image into preview
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CameraUtils.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                final Bitmap takenImage = BitmapFactory.decodeFile(profileImageFile.getAbsolutePath());
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                takenImage.compress(Bitmap.CompressFormat.JPEG, 10, stream);
                byte[] bitmapBytes = stream.toByteArray();
                ParseFile image = new ParseFile(CameraUtils.getProfileFileName(), bitmapBytes);
                user.put("image", image);
                user.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.e(TAG, "Error saving photo");
                            return;
                        }
                        // Load the taken image into a preview
                        ivProfile.setImageBitmap(takenImage);
                        Toast.makeText(context, "Profile Picture Updated", Toast.LENGTH_SHORT).show();
                    }
                });
            } else { // Result was a failure
                Toast.makeText(context, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void logOut() {
        ParseUser.logOutInBackground(new LogOutCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with logout:", e);
                    return;
                }
                Intent intent = new Intent(context, LoginActivity.class);
                startActivity(intent);
                Activity activity = getActivity();
                if (activity != null) {
                    activity.finishAffinity();
                }
            }
        });
    }


    private void goToConversation() {
        // Specify which class to query
        ParseQuery<Conversation> userOneQuery = ParseQuery.getQuery(Conversation.class);
        ParseQuery<Conversation> userTwoQuery = ParseQuery.getQuery(Conversation.class);

        // Find the Conversation that include the current user and the other user
        userOneQuery.whereEqualTo(Conversation.KEY_USER_ONE, ParseUser.getCurrentUser());
        userOneQuery.whereEqualTo(Conversation.KEY_USER_TWO, user);

        userTwoQuery.whereEqualTo(Conversation.KEY_USER_TWO, ParseUser.getCurrentUser());
        userTwoQuery.whereEqualTo(Conversation.KEY_USER_ONE, user);

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
                    targetConversation.setUserTwo(user);
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


    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_profile_options, menu);
        if (user.getUsername().equals(ParseUser.getCurrentUser().getUsername())) {
            menu.findItem(R.id.actionReport).setVisible(false);
            menu.findItem(R.id.actionBlock).setVisible(false);
        } else {
            menu.findItem(R.id.actionLogOut).setVisible(false);
            menu.findItem(R.id.actionSettings).setVisible(false);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.actionReport) {
            reportUser(user);
            return true;
        } else if (id == R.id.actionBlock) {
            blockUser(user);
            return true;
        } else if (id == R.id.actionLogOut) {
            logOut();
            return true;
        } else if (id == R.id.actionSettings) {
            Toast.makeText(context, "Not yet implemented", Toast.LENGTH_SHORT).show();
            return true;
        } else {
            return false;
        }
    }

    private void reportUser(final ParseUser reported) {
        ParseRelation<Report> relation = currentUser.getRelation("reports");
        ParseQuery<Report> query = relation.getQuery();
        query.include(Report.KEY_REPORTING);
        query.include(Report.KEY_REPORTED);
        query.findInBackground(new FindCallback<Report>() {
            @Override
            public void done(List<Report> reports, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error retrieving reports", e);
                    Toast.makeText(context, "Error while reporting", Toast.LENGTH_SHORT).show();
                    return;
                }
                for (Report report: reports) {
                    if (report.getReported().getUsername().equals(reported.getUsername())) {
                        Toast.makeText(context, "Already reported!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                final Report report = new Report();
                report.setReporting(ParseUser.getCurrentUser());
                report.setReported(user);
                report.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Toast.makeText(context, "Error while reporting", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Toast.makeText(context, "Report sent!", Toast.LENGTH_SHORT).show();
                        currentUser.getRelation("reports").add(report);
                        currentUser.saveInBackground();
                    }
                });
            }
        });
    }

    private void blockUser(final ParseUser blocked) {
        ParseRelation<Block> relation = currentUser.getRelation("blocks");
        ParseQuery<Block> query = relation.getQuery();
        query.include(Block.KEY_BLOCKING);
        query.include(Block.KEY_BLOCKED);
        query.findInBackground(new FindCallback<Block>() {
            @Override
            public void done(List<Block> blocks, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error retrieving blocks", e);
                    Toast.makeText(context, "Error while blocking", Toast.LENGTH_SHORT).show();
                    return;
                }
                for (Block block: blocks) {
                    if (block.getBlocked().getUsername().equals(blocked.getUsername())) {
                        Toast.makeText(context, blocked.getUsername() + " is already blocked!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                final Block block = new Block();
                block.setBlocking(ParseUser.getCurrentUser());
                block.setBlocked(user);
                block.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Toast.makeText(context, "Error while blocking", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Toast.makeText(context, blocked.getUsername() + " is now blocked!", Toast.LENGTH_SHORT).show();
                        currentUser.getRelation("blocks").add(block);
                        currentUser.saveInBackground();
                    }
                });
            }
        });
    }

}