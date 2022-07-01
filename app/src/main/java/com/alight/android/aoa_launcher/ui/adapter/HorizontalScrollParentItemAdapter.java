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
import com.alight.android.aoa_launcher.application.LauncherApplication;
import com.alight.android.aoa_launcher.common.bean.Parent;
import com.alight.android.aoa_launcher.common.constants.AppConstants;
import com.alight.android.aoa_launcher.ui.view.CustomDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.tencent.mmkv.MMKV;

import java.util.List;

public class HorizontalScrollParentItemAdapter extends RecyclerView.Adapter<HorizontalScrollParentItemAdapter.HorizontalItemHolder> {
    private List<Parent> itemBeans;
    private Context context;
    private String callType;
    private CustomDialog avDialog;

    public HorizontalScrollParentItemAdapter(Context context, List<Parent> itemBeans, String callType, CustomDialog avDialog) {
        this.context = context;
        this.itemBeans = itemBeans;
        this.callType = callType;
        this.avDialog = avDialog;
    }

    @NonNull
    @Override
    public HorizontalItemHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_dialog_home_parent, viewGroup, false);
        return new HorizontalItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HorizontalItemHolder viewHolder, int position) {
        Parent itemBean = itemBeans.get(position);
        viewHolder.setItem(context, itemBean);
        viewHolder.itemView.setOnClickListener(v -> {
            callParent(itemBean, context);
        });
    }

    private void callParent(Parent parent, Context context) {
        if (avDialog != null) {
            avDialog.dismiss();
        }
        Intent intent = new Intent("com.alight.trtcav.WindowActivity");
        if (parent != null) {
            MMKV mmkv = LauncherApplication.Companion.getMMKV();
            intent.putExtra("called", 1);   //主叫
            intent.putExtra("parentId", parent.getUser_id() + "");
            intent.putExtra("parentName", parent.getName());
            intent.putExtra("parentAvatar", parent.getAvatar());
            intent.putExtra("childId", mmkv.getInt(AppConstants.AOA_LAUNCHER_USER_INFO_USER_ID, 0) + "");
            intent.putExtra("token", mmkv.getString(AppConstants.AOA_LAUNCHER_USER_INFO_TOKEN, ""));
            intent.putExtra("callType", callType);
            intent.putExtra("isCallback", true);
        }
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

        public void setItem(Context context, Parent parent) {
            Glide.with(context).load(parent.getAvatar())
                    .error((parent.getRole_type() == 1) ? R.drawable.father : R.drawable.mather)
                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                    .into(verticalImgView);
            verticalText.setText(parent.getName());
        }
    }
}