package com.alight.android.aoa_launcher.ui.view

import android.app.Dialog
import android.content.Context
import android.view.View
import android.widget.TextView
import com.alight.android.aoa_launcher.R

/**
 * 二次确认弹窗
 */
class ConfirmDialog(context: Context) :
    Dialog(context, R.style.custom_dialog) {
    private var view: View = View.inflate(context, R.layout.dialog_confirm, null)

    init {
        setContentView(view)
        //点击外部不可消失
        //setCancelable(false);
        val confirm = view.findViewById<TextView>(R.id.confirm)
        confirm.setOnClickListener {
            if (onItemClickListener != null) {
                onItemClickListener?.onConfirmClick()
            }
        }
        val cancel = view.findViewById<TextView>(R.id.cancel)
        cancel.setOnClickListener {
            dismiss()
        }
    }

    /**
     *  可通过外部设置提示文本
     */
    fun setTitle(text: String) {
        val textView = view.findViewById<TextView>(R.id.text)
        textView.text = text
    }

    private var onItemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onConfirmClick()
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }

}