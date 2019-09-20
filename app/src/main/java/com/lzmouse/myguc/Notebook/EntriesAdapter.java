package com.lzmouse.myguc.Notebook;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
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
import com.ohoussein.playpause.PlayPauseView;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Ahmed Ali on 4/23/2018.
 */

public class EntriesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final static int IMAGE_TYPE = 0;
    private final static int RECORD_TYPE = 1;
    private final static int NOTE_TYPE = 2;
    interface Listener
    {
        void onEntryClick(Entry entry,int pos);
        void onEntryDeleted(Entry entry,int pos);
        void onEntryShared(Entry entry,int pos);
        void onEntryNote(Entry entry,int pos);
        void onPopUpMenu(Entry entry,View v,int pos);
        void onPlayPause(RecordEntry recordEntry,int pos);

    }
    private Context context;
    private List<Entry> entryList;
    private Listener listener;

    public EntriesAdapter(Context context, Listener listener,List<Entry> entryList) {
        this.context = context;
        this.entryList = entryList;
        this.listener = listener;

    }

    public static class ImageHolder extends RecyclerView.ViewHolder
    {
        ImageView imageView;
        TextView dateView;
        ImageButton note,share,delete;
        public ImageHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
            dateView = itemView.findViewById(R.id.date);
            note = itemView.findViewById(R.id.note);
            share  = itemView.findViewById(R.id.share);
            delete  = itemView.findViewById(R.id.delete);
        }
    }
    public static class RecordHolder extends RecyclerView.ViewHolder
    {
        PlayPauseView playPauseView;
        TextView title;
        TextView duration;
        ImageButton menu;
        public RecordHolder(View itemView) {
            super(itemView);
            playPauseView = itemView.findViewById(R.id.play_pause_view);
            title = itemView.findViewById(R.id.title);
            duration = itemView.findViewById(R.id.duration);
            menu = itemView.findViewById(R.id.menu);
        }
    }
    public static class NoteHolder extends RecyclerView.ViewHolder
    {
        TextView textView;
        ImageButton share,delete,menu;

        public NoteHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.note);
            share  = itemView.findViewById(R.id.share);
            delete  = itemView.findViewById(R.id.delete);
            menu = itemView.findViewById(R.id.menu);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType)
        {
            case IMAGE_TYPE:
                final ImageHolder holder = new ImageHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.entry_image_view,parent,false));
                holder.imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int pos = holder.getAdapterPosition();
                        if(pos != RecyclerView.NO_POSITION && pos < entryList.size())
                            listener.onEntryClick(entryList.get(pos),pos);
                    }
                });
                holder.delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int pos = holder.getAdapterPosition();
                        if(pos != RecyclerView.NO_POSITION && pos < entryList.size())
                            listener.onEntryDeleted(entryList.get(pos),pos);
                    }
                });
                holder.note.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int pos = holder.getAdapterPosition();
                        if(pos != RecyclerView.NO_POSITION && pos < entryList.size())
                            listener.onEntryNote(entryList.get(pos),pos);                    }
                });
                holder.share.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int pos = holder.getAdapterPosition();
                        if(pos != RecyclerView.NO_POSITION && pos < entryList.size())
                            listener.onEntryShared(entryList.get(pos),pos);
                    }
                });
                return holder;

            case RECORD_TYPE:
                final RecordHolder recordHolder = new RecordHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.entry_record_view,parent,false));
                recordHolder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                int pos = recordHolder.getAdapterPosition();
                                if(pos != RecyclerView.NO_POSITION && pos < entryList.size())
                                    listener.onEntryClick(entryList.get(pos),pos);
                            }
                });
                recordHolder.menu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int pos = recordHolder.getAdapterPosition();
                        if(pos != RecyclerView.NO_POSITION && pos < entryList.size())
                            listener.onPopUpMenu(entryList.get(pos),recordHolder.menu,pos);
                    }
                });
                recordHolder.playPauseView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int pos = recordHolder.getAdapterPosition();
                        if(pos != RecyclerView.NO_POSITION && pos < entryList.size())
                            listener.onPlayPause((RecordEntry) entryList.get(pos),pos);
                    }
                });
               return recordHolder;
            default:
                final NoteHolder noteHolder = new NoteHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.entry_note_view,parent,false));
                noteHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int pos = noteHolder.getAdapterPosition();
                        if(pos != RecyclerView.NO_POSITION && pos < entryList.size())
                            listener.onEntryClick(entryList.get(pos),pos);
                    }
                });
                noteHolder.menu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int pos = noteHolder.getAdapterPosition();
                        if(pos != RecyclerView.NO_POSITION && pos < entryList.size())
                            listener.onPopUpMenu(entryList.get(pos),noteHolder.menu,pos);
                    }
                });
                return noteHolder;
        }

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder h, int position) {
        Entry  entry = entryList.get(position);
        switch (getItemViewType(position))
        {
            case IMAGE_TYPE:
                ImageEntry imageEntry = (ImageEntry)entry;
                ImageHolder holder = (ImageHolder) h ;
                Glide.with(context).load(imageEntry.getImagePath()).thumbnail(.3f).into(holder.imageView);
                holder.dateView.setText(Helper.getDate(imageEntry.getDate()));
                break;
            case RECORD_TYPE:
                RecordEntry recordEntry = (RecordEntry)entry;
                RecordHolder recordHolder = (RecordHolder)h;

                if(recordEntry.getDuration(context) == -1)
                    recordHolder.duration.setText("Recording...");
                else
                    recordHolder.duration.setText(String.format("%02d min, %02d sec",
                            TimeUnit.MILLISECONDS.toMinutes(recordEntry.getDuration(context)),
                            TimeUnit.MILLISECONDS.toSeconds(recordEntry.getDuration(context)) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(recordEntry.getDuration(context)))
                    ));

                recordHolder.title.setText(recordEntry.getName());
                break;
            default:
                NoteHolder noteHolder = (NoteHolder) h;
                noteHolder.textView.setText(entry.getNote());
                break;
        }
    }

    @Override
    public int getItemCount() {
        return entryList.size();
    }

    @Override
    public int getItemViewType(int position) {
       Entry entry =  entryList.get(position);
       if(entry instanceof ImageEntry)
           return IMAGE_TYPE;
       else if(entry instanceof RecordEntry)
           return RECORD_TYPE;
       else
           return NOTE_TYPE;
    }

    public static class Entry
    {
        String note;
        long id,sourceId,date;
        public static final int TYPE_IMAGE = 0;
        public static final int TYPE_VIDEO = 1;
        public static final int TYPE_RECORD = 2;
        public static final int TYPE_NOTE = 3;


        public Entry(long id,long sourceId,String note,long date)
        {
            this.sourceId =sourceId;
            this.note = note;
            this.date = date;
            this.id = id;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }

        public long getDate() {
            return date;
        }

        public void setDate(long date) {
            this.date = date;
        }
        public  int getType()
        {
            if(this instanceof ImageEntry) {
                ImageEntry imageEntry = (ImageEntry) this;
                return imageEntry.isVideo ? TYPE_VIDEO:TYPE_IMAGE;
            }
            else if(this instanceof RecordEntry)
                return TYPE_RECORD;
            else
                return TYPE_NOTE;
        }
    }
    public static class ImageEntry extends Entry{
        String imagePath;boolean isVideo;
        public ImageEntry(long id,long sourceId,String note,long date,String imagePath,boolean isVideo)
        {
            super(id,sourceId,note,date);
            this.imagePath = imagePath;
            this.isVideo = isVideo;
        }

        public String getImagePath() {
            return imagePath;
        }

        public void setImagePath(String imagePath) {
            this.imagePath = imagePath;
        }

        public boolean isVideo() {
            return isVideo;
        }

        public void setVideo(boolean video) {
            isVideo = video;
        }
    }
    public static class RecordEntry extends Entry
    {
        private String recordPath,name;
        private long duration = -1;
        public RecordEntry(long id,long sourceId,String note,long date,String name,String recordPath)
        {
            super(id,sourceId,note,date);
            this.recordPath = recordPath;
            this.name= name;
        }

        public String getRecordPath() {
            return recordPath;
        }

        public void setRecordPath(String recordPath) {
            this.recordPath = recordPath;
        }

        public long getDuration(Context context) {
            if(duration == -1) {
                MediaPlayer mp = MediaPlayer.create(context, Uri.parse(getRecordPath()));
                if (mp == null)
                    return -1;
                setDuration(mp.getDuration());
            }
            return duration;
        }

        public void setDuration(long duration) {
            this.duration = duration;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
