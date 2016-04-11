package com.sangcomz.fishbun.adapter;

import android.content.Context;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.luminous.pick.R;
import com.sangcomz.fishbun.bean.MediaObject;
import com.sangcomz.fishbun.util.SquareImageView;

import java.util.ArrayList;
import java.util.HashSet;


public class ImageGalleryGridAdapter extends BaseAdapter {

    private final ArrayList<MediaObject> mediaObjectArrayList;
    private final int pickCount;
    private final Context mContext;
    String saveDir;
    ActionBar actionBar;
    private String bucketTitle;

    public ImageGalleryGridAdapter(Context context, ArrayList<MediaObject> pickedImageBeans,
                                   String saveDir, int pickCount, ActionBar supportActionBar, String bucketTitle) {
        this.mContext = context;
        this.mediaObjectArrayList = pickedImageBeans;
        this.saveDir = saveDir;
        this.pickCount = pickCount;
        this.actionBar = supportActionBar;
        this.bucketTitle = bucketTitle;
    }

    HashSet<Integer> selectedPositions = new HashSet<>();

    @Override
    public int getCount() {
        return mediaObjectArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            holder = new ViewHolder();
            holder.position = position;
            convertView = vi.inflate(R.layout.list_item_gallery, null);
            holder.imgThumb = (SquareImageView) convertView.findViewById(R.id.imgThumbnail);
            holder.videoDuration = (TextView) convertView.findViewById(R.id.txtDuration);
            holder.selectIv = (CheckBox) convertView
                    .findViewById(R.id.imgQueueMultiSelected);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (mediaObjectArrayList.get(position).isSelected) {
            selectedPositions.add(position);
        } else
            selectedPositions.remove(position);

        Glide.with(holder.imgThumb.getContext())
                .load(mediaObjectArrayList.get(position).getPath())
                .asBitmap()
                /*.override(Define.ALBUM_THUMBNAIL_SIZE, Define.ALBUM_THUMBNAIL_SIZE)*/
                .placeholder(R.drawable.loading_img)
                .into(holder.imgThumb);

        holder.selectIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performCheck(holder, position);
            }
        });

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performCheck(holder, position);
            }
        });

        holder.selectIv.setChecked(mediaObjectArrayList.get(position).isSelected);

        return convertView;
    }

    private void performCheck(ViewHolder holder, int position) {
        if (selectedPositions.size() == pickCount && !mediaObjectArrayList.get(position).isSelected) {
            if (pickCount == 1)
                Toast.makeText(mContext, "You can select max " + pickCount + " image", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(mContext, "You can select max " + pickCount + " images", Toast.LENGTH_SHORT).show();
        } else {
            if (!mediaObjectArrayList.get(position).isSelected) {
                selectedPositions.add(position);
            } else
                selectedPositions.remove(position);

            mediaObjectArrayList.get(position).isSelected = !mediaObjectArrayList.get(position).isSelected;
            setActionbarTitle(getCount());
        }
        holder.selectIv.setChecked(mediaObjectArrayList.get(position).isSelected);
    }

    public static class ViewHolder {
        public CheckBox selectIv;
        public SquareImageView imgThumb;
        public TextView videoDuration;
        public MediaObject object;
        public int position;
    }

    public void setActionbarTitle(int total) {
        if (pickCount == 1)
            actionBar.setTitle(bucketTitle);
        else
            actionBar.setTitle(bucketTitle + " (" + selectedPositions.size() + "/" + String.valueOf(total) + ")");
    }
}