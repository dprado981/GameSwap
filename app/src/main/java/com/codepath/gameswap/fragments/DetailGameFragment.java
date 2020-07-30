package com.codepath.gameswap.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.codepath.gameswap.R;

import java.util.Locale;

public class DetailGameFragment extends DetailFragment {

    public static final String TAG = DetailGameFragment.class.getSimpleName();

    private TextView tvPlayersValue;
    private TextView tvPlaytimeValue;

    public DetailGameFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_detail_game, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context = getContext();

        tvPlayersValue = view.findViewById(R.id.tvPlayersValue);
        tvPlaytimeValue = view.findViewById(R.id.tvPlaytimeValue);

        int minPlayers = post.getMinPlayers();
        int maxPlayers = post.getMaxPlayers();
        if (minPlayers == 0) {
            tvPlayersValue.setText(R.string.not_specified);
        } else if (maxPlayers <= minPlayers) {
            tvPlayersValue.setText(String.format(Locale.getDefault(), "%d+ players", minPlayers));
        } else {
            tvPlayersValue.setText(String.format(Locale.getDefault(), "%d - %d players", minPlayers, maxPlayers));
        }

        int minPlaytime = post.getMinPlaytime();
        int maxPlaytime = post.getMaxPlaytime();
        if (minPlayers == 0) {
            tvPlaytimeValue.setText(R.string.not_specified);
        } else if (maxPlaytime <= minPlaytime) {
            tvPlaytimeValue.setText(String.format(Locale.getDefault(), "%d+ minutes", minPlaytime));
        } else {
            tvPlaytimeValue.setText(String.format(Locale.getDefault(), "%d - %d minutes", minPlaytime, maxPlaytime));
        }

    }

}
