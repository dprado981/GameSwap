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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
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
import com.codepath.gameswap.models.Filters;
import com.codepath.gameswap.models.Post;
import com.codepath.gameswap.utils.MapUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.slider.RangeSlider;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

/**
 * Use the {@link HomeFragment} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment
        implements MapsFragment.MapsFragmentInterface, PostsFragment.PostsFragmentInterface,
        TextView.OnEditorActionListener, View.OnClickListener, RangeSlider.OnChangeListener {

    public static final String TAG = HomeFragment.class.getSimpleName();
    public static final int MAX_QUERY_SIZE = 20;
    public static final List<Float> DEFAULT_VALUES = new ArrayList<>(Arrays.asList(0.0f, 5.0f));

    private Context context;
    private EditText etSearch;
    private ImageButton ibClear;
    private ImageButton ibSearch;
    private ImageButton ibFilter;
    private RelativeLayout rlBottomSheetFilter;
    private BottomSheetBehavior<RelativeLayout> bottomSheetBehavior;
    private CheckBox gameCheck;
    private CheckBox puzzleCheck;
    private RangeSlider conditionSlider;
    private TextView tvConditionLowerLimit;
    private TextView tvConditionUpperLimit;
    private RangeSlider difficultySlider;
    private TextView tvDifficultyLowerLimit;
    private TextView tvDifficultyUpperLimit;
    private RangeSlider ageRatingSlider;
    private TextView tvAgeRatingLowerLimit;
    private TextView tvAgeRatingUpperLimit;
    private Button btnFilter;
    private Button btnClear;

    private MapsFragment mapsFragment;
    private PostsFragment postsFragment;

    private List<Post> allPosts;
    private ParseQuery<Post> lastQuery;
    private Filters filters;
    private LatLng recentLatLng;
    private FusedLocationProviderClient locationClient;
    private int lastPosition;
    private int pages;

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
        ibFilter = view.findViewById(R.id.ibFilter);
        rlBottomSheetFilter = view.findViewById(R.id.rlBottomSheetFilter);
        bottomSheetBehavior = BottomSheetBehavior.from(rlBottomSheetFilter);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        gameCheck = view.findViewById(R.id.gameCheck);
        puzzleCheck = view.findViewById(R.id.puzzleCheck);
        conditionSlider = view.findViewById(R.id.conditionSlider);
        tvConditionLowerLimit = view.findViewById(R.id.tvConditionLowerLimit);
        tvConditionUpperLimit = view.findViewById(R.id.tvConditionUpperLimit);
        difficultySlider = view.findViewById(R.id.difficultySlider);
        tvDifficultyLowerLimit = view.findViewById(R.id.tvDifficultyLowerLimit);
        tvDifficultyUpperLimit = view.findViewById(R.id.tvDifficultyUpperLimit);
        ageRatingSlider = view.findViewById(R.id.ageRatingSlider);
        tvAgeRatingLowerLimit = view.findViewById(R.id.tvAgeRatingLowerLimit);
        tvAgeRatingUpperLimit = view.findViewById(R.id.tvAgeRatingUpperLimit);
        btnFilter = view.findViewById(R.id.btnFilter);
        btnClear = view.findViewById(R.id.btnClear);

        gameCheck.setChecked(true);
        puzzleCheck.setChecked(true);

        conditionSlider.setValues(0f,5f);
        difficultySlider.setValues(0f,5f);
        ageRatingSlider.setValues(2f,21f);
        conditionSlider.setStepSize(0.1f);
        difficultySlider.setStepSize(0.1f);
        ageRatingSlider.setStepSize(1);
        conditionSlider.addOnChangeListener(this);
        difficultySlider.addOnChangeListener(this);
        ageRatingSlider.addOnChangeListener(this);

        mapsFragment = new MapsFragment(this);
        postsFragment = new PostsFragment(this);
        allPosts = new ArrayList<>();
        pages = 1;

        etSearch.setOnEditorActionListener(this);
        ibClear.setOnClickListener(this);
        ibSearch.setOnClickListener(this);
        ibFilter.setOnClickListener(this);
        btnFilter.setOnClickListener(this);
        btnClear.setOnClickListener(this);
        FragmentManager fragmentManager = ((FragmentActivity)context).getSupportFragmentManager();
        // If scroll position has been saved, send to fragments
        if (lastPosition == 0) {
            mapsFragment.setArguments(null);
            postsFragment.setArguments(null);
        } else {
            Bundle bundle = new Bundle();
            bundle.putInt("lastPosition", lastPosition);
            postsFragment.setArguments(bundle);
        }
        fragmentManager.beginTransaction().replace(R.id.mapContainer, mapsFragment).commit();
        fragmentManager.beginTransaction().replace(R.id.listContainer, postsFragment).commit();
    }

    @Override
    public void onValueChange(@NonNull RangeSlider slider, float value, boolean fromUser) {
        List<Float> sliderValues = slider.getValues();
        float lowerLimit = Collections.min(sliderValues);
        float upperLimit = Collections.max(sliderValues);
        TextView tvLowerLimit;
        TextView tvUpperLimit;
        if (slider == conditionSlider) {
            tvLowerLimit = tvConditionUpperLimit;
            tvUpperLimit = tvConditionUpperLimit;
        } else if (slider == difficultySlider) {
            tvLowerLimit = tvDifficultyLowerLimit;
            tvUpperLimit = tvDifficultyUpperLimit;
        } else if (slider == ageRatingSlider) {
            tvLowerLimit = tvAgeRatingLowerLimit;
            tvUpperLimit = tvAgeRatingUpperLimit;
            tvLowerLimit.setText(String.format(Locale.getDefault(), "%d+", (int)lowerLimit));
            tvUpperLimit.setText(String.format(Locale.getDefault(), "%d+", (int)upperLimit));
            return;
        } else {
            Log.e(TAG, "Error getting slider info");
            return;
        }
        tvLowerLimit.setText(String.format(Locale.getDefault(), "%.1f", lowerLimit));
        tvUpperLimit.setText(String.format(Locale.getDefault(), "%.1f", upperLimit));
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
                lastQuery = null;
                filters = null;
                startSearch();
            }
            etSearch.setText("");
        } else if (view == ibSearch) {
            startSearch();
        } else if (view == ibFilter) {
            int state = bottomSheetBehavior.getState();
            if (state == BottomSheetBehavior.STATE_HIDDEN) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else if (state == BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        } else if (view == btnFilter) {
            boolean games = gameCheck.isChecked();
            boolean puzzles = puzzleCheck.isChecked();
            List<Float> conditionSliderValues = conditionSlider.getValues();
            List<Float> difficultySliderValues = difficultySlider.getValues();
            List<Float> ageRatingSliderValues = ageRatingSlider.getValues();
            int lowerConditionLimit = (int) (Collections.min(conditionSliderValues) * 10);
            int upperConditionLimit = (int) (Collections.max(conditionSliderValues) * 10);
            int lowerDifficultyLimit = (int) (Collections.min(difficultySliderValues) * 10);
            int upperDifficultyLimit = (int) (Collections.max(difficultySliderValues) * 10);
            int lowerAgeRatingLimit = (int) Math.floor(Collections.min(ageRatingSliderValues));
            int upperAgeRatingLimit = (int) Math.floor(Collections.max(ageRatingSliderValues));
            filters = new Filters();
            filters.setGames(games)
                    .setPuzzles(puzzles)
                    .setLowerConditionLimit(lowerConditionLimit)
                    .setUpperConditionLimit(upperConditionLimit)
                    .setLowerDifficultyLimit(lowerDifficultyLimit)
                    .setUpperDifficultyLimit(upperDifficultyLimit)
                    .setLowerAgeRatingLimit(lowerAgeRatingLimit)
                    .setUpperAgeRatingLimit(upperAgeRatingLimit);
            String queryString = etSearch.getText().toString().trim();
            if (!queryString.isEmpty()) {
                searchQuery(queryString);
            } else {
                queryPosts();
            }
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        } else if (view == btnClear) {
            gameCheck.setChecked(true);
            puzzleCheck.setChecked(true);
            conditionSlider.setValues(DEFAULT_VALUES);
        }
    }

    private void startSearch() {
        pages = 1;
        allPosts.clear();
        mapsFragment.clear();
        InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
        etSearch.clearFocus();
        searchQuery(etSearch.getText().toString());
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
                        queryFirst();
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
        queryFirst();
    }

    @Override
    public void onMarkerClick(Post post) {
        int index = allPosts.indexOf(post);
        postsFragment.smoothScrollTo(index);
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
                Toast.makeText(context, "Location Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onLoadMore() {
        int querySize = HomeFragment.MAX_QUERY_SIZE * ++pages;
        lastQuery.setLimit(querySize);
        processQuery(lastQuery, false, true, false, false);
    }

    @Override
    public void onRefresh() {
        pages = 1;
        processQuery(lastQuery, false, false, false, false);
    }

    private void queryFirst() {
        querySearch(null, false, true);
    }

    private void queryPosts() {
        querySearch(null, false, false);
    }

    private void searchQuery(String queryString) {
        querySearch(queryString,  false, false);

    }

    private void applyFilters(ParseQuery<Post> query) {
        if (filters.getPuzzles() && !filters.getGames()) {
            query.whereEqualTo(Post.KEY_TYPE, "puzzle");
        } else if (filters.getGames() && !filters.getPuzzles()) {
            query.whereEqualTo(Post.KEY_TYPE, "game");
        }
        query.whereGreaterThanOrEqualTo(Post.KEY_CONDITION, filters.getLowerConditionLimit());
        query.whereLessThanOrEqualTo(Post.KEY_CONDITION, filters.getUpperConditionLimit());
        query.whereGreaterThanOrEqualTo(Post.KEY_DIFFICULTY, filters.getLowerDifficultyLimit());
        query.whereLessThanOrEqualTo(Post.KEY_DIFFICULTY, filters.getUpperDifficultyLimit());
        query.whereGreaterThanOrEqualTo(Post.KEY_AGE_RATING, filters.getLowerAgeRatingLimit());
        query.whereLessThanOrEqualTo(Post.KEY_AGE_RATING, filters.getUpperAgeRatingLimit());
    }

    private void querySearch(String queryString, final boolean forLoadMore,
                             final boolean firstQuery) {
        // Specify which class to query
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        // Find all posts
        query.include(Post.KEY_USER);
        if (recentLatLng != null) {
            query.whereNear(Post.KEY_COORDINATES, new ParseGeoPoint(recentLatLng.latitude, recentLatLng.longitude));
        }
        query.setLimit(MAX_QUERY_SIZE);
        boolean forFilter = (filters != null && !filters.areDefault());
        if (forFilter) {
            applyFilters(query);
        }
        boolean forSearch = (queryString != null);
        if (forSearch || forFilter) {
            allPosts.clear();
            postsFragment.clear();
            mapsFragment.clear();
            if (forSearch) {
                query.whereContains(Post.KEY_TITLE, queryString);
            }
        }
        lastQuery = query;
        processQuery(query, forSearch, forLoadMore, firstQuery, forFilter);
    }

    private void processQuery(ParseQuery<Post> query, final boolean forSearch, final boolean forLoadMore, final boolean firstQuery, final boolean forFilter) {
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(final List<Post> posts, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }
                if (!posts.isEmpty()) {
                    filterUnblocked(posts, forSearch, forLoadMore, firstQuery, forFilter);
                } else {
                    Toast.makeText(context, "No posts matched that query", Toast.LENGTH_SHORT).show();
                    queryPosts();
                }
            }
        });
    }

    private void filterUnblocked(final List<Post> posts, final boolean forSearch, final boolean forLoadMore, final boolean firstQuery, final boolean forFilter) {
        ParseRelation<Block> blockRelation = ParseUser.getCurrentUser().getRelation("blocks");
        ParseQuery<Block> blockQuery = blockRelation.getQuery();
        blockQuery.include(Block.KEY_USER);
        blockQuery.include(Block.KEY_BLOCKED_BY);
        blockQuery.findInBackground(new FindCallback<Block>() {
            @Override
            public void done(List<Block> blocks, ParseException e) {
                if (!forLoadMore && !firstQuery) {
                    pages = 1;
                    allPosts.clear();
                    postsFragment.clear();
                }
                List<Post> newPosts = new ArrayList<>();
                for (Post post : posts) {
                    boolean blockedPost = false;
                    for (Block block : blocks) {
                        String blockedUsername = block.getUser().getUsername();
                        if (blockedUsername.equals(post.getUser().getUsername())) {
                            blockedPost = true;
                            break;
                        }
                    }
                    if (!blockedPost) {
                        if (!post.containedIn(allPosts)) {
                            newPosts.add(post);
                            ParseGeoPoint geoPoint = post.getCoordinates();
                            LatLng point = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
                            mapsFragment.addPoint(point, post);
                        }
                    }
                }
                allPosts.addAll(newPosts);
                postsFragment.addPosts(newPosts);
                if (!newPosts.isEmpty() && !forLoadMore) {
                    postsFragment.scrollTo(0);
                    if (forSearch || forFilter) {
                        ParseGeoPoint newGeoPoint = allPosts.get(0).getCoordinates();
                        LatLng point = new LatLng(newGeoPoint.getLatitude(), newGeoPoint.getLongitude());
                        mapsFragment.panTo(point, 12);
                    }
                }
            }
        });
    }

    // TODO: add toolbar on conversation fragment with options to block/report user and display username

    // TODO: do something different with info window maybe

    // TODO: improve search of chats to include messages (?)

    // TODO: add ability to change password
    // TODO: implement 'forgot password'

}