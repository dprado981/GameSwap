package com.codepath.gameswap.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.codepath.gameswap.R;
import com.codepath.gameswap.models.Post;
import com.codepath.gameswap.utils.CameraUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class EditPuzzleFragment extends ComposePuzzleFragment implements View.OnClickListener, GetDataCallback {

    public final String TAG = EditPuzzleFragment.class.getSimpleName();

    private Post post;
    private int numPics = 0;

    public EditPuzzleFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_compose_puzzle, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = getContext();

        btnPost.setText(R.string.save);

        Bundle bundle = getArguments();
        if (bundle != null) {
            post = bundle.getParcelable(Post.TAG);
            if (post != null) {
                etTitle.setText(post.getTitle());
                rbCondition.setRating((float) post.getCondition() / 10);
                rbDifficulty.setRating((float) post.getDifficulty() / 10);
                String ageRating = post.getAgeRating();
                int spinnerPosition = spAdapter.getPosition(ageRating);
                spAgeRating.setSelection(spinnerPosition);
                etNotes.setText(post.getNotes());

                etPieces.setText(String.format(Locale.getDefault(), "%d", post.getPieces()));
                etWidth.setText(String.format(Locale.getDefault(), "%.2f", post.getWidth()));
                etHeight.setText(String.format(Locale.getDefault(), "%.2f", post.getHeight()));

                ParseFile imageOne = post.getImageOne();
                ParseFile imageTwo = post.getImageTwo();
                ParseFile imageThree = post.getImageThree();
                ParseFile imageFour = post.getImageFour();
                if (imageOne != null) {
                    imageOne.getDataInBackground(this);
                }
                if (imageTwo != null) {
                    imageTwo.getDataInBackground(this);
                }
                if (imageThree != null) {
                    imageThree.getDataInBackground(this);
                }
                if (imageFour != null) {
                    imageFour.getDataInBackground(this);
                }
            }
        }
    }

    @Override
    public void done(byte[] data, ParseException e) {
        if (e != null) {
            Log.e(TAG, "Could not retrieve image from parseFile", e);
        } else {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            File newFile = CameraUtils.getPhotoFileUri(context, CameraUtils.getFileName(++numPics), TAG);
            loadBitmap(bitmap, newFile);
        }
    }

    @Override
    public void savePost() {
        pbLoading.setVisibility(View.VISIBLE);
        if (post == null) {
            post = new Post();
        }
        post.setTitle(etTitle.getText().toString());
        post.setNotes(etNotes.getText().toString());
        post.setCondition((int)(rbCondition.getRating()*10));
        post.setDifficulty((int)(rbDifficulty.getRating()*10));
        post.setAgeRating((String) spAgeRating.getSelectedItem());
        List<ParseFile> parseFiles = new ArrayList<>(4);
        for (File file : photoFiles) {
            parseFiles.add(new ParseFile(file));
        }
        post.setImages(parseFiles);
        post.setUser(ParseUser.getCurrentUser());
        post.setType(Post.GAME);
        String pieces = etPieces.getText().toString();
        if (!pieces.isEmpty()) {
            post.setPieces(Integer.parseInt(pieces));
        }
        String width = etWidth.getText().toString();
        if (!width.isEmpty()) {
            post.setWidth(Float.parseFloat(width));
        }
        String height = etHeight.getText().toString();
        if (!height.isEmpty()) {
            post.setHeight(Integer.parseInt(height));
        }
        if (currentLocation != null) {
            post.setCoordinates(new ParseGeoPoint(currentLocation.latitude, currentLocation.longitude));
        }
        etTitle.setText("");
        etNotes.setText("");
        rbCondition.setRating(0);
        rbDifficulty.setRating(0);
        spAgeRating.setSelection(0);
        etPieces.setText("");
        etWidth.setText("");
        etHeight.setText("");
        currentLocation = null;
        post.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error while saving", e);
                    Toast.makeText(context, "Error while saving", Toast.LENGTH_SHORT).show();
                    return;
                }
                pbLoading.setVisibility(View.INVISIBLE);
                FragmentActivity activity = (FragmentActivity) context;
                // Ensure that correct menu item is selected
                ((BottomNavigationView) activity.findViewById(R.id.bottomNavigation)).setSelectedItemId(R.id.actionProfile);
                // Go to profile fragment
                FragmentManager fragmentManager = activity.getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.flContainer, new ProfileFragment()).commit();
            }
        });
    }
}