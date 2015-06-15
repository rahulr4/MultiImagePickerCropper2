package com.app.multiimagepickercropper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.HashMap;

public class ImageAdapter extends BaseAdapter {
    private final LayoutInflater mInflater;
    ArrayList<MediaBean> mMediaPathArrayList2;
    private ImageLoader imageLoader;
    private ViewHolder holder;

    public ArrayList<MediaBean> getmMediaPathArrayList2() {
        return mMediaPathArrayList2;
    }

    public ImageAdapter(HashMap<String, MediaBean> mMediaPathArrayList, Context mContext) {
        this.mMediaPathArrayList2 = new ArrayList<MediaBean>(mMediaPathArrayList.values());
        imageLoader = ImageLoader.getInstance();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                mContext).build();
        imageLoader.init(config);
        mInflater = (LayoutInflater.from(mContext));
    }

    @Override
    public int getCount() {
        return mMediaPathArrayList2.size();
    }

    @Override
    public Object getItem(int position) {
        return mMediaPathArrayList2.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.adapter_create_tikkrr_images,
                    null);
            holder.imageView = (ImageView) convertView
                    .findViewById(R.id.image);
            holder.imagePathTv = (TextView) convertView
                    .findViewById(R.id.image_path);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.imagePathTv.setText(mMediaPathArrayList2.get(position).getImagePath());
        imageLoader.displayImage("file://" + mMediaPathArrayList2.get(position).getImagePath(),
                holder.imageView, new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        holder.imageView
                                .setImageResource(com.luminous.pick.R.drawable.placeholder_470x352);
                        super.onLoadingStarted(imageUri, view);
                    }
                });

        return convertView;
    }

    public void customNotify(HashMap<String, MediaBean> mMediaPathArrayList) {
        mMediaPathArrayList2.clear();
        ArrayList<MediaBean> dataT2 = new ArrayList<MediaBean>(mMediaPathArrayList.values());
        this.mMediaPathArrayList2.addAll(dataT2);
        notifyDataSetChanged();
    }

    static class ViewHolder {
        ImageView imageView;
        TextView imagePathTv;
    }

}
