package com.sangcomz.fishbun.adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.luminous.pick.R;
import com.sangcomz.fishbun.bean.Album;
import com.sangcomz.fishbun.define.Define;
import com.sangcomz.fishbun.ui.picker.ImageGalleryPickerActivity;

import java.util.ArrayList;
import java.util.List;


public class ImageAlbumListAdapter
        extends RecyclerView.Adapter<ImageAlbumListAdapter.ViewHolder> {

    private final int pickCount;
    private List<Album> albumlist;
    private List<String> thumbList = new ArrayList<String>();
    private String thumPath;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgAlbum;
        private TextView txtAlbum;
        private TextView txtAlbumCount;
        private RelativeLayout areaAlbum;


        public ViewHolder(View view) {
            super(view);
            imgAlbum = (ImageView) view.findViewById(R.id.img_album);
            imgAlbum.setLayoutParams(new RelativeLayout.LayoutParams(Define.ALBUM_THUMBNAIL_SIZE, Define.ALBUM_THUMBNAIL_SIZE));
            txtAlbum = (TextView) view.findViewById(R.id.txt_album);
            txtAlbumCount = (TextView) view.findViewById(R.id.txt_album_count);
            areaAlbum = (RelativeLayout) view.findViewById(R.id.area_album);
        }
    }

    public ImageAlbumListAdapter(List<Album> albumList, int pickCount) {
        this.albumlist = albumList;
        this.pickCount = pickCount;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.album_item, parent, false);
        return new ViewHolder(view);
    }

    public void setThumbList(List<String> thumbList) {
        this.thumbList = thumbList;
        notifyDataSetChanged();
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        if (thumbList != null && thumbList.size() > position)
            thumPath = thumbList.get(position);


        if (thumbList != null) {
            if (thumbList.size() > position) {
                Glide
                        .with(holder.imgAlbum.getContext())
                        .load(thumPath)
                        .asBitmap()
                        /*.override(Define.ALBUM_THUMBNAIL_SIZE, Define.ALBUM_THUMBNAIL_SIZE)*/
                        .placeholder(R.drawable.loading_img)
                        .into(holder.imgAlbum);
            } else {
                Glide.with(holder.imgAlbum.getContext()).load(R.drawable.loading_img).into(holder.imgAlbum);
            }
        }
        holder.areaAlbum.setTag(albumlist.get(position));
        Album a = (Album) holder.areaAlbum.getTag();
        holder.txtAlbum.setText(albumlist.get(position).bucketname);
        holder.txtAlbumCount.setText(String.valueOf(a.counter));


        holder.areaAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Album a = (Album) v.getTag();
                Intent i = new Intent(holder.areaAlbum.getContext(), ImageGalleryPickerActivity.class);
                i.putExtra("album", a);
                i.putExtra("album_title", albumlist.get(position).bucketname);
                i.putExtra("pickCount", pickCount);
                ((Activity) holder.areaAlbum.getContext()).startActivityForResult(i, Define.ENTER_ALBUM_REQUEST_CODE);
            }
        });
    }

    @Override
    public int getItemCount() {
        return albumlist.size();
    }


}


