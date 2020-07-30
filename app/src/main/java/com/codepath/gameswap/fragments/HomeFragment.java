package com.codepath.gameswap.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.codepath.gameswap.R;
import com.codepath.gameswap.models.Block;
import com.codepath.gameswap.models.Post;
import com.codepath.gameswap.utils.MapUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

/**
 * Use the {@link HomeFragment} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment
        implements MapsFragment.MapsFragmentInterface, PostsFragment.PostsFragmentInterface,
        TextView.OnEditorActionListener, View.OnClickListener {

    public static final String TAG = HomeFragment.class.getSimpleName();

    private Context context;
    private EditText etSearch;
    private ImageButton ibClear;
    private ImageButton ibSearch;
    private MapsFragment mapsFragment;
    private PostsFragment postsFragment;
    private List<Post> allPosts;
    private ParseQuery<Post> lastQuery;

    private LatLng recentLatLng;
    private FusedLocationProviderClient locationClient;
    private int lastPosition;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = getContext();
        etSearch = view.findViewById(R.id.etSearch);
        ibClear = view.findViewById(R.id.ibClear);
        ibSearch = view.findViewById(R.id.ibSearch);

        mapsFragment = new MapsFragment(this);
        postsFragment = new PostsFragment(this);
        allPosts = new ArrayList<>();

        etSearch.setOnEditorActionListener(this);
        ibClear.setOnClickListener(this);
        ibSearch.setOnClickListener(this);
        FragmentManager fragmentManager = ((FragmentActivity)context).getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.mapContainer, mapsFragment).commit();
        fragmentManager.beginTransaction().replace(R.id.listContainer, postsFragment).commit();
    }

    @Override
    public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
        if (id == EditorInfo.IME_ACTION_SEARCH) {
            startSearch();
            return true;
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        if (view == ibClear) {
            if (etSearch.getText().toString().isEmpty()) {
                startSearch();
            }
            etSearch.setText("");
        } else if (view == ibSearch) {
            startSearch();
        }
    }

    private void startSearch() {
        allPosts.clear();
        mapsFragment.clear();
        InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
        etSearch.clearFocus();
        querySearch(etSearch.getText().toString());
    }

    @Override
    public void onMapReady() {
        locationClient = getFusedLocationProviderClient(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    MapUtils.LOCATION_PERMISSION_CODE);
        } else {
            // Get current location and zoom in
            locationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>(){
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        double currentLatitude = location.getLatitude();
                        double currentLongitude = location.getLongitude();
                        recentLatLng = new LatLng(currentLatitude, currentLongitude);
                        mapsFragment.moveTo(recentLatLng, 14);
                        queryPosts();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Error trying to get last GPS location", e);
                }
            });
            return;
        }
        queryPosts();
    }

    @Override
    public void onMarkerClick(Post post) {
        int index = allPosts.indexOf(post);
        postsFragment.smoothScrollTo(index);
    }


    @Override
    public void onLoadMore() {
        Date olderThanDate = getOldestPostDate(allPosts);
        lastQuery.whereLessThan(Post.KEY_CREATED_AT, olderThanDate);
        processQuery(lastQuery, false, true);
    }

    @Override
    public void onRefresh() {
        //queryPosts();
        processQuery(lastQuery, false, false);
    }

    private Date getOldestPostDate(List<Post> posts) {
        Date oldestDate = new Date();
        for (Post post : posts) {
            Date temp = post.getCreatedAt();
            if (temp.before(oldestDate)) {
                oldestDate = temp;
            }
        }
        return oldestDate;
    }

    @Override
    public void onSnapPositionChange(Post post, int position) {
        mapsFragment.focusOn(post);
        lastPosition = position;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MapUtils.LOCATION_PERMISSION_CODE) {
            // Checking whether user granted the permission or not.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Showing the toast message
                Toast.makeText(context,
                        "Location Permission Granted",
                        Toast.LENGTH_SHORT)
                        .show();
                onMapReady();
            } else {
                Toast.makeText(context,
                        "Location Permission Denied",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    private void queryPosts() {
        querySearch(null);
    }

    private void querySearch(String queryString) {
        // Specify which class to query
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        // Find all posts
        query.include(Post.KEY_USER);
        if (recentLatLng != null) {
            query.whereNear(Post.KEY_COORDINATES, new ParseGeoPoint(recentLatLng.latitude, recentLatLng.longitude));
        }
        query.setLimit(20);
        boolean forSearch = (queryString != null);
        if (forSearch) {
            query.whereContains(Post.KEY_TITLE, queryString);
        }
        lastQuery = query;
        processQuery(query, forSearch, false);
    }

    private void processQuery(ParseQuery<Post> query, final boolean forSearch, final boolean forLoadMore) {
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(final List<Post> posts, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }
                if (!posts.isEmpty()) {
                    getUnblocked(posts, forSearch, forLoadMore);
                }
            }
        });
    }

    private void getUnblocked(final List<Post> posts, final boolean forSearch, final boolean forLoadMore) {
        ParseRelation<Block> blockRelation = ParseUser.getCurrentUser().getRelation("blocks");
        ParseQuery<Block> blockQuery = blockRelation.getQuery();
        blockQuery.include(Block.KEY_BLOCKING);
        blockQuery.include(Block.KEY_BLOCKED);
        blockQuery.findInBackground(new FindCallback<Block>() {
            @Override
            public void done(List<Block> blocks, ParseException e) {
                if (!forLoadMore) {
                    allPosts.clear();
                    postsFragment.clear();
                }
                for (Post post : posts) {
                    boolean blockedPost = false;
                    for (Block block : blocks) {
                        if (block.getBlocked().getUsername().equals(post.getUser().getUsername())) {
                            blockedPost = true;
                            break;
                        }
                    }
                    if (!blockedPost) {
                        allPosts.add(post);
                        ParseGeoPoint geoPoint = post.getCoordinates();
                        LatLng point = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
                        mapsFragment.addPoint(point, post);
                    }
                }
                postsFragment.addPosts(allPosts);
                if (forSearch) {
                    ParseGeoPoint newGeoPoint = allPosts.get(0).getCoordinates();
                    LatLng point = new LatLng(newGeoPoint.getLatitude(), newGeoPoint.getLongitude());
                    mapsFragment.panTo(point, 12);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        FragmentManager fragmentManager = ((FragmentActivity)context).getSupportFragmentManager();
        Fragment fragment = postsFragment;
        Bundle bundle = new Bundle();
        bundle.putInt("lastPosition", lastPosition);
        fragment.setArguments(bundle);
        fragmentManager.beginTransaction().replace(R.id.mapContainer, mapsFragment).commit();
        fragmentManager.beginTransaction().replace(R.id.listContainer, postsFragment).commit();
    }
}