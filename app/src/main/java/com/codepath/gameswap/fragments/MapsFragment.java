package com.codepath.gameswap.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.codepath.gameswap.CustomWindowAdapter;
import com.codepath.gameswap.R;
import com.codepath.gameswap.models.Post;
import com.codepath.gameswap.utils.MapUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class MapsFragment extends Fragment implements OnMyLocationButtonClickListener,
        OnMyLocationClickListener, OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {

    public static final String TAG = MapsFragment.class.getSimpleName();

    private Context context;
    private FusedLocationProviderClient locationClient;
    private GoogleMap map;
    private LatLng recentLatLng;

    private BottomNavigationView bottomNavigation;

    // TODO: Chnage bottomNavigation into a horizontal recyclerview/swiping cardview thats populated with results of search queries

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = getContext();
        bottomNavigation = view.findViewById(R.id.bottomNavigation);
        setHasOptionsMenu(true);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void addPoints(final GoogleMap map) {
        // Specify which class to query
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        // Find all posts
        query.include(Post.KEY_USER);
        query.setLimit(20);
        query.addDescendingOrder(Post.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }
                for (Post post : posts) {
                    ParseGeoPoint geoPoint = post.getCoordinates();
                    LatLng point = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
                    map.addCircle(new CircleOptions()
                            .center(point)
                            .radius(3000)
                            .strokeColor(Color.RED)
                            .strokeWidth(4)
                            .fillColor(Color.argb(30, 255, 0, 0)));
                    map.addMarker(new MarkerOptions().position(point).title(post.getTitle())).setTag(post);
                }
            }
        });
    }

    @Override
    public void onMapReady(final GoogleMap map) {
        locationClient = getFusedLocationProviderClient(context);
        this.map = map;
        map.getUiSettings().setZoomControlsEnabled(true);
        addPoints(map);
        map.setInfoWindowAdapter(new CustomWindowAdapter(getLayoutInflater(), context));
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
                    double currentLatitude = location.getLatitude();
                    double currentLongitude = location.getLongitude();
                    recentLatLng = new LatLng(currentLatitude, currentLongitude);
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(recentLatLng, 12));
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Error trying to get last GPS location", e);
                }
            });
            map.setMyLocationEnabled(true);
            map.setOnMyLocationButtonClickListener(this);
            map.setOnMyLocationClickListener(this);
        }

        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Post post = (Post) marker.getTag();
                FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();
                Fragment fragment;
                if (post.getType().equals(Post.GAME)) {
                    fragment = new DetailGameFragment();
                } else if (post.getType().equals(Post.PUZZLE)) {
                        fragment = new DetailPuzzleFragment();
                } else {
                    Toast.makeText(context, "Try again later", Toast.LENGTH_SHORT).show();
                    return;
                }
                Bundle bundle = new Bundle();
                bundle.putParcelable(Post.TAG, post);
                fragment.setArguments(bundle);
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).addToBackStack(null).commit();
            }
        });

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
                onMapReady(map);
            } else {
                Toast.makeText(context,
                        "Location Permission Denied",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(context, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(context, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search_bar, menu);
        final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setMaxWidth( Integer.MAX_VALUE );
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String queryString) {
                // perform query here
                querySearch(queryString);
                // workaround to avoid issues with some emulators and keyboard devices firing twice if a keyboard enter is used
                // see https://code.google.com/p/android/issues/detail?id=24599
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String queryString) {
                return false;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                addPoints(map);
                bottomNavigation.setVisibility(View.GONE);
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void querySearch(String queryString) {
        // Specify which class to query
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        // Find all posts
        query.include(Post.KEY_USER);
        query.whereNear(Post.KEY_COORDINATES, new ParseGeoPoint(recentLatLng.latitude, recentLatLng.longitude));
        query.setLimit(20);
        if (!queryString.isEmpty()) {
            query.whereContains(Post.KEY_TITLE, queryString);
        }
        query.addDescendingOrder(Post.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }
                if (!posts.isEmpty()) {
                    map.clear();
                    for (Post post : posts) {
                        ParseGeoPoint geoPoint = post.getCoordinates();
                        LatLng point = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
                        map.addCircle(new CircleOptions()
                                .center(point)
                                .radius(3000)
                                .strokeColor(Color.RED)
                                .strokeWidth(4)
                                .fillColor(Color.argb(50, 255, 0, 0)));
                        map.addMarker(new MarkerOptions().position(point).title(post.getTitle())).setTag(post);
                        bottomNavigation.setVisibility(View.VISIBLE);
                    }
                    ParseGeoPoint newGeoPoint = posts.get(0).getCoordinates();
                    LatLng newLatLng = new LatLng(newGeoPoint.getLatitude(), newGeoPoint.getLongitude());
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(newLatLng, 14));
                }
            }
        });
    }
}