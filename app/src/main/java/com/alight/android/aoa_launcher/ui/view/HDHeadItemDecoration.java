package com.alight.android.aoa_launcher.ui.view;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class HDHeadItemDecoration extends RecyclerView.ItemDecoration {

    private int space;

    public HDHeadItemDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (parent.getChildLayoutPosition(view) != 0 && parent.getChildLayoutPosition(view) != 1) {
            outRect.left = space;
        }
    }
}