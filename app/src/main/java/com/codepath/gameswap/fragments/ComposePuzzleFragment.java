package com.codepath.gameswap.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.codepath.gameswap.R;
import com.codepath.gameswap.models.Post;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ComposePuzzleFragment extends ComposeFragment {

    protected EditText etPieces;
    protected EditText etWidth;
    protected EditText etHeight;

    public ComposePuzzleFragment() {
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

        etPieces = view.findViewById(R.id.etPieces);
        etWidth = view.findViewById(R.id.etWidth);
        etHeight = view.findViewById(R.id.etHeight);
    }

    @Override
    public void savePost() {
        pbLoading.setVisibility(View.VISIBLE);
        final Post post = new Post();
        post.setTitle(etTitle.getText().toString());
        post.setNotes(etNotes.getText().toString());
        post.setCondition((int)(rbCondition.getRating()*10));
        post.setDifficulty((int)(rbDifficulty.getRating()*10));
        String ageRating = spAgeRating.getSelectedItem().toString().replace("+","");
        post.setAgeRating(Integer.parseInt(ageRating));
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
            post.setHeight(Float.parseFloat(height));
        }
        if (currentLocation != null) {
            post.setCoordinates(new ParseGeoPoint(currentLocation.latitude, currentLocation.longitude));
        }

        List<ParseFile> parseFiles = new ArrayList<>(4);
        for (File file : photoFiles) {
            parseFiles.add(new ParseFile(file));
        }
        post.setImages(parseFiles);
        post.setUser(ParseUser.getCurrentUser());
        post.setType(Post.PUZZLE);
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
                ((BottomNavigationView) activity.findViewById(R.id.bottomNavigation)).setSelectedItemId(R.id.actionHome);
            }
        });
    }
}
