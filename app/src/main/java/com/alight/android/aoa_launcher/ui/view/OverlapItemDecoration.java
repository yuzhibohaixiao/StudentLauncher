package com.alight.android.aoa_launcher.ui.view;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

/**
 * 实现item重叠的装饰器
 */
public class OverlapItemDecoration extends RecyclerView.ItemDecoration {

    private int space;

    public OverlapItemDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        /**
         * 除了前两位不进行挪动从而实现重叠的效果
         */
        if (parent.getChildLayoutPosition(view) != 0 && parent.getChildLayoutPosition(view) != 1) {
            outRect.left = space;
        }
    }
}