package com.alight.android.aoa_launcher.ui.adapter;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.alight.android.aoa_launcher.R;
import com.alight.android.aoa_launcher.net.model.File;

import java.util.List;

/**
 * 文件下载适配器
 */
public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {
    private List<File> list;
    private Context context;

    public FileAdapter(List<File> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_download, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int i) {
        final File file = list.get(i);
        viewHolder.file_name.setText(file.getFileName());
      /*  if (file.getFileType().equals("doc") || file.getFileType().equals("docx")) {
            viewHolder.img.setImageDrawable(context.getResources().getDrawable(R.drawable.ppt));
        } else if (file.getFileType().equals("xls") || file.getFileType().equals("xlsx")) {
            viewHolder.img.setImageDrawable(context.getResources().getDrawable(R.drawable.xls));
        } else if (file.getFileType().equals("txt")) {
            viewHolder.img.setImageDrawable(context.getResources().getDrawable(R.drawable.xls));
        } else if (file.getFileType().equals("pdf")) {
            viewHolder.img.setImageDrawable(context.getResources().getDrawable(R.drawable.pdf));
        } else if (file.getFileType().equals("mp3")) {
            viewHolder.img.setImageDrawable(context.getResources().getDrawable(R.drawable.mp3));
        } else if (file.getFileType().equals("mp4")) {
            viewHolder.img.setImageDrawable(context.getResources().getDrawable(R.drawable.mp4));
        }
        else */
            if (file.getFileType().equals("zip") || file.getFileType().equals("rar") || file.getFileType().equals("tar.gz") || file.getFileType().equals("tar") ||
                file.getFileType().equals("7z")) {
            viewHolder.img.setImageDrawable(context.getResources().getDrawable(R.drawable.zip));
        } else {
            viewHolder.img.setImageDrawable(context.getResources().getDrawable(R.drawable.file));
        }

        switch (file.getStatus()) {
            case File.DOWNLOAD_PAUSE://暂停->开始
                viewHolder.start.setVisibility(View.VISIBLE);
                viewHolder.download.setVisibility(View.GONE);
                viewHolder.refresh.setVisibility(View.GONE);
                viewHolder.pause.setVisibility(View.GONE);
                viewHolder.progressBar.setVisibility(View.VISIBLE);
                viewHolder.speed.setVisibility(View.GONE);
                viewHolder.progressBar.setProgress(file.getProgress());
                viewHolder.size.setVisibility(View.VISIBLE);
                viewHolder.size.setText(file.getSizeStr());
                break;
            case File.DOWNLOAD_PROCEED://开始->暂停
                viewHolder.start.setVisibility(View.GONE);
                viewHolder.download.setVisibility(View.GONE);
                viewHolder.refresh.setVisibility(View.GONE);
                viewHolder.pause.setVisibility(View.VISIBLE);
                viewHolder.progressBar.setVisibility(View.VISIBLE);
                viewHolder.progressBar.setProgress(file.getProgress());
                viewHolder.progressBar.setVisibility(View.VISIBLE);
                viewHolder.speed.setVisibility(View.VISIBLE);
                viewHolder.speed.setText(file.getSpeed());
                viewHolder.size.setVisibility(View.VISIBLE);
                viewHolder.size.setText(file.getSizeStr());
                break;
            case File.DOWNLOAD_ERROR://出错
                viewHolder.start.setVisibility(View.GONE);
                viewHolder.download.setVisibility(View.GONE);
                viewHolder.refresh.setVisibility(View.VISIBLE);
                viewHolder.pause.setVisibility(View.GONE);
                viewHolder.progressBar.setVisibility(View.GONE);
                viewHolder.speed.setVisibility(View.GONE);
                viewHolder.size.setVisibility(View.GONE);
                viewHolder.refresh.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onItemClickListener.onItemClick(view, i, File.DOWNLOAD_ERROR);
                    }
                });
                break;
            case File.DOWNLOAD_COMPLETE://完成
                viewHolder.progressBar.setVisibility(View.GONE);
                viewHolder.start.setVisibility(View.GONE);
                viewHolder.download.setVisibility(View.GONE);
                viewHolder.refresh.setVisibility(View.GONE);
                viewHolder.pause.setVisibility(View.GONE);
                viewHolder.speed.setVisibility(View.GONE);
                viewHolder.size.setVisibility(View.GONE);
                viewHolder.size_str.setVisibility(View.VISIBLE);
                viewHolder.size_str.setText(file.getSizeStr());
                viewHolder.space.setVisibility(View.VISIBLE);
                viewHolder.create_time.setVisibility(View.VISIBLE);
                viewHolder.create_time.setText(DateFormat.format("yyyy/MM/dd", file.getCreateTime()));
                break;
            case File.DOWNLOAD_REDYA://准备下载 ->开始
                viewHolder.start.setVisibility(View.GONE);
                viewHolder.download.setVisibility(View.VISIBLE);
                viewHolder.refresh.setVisibility(View.GONE);
                viewHolder.pause.setVisibility(View.GONE);
                viewHolder.progressBar.setVisibility(View.GONE);
                viewHolder.speed.setVisibility(View.GONE);
                viewHolder.size.setVisibility(View.GONE);
                viewHolder.create_time.setVisibility(View.GONE);
                viewHolder.space.setVisibility(View.GONE);
                break;
            default:
                break;
        }

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClickListener.onItemClick(view, i, file.getStatus());
            }
        });
        viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                onItemClickListener.onItemLongClick(view, i, file.getStatus());
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView img, start, pause, download;
        private TextView speed, size, space, size_str, create_time, file_name;
        private ProgressBar progressBar;
        private TextView refresh;

        public ViewHolder(View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.img);
            refresh = itemView.findViewById(R.id.refresh);
            file_name = itemView.findViewById(R.id.file_name);
            speed = itemView.findViewById(R.id.speed);
            size = itemView.findViewById(R.id.size);
            start = itemView.findViewById(R.id.start);
            pause = itemView.findViewById(R.id.pause);
            download = itemView.findViewById(R.id.download);
            create_time = itemView.findViewById(R.id.create_time);
            space = itemView.findViewById(R.id.space);
            size_str = itemView.findViewById(R.id.size_str);
            progressBar = itemView.findViewById(R.id.progressBar);
            progressBar.setVisibility(View.GONE);
        }
    }

    //点击 RecyclerView 某条的监听
    public interface OnItemClickListener {

        /**
         * 当RecyclerView某个被点击的时候回调
         *
         * @param view 点击item的视图
         */
        void onItemClick(View view, int position, int type);

        void onItemLongClick(View view, int position, int type);

    }

    private FileAdapter.OnItemClickListener onItemClickListener;

    /**
     * 设置RecyclerView某个的监听
     *
     * @param onItemClickListener
     */
    public void setOnItemClickListener(FileAdapter.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

}
