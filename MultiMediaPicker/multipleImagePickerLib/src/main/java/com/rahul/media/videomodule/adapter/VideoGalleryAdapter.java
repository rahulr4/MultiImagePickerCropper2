package com.rahul.media.videomodule.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.luminous.pick.R;
import com.sangcomz.fishbun.bean.MediaObject;
import com.sangcomz.fishbun.util.ProcessGalleryFile;
import com.sangcomz.fishbun.util.SquareImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class VideoGalleryAdapter extends BaseAdapter {

    private Context mContext;
    private Set<ProcessGalleryFile> tasks;
    ArrayList<MediaObject> mediaObjectArrayList;

    public VideoGalleryAdapter(Context c, ArrayList<MediaObject> mediaObjectArrayList) {
        mContext = c;
        isFirstTime = true;
        tasks = new HashSet<>();
        this.mediaObjectArrayList = mediaObjectArrayList;
        Collections.sort(mediaObjectArrayList);
    }

    private boolean isFirstTime;

    public void setFirstTime(boolean firstTime) {
        this.isFirstTime = firstTime;
    }

    public ArrayList<String> getSelectedStringArray() {
        ArrayList<String> dataT = new ArrayList<String>();

        for (int i = 0; i < mediaObjectArrayList.size(); i++) {
            if (mediaObjectArrayList.get(i).isSelected) {
                dataT.add(mediaObjectArrayList.get(i).getPath());
            }
        }

        return dataT;
    }
    // Get max available VM memory, exceeding this amount will throw an
    // OutOfMemory exception. Stored in kilobytes as LruCache takes an
    // int in its constructor.

    public static class ViewHolder {
        public CheckBox selectIv;
        public SquareImageView imgThumb;
        public TextView videoDuration;
        public MediaObject object;
        public int position;
    }

    @Override
    public int getCount() {
        return mediaObjectArrayList.size();
    }

    @Override
    public MediaObject getItem(int i) {
        return mediaObjectArrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
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
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.imgThumb.setImageResource(R.drawable.placeholder_470x352);
        holder.object = getItem(position);
        convertView.setTag(holder);

        holder.selectIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaObjectArrayList.get(position).isSelected = !mediaObjectArrayList.get(position).isSelected;
                holder.selectIv.setChecked(mediaObjectArrayList.get(position).isSelected);
            }
        });

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaObjectArrayList.get(position).isSelected = !mediaObjectArrayList.get(position).isSelected;
                holder.selectIv.setChecked(mediaObjectArrayList.get(position).isSelected);
            }
        });

        holder.selectIv.setChecked(mediaObjectArrayList.get(position).isSelected);

        if (isFirstTime) {
            /*
            Create new async task in getView() method is a very bad practice.
            getView() gets called very frequently and therefore we will eventually exceed the number of
            allowed threads if we're not checking if a thread is currently running on a row.
            Temporary solution for avoiding java.util.concurrent.RejectedExecutionException
            */
            ProcessGalleryFile processGalleryFile = new ProcessGalleryFile(holder.imgThumb, holder.videoDuration, holder.object.getPath(), holder.object.getMediaType());
            if (tasks == null) {
                tasks = new HashSet<ProcessGalleryFile>();
            }
            if (!tasks.contains(processGalleryFile)) {
                try {
                    processGalleryFile.execute();
                    tasks.add(processGalleryFile);
                } catch (Exception ignored) {
                }
            }
        } else {
            try {
                cancelAll();
            } catch (Exception ignored) {
            }
            holder.videoDuration.setVisibility(View.INVISIBLE);
        }
        return convertView;
    }

    public void cancelAll() throws Exception {
        final Iterator<ProcessGalleryFile> iterator = tasks.iterator();
        while (iterator.hasNext()) {
            iterator.next().cancel(true);
            iterator.remove();
        }
    }
}
