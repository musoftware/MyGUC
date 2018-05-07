package com.lzmouse.myguc.Intranet;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lzmouse.myguc.R;

import java.util.List;


/**
 * Created by Ahmed Ali on 18-3-18.
 */

public class PathsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {
    public interface IPathsAdapter
    {
        void onPathClick(Path path, int pos);
    }
    private List<Path>paths;
    private IPathsAdapter iPathsAdapter;

     public PathsAdapter(IPathsAdapter iPathsAdapter, List<Path> paths) {
        this.iPathsAdapter = iPathsAdapter;
        this.paths = paths;
    }

    private static class ViewHolder extends RecyclerView.ViewHolder{
         ViewHolder(View itemView) {
            super(itemView);
        }
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.path_view,parent,false));
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final Path path = paths.get(position);
        TextView name =(TextView)holder.itemView.findViewById(R.id.name);
        name.setText(path.getName());
        ImageView arrow =(ImageView)holder.itemView.findViewById(R.id.arrow);
        if(path.showArrow)
            arrow.setVisibility(View.VISIBLE);
        else
            arrow.setVisibility(View.INVISIBLE);
        name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iPathsAdapter.onPathClick(paths.get(holder.getAdapterPosition()),holder.getAdapterPosition());
            }
        });

    }

    @Override
    public int getItemCount() {
        return paths.size();
    }
    public static class Path implements Parcelable
    {
        private String name;
        private String path;
        private boolean showArrow;

        public Path(String name, String path, boolean showArrow) {
            this.name = name;
            this.path = path;
            this.showArrow=showArrow;
        }
        public Path(Parcel in) {
            super();
            readFromParcel(in);
        }

        String getName() {
            return name;
        }

        void setName(String name) {
            this.name = name;
        }

        public String getPath() {
            return path;
        }

        void setPath(String path) {
            this.path = path;
        }

        public boolean isShowArrow() {
            return showArrow;
        }

        public void setShowArrow(boolean showArrow) {
            this.showArrow = showArrow;
        }

        @Override
        public int hashCode() {
            return path.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Path && path.equals(((Path)obj).getPath());
        }



        public static final Creator<Path> CREATOR = new Creator<Path>() {
            public Path createFromParcel(Parcel in) {
                return new Path(in);
            }

            public Path[] newArray(int size) {

                return new Path[size];
            }

        };

        public void readFromParcel(Parcel in) {
            name = in.readString();
            path = in.readString();
            showArrow = in.readByte() != 0;
        }
        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(name);
            dest.writeString(path);
            dest.writeByte((byte) (showArrow ? 1 : 0));     //if myBoolean == true, byte == 1

        }
    }
}
