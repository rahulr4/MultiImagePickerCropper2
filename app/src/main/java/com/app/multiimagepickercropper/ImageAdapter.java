package com.app.multiimagepickercropper;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class ImageAdapter extends BaseAdapter {
    private final LayoutInflater mInflater;
    ArrayList<MediaBean> mMediaPathArrayList2;
    Context mContext;

    public ArrayList<MediaBean> getmMediaPathArrayList2() {
        return mMediaPathArrayList2;
    }

    public ImageAdapter(HashMap<String, MediaBean> mMediaPathArrayList, Context mContext) {
        this.mMediaPathArrayList2 = new ArrayList<MediaBean>(mMediaPathArrayList.values());
        mInflater = (LayoutInflater.from(mContext));
        this.mContext = mContext;
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

        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.adapter_create_tikkrr_images,
                    null);
            holder.imageView = (ImageView) convertView
                    .findViewById(R.id.image);
            holder.imagePathTv = (TextView) convertView
                    .findViewById(R.id.image_path);
            holder.imageSize = (TextView) convertView
                    .findViewById(R.id.image_size);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.imagePathTv.setText(mMediaPathArrayList2.get(position).getImagePath());

        File file = new File(mMediaPathArrayList2.get(position).getImagePath());
        long length = file.length() / 1024; // Size in KB

        holder.imageSize.setText("Size :- " + length + " KB");

        
        Glide.with(mContext)
                .load(Uri.parse("file://" + mMediaPathArrayList2.get(position).getImagePath()))
                .asBitmap()
                .into(new SimpleTarget<Bitmap>(100, 100) {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                        holder.imageView.setImageBitmap(resource); // Possibly runOnUiThread()
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
        TextView imagePathTv, imageSize;
    }

}
