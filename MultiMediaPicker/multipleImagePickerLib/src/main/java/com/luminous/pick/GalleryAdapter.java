package com.luminous.pick;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.luminous.pick.controller.MediaSingleTon;

import java.util.ArrayList;

public class GalleryAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater infalter;
    private ArrayList<CustomGallery> data = new ArrayList<CustomGallery>();
    MediaSingleTon mediaSingleTon;
    private boolean isActionMultiplePick;

    public GalleryAdapter(Context c) {
        infalter = (LayoutInflater) c
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = c;
        mediaSingleTon = MediaSingleTon.getInstance();
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public CustomGallery getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setMultiplePick(boolean isMultiplePick) {
        this.isActionMultiplePick = isMultiplePick;
    }

	/*public void selectAll(boolean selection) {
        for (int i = 0; i < data.size(); i++) {
			data.get(i).isSelected = selection;

		}
		notifyDataSetChanged();
	}*/

	/*public boolean isAllSelected() {
        boolean isAllSelected = true;

		for (int i = 0; i < data.size(); i++) {
			if (!data.get(i).isSelected) {
				isAllSelected = false;
				break;
			}
		}

		return isAllSelected;
	}*/

	/*public boolean isAnySelected() {
        boolean isAnySelected = false;

		for (int i = 0; i < data.size(); i++) {
			if (data.get(i).isSelected) {
				isAnySelected = true;
				break;
			}
		}

		return isAnySelected;
	}*/

	/*public ArrayList<CustomGallery> getSelected() {
        ArrayList<CustomGallery> dataT = new ArrayList<CustomGallery>();

		for (int i = 0; i < data.size(); i++) {
			if (data.get(i).isSelected) {
				dataT.add(data.get(i));
			}
		}

		return dataT;
	}*/


    public String[] getSelectedStringArray() {
        ArrayList<String> dataT = new ArrayList<String>();

        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).isSeleted) {
                dataT.add(data.get(i).sdcardPath);
            }
        }

        return dataT.toArray(new String[dataT.size()]);
    }

    public void addAll(ArrayList<CustomGallery> files) {

        try {
            this.data.clear();
            this.data.addAll(files);

        } catch (Exception e) {
            e.printStackTrace();
        }

        notifyDataSetChanged();
    }

    public void changeSelection(View v, int position) {

        data.get(position).isSeleted = !data.get(position).isSeleted;

        ((ViewHolder) v.getTag()).imgQueueMultiSelected.setSelected(data
                .get(position).isSeleted);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;
        if (convertView == null) {

            convertView = infalter.inflate(R.layout.gallery_item, null);
            holder = new ViewHolder();
            holder.imgQueue = (ImageView) convertView
                    .findViewById(R.id.imgQueue);

            holder.imgQueueMultiSelected = (ImageView) convertView
                    .findViewById(R.id.imgQueueMultiSelected);

            if (isActionMultiplePick) {
                holder.imgQueueMultiSelected.setVisibility(View.VISIBLE);
            } else {
                holder.imgQueueMultiSelected.setVisibility(View.GONE);
            }

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }
//        holder.imgQueue.setTag(position);

        try {

//            Picasso.with(mContext).load("file://" + data.get(position).sdcardPath).into(holder.imgQueue);

            if (mediaSingleTon.getBitmapHashMap().containsKey(data.get(position).sdcardPath)) {
                holder.imgQueue.setImageBitmap(mediaSingleTon.getBitmapHashMap().get((data.get(position).sdcardPath)));
            } else
                Glide.with(mContext)
                        .load(Uri.parse("file://" + data.get(position).sdcardPath))
                        .asBitmap()
                        .into(new SimpleTarget<Bitmap>(100, 100) {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                                mediaSingleTon.getBitmapHashMap().put(data.get(position).sdcardPath, resource);
                                holder.imgQueue.setImageBitmap(resource); // Possibly runOnUiThread()
                            }
                        });

//            imageLoader.displayImage("file://" + data.get(position).sdcardPath,
//                    holder.imgQueue, new SimpleImageLoadingListener() {
//                        @Override
//                        public void onLoadingStarted(String imageUri, View view) {
//                            holder.imgQueue
//                                    .setImageResource(R.drawable.no_media);
//                            super.onLoadingStarted(imageUri, view);
//                        }
//
//                    });

            if (isActionMultiplePick) {

                holder.imgQueueMultiSelected
                        .setSelected(data.get(position).isSeleted);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return convertView;
    }

    public class ViewHolder {
        ImageView imgQueue;
        ImageView imgQueueMultiSelected;
    }

	/*public void clearCache() {
        imageLoader.clearDiscCache();
		imageLoader.clearMemoryCache();
	}*/

    public void clear() {
        data.clear();
        notifyDataSetChanged();
    }
}
