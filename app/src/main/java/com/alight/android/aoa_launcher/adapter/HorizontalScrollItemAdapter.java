package com.alight.android.aoa_launcher.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alight.android.aoa_launcher.R;
import com.alight.android.aoa_launcher.bean.AppBean;

import java.util.List;

public class HorizontalScrollItemAdapter extends RecyclerView.Adapter<HorizontalScrollItemAdapter.HorizontalItemHolder> {
    private List<AppBean> itemBeans;
    private Context context;

    public HorizontalScrollItemAdapter(Context context, List<AppBean> itemBeans) {
        this.context = context;
        this.itemBeans = itemBeans;
    }

    @NonNull
    @Override
    public HorizontalItemHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_dialog_launcher, viewGroup, false);
        return new HorizontalItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HorizontalItemHolder viewHolder, int position) {
        AppBean itemBean = itemBeans.get(position);
        viewHolder.setItem(itemBean);
        viewHolder.itemView.setOnClickListener(v -> {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(itemBean.getAppPackName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return itemBeans.size();
    }

    public static class HorizontalItemHolder extends RecyclerView.ViewHolder {
        ImageView verticalImgView;
        TextView verticalText;

        public HorizontalItemHolder(@NonNull View itemView) {
            super(itemView);
            verticalImgView = itemView.findViewById(R.id.verticalImgView);
            verticalText = itemView.findViewById(R.id.verticalText);
        }

        public void setItem(AppBean appBean) {
            verticalImgView.setImageDrawable(appBean.getAppIcon());
            verticalText.setText(appBean.getAppName());
        }
    }
}