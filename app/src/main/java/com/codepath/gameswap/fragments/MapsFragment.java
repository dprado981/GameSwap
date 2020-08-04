package com.codepath.gameswap.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.codepath.gameswap.CustomWindowAdapter;
import com.codepath.gameswap.R;
import com.codepath.gameswap.models.Post;
import com.codepath.gameswap.utils.MapUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.ParseFile;

import java.util.HashMap;
import java.util.Map;

public class MapsFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener, GoogleMap.OnInfoWindowCloseListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    public interface MapsFragmentInterface {
        void onMapReady();
        void onMarkerClick(Post post);
    }

    public static final String TAG = MapsFragment.class.getSimpleName();

    private Context context;
    private GoogleMap map;
    private MapsFragmentInterface callback;
    private Map<Post, Marker> markers;

    public MapsFragment(Fragment fragment) {
        callback = (MapsFragmentInterface) fragment;
    }

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
        markers = new HashMap<>();
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        View locationButton = view.findViewWithTag("GoogleMapMyLocationButton");
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                locationButton.getLayoutParams();
        // position on right bottom
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        layoutParams.setMargins(0, 0, 0, 450);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(final GoogleMap map) {
        this.map = map;
        callback.onMapReady();
        map.setInfoWindowAdapter(new CustomWindowAdapter(getLayoutInflater(), context));
        map.setMyLocationEnabled(true);
        map.setOnMarkerClickListener(this);
        map.setOnInfoWindowClickListener(this);
        map.setOnInfoWindowCloseListener(this);
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
    public boolean onMarkerClick(Marker marker) {
        Post post = (Post) marker.getTag();
        if (post != null) {
            focusOn(marker, post);
            callback.onMarkerClick(post);
        }
        return false;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Post post = (Post) marker.getTag();
        FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();
        Fragment fragment;
        if (post != null) {
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
    }

    @Override
    public void onInfoWindowClose(Marker marker) {
        Post post = (Post) marker.getTag();
        LatLng latLng = marker.getPosition();
        markers.remove(post);
        marker.remove();
        addPoint(latLng, post);
    }

    public void addPoint(final LatLng point, final Post post) {
        addPoint(point, post, Color.WHITE);
    }

    public void addPoint(final LatLng point, final Post post, final int color) {
        ParseFile image = post.getImageOne();
        Glide.with(context)
                .asBitmap()
                .load(image.getUrl())
                .override(192)
                .circleCrop()
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        Bitmap bordered = addBorder(resource, color);
                        Marker marker = map.addMarker(new MarkerOptions()
                                .position(point)
                                .title(post.getTitle())
                                .icon(BitmapDescriptorFactory.fromBitmap(bordered))
                                .flat(true));
                        marker.setTag(post);
                        if (color != Color.WHITE) {
                            marker.showInfoWindow();
                        }
                        markers.put(post, marker);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }
                });
    }

    private Bitmap addBorder(Bitmap bitmap, int color) {
        int border = 15;
        int width = bitmap.getWidth() + border;
        int height = bitmap.getHeight() + border;
        Bitmap bmpWithBorder = Bitmap.createBitmap(width, height, bitmap.getConfig());
        Canvas canvas = new Canvas(bmpWithBorder);
        Paint paint = new Paint();
        paint.setColor(color);
        canvas.drawCircle((width)/2f, (height)/2f, (width)/2f, paint);
        canvas.drawColor(Color.TRANSPARENT);
        canvas.drawBitmap(bitmap, border/2f, border/2f, null);
        return bmpWithBorder;
    }

    public void moveTo(LatLng point, float zoom) {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(point, zoom));
    }

    public void panTo(LatLng point, float zoom) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(point, zoom));
    }


    public void focusOn(final Marker marker, final Post post) {
        ParseFile image = post.getImageOne();
        Glide.with(context)
                .asBitmap()
                .load(image.getUrl())
                .override(192)
                .circleCrop()
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        int color = ContextCompat.getColor(context, R.color.colorAccent);
                        Bitmap bordered = addBorder(resource, color);
                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(bordered));
                        marker.showInfoWindow();
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 14f));
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }
                });
    }

    public void focusOn(final Marker marker) {
        Post post = (Post) marker.getTag();
        if (post != null) {
            focusOn(marker, post);
        }
    }

    public void focusOn(Post post) {
        Marker marker = markers.get(post);
        if (marker != null) {
            focusOn(marker, post);
        }
    }

    public void clear() {
        if (map != null) {
            try {
                map.clear();
            } catch (Exception e) {
                Log.e(TAG, "Error clearing map");
            }
        }
    }

}
