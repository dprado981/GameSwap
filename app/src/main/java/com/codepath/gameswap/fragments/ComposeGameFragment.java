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
import androidx.fragment.app.FragmentManager;

import com.codepath.gameswap.R;
import com.codepath.gameswap.models.BGGGame;
import com.codepath.gameswap.models.Post;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ComposeGameFragment extends ComposeFragment {

    protected EditText etMinPlayers;
    protected EditText etMaxPlayers;
    protected EditText etMinPlaytime;
    protected EditText etMaxPlaytime;

    public ComposeGameFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_compose_game, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = getContext();
        etMinPlayers = view.findViewById(R.id.etMinPlayers);
        etMaxPlayers = view.findViewById(R.id.etMaxPlayers);
        etMinPlaytime = view.findViewById(R.id.etMinPlaytime);
        etMaxPlaytime = view.findViewById(R.id.etMaxPlaytime);

        Bundle bundle = getArguments();
        if (bundle != null) {
            BGGGame game = bundle.getParcelable(BGGGame.TAG);
            if (game != null) {
                etTitle.setText(game.getTitle());
                rbDifficulty.setRating(game.getDifficulty());
                spAgeRating.setSelection(spAdapter.getPosition(game.getAgeRating()));
                etMinPlayers.setText(String.format(Locale.getDefault(), "%d", game.getMinPlayers()));
                etMaxPlayers.setText(String.format(Locale.getDefault(), "%d", game.getMaxPlayers()));
                etMinPlaytime.setText(String.format(Locale.getDefault(), "%d", game.getMinPlaytime()));
                etMaxPlaytime.setText(String.format(Locale.getDefault(), "%d", game.getMaxPlaytime()));
            }
        }

        Snackbar.make(view, "Autofill with BoardGameGeek?", Snackbar.LENGTH_INDEFINITE)
                .setAction("Go", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FragmentManager fragmentManager = ((FragmentActivity)context).getSupportFragmentManager();
                        fragmentManager.beginTransaction()
                                .replace(R.id.flContainer, new BGGSearchFragment())
                                .addToBackStack(null)
                                .commit();
                    }
                }).show();
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
        String minPlayers = etMinPlayers.getText().toString();
        if (!minPlayers.isEmpty()) {
            post.setMinPlayers(Integer.parseInt(minPlayers));
        }
        String maxPlayers = etMaxPlayers.getText().toString();
        if (!maxPlayers.isEmpty()) {
            post.setMaxPlayers(Integer.parseInt(maxPlayers));
        }
        String minPlaytime = etMinPlaytime.getText().toString();
        if (!minPlaytime.isEmpty()) {
            post.setMinPlaytime(Integer.parseInt(minPlaytime));
        }
        String maxPlaytime = etMaxPlaytime.getText().toString();
        if (!maxPlaytime.isEmpty()) {
            post.setMaxPlaytime(Integer.parseInt(maxPlaytime));
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
        post.setType(Post.GAME);
        etTitle.setText("");
        etNotes.setText("");
        rbCondition.setRating(0);
        rbDifficulty.setRating(0);
        spAgeRating.setSelection(0);
        etMinPlayers.setText("");
        etMaxPlayers.setText("");
        etMinPlaytime.setText("");
        etMaxPlaytime.setText("");
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
                ((BottomNavigationView) activity.findViewById(R.id.bottomNavigation)).setSelectedItemId(R.id.actionHome);
                // Go to home fragment
                Log.e(TAG, "FIX MOVING BACK TO HOME FRAGMENT");
                //FragmentManager fragmentManager = activity.getSupportFragmentManager();
                //fragmentManager.beginTransaction().replace(R.id.flContainer, new PostsFragment()).commit();
            }
        });
    }
}
