package com.lzmouse.myguc.Main;

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

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.ViewHolder>{

    public static interface Listener{
        void onClick(Item item);
    }
    private Context context;
    private List<Item> items;
    private Listener listener;
    public MainAdapter(Context context,Listener listener,List<Item>items)
    {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final ViewHolder v = new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.main_view_layout,parent,false));
        v.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = v.getAdapterPosition();
                if(pos != RecyclerView.NO_POSITION && pos < items.size())
                {
                    Item i  = items.get(pos);
                    listener.onClick(i);
                }
            }
        });
        return v;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Item item = items.get(position);
        holder.txt.setText(item.getText());
        holder.image.setImageResource(item.getColorId());
    }

    @Override
    public int getItemCount() {
        return items.size();
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
    public static class Item
    {
        private String text;
        private int drawableId;
        public Item(String text,int drawableId)
        {
            this.text = text;
            this.drawableId = drawableId;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public int getColorId() {
            return drawableId;
        }

        public void setColorId(int colorId) {
            this.drawableId = colorId;
        }
    }
}
