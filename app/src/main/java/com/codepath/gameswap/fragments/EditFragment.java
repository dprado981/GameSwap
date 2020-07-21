package com.codepath.gameswap.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.codepath.gameswap.utils.CameraUtils;
import com.codepath.gameswap.R;
import com.codepath.gameswap.models.Post;
import com.codepath.gameswap.utils.MapUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.io.IOException;

import static android.app.Activity.RESULT_OK;
import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class EditFragment extends Fragment implements View.OnClickListener {

    public final String TAG = ComposeFragment.class.getSimpleName();

    public enum ImageLocation { CAMERA, GALLERY }
    private File photoFile;
    private boolean photoStored;

    private Context context;
    private Post post;
    private ParseUser user;
    private LatLng currentLocation;

    private EditText etTitle;
    private Button btnCamera;
    private Button btnGallery;
    private ImageView ivPreview;
    private EditText etNotes;
    private RatingBar rbCondition;
    private RatingBar rbDifficulty;
    private Spinner spAgeRating;
    private Button btnPost;
    private ProgressBar pbLoading;

    public EditFragment() {
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

        etTitle = view.findViewById(R.id.etTitle);
        btnCamera = view.findViewById(R.id.btnCapture);
        btnGallery = view.findViewById(R.id.btnGallery);
        ivPreview = view.findViewById(R.id.ivPreview);
        etNotes = view.findViewById(R.id.etNotes);
        rbCondition = view.findViewById(R.id.rbCondition);
        rbDifficulty = view.findViewById(R.id.rbDifficulty);
        spAgeRating = view.findViewById(R.id.spAgeRating);
        btnPost = view.findViewById(R.id.btnPost);
        pbLoading = view.findViewById(R.id.pbLoading);

        btnCamera.setOnClickListener(this);
        btnGallery.setOnClickListener(this);
        btnPost.setOnClickListener(this);
        btnPost.setText(R.string.save);

        ivPreview.setImageDrawable(context.getDrawable(R.drawable.ic_image));

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                R.array.age_ratings_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spAgeRating.setAdapter(adapter);

        setCurrentLocation();

        Bundle bundle = getArguments();
        post = bundle.getParcelable(Post.TAG);
        user = post.getUser();
        etTitle.setText(post.getTitle());
        rbCondition.setRating((float) post.getCondition() / 2);
        rbDifficulty.setRating((float) post.getDifficulty() / 2);
        int ageRating = post.getAgeRating();
        int spinnerPosition = adapter.getPosition(Integer.toString(ageRating));
        spAgeRating.setSelection(spinnerPosition);
        etNotes.setText(post.getNotes());
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
                            ivPreview.getLayoutParams().height = ((View) ivPreview.getParent()).getWidth();
                            return false;
                        }
                    })
                    .into(ivPreview);
        }

    }

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

    public void getPhoto(ImageLocation imageLocation) {
        photoFile = getPhotoFileUri(CameraUtils.PHOTO_FILE_NAME);
        Intent intent = null;
        int requestCode = -1;
        if (imageLocation == ImageLocation.CAMERA) {
            // Open the camera
            intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Set URI for new photo
            Uri fileProvider = FileProvider.getUriForFile(context, "com.codepath.fileprovider.gameswap", photoFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);
            requestCode = CameraUtils.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE;
        } else if (imageLocation == ImageLocation.GALLERY) {
            // Open the photo gallery
            intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            requestCode = CameraUtils.PICK_IMAGE_ACTIVITY_REQUEST_CODE;
        }
        // Ensure that it's safe to use the Intent
        if (intent != null && intent.resolveActivity(context.getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, requestCode);
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
            Log.e(TAG, "failed to create directory");
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
        if (requestCode == CameraUtils.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Get image from path into bitmap
                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                Bitmap adjustedImage;
                try {
                    adjustedImage = CameraUtils.adjustRotation(takenImage, photoFile);
                } catch (IOException e) {
                    adjustedImage = takenImage;
                }
                loadImage(adjustedImage);
            } else { // Result was a failure
                Toast.makeText(context, "Image wasn't captured!", Toast.LENGTH_SHORT).show();
            }
        } else if (data != null && requestCode == CameraUtils.PICK_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Get image from path into bitmap
                Uri photoUri = data.getData();
                Bitmap selectedImage = CameraUtils.loadFromUri(context, photoUri);
                loadImage(selectedImage);
            }
        } else { // Result was a failure
            Toast.makeText(context, "Image wasn't selected!", Toast.LENGTH_SHORT).show();
        }
    }



    private void loadImage(Bitmap bitmap) {
        try {
            CameraUtils.compressBitmap(bitmap, photoFile);
        } catch (IOException e) {
            Log.e(TAG, "Error compressing image", e);
        }
        // Load into ImageView
        ivPreview.setImageBitmap(bitmap);
        ivPreview.setScaleType(ImageView.ScaleType.CENTER);
        ivPreview.getLayoutParams().height = ((View) ivPreview.getParent()).getWidth();
        photoStored = true;
    }

    @Override
    public void onClick(View view) {
        if (view == btnCamera) {
            getPhoto(ImageLocation.CAMERA);
        } else if (view == btnGallery) {
            getPhoto(ImageLocation.GALLERY);
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
        } if (rbDifficulty.getRating() == 0) {
            Toast.makeText(context, "Game must have a difficulty", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void savePost() {
        pbLoading.setVisibility(View.VISIBLE);
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
        post.setImage(new ParseFile(photoFile));
        post.setUser(ParseUser.getCurrentUser());
        etTitle.setText("");
        ivPreview.setImageResource(0);
        etNotes.setText("");
        rbCondition.setRating(0);
        rbDifficulty.setRating(0);
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
                    ((BottomNavigationView) activity.findViewById(R.id.bottomNavigation)).setSelectedItemId(R.id.actionProfile);
                    // Go back to profile fragment
                    FragmentManager fragmentManager = activity.getSupportFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.flContainer, new ProfileFragment()).commit();
                }
            }
        });


    }

}