package com.codepath.gameswap.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
    private TextView tvName;
    private TextView tvBio;
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

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        TextView tvTitle = toolbar.findViewById(R.id.tvTitle);
        rvPosts = view.findViewById(R.id.rvPosts);
        swipeContainer = view.findViewById(R.id.swipeContainer);
        ivProfile = view.findViewById(R.id.ivProfile);
        tvName = view.findViewById(R.id.tvName);
        tvBio = view.findViewById(R.id.tvBio);
        btnMessage = view.findViewById(R.id.btnMessage);

        toolbar.setTitle("");
        tvTitle.setText(getString(R.string.profile));
        setHasOptionsMenu(true);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
        }

        context = getContext();
        currentUser = ParseUser.getCurrentUser();

        layoutManager = new LinearLayoutManager(context);
        allPosts = new ArrayList<>();
        adapter = new ProfilePostsAdapter(context, allPosts);
        rvPosts.setAdapter(adapter);
        rvPosts.setLayoutManager(layoutManager);

        Bundle bundle = getArguments();
        if (bundle == null) {
            user = ParseUser.getCurrentUser();
        } else {
            user = bundle.getParcelable(Post.KEY_USER);
        }

        if (user.getUsername().equals(ParseUser.getCurrentUser().getUsername())) {
            btnMessage.setVisibility(View.GONE);
        } else {
            btnMessage.setVisibility(View.VISIBLE);
        }

        tvTitle.setText(user.getUsername());
        tvName.setText(String.format(Locale.getDefault(), "%s %c.",
                user.getString("firstName"), user.getString("lastName").charAt(0)));
        tvBio.setText(user.getString("bio"));

        ParseFile image = (ParseFile) user.get("image");
        if (image != null) {
            Glide.with(context)
                    .load(image.getUrl())
                    .placeholder(R.drawable.ic_profile)
                    .into(ivProfile);
        }

        btnMessage.setOnClickListener(this);

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
                R.color.colorAccent,
                R.color.colorPrimary,
                R.color.colorDelete);
    }


    protected void queryPosts(final boolean loadNext) {
        // Specify which class to query
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        // Find all posts
        query.include(Post.KEY_USER);
        query.setLimit(HomeFragment.MAX_QUERY_SIZE);
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
       if (view == btnMessage) {
            goToConversation();
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
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_profile_options, menu);
        if (user.getUsername().equals(ParseUser.getCurrentUser().getUsername())) {
            menu.findItem(R.id.actionReport).setVisible(false);
            menu.findItem(R.id.actionBlock).setVisible(false);
        } else {
            menu.findItem(R.id.actionLogOut).setVisible(false);
            menu.findItem(R.id.actionEdit).setVisible(false);
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
        } else if (id == R.id.actionEdit) {
            goToEditProfile();
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

    private void goToEditProfile() {
        FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();
        Fragment fragment = new EditProfileFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(Post.KEY_USER, currentUser);
        fragment.setArguments(bundle);
        fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).addToBackStack(null).commit();
    }

}