package com.alight.android.aoa_launcher.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alight.android.aoa_launcher.R
import kotlinx.android.synthetic.main.item_dialog_launcher.view.*

class LauncherAppDialogAdapter(var context: Context, var mList: List<String>) :
    RecyclerView.Adapter<LauncherAppDialogAdapter.Holder>() {
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): Holder {
        var view = LayoutInflater.from(context).inflate(R.layout.item_dialog_launcher, null)
        return Holder(view)
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.itemView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        holder.itemView.tv_app_name_dialog.text = mList[position]
//        holder.itemView.hot_Image.setImageURI(mList[position].imageUrl)
//        Glide.with(context).load(mList.get(position).imageUrl).into(holder.itemView.hot_Image)
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView)
}