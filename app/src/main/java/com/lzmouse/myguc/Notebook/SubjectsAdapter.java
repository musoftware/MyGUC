package com.lzmouse.myguc.Notebook;

import android.support.annotation.ColorInt;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.lzmouse.myguc.R;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Ahmed Ali on 4/16/2018.
 */

public class SubjectsAdapter extends  RecyclerView.Adapter<SubjectsAdapter.ViewHolder>{
    interface Listener {
        void onSubjectClick(Subject subject,int index);
        void onDeleteClick(Subject subject,int index);
    }
    private List<Subject> subjects;
    private Listener listener;
    public SubjectsAdapter(Listener listener,List<Subject> subjects)
    {
        this.subjects = subjects;
        this.listener = listener;
    }
    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView txtName,txtLecs,txtTuts,txtFiles;
        ImageButton delete;
        public ViewHolder(View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.name);
            txtLecs = itemView.findViewById(R.id.lectures);
            txtTuts = itemView.findViewById(R.id.tuts);
            txtFiles = itemView.findViewById(R.id.files);
            delete = itemView.findViewById(R.id.delete);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final ViewHolder holder = new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.subject_view,parent,false));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = holder.getAdapterPosition();
                if(pos != RecyclerView.NO_POSITION && pos < subjects.size())
                    listener.onSubjectClick(subjects.get(pos),pos);
            }
        });
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = holder.getAdapterPosition();
                if(pos != RecyclerView.NO_POSITION && pos < subjects.size())
                    listener.onDeleteClick(subjects.get(pos),pos);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Subject subject =  subjects.get(position);
        holder.delete.setColorFilter(subject.getColor(), android.graphics.PorterDuff.Mode.SRC_IN);
        holder.txtName.setText(subject.getName());
        holder.txtName.setBackgroundColor(subject.getColor());
        holder.txtFiles.setText(String.format("Files: %02d", subject.getFiles()));
        holder.txtTuts.setText(String.format("Tutorials: %02d", subject.getTuts()));
        holder.txtLecs.setText(String.format("Lectures: %02d", subject.getLectures()));
    }

    @Override
    public int getItemCount() {
        return subjects.size();
    }

    public static class Subject implements Serializable{
        private long uid;
        private int  lectures, tuts,files, color;
        private String name;
        private long date;

        public Subject(long uid, String name,long date, @ColorInt int color)
        {
            this.uid = uid;
            this.name = name;
            this.color = color;
            this.date = date;
        }
        public Subject(long uid, String name, long date,int lectures, int tuts, int files, @ColorInt  int color) {
            this(uid,name,date,color);
            this.lectures = lectures;
            this.tuts = tuts;
            this.files = files;

        }

        public long getDate() {
            return date;
        }

        public void setDate(long date) {
            this.date = date;
        }

        public int getColor() {
            return color;
        }

        public void setColor(int color) {
            this.color = color;
        }

        public long getUid() {
            return uid;
        }


        public int getLectures() {
            return lectures;
        }

        public void setLectures(int lectures) {
            this.lectures = lectures;
        }

        public int getTuts() {
            return tuts;
        }

        public void setTuts(int tuts) {
            this.tuts = tuts;
        }

        public int getFiles() {
            return files;
        }

        public void setFiles(int files) {
            this.files = files;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Subject subject = (Subject) o;

            return uid == subject.uid;
        }

        @Override
        public int hashCode() {
            return (int) (uid ^ (uid >>> 32));
        }

        @Override
        public String toString() {
            return getName();
        }
    }
}
