package com.rahul.media.imagemodule.adapter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.rahul.media.R;
import com.rahul.media.model.MediaObject;

import java.util.ArrayList;
import java.util.HashSet;


public class ImageGalleryGridAdapter extends BaseAdapter {

    private final ArrayList<MediaObject> mediaObjectArrayList;
    private final int pickCount;
    private final Context mContext;
    private final DisplayImageOptions options;
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
        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(context));


        BitmapFactory.Options resizeOptions = new BitmapFactory.Options();
        resizeOptions.inSampleSize = 3; // decrease size 3 times

        options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .showImageOnLoading(R.drawable.placeholder_470x352)
                .showImageForEmptyUri(R.drawable.placeholder_470x352)
                .showImageOnFail(R.drawable.placeholder_470x352)
                .decodingOptions(resizeOptions)
                .cacheOnDisk(true).build();
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
            LayoutInflater vi = LayoutInflater.from(mContext);
            holder = new ViewHolder();
            holder.position = position;
            convertView = vi.inflate(R.layout.list_image_item_gallery, null);
            holder.imgThumb = (SimpleDraweeView) convertView.findViewById(R.id.imgThumbnail);
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

        holder.imgThumb.setImageURI(Uri.parse("file://" + mediaObjectArrayList.get(position).getPath()));

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

    private static class ViewHolder {
        CheckBox selectIv;
        SimpleDraweeView imgThumb;
        TextView videoDuration;
        public int position;
    }

    private void setActionbarTitle(int total) {
        if (pickCount == 1)
            actionBar.setTitle(bucketTitle);
        else
            actionBar.setTitle(bucketTitle + " (" + selectedPositions.size() + "/" + String.valueOf(total) + ")");
    }
}