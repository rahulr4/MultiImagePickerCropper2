package com.luminous.pick;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.HashMap;

public class ImageListAdapter extends BaseAdapter {
    Context mContext;
    public ArrayList<CustomGallery> imageUri;
    private ImageLoader imageLoader;
    private DisplayImageOptions imageOptions;

    public ImageListAdapter(Context context,
                            HashMap<String, CustomGallery> imagesUri) {
        mContext = context;

        this.imageUri = new ArrayList<CustomGallery>(imagesUri.values());

        imageLoader = ImageLoader.getInstance();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                mContext).build();
        imageLoader.init(config);
        imageOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .showImageOnLoading(R.drawable.placeholder_470x352)
                .showImageForEmptyUri(R.drawable.placeholder_470x352)
                .showImageOnFail(R.drawable.placeholder_470x352)
                .cacheOnDisk(true).build();
    }

    public void customNotify(HashMap<String, CustomGallery> dataHashmap) {
        imageUri.clear();
        ArrayList<CustomGallery> dataT2 = new ArrayList<CustomGallery>(dataHashmap.values());
        this.imageUri.addAll(dataT2);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {

        return imageUri.size();
    }

    @Override
    public Object getItem(int position) {

        return imageUri.get(position);
    }

    @Override
    public long getItemId(int position) {

        return position;
    }
    ViewHolder holder = null;
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            holder = new ViewHolder();
            LayoutInflater mInflater = (LayoutInflater) mContext
                    .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.image_strip_view,
                    parent, false);

            holder.imageView = (ImageView) convertView
                    .findViewById(R.id.strip_image);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        imageLoader.displayImage("file://" + imageUri.get(position).sdcardPath,
                holder.imageView, new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        holder.imageView
                                .setImageResource(R.drawable.placeholder_470x352);
                        super.onLoadingStarted(imageUri, view);
                    }
                });
        
        return convertView;

    }

    static class ViewHolder {
        ImageView imageView;
    }
}