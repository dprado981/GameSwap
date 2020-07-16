package com.codepath.gameswap.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.gameswap.LoginActivity;
import com.codepath.gameswap.ProfilePostsAdapter;
import com.codepath.gameswap.R;
import com.codepath.gameswap.models.Post;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    public static final String TAG = ProfileFragment.class.getSimpleName();
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = ComposeFragment.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE;
    public final static int SELECT_IMAGE_ACTIVITY_REQUEST_CODE = ComposeFragment.SELECT_IMAGE_ACTIVITY_REQUEST_CODE;
    private String photoFileName = "profile_photo.jpg";

    private Context context;

    private RecyclerView rvPosts;
    private ImageView ivProfile;
    private TextView tvUsername;
    private Button btnLogout;

    private List<Post> allPosts;
    private LinearLayoutManager layoutManager;
    private ProfilePostsAdapter adapter;

    private ParseUser user;
    private File profileImageFile;

    public ProfileFragment() {
        // Required empty public constructor
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

        rvPosts = view.findViewById(R.id.rvPosts);
        ivProfile = view.findViewById(R.id.ivProfile);
        tvUsername = view.findViewById(R.id.tvUsername);
        btnLogout = view.findViewById(R.id.btnLogout);

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

        queryPosts();

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                        if (activity != null ){
                            activity.finishAffinity();
                        }
                    }
                });
            }
        });

        ivProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchCamera();
            }
        });
    }

    private void queryPosts() {
        // Specify which class to query
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        // Find all posts
        query.include(Post.KEY_USER);
        query.whereEqualTo(Post.KEY_USER, user);
        query.addDescendingOrder(Post.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }
                adapter.clear();
                adapter.addAll(posts);
                adapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * Starts the camera and sets the URI where the photo will be stored
     */
    public void launchCamera() {
        // Create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference for future access
        profileImageFile = getPhotoFileUri(photoFileName);
        // Wrap File object into a content provider (required for API >= 24)
        Uri fileProvider = FileProvider.getUriForFile(context, "com.codepath.fileprovider.gameswap", profileImageFile);
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
                final Bitmap takenImage = BitmapFactory.decodeFile(profileImageFile.getAbsolutePath());
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                takenImage.compress(Bitmap.CompressFormat.PNG, 0, stream);
                byte[] bitmapBytes = stream.toByteArray();
                ParseFile image = new ParseFile(photoFileName, bitmapBytes);
                user.put("image", image);
                user.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.i(TAG, "Error saving photo");
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

}