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

public class DetailPuzzleFragment extends DetailFragment {

    public static final String TAG = DetailPuzzleFragment.class.getSimpleName();

    private TextView tvPiecesValue;
    private TextView tvDimensionsValue;

    public DetailPuzzleFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_detail_puzzle, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context = getContext();

        tvPiecesValue = view.findViewById(R.id.tvPiecesValue);
        tvDimensionsValue = view.findViewById(R.id.tvDimensionsValue);

        int pieces = post.getPieces();
        if (pieces <= 0) {
            tvPiecesValue.setText(R.string.not_specified);
        } else {
            tvPiecesValue.setText(String.format(Locale.getDefault(), "%d pieces", pieces));
        }

        float width = post.getWidth();
        float height = post.getHeight();
        if (width == 0 || height == 0) {
            tvDimensionsValue.setText(R.string.not_specified);
        } else {
            tvDimensionsValue.setText(String.format(Locale.getDefault(), "%.2f x %.2f inches", width, height));
        }

    }

}
