package com.lzmouse.myguc.Intranet;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.like.LikeButton;
import com.like.OnLikeListener;
import com.lzmouse.myguc.R;

import java.io.Serializable;
import java.util.List;


/**
 * Created by Ahmed Ali on 3/17/2018.
 */

public class LinksAdapter extends RecyclerView.Adapter<LinksAdapter.ViewHolder> {


    public  interface Listener{
        void onClick(Link link);
        void onFavStateChanged(Link link,boolean newState,int pos);
    }

    private Context context;
    private Listener listener;
    private List<Link> links;
    private boolean isFavActivity;

    public LinksAdapter(Context context, Listener listener, List<Link> links,boolean isFavActivity) {
        this.context = context;
        this.listener = listener;
        this.links = links;
        this.isFavActivity =isFavActivity;
    }

    public static class ViewHolder  extends RecyclerView.ViewHolder
    {
        View parent;
        TextView name;
        LikeButton fav;
        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.text);
            fav = itemView.findViewById(R.id.fav);
            parent = itemView.findViewById(R.id.parent);
        }
    }

    public static class FavHolder extends ViewHolder{
        View parent;
        TextView name,path;
        LikeButton fav;

        public FavHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.text);
            fav = itemView.findViewById(R.id.fav);
            parent = itemView.findViewById(R.id.parent);
            path =itemView.findViewById(R.id.path);
        }

    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
       final ViewHolder holder;
       if(isFavActivity)
           holder = new FavHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.fav_view_layout,parent,false));
       else
           holder = new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.link_view_layout,parent,false));
       holder.parent.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               int pos = holder.getAdapterPosition();
               if(pos != RecyclerView.NO_POSITION && pos < links.size())
               {
                   Link link = links.get(pos);
                   listener.onClick(link);
               }
           }
       });
       holder.fav.setOnLikeListener(new OnLikeListener() {
           @Override
           public void liked(LikeButton likeButton) {
               int pos = holder.getAdapterPosition();
               if(pos != RecyclerView.NO_POSITION && pos < links.size()) {
                    Link link = links.get(pos);
                    link.setFav(true);
                    listener.onFavStateChanged(link,true,pos);
               }
           }

           @Override
           public void unLiked(LikeButton likeButton) {
               int pos = holder.getAdapterPosition();
               if(pos != RecyclerView.NO_POSITION && pos < links.size()) {
                   Link link = links.get(pos);
                   link.setFav(false);
                   listener.onFavStateChanged(link,false,pos);
               }
           }
       });
       return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Link link = links.get(position);
        holder.name.setText(link.getName());
        if(!link.isFile())
        {
            holder.fav.setVisibility(View.VISIBLE);
            holder.fav.setLiked(link.isFav());
        }
        else
            holder.fav.setVisibility(View.GONE);
        if(isFavActivity)
            ((FavHolder)holder).path.setText(link.getLink());
    }

    @Override
    public int getItemCount() {
        return links.size();
    }
    public static class Link implements Serializable
    {
        private String name,link;
        private boolean isFav,isFile;

        public Link(String name, String link, boolean isFav,boolean isFile) {
            this.name = name;
            this.link = link;
            this.isFav = isFav;
            this.isFile = isFile;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }

        public boolean isFav() {
            return isFav;
        }

        public void setFav(boolean fav) {
            isFav = fav;
        }

        public boolean isFile() {
            return isFile;
        }

        public void setFile(boolean file) {
            isFile = file;
        }

        @Override
        public String toString() {
            return name + "_" + link;
        }
    }
}
