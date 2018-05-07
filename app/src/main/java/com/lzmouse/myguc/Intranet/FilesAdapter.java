package com.lzmouse.myguc.Intranet;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.lzmouse.myguc.Helper;
import com.lzmouse.myguc.R;

import java.io.File;
import java.util.List;


/**
 * Created by Ahmed Ali on 3/21/2018.
 */

public class FilesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final static int STORAGE_TYPE = 0;
    private final static int FILES_TYPE = 1;

    public interface FileListener {
        void onFileClick(FileInfo fileInfo,int pos);
        void onDeleteClick(FileInfo fileInfo,int pos);
        void onShareClick(FileInfo fileInfo,int pos);
    }



    private Context context;
    private FileListener fileListener;
    private List<FileInfo> files;

    public FilesAdapter(Context context, FileListener fileListener, List<FileInfo> files) {
        this.context = context;
        this.fileListener = fileListener;
        this.files = files;
    }

    private static class FileHolder extends RecyclerView.ViewHolder {
        private ImageView iconView;
        private TextView titleView;
        private TextView sizeView;
        private ImageButton delete,share;
        public FileHolder(View itemView) {
            super(itemView);
            this.iconView =  itemView.findViewById(R.id.icon);
            this.titleView = itemView.findViewById(R.id.title);
            this.sizeView =  itemView.findViewById(R.id.size);
            delete = itemView.findViewById(R.id.delete);
            share  = itemView.findViewById(R.id.share);
        }
    }



    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final RecyclerView.ViewHolder viewHolder;
        final FileHolder fileHolder = new FileHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.file_layout_view, parent, false));
        fileHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = fileHolder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && pos < files.size()) {
                    FileInfo fileInfo =  files.get(fileHolder.getAdapterPosition());
                    fileInfo.setSelected(!fileInfo.isSelected());
                    fileListener.onFileClick(fileInfo,pos);
                }

            }

        });
        fileHolder.share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = fileHolder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && pos < files.size()) {
                    FileInfo fileInfo =  files.get(fileHolder.getAdapterPosition());
                    fileInfo.setSelected(!fileInfo.isSelected());
                    fileListener.onShareClick(fileInfo,pos);
                }

            }

        });
        fileHolder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = fileHolder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && pos < files.size()) {
                    FileInfo fileInfo =  files.get(fileHolder.getAdapterPosition());
                    fileInfo.setSelected(!fileInfo.isSelected());
                    fileListener.onDeleteClick(fileInfo,pos);
                }

            }

        });

        return fileHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        FileInfo fileInfo = files.get(position);
        FileHolder fileHolder = (FileHolder) holder;
        fileInfo.setImageViewIcon(context, fileHolder.iconView);
        fileHolder.titleView.setText(fileInfo.getName());

        if (fileInfo.isFile()) {
            fileHolder.sizeView.setVisibility(View.VISIBLE);
            fileHolder.sizeView.setText(fileInfo.getReadableFileSize());
        } else
            fileHolder.sizeView.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return files.size();
    }



    public static class FileInfo  {

        private File file;
        private boolean selected;

        public FileInfo(File file) {
            this.file = file;
        }


        public void setImageViewIcon(Context context, ImageView imageView) {

            imageView.setColorFilter(null);
            String ext = Helper.getExtension(file.getName());
            switch (ext.toLowerCase()) {
                case "png":
                case "jpg":
                case "jpeg":
                case "gif":
                case "mp4":
                case "avi":
                    Glide.with(context.getApplicationContext()).load(file).centerCrop().error(R.drawable.ic_file_web).into(imageView);
                    break;
                default:
                    imageView.setImageDrawable(Helper.getExtDrawable(context, file));
                    break;
            }
        }


        public boolean isFile() {
            return file.isFile();
        }

        public boolean isSelected() {
            return selected;
        }
        public String getName()
        {
            return file.getName();
        }
        public String getPath()
        {
            return file.getAbsolutePath();
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public String getReadableFileSize() {
            return Helper.getReadableBuffer(file.length());

        }

        public File getFile() {
            return file;
        }
    }


}
