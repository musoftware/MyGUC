package com.lzmouse.myguc.Notebook;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.lzmouse.myguc.Helper;
import com.lzmouse.myguc.R;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Ahmed Ali on 4/26/2018.
 */

public class LecturesAdapter extends RecyclerView.Adapter<LecturesAdapter.ViewHolder> {




    interface  Listener {
        void onLectureClick(Lecture lecture,int pos);
        void onDeleteClick(Lecture lecture,int pos);
    }

    private List<Lecture> lectures;
    private Listener listener;
    public LecturesAdapter(Listener listener,List<Lecture> lectures)
    {
        this.listener = listener;
        this.lectures = lectures;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView name,date;
        ImageButton delete;
        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.title);
            date = itemView.findViewById(R.id.date);
            delete = itemView.findViewById(R.id.delete);
        }
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final ViewHolder holder =
                new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.lecture_view,parent,false));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = holder.getAdapterPosition();
                if(pos != RecyclerView.NO_POSITION && pos < lectures.size())
                    listener.onLectureClick(lectures.get(pos),pos);
            }
        });
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = holder.getAdapterPosition();
                if(pos != RecyclerView.NO_POSITION && pos < lectures.size())
                    listener.onDeleteClick(lectures.get(pos),pos);
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Lecture lecture = lectures.get(position);
        holder.name.setText(lecture.getName());
        holder.date.setText(Helper.getDate(lecture.date));
    }

    @Override
    public int getItemCount() {
        return lectures.size();
    }
    public static class Lecture implements Serializable
    {
        private long id;
        private long subjectId;
        private String name;
        private long date;
        public Lecture(long id,long subjectId,String name,long date)
        {
            this.id = id;
            this.subjectId = subjectId;
            this.name = name;
            this.date = date;
        }

        public long getSubjectId() {
            return subjectId;
        }

        public void setSubjectId(long subjectId) {
            this.subjectId = subjectId;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getDate() {
            return date;
        }

        public void setDate(long date) {
            this.date = date;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Lecture lecture = (Lecture) o;

            return id == lecture.id;
        }

        @Override
        public int hashCode() {
            return (int) (id ^ (id >>> 32));
        }
    }
}
