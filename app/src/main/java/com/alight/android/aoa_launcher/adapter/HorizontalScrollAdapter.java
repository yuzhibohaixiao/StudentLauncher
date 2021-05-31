package com.alight.android.aoa_launcher.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;

import com.alight.android.aoa_launcher.bean.AppBean;

import java.util.List;

public class HorizontalScrollAdapter extends PagerAdapter {
    private Context context;
    private List<List<AppBean>> maps;

    public HorizontalScrollAdapter(Context context, List<List<AppBean>> maps) {
        this.context = context;
        this.maps = maps;
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
        GridLayoutManager layoutManager = new GridLayoutManager(context, 3, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        HorizontalScrollItemAdapter itemAdapter = new HorizontalScrollItemAdapter(context, maps.get(position));
        recyclerView.setAdapter(itemAdapter);
        container.addView(recyclerView);//将recyclerView作为子视图加入 container即为viewpager
        return recyclerView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}