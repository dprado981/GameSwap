package com.codepath.gameswap.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.codepath.gameswap.R;
import com.codepath.gameswap.models.Post;
import com.codepath.gameswap.utils.CameraUtils;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class EditProfileFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = EditProfileFragment.class.getSimpleName();
    private static final int MAX_BIO_LENGTH = 140;

    private Context context;
    private ParseUser user;
    private int charCount;
    private File photoFile;

    private TextView tvChangeImage;
    private ImageView ivProfile;
    private Button btnCapture;
    private Button btnGallery;
    private EditText etUsername;
    private EditText etFirstName;
    private EditText etLastName;
    private EditText etBio;
    private TextView tvCharCount;
    private Button btnSave;

    public EditProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context = getContext();

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        TextView tvTitle = toolbar.findViewById(R.id.tvTitle);
        tvChangeImage = view.findViewById(R.id.tvChangeImage);
        ivProfile = view.findViewById(R.id.ivProfile);
        btnCapture = view.findViewById(R.id.btnCapture);
        btnGallery = view.findViewById(R.id.btnGallery);
        etUsername = view.findViewById(R.id.etUsername);
        etFirstName = view.findViewById(R.id.etFirstName);
        etLastName = view.findViewById(R.id.etLastName);
        etBio = view.findViewById(R.id.etBio);
        tvCharCount = view.findViewById(R.id.tvCharCount);
        btnSave = view.findViewById(R.id.btnSave);

        tvTitle.setText(R.string.edit_profile);

        Bundle bundle = getArguments();
        if (bundle != null) {
            user = bundle.getParcelable(Post.KEY_USER);
            if (user != null) {
                etUsername.setText(user.getString("username"));
                etFirstName.setText(user.getString("firstName"));
                etLastName.setText(user.getString("lastName"));
                String bio = user.getString("bio");
                int bioLength = 0;
                if (bio != null) {
                    etBio.setText(user.getString("bio"));
                    bioLength = bio.length();
                }
                tvCharCount.setText(String.format(Locale.getDefault(), "%3d / %d",
                        bioLength, MAX_BIO_LENGTH));
                ParseFile oldPhoto = user.getParseFile("image");
                if (oldPhoto != null) {
                    Glide.with(context)
                            .load(oldPhoto.getUrl())
                            .placeholder(R.drawable.ic_profile)
                            .into(ivProfile);
                }
            }
        }

        tvChangeImage.setOnClickListener(this);
        ivProfile.setOnClickListener(this);
        btnCapture.setOnClickListener(this);
        btnGallery.setOnClickListener(this);
        btnSave.setOnClickListener(this);

        etBio.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                if ((event != null
                        && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
                        || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    btnSave.performClick();
                }
                return false;
            }
        });
        etBio.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                charCount = charSequence.length();
                tvCharCount.setText(String.format(Locale.getDefault(),"%3d / %d", charCount, MAX_BIO_LENGTH));
                if (charCount > MAX_BIO_LENGTH) {
                    tvCharCount.setTextColor(Color.RED);
                }
                if (charCount <= MAX_BIO_LENGTH) {
                    tvCharCount.setTextColor(Color.BLACK);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view == btnSave && allFieldsFilled()) {
            saveAndReturn();
        } else if (view == ivProfile || view == tvChangeImage || view == btnCapture) {
            setImage(CameraUtils.ImageLocation.CAMERA);
        } else if (view == btnGallery) {
            setImage(CameraUtils.ImageLocation.GALLERY);
        }
    }

    private boolean allFieldsFilled() {
        if (etUsername.getText().toString().trim().isEmpty()) {
            Toast.makeText(context, "Must have a username", Toast.LENGTH_SHORT).show();
            return false;
        } else if (etFirstName.getText().toString().trim().isEmpty()) {
            Toast.makeText(context, "Must have a first name", Toast.LENGTH_SHORT).show();
            return false;
        } else if (etLastName.getText().toString().trim().isEmpty()) {
            Toast.makeText(context, "Must have a last name", Toast.LENGTH_SHORT).show();
            return false;
        } else if (etBio.getText().toString().trim().isEmpty()) {
            Toast.makeText(context, "Must have a bio", Toast.LENGTH_SHORT).show();
            return false;
        } else if (charCount > MAX_BIO_LENGTH) {
            Toast.makeText(context, "Bio is too long", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    private void saveAndReturn() {
        final String newUsername = etUsername.getText().toString();
        user.setUsername(newUsername);
        user.put("firstName", etFirstName.getText().toString());
        user.put("lastName", etLastName.getText().toString());
        user.put("bio", etBio.getText().toString());
        if (photoFile != null) {
            user.put("image", new ParseFile(photoFile));
        }
        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    if (e.getCode() == 202) {
                        Toast.makeText(context, "Username is already taken", Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "Username is already taken");
                    } else {
                        Log.e(TAG, "Error while saving updates", e);
                    }
                    return;
                }
                etUsername.setText(user.getString(""));
                etFirstName.setText(user.getString(""));
                etLastName.setText(user.getString(""));
                etBio.setText(user.getString(""));
                FragmentManager manager = getFragmentManager();
                if (manager != null) {
                    manager.popBackStackImmediate();
                }
            }
        });
    }

    /**
     * Starts either the Camera or the Gallery to retrieve the image(s) and prepares for the result
     */
    public void setImage(CameraUtils.ImageLocation imageLocation) {
        Intent intent = null;
        int requestCode = -1;
        if (imageLocation == CameraUtils.ImageLocation.CAMERA) {
            // Open the camera
            intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            photoFile = CameraUtils.getPhotoFileUri(context, CameraUtils.getProfileFileName(), TAG);
            // Set URI for new photo
            Uri fileProvider = FileProvider.getUriForFile(context, "com.codepath.fileprovider.gameswap", photoFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);
            requestCode = CameraUtils.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE;
        } else if (imageLocation == CameraUtils.ImageLocation.GALLERY) {
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
     * Loads image into preview
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CameraUtils.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE
                && resultCode == RESULT_OK) {
            // Get image from path into bitmap
            Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
            // Load correctly oriented bitmap into ViewPager
            try {
                loadBitmap(CameraUtils.adjustRotation(takenImage, photoFile), photoFile);
            } catch (IOException e) {
                loadBitmap(takenImage, photoFile);
            }
        } else if (data != null && requestCode == CameraUtils.PICK_IMAGE_ACTIVITY_REQUEST_CODE
                && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            Bitmap selectedImage = CameraUtils.loadFromUri(context, imageUri);
            photoFile = CameraUtils.getPhotoFileUri(context, CameraUtils.getProfileFileName(), TAG);
            loadBitmap(selectedImage, photoFile);
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
        ivProfile.setImageBitmap(bitmap);
    }
}