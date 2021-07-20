package com.alight.android.aoa_launcher.ui.view;

import android.app.Dialog;
import android.content.Context;

import com.alight.android.aoa_launcher.R;

public class CustomDialog extends Dialog {

    public CustomDialog(Context context, int layoutId) {

        //使用自定义Dialog样式
        super(context, R.style.custom_dialog);

        //指定布局
        setContentView(layoutId);

        //点击外部不可消失
        //setCancelable(false);
    }
}