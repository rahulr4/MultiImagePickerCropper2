package com.rahul.media.videomodule.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import com.rahul.media.R;
import com.rahul.media.model.GalleryPhotoAlbum;

public abstract class VideoAlbumListAdapter
        extends RecyclerView.Adapter<VideoAlbumListAdapter.ViewHolder> {

    private ArrayList<GalleryPhotoAlbum> albumList;
    Context mContext;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgAlbum;
        private TextView txtAlbum;
        private TextView txtAlbumCount;
        private LinearLayout areaAlbum;
        private View view;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            imgAlbum = (ImageView) view.findViewById(R.id.img_album);
            txtAlbum = (TextView) view.findViewById(R.id.album_name);
            txtAlbumCount = (TextView) view.findViewById(R.id.album_photos_count);
            areaAlbum = (LinearLayout) view.findViewById(R.id.area_album);
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

        Glide.with(mContext)
                .load(albumList.get(position).getData())
                .apply(new RequestOptions().centerCrop().priority(Priority.HIGH)
                        .error(R.drawable.ic_empty_amoled).placeholder(R.drawable.ic_empty_amoled))
                .into(holder.imgAlbum);

        holder.areaAlbum.setTag(albumList.get(position));
        holder.txtAlbum.setText(albumList.get(position).getBucketName());

        holder.txtAlbumCount.setText(Html.fromHtml(
                "<b><font color='#03A9F4'>" + albumList.get(position).getTotalCount()
                        + "</font></b>" + "<font " +
                        "color='#FFFFFF'> Media </font>"));

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


