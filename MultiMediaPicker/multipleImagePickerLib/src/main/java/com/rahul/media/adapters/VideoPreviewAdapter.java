package com.rahul.media.adapters;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.rahul.media.R;
import com.rahul.media.model.CustomGallery;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by rahul on 6/5/2016.
 */

public class VideoPreviewAdapter extends PagerAdapter {

    private final Context mContext;
    private LayoutInflater mLayoutInflater;
    private ArrayList<CustomGallery> dataT;
    private int playResId = R.drawable.video_play;

    public VideoPreviewAdapter(Context mContext, HashMap<String, CustomGallery> dataT) {
        this.mContext = mContext;
        this.dataT = new ArrayList<>(dataT.values());
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setPlayResId(int playResId) {
        this.playResId = playResId;
    }

    public void customNotify(HashMap<String, CustomGallery> dataHashmap) {
        dataT.clear();
        ArrayList<CustomGallery> dataT2 = new ArrayList<CustomGallery>(dataHashmap.values());
        this.dataT.addAll(dataT2);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return dataT.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        View itemView = mLayoutInflater.inflate(R.layout.video_image_pager_item, container, false);

        final SimpleDraweeView imageView = (SimpleDraweeView) itemView.findViewById(R.id.full_screen_image);

        final ImageView playIcon = (ImageView) itemView.findViewById(R.id.play_icon);
        playIcon.setImageResource(playResId);

        imageView.setImageURI(Uri.parse("file://" + dataT.get(position).sdcardPath));

        container.addView(itemView);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(dataT.get(position).sdcardPath), "video/*");
                    mContext.startActivity(Intent.createChooser(intent, "Complete action using .."));

                } catch (Exception e) {
                    e.printStackTrace();
                    if (e instanceof ActivityNotFoundException) {
                        Toast.makeText(mContext, "Video Player not found", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });
        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((FrameLayout) object);
    }
}