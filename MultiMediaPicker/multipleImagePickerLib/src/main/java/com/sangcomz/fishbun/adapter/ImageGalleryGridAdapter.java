package com.sangcomz.fishbun.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.luminous.pick.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.sangcomz.fishbun.bean.MediaObject;
import com.sangcomz.fishbun.util.SquareImageView;

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

        /*if (!TextUtils.isEmpty(mediaObjectArrayList.get(position).getPath())) {
            Bitmap bitmap = MediaSingleTon.getInstance().getImage(mediaObjectArrayList.get(position).getPath());
            if (bitmap != null) {
                try {
                    Log.i("Image", "Set From Bitmap");
                    holder.imgThumb.setImageBitmap(bitmap);
                } catch (Exception e) {
                    Log.i("Image", "Set From Bitmap Failed");
                    loadImage(mediaObjectArrayList.get(position).getPath(), holder.imgThumb);
                }
            } else {
                loadImage(mediaObjectArrayList.get(position).getPath(), holder.imgThumb);
            }
        } else {
            holder.imgThumb.setImageResource(R.drawable.loading_img);
        }*/

        /*Bitmap bitmap = MediaSingleTon.getInstance().getImage(mediaObjectArrayList.get(position).getPath());
        if (bitmap != null) {
            holder.imgThumb.setImageBitmap(bitmap);
            Log.i("Image", "Set From Bitmap : " + position);
        } else {
            Log.i("Image", "Set From Glide  : " + position);
            loadImage(mediaObjectArrayList.get(position).getPath(), holder.imgThumb);
        }*/

        loadImage(mediaObjectArrayList.get(position).getPath(), holder.imgThumb);

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

    private void loadImage(final String path, final SquareImageView imgThumb) {

        ImageLoader.getInstance().displayImage("file://" + path, new ImageView(mContext), options, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {

            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {

            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                Log.i("Image", "UIL Bitmap Loaded");
            }

            @Override
            public void onLoadingCancelled(String s, View view) {

            }
        });
        /*Glide.with(imgThumb.getContext())
                .load(path)
                .asBitmap()
                .placeholder(R.drawable.loading_img)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap bitmap, GlideAnimation glideAnimation) {
                        Log.i("Image", "Bitmap Loaded");
                        MediaSingleTon.getInstance().putImage(path, bitmap);
                        imgThumb.setImageBitmap(bitmap);
                    }
                });*/
        Glide.with(imgThumb.getContext())
                .load(path)
                .asBitmap()
                .override(150, 150)
                .placeholder(R.drawable.loading_img)
                .into(imgThumb);
//        Picasso.with(imgThumb.getContext())
//                .load(new File(path))
//                .placeholder(R.drawable.loading_img)
//                .into(imgThumb);

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