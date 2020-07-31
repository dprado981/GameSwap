package com.codepath.gameswap.fragments;

import android.Manifest;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.codepath.gameswap.ImagePagerAdapter;
import com.codepath.gameswap.R;
import com.codepath.gameswap.utils.CameraUtils;
import com.codepath.gameswap.utils.MapUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;


/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */

public abstract class ComposeFragment extends Fragment implements View.OnClickListener {

    public final String TAG = ComposeFragment.class.getSimpleName();

    protected Context context;
    protected LatLng currentLocation;
    protected ImagePagerAdapter<Bitmap> adapter;
    protected List<File> photoFiles;

    protected EditText etTitle;
    protected Button btnCamera;
    protected Button btnGallery;
    protected ViewPager viewPager;
    protected EditText etNotes;
    protected RatingBar rbCondition;
    protected RatingBar rbDifficulty;
    protected Spinner spAgeRating;
    protected Button btnPost;
    protected ProgressBar pbLoading;

    protected ArrayAdapter<CharSequence> spAdapter;

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

        etTitle = view.findViewById(R.id.etTitle);
        btnCamera = view.findViewById(R.id.btnCapture);
        btnGallery = view.findViewById(R.id.btnGallery);
        viewPager = view.findViewById(R.id.viewPager);
        etNotes = view.findViewById(R.id.etNotes);
        rbCondition = view.findViewById(R.id.rbCondition);
        rbDifficulty = view.findViewById(R.id.rbDifficulty);
        spAgeRating = view.findViewById(R.id.spAgeRating);
        btnPost = view.findViewById(R.id.btnPost);
        pbLoading = view.findViewById(R.id.pbLoading);

        // Set up ViewPager for Images
        photoFiles = new ArrayList<>(4);
        List<Bitmap> bitmaps = new ArrayList<>();
        adapter = new ImagePagerAdapter<>(context, bitmaps);
        viewPager.setAdapter(adapter);

        // Set up Age Rating Spinner
        spAdapter = ArrayAdapter.createFromResource(context,
                R.array.age_ratings_array, android.R.layout.simple_spinner_item);
        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spAgeRating.setAdapter(spAdapter);
        spAgeRating.setSelection(0);

        btnCamera.setOnClickListener(this);
        btnGallery.setOnClickListener(this);
        btnPost.setOnClickListener(this);

        setCurrentLocation();
    }

    @Override
    public void onClick(View view) {
        if (view == btnCamera) {
            getPhoto(CameraUtils.ImageLocation.CAMERA);
        } else if (view == btnGallery) {
            getPhoto(CameraUtils.ImageLocation.GALLERY);
        } else if (view == btnPost) {
            if (allFieldsFilled()) {
                savePost();
            }
        }
    }

    /**
     * If has permission, it stores the users current location, otherwise it asks for permission
     */
    private void setCurrentLocation() {
        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    MapUtils.LOCATION_PERMISSION_CODE);
        } else {
            locationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                double currentLatitude = location.getLatitude();
                                double currentLongitude = location.getLongitude();
                                currentLocation = MapUtils.adjustedLatLng(currentLatitude, currentLongitude, 1000);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Error trying to get last GPS location");
                            e.printStackTrace();
                        }
                    });
        }
    }

    /**
     * Displays a Toast showing the users permission decision
     * Sets the current location if the user provides permission
     */
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
     * Starts either the Camera or the Gallery to retrieve the image(s) and prepares for the result
     */
    public void getPhoto(CameraUtils.ImageLocation imageLocation) {
        Intent intent = null;
        int requestCode = -1;
        if (imageLocation == CameraUtils.ImageLocation.CAMERA) {
            // Open the camera
            intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File photoFile = CameraUtils.getPhotoFileUri(context, CameraUtils.getFileName(), TAG);
            photoFiles.add(photoFile);
            // Set URI for new photo
            Uri fileProvider = FileProvider.getUriForFile(context, "com.codepath.fileprovider.gameswap", photoFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);
            requestCode = CameraUtils.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE;
        } else if (imageLocation == CameraUtils.ImageLocation.GALLERY) {
            // Open the photo gallery
            intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            requestCode = CameraUtils.PICK_IMAGE_ACTIVITY_REQUEST_CODE;
        }
        // Ensure that it's safe to use the Intent
        if (intent != null && intent.resolveActivity(context.getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, requestCode);
        }
    }

    /**
     * Loads image into preview
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CameraUtils.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE
                && resultCode == RESULT_OK) {
            File photoFile = photoFiles.get(0);
            // Get image from path into bitmap
            Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
            // Load correctly oriented bitmap into ViewPager
            adapter.clear();
            try {
                loadBitmap(CameraUtils.adjustRotation(takenImage, photoFile), photoFile);
            } catch (IOException e) {
                loadBitmap(takenImage, photoFile);
            }
        } else if (data != null && requestCode == CameraUtils.PICK_IMAGE_ACTIVITY_REQUEST_CODE
                && resultCode == RESULT_OK) {
            ClipData clipData = data.getClipData();
            if (clipData != null) {
                // Ensure that only up to 4 photos are selected
                if (clipData.getItemCount() > 4) {
                    Toast.makeText(context, "Only select up to four images", Toast.LENGTH_SHORT).show();
                    return;
                }
                adapter.clear();
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    Uri photoUri = clipData.getItemAt(i).getUri();
                    Bitmap selectedImage = CameraUtils.loadFromUri(context, photoUri);
                    File newPhotoFile = CameraUtils.getPhotoFileUri(context, CameraUtils.getFileName(i+1), TAG);
                    loadBitmap(selectedImage, newPhotoFile);
                }
            }
        } else {
            Toast.makeText(context, "Image wasn't selected!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Compress specified bitmap into the specified file, then bind to the adapter
     */
    protected void loadBitmap(Bitmap bitmap, File file) {
        try {
            CameraUtils.compressBitmap(bitmap, file);
        } catch (IOException e) {
            Log.e(TAG, "Error compressing image", e);
        }
        adapter.add(bitmap);
        photoFiles.add(file);
        viewPager.getLayoutParams().height = ((View) viewPager.getParent()).getWidth();
    }

    /**
     * Returns true if all required fields are filled out (title, photo, notes, condition, difficulty)
     * Returns false and displays a Toast if one of the fields is not filled out
     */
    private boolean allFieldsFilled() {
        if (etTitle.getText().toString().isEmpty()) {
            Toast.makeText(context, "Must include a title", Toast.LENGTH_SHORT).show();
            return false;
        } else if (photoFiles.isEmpty()) {
            Toast.makeText(context, "Must include a photo", Toast.LENGTH_SHORT).show();
            return false;
        } else if (etNotes.getText().toString().isEmpty()) {
            Toast.makeText(context, "Must include a note", Toast.LENGTH_SHORT).show();
            return false;
        } else if (rbCondition.getRating() == 0) {
            Toast.makeText(context, "Must include a condition", Toast.LENGTH_SHORT).show();
            return false;
        } else if (rbDifficulty.getRating() == 0) {
            Toast.makeText(context, "Must include a difficulty", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    protected abstract void savePost();

}