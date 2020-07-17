package com.codepath.gameswap.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.codepath.gameswap.R;
import com.codepath.gameswap.models.Post;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Random;

import static android.app.Activity.RESULT_OK;
import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class ComposeFragment extends Fragment implements View.OnClickListener {

    public final String TAG = ComposeFragment.class.getSimpleName();
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 101;
    public final static int SELECT_IMAGE_ACTIVITY_REQUEST_CODE = 102;
    public final static int LOCATION_PERMISSION_CODE = MapsFragment.LOCATION_PERMISSION_CODE;
    private String photoFileName = "photo.jpg";
    private File photoFile;
    private boolean photoStored;

    private Context context;
    private LatLng currentLocation;

    private EditText etTitle;
    private Button btnCapture;
    private ImageView ivPreview;
    private EditText etNotes;
    private RatingBar rbCondition;
    private RatingBar rbDifficulty;
    private Spinner spAgeRating;
    private Button btnPost;
    private ProgressBar pbLoading;

    public ComposeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_compose, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = getContext();

        photoStored = false;

        btnCapture = view.findViewById(R.id.btnCapture);
        etTitle = view.findViewById(R.id.etTitle);
        ivPreview = view.findViewById(R.id.ivPreview);
        etNotes = view.findViewById(R.id.etNotes);
        rbCondition = view.findViewById(R.id.rbCondition);
        rbDifficulty = view.findViewById(R.id.rbDifficulty);
        spAgeRating = view.findViewById(R.id.spAgeRating);
        btnPost = view.findViewById(R.id.btnPost);
        pbLoading = view.findViewById(R.id.pbLoading);

        btnCapture.setOnClickListener(this);
        btnPost.setOnClickListener(this);

        ivPreview.setImageDrawable(context.getDrawable(R.drawable.ic_image));

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                R.array.age_ratings_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spAgeRating.setAdapter(adapter);
        spAgeRating.setSelection(0);

        setCurrentLocation();

    }

    private void setCurrentLocation() {
        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_CODE);
        } else {
            locationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                double currentLatitude = location.getLatitude();
                                double currentLongitude = location.getLongitude();
                                currentLocation = adjustedLatLng(currentLatitude, currentLongitude, 1000);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("MapDemoActivity", "Error trying to get last GPS location");
                            e.printStackTrace();
                        }
                    });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE) {
            // Checking whether user granted the permission or not.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Showing the toast message
                Toast.makeText(context,
                        "Location Permission Granted",
                        Toast.LENGTH_SHORT)
                        .show();
                setCurrentLocation();
            } else {
                Toast.makeText(context,
                        "Location Permission Denied",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    /**
     * Starts the camera and sets the URI where the photo will be stored
     */
    public void launchCamera() {
        // Create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference for future access
        photoFile = getPhotoFileUri(photoFileName);
        // Wrap File object into a content provider (required for API >= 24)
        Uri fileProvider = FileProvider.getUriForFile(context, "com.codepath.fileprovider.gameswap", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);
        // Ensure that it's safe to use the Intent
        Activity activity = getActivity();
        if (activity != null && intent.resolveActivity(activity.getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    /**
     * Returns the File for a photo stored on disk given the fileName
     */
    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        return new File(mediaStorageDir.getPath() + File.separator + fileName);
    }

    /**
     * Loads image into preview
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // by this point we have the camera photo on disk
                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                // RESIZE BITMAP, see section below
                // Load the taken image into a preview
                ivPreview.setScaleType(ImageView.ScaleType.CENTER);
                ivPreview.setImageBitmap(takenImage);
                photoStored = true;
            } else { // Result was a failure
                Toast.makeText(context, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onClick(View view) {
        if (view == btnCapture) {
            launchCamera();
        } else if (view == btnPost) {
            if (allFieldsFilled()) {
                savePost();
            }
        }
    }

    private boolean allFieldsFilled() {
        if (etTitle.getText().toString().isEmpty()) {
            Toast.makeText(context, "Game must have a title", Toast.LENGTH_SHORT).show();
            return false;
        } if (!photoStored) {
            Toast.makeText(context, "Post must include a photo", Toast.LENGTH_SHORT).show();
            return false;
        } if (etNotes.getText().toString().isEmpty()) {
            Toast.makeText(context, "Game must have a title", Toast.LENGTH_SHORT).show();
            return false;
        } if (etNotes.getText().toString().isEmpty()) {
            Toast.makeText(context, "Game must have a title", Toast.LENGTH_SHORT).show();
            return false;
        } if (rbCondition.getRating() == 0) {
            Toast.makeText(context, "Game must have a condition", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void savePost() {
        pbLoading.setVisibility(View.VISIBLE);
        Post post = new Post();
        post.setTitle(etTitle.getText().toString());
        post.setNotes(etNotes.getText().toString());
        post.setCondition((int)(rbCondition.getRating()*2));
        post.setDifficulty((int)(rbDifficulty.getRating()*2));
        post.setAgeRating(Integer.parseInt((String)spAgeRating.getSelectedItem()));
        if (currentLocation != null) {
            post.setCoordinates(new ParseGeoPoint(currentLocation.latitude, currentLocation.longitude));
        } else {
            post.setCoordinates(new ParseGeoPoint(0,0));
        }
        Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        takenImage.compress(Bitmap.CompressFormat.PNG, 0, stream);
        byte[] bitmapBytes = stream.toByteArray();
        ParseFile image = new ParseFile("myImage", bitmapBytes);
        post.setImage(image);
        post.setUser(ParseUser.getCurrentUser());

        etTitle.setText("");
        ivPreview.setImageResource(0);
        etNotes.setText("");
        rbCondition.setRating(0);

        post.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error while saving", e);
                    Toast.makeText(context, "Error while saving", Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.i(TAG, "Post was saved successfully");
                pbLoading.setVisibility(View.INVISIBLE);

                FragmentActivity activity = getActivity();
                if (activity != null) {
                    // Ensure that correct menu item is selected
                    ((BottomNavigationView) activity.findViewById(R.id.bottomNavigation)).setSelectedItemId(R.id.actionHome);
                    // Go to home fragment
                    FragmentManager fragmentManager = activity.getSupportFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.flContainer, new PostsFragment()).commit();
                }
            }
        });

    }

    private LatLng adjustedLatLng(double latitude, double longitude, int radius) {
        double metersToDegrees = 111111;
        Random random = new Random();
        int precisionDecimals = (int) Math.log10(radius);
        int powerOfTen = (int) Math.pow(10, precisionDecimals);
        int bound = 2 * powerOfTen + 1;
        double latAdjustment = radius / metersToDegrees * (random.nextInt(bound) - powerOfTen) / powerOfTen;
        double longAdjustment = radius / metersToDegrees * (random.nextInt(bound) - powerOfTen) / powerOfTen;
        return new LatLng(latitude + latAdjustment, longitude + longAdjustment);
    }

}