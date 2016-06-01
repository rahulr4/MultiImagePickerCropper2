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
    private List<Album> albumList;
    private List<String> thumbList = new ArrayList<String>();

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgAlbum;
        private TextView txtAlbum;
        private TextView txtAlbumCount;
        private RelativeLayout areaAlbum;


        public ViewHolder(View view) {
            super(view);
            imgAlbum = (ImageView) view.findViewById(R.id.img_album);
            txtAlbum = (TextView) view.findViewById(R.id.txt_album);
            txtAlbumCount = (TextView) view.findViewById(R.id.txt_album_count);
            areaAlbum = (RelativeLayout) view.findViewById(R.id.area_album);
        }
    }

    public ImageAlbumListAdapter(List<Album> albumList, int pickCount) {
        this.albumList = albumList;
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

        String thumbPath = "";
        if (thumbList != null && thumbList.size() > position)
            thumbPath = thumbList.get(position);


        /*if (!TextUtils.isEmpty(thumbPath)) {
            Bitmap bitmap = MediaSingleTon.getInstance().getImage(thumbPath);
            if (bitmap != null) {
                try {
                    Log.i("Image", "Set From Bitmap");
                    holder.imgAlbum.setImageBitmap(bitmap);
                } catch (Exception e) {
                    Log.i("Image", "Set From Bitmap Failed");
                    loadImage(thumbPath, holder.imgAlbum);
                }
            } else
                loadImage(thumbPath, holder.imgAlbum);

        } else {
            holder.imgAlbum.setImageResource(R.drawable.loading_img);
        }*/

        loadImage(thumbPath, holder.imgAlbum);

        holder.areaAlbum.setTag(albumList.get(position));
        Album a = (Album) holder.areaAlbum.getTag();
        holder.txtAlbum.setText(albumList.get(position).bucketname);
        holder.txtAlbumCount.setText(String.valueOf(a.counter));


        holder.areaAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Album a = (Album) v.getTag();
                Intent i = new Intent(holder.areaAlbum.getContext(), ImageGalleryPickerActivity.class);
                i.putExtra("album", a);
                i.putExtra("album_title", albumList.get(position).bucketname);
                i.putExtra("pickCount", pickCount);
                ((Activity) holder.areaAlbum.getContext()).startActivityForResult(i, Define.ENTER_ALBUM_REQUEST_CODE);
            }
        });
    }

    private void loadImage(String thumbPath, final ImageView imgAlbum) {
        final String finalThumbPath = thumbPath;
        /*Glide.with(imgAlbum.getContext())
                .load(thumbPath)
                .asBitmap()
                .placeholder(R.drawable.loading_img)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap bitmap, GlideAnimation glideAnimation) {
                        Log.i("Image", "Bitmap Loaded");
                        MediaSingleTon.getInstance().putImage(finalThumbPath, bitmap);
                        imgAlbum.setImageBitmap(bitmap);
                    }
                });*/
        Glide.with(imgAlbum.getContext())
                .load(thumbPath)
                .asBitmap()
                .placeholder(R.drawable.loading_img)
                .into(imgAlbum);

    }

    @Override
    public int getItemCount() {
        return albumList.size();
    }


}


