package com.rahul.media.videomodule.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rahul.media.R;
import com.rahul.media.model.Define;
import com.rahul.media.model.GalleryPhotoAlbum;

import java.util.ArrayList;


public abstract class VideoAlbumListAdapter
        extends RecyclerView.Adapter<VideoAlbumListAdapter.ViewHolder> {

    private ArrayList<GalleryPhotoAlbum> albumList;
    Context mContext;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgAlbum;
        private TextView txtAlbum;
        private TextView txtAlbumCount;
        private RelativeLayout areaAlbum;
        private View view;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            imgAlbum = (ImageView) view.findViewById(R.id.img_album);
            imgAlbum.setLayoutParams(new RelativeLayout.LayoutParams(Define.ALBUM_THUMBNAIL_SIZE, Define.ALBUM_THUMBNAIL_SIZE));
            txtAlbum = (TextView) view.findViewById(R.id.txt_album);
            txtAlbumCount = (TextView) view.findViewById(R.id.txt_album_count);
            areaAlbum = (RelativeLayout) view.findViewById(R.id.area_album);
        }
    }

    public VideoAlbumListAdapter(Context mContext, ArrayList<GalleryPhotoAlbum> path) {
        this.albumList = path;
        this.mContext = mContext;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.album_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        holder.imgAlbum.setVisibility(View.GONE);
        holder.areaAlbum.setTag(albumList.get(position));
        holder.txtAlbum.setText(albumList.get(position).getBucketName());
        holder.txtAlbumCount.setText(albumList.get(position).getTotalCount() + "");
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClick(holder);
            }
        });
    }

    @Override
    public int getItemCount() {
        return albumList.size();
    }

    public abstract void onItemClick(ViewHolder holder);
}


