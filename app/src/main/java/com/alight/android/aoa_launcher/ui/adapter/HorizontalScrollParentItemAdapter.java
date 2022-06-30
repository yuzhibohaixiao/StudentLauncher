package com.alight.android.aoa_launcher.ui.adapter;

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
import com.alight.android.aoa_launcher.common.bean.AppBean;
import com.alight.android.aoa_launcher.common.bean.Parent;
import com.alight.android.aoa_launcher.utils.ToastUtils;

import java.util.List;

public class HorizontalScrollParentItemAdapter extends RecyclerView.Adapter<HorizontalScrollParentItemAdapter.HorizontalItemHolder> {
    private List<Parent> itemBeans;
    private Context context;

    public HorizontalScrollParentItemAdapter(Context context, List<Parent> itemBeans) {
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
        Parent itemBean = itemBeans.get(position);
        viewHolder.setItem(itemBean);
        viewHolder.itemView.setOnClickListener(v -> {
            ToastUtils.showShort(context, "点我干嘛");
//            Intent intent = context.getPackageManager().getLaunchIntentForPackage(itemBean.getAppPackName());
//            context.startActivity(intent);
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

        public void setItem(Parent Parent) {
//            verticalImgView.setImageDrawable(Parent.getAppIcon());
//            verticalText.setText(Parent.getAppName());
        }
    }
}