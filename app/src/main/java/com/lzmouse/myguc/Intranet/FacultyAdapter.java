package com.lzmouse.myguc.Intranet;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lzmouse.myguc.R;

import java.util.List;

/**
 * Created by Ahmed Ali on 3/16/2018.
 */

public class FacultyAdapter extends RecyclerView.Adapter<FacultyAdapter.ViewHolder>{
    public static interface Listener{
        void onFacultyClick(Faculty faculty);

    }
    private Context context;
    private List<Faculty> faculties;
    private Listener listener;
    public FacultyAdapter(Context context,Listener listener,List<Faculty>faculties)
    {
        this.context = context;
        this.faculties = faculties;
        this.listener = listener;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final ViewHolder holder =  new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.fac_view_layout,parent,false));
        holder.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = holder.getAdapterPosition();
                if(pos != RecyclerView.NO_POSITION && pos < faculties.size())
                {
                    Faculty faculty = faculties.get(pos);
                    listener.onFacultyClick(faculty);
                }
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Faculty faculty = faculties.get(position);
        holder.txt.setText(faculty.getName());
        holder.image.setImageResource(faculty.getDrawableId());
    }

    @Override
    public int getItemCount() {
        return faculties.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        TextView txt;
        View parent;
        ImageView image;
        public ViewHolder(View itemView) {
            super(itemView);
            txt = itemView.findViewById(R.id.text);
            parent =  itemView.findViewById(R.id.parent);
            image = itemView.findViewById(R.id.image);
        }
    }
    public static class Faculty
    {
        private String name,link;
        private int drawableId;

        public Faculty(String name,String link, int drawableId) {

            this.name = name;
            this.drawableId = drawableId;
            this.link = link;
        }
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getDrawableId() {
            return drawableId;
        }

        public void setDrawableId(int drawableId) {
            this.drawableId = drawableId;
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }
    }
}
