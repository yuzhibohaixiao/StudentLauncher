package com.alight.android.aoa_launcher.ui.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;

import com.alight.android.aoa_launcher.common.bean.Parent;
import com.alight.android.aoa_launcher.ui.view.CustomDialog;

import java.util.List;

public class HorizontalChildParentAdapter extends PagerAdapter {
    private Context context;
    private List<List<Parent>> maps;
    private String callType;
    private CustomDialog avDialog;

    public HorizontalChildParentAdapter(Context context, List<List<Parent>> maps, String callType, CustomDialog avDialog) {
        this.context = context;
        this.maps = maps;
        this.callType = callType;
        this.avDialog = avDialog;
    }

    /**
     * 决定了有多少页     *     * @return
     */
    @Override
    public int getCount() {
        return maps.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == o;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        RecyclerView recyclerView = new RecyclerView(context);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false) {
            @Override
            public boolean canScrollHorizontally() {
                return false;
            }
        };
        recyclerView.setLayoutManager(layoutManager);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);
        HorizontalScrollParentItemAdapter itemAdapter = new HorizontalScrollParentItemAdapter(context, maps.get(position), callType, avDialog);
        recyclerView.setAdapter(itemAdapter);
        container.addView(recyclerView);//将recyclerView作为子视图加入 container即为viewpager
        return recyclerView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}