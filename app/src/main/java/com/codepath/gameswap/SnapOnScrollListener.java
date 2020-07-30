package com.codepath.gameswap;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.codepath.gameswap.fragments.OnSnapPositionChangeListener;

public class SnapOnScrollListener extends RecyclerView.OnScrollListener {

    private SnapHelper snapHelper;
    private OnSnapPositionChangeListener onSnapPositionChangeListener;
    private int oldPosition = RecyclerView.NO_POSITION;

    public SnapOnScrollListener(SnapHelper snapHelper, OnSnapPositionChangeListener onSnapPositionChangeListener) {
        this.snapHelper = snapHelper;
        this.onSnapPositionChangeListener = onSnapPositionChangeListener;
    }

    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager != null) {
            maybeNotifySnapPositionChange(layoutManager);
        }
    }

    private void maybeNotifySnapPositionChange(RecyclerView.LayoutManager layoutManager) {
        View view = snapHelper.findSnapView(layoutManager);
        int newPosition = 0;
        if (view != null) {
            newPosition = layoutManager.getPosition(view);
        }
        if (newPosition != oldPosition) {
            onSnapPositionChangeListener.onSnapPositionChange(newPosition);
            oldPosition = newPosition;
        }
    }
}
