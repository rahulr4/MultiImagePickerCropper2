package com.sangcomz.fishbun.videomodule.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.luminous.pick.R;
import com.sangcomz.fishbun.bean.GalleryPhotoAlbum;
import com.sangcomz.fishbun.define.Define;
import com.sangcomz.fishbun.videomodule.VideoAlbumGalleryActivity;

import java.util.ArrayList;


public class VideoAlbumListAdapter
        extends RecyclerView.Adapter<VideoAlbumListAdapter.ViewHolder> {

    private ArrayList<GalleryPhotoAlbum> albumlist;
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
            imgAlbum.setLayoutParams(new RelativeLayout.LayoutParams(Define.ALBUM_THUMNAIL_SIZE, Define.ALBUM_THUMNAIL_SIZE));
            txtAlbum = (TextView) view.findViewById(R.id.txt_album);
            txtAlbumCount = (TextView) view.findViewById(R.id.txt_album_count);
            areaAlbum = (RelativeLayout) view.findViewById(R.id.area_album);
        }
    }

    public VideoAlbumListAdapter(Context mContext, ArrayList<GalleryPhotoAlbum> path) {
        this.albumlist = path;
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
        holder.areaAlbum.setTag(albumlist.get(position));
        holder.txtAlbum.setText(albumlist.get(position).getBucketName());
        holder.txtAlbumCount.setText(albumlist.get(position).getTotalCount() + "");
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, VideoAlbumGalleryActivity.class);
                intent.putExtra("bucketName", albumlist.get(position).getBucketName());

                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return albumlist.size();
    }


}


