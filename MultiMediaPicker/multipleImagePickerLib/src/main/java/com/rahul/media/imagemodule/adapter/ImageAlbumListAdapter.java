package com.rahul.media.imagemodule.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.rahul.media.R;
import com.rahul.media.imagemodule.ImageGalleryPickerActivity;
import com.rahul.media.model.Album;
import com.rahul.media.model.Define;
import com.rahul.media.utils.MediaSingleTon;
import com.rahul.media.utils.MediaUtility;
import com.rahul.media.utils.SquareImageView;

public class ImageAlbumListAdapter
        extends RecyclerView.Adapter<ImageAlbumListAdapter.ViewHolder> {

    private final int pickCount;
    private List<Album> albumList;
    private List<String> thumbList = new ArrayList<String>();

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private SquareImageView imgAlbum;
        private TextView albumNameTv;
        private TextView albumCountTv;
        private LinearLayout areaAlbum;

        public ViewHolder(View view) {
            super(view);
            imgAlbum = (SquareImageView) view.findViewById(R.id.img_album);
            albumNameTv = (TextView) view.findViewById(R.id.album_name);
            albumCountTv = (TextView) view.findViewById(R.id.album_photos_count);
            areaAlbum = (LinearLayout) view.findViewById(R.id.area_album);
        }
    }

    public ImageAlbumListAdapter(List<Album> albumList, int pickCount) {
        this.albumList = albumList;
        this.pickCount = pickCount;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.image_album_item, parent, false);
        return new ViewHolder(view);
    }

    public void setThumbList(List<String> thumbList) {
        this.thumbList = thumbList;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        String thumbPath = "";
        if (thumbList != null && thumbList.size() > position) {
            thumbPath = thumbList.get(position);
        }

        loadImage(thumbPath, holder.imgAlbum);

        holder.areaAlbum.setTag(albumList.get(position));
        Album a = (Album) holder.areaAlbum.getTag();
        holder.albumNameTv.setText(albumList.get(position).bucketname);

        holder.albumCountTv.setText(
                Html.fromHtml("<b><font color='#03A9F4'>" + a.counter + "</font></b>" + "<font " +
                        "color='#FFFFFF'> Media </font>"));

        holder.areaAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Album a = (Album) v.getTag();
                Intent i = new Intent(holder.areaAlbum.getContext(),
                        ImageGalleryPickerActivity.class);
                i.putExtra("album", a);
                i.putExtra("album_title", albumList.get(position).bucketname);
                i.putExtra("pickCount", pickCount);
                ((Activity) holder.areaAlbum.getContext())
                        .startActivityForResult(i, Define.ENTER_ALBUM_REQUEST_CODE);
            }
        });
    }

    private void loadImage(String thumbPath, final SquareImageView imgAlbum) {
        Context mContext = imgAlbum.getContext();
        byte[] imageByte = MediaSingleTon.getInstance().getImageByte(thumbPath);
        if (imageByte != null) {
            Glide.with(mContext)
                    .load(imageByte)
                    .apply(new RequestOptions().centerCrop())
                    .into(imgAlbum);
        } else {
            byte[] thumbnail = MediaUtility.getThumbnail(thumbPath);
            if (thumbnail != null) {
                MediaSingleTon.getInstance().putImageByte(thumbPath, thumbnail);
                Glide.with(mContext)
                        .load(thumbnail)
                        .apply(new RequestOptions().centerCrop())
                        .into(imgAlbum);
            } else {
                Glide.with(mContext)
                        .load(Uri.parse("file://" + thumbPath))
                        .apply(new RequestOptions().centerCrop()
                                .override(200,
                                        200)) // resizes the image to these dimensions (in pixel). does not respect aspect ratio
                        .into(imgAlbum);
            }
        }
//        imgAlbum.setImageURI(Uri.parse("file://" + thumbPath));
    }

    @Override
    public int getItemCount() {
        return albumList.size();
    }
}


