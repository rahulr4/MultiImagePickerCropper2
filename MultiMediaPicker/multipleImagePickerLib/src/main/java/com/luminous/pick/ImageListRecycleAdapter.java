package com.luminous.pick;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.luminous.pick.controller.MediaSingleTon;

import java.util.ArrayList;
import java.util.HashMap;

public class ImageListRecycleAdapter extends RecyclerView.Adapter<ImageListRecycleAdapter.VerticalItemHolder> {

    private final Context mContext;
    public ArrayList<CustomGallery> mItems;
    MediaSingleTon mediaSingleTon;

    private AdapterView.OnItemClickListener mOnItemClickListener;

    public ImageListRecycleAdapter(Context context,
                                   HashMap<String, CustomGallery> imagesUri) {
        mContext = context;
        this.mItems = new ArrayList<CustomGallery>(imagesUri.values());
        mediaSingleTon = MediaSingleTon.getInstance();
    }

    public void removeItem(int position) {
        if (position >= mItems.size()) return;
        mItems.remove(position);
        notifyItemRemoved(position);
    }

    public void customNotify(HashMap<String, CustomGallery> dataT) {
        mItems.clear();
        ArrayList<CustomGallery> dataT2 = new ArrayList<CustomGallery>(dataT.values());
        this.mItems.addAll(dataT2);
        notifyDataSetChanged();
    }

    @Override
    public VerticalItemHolder onCreateViewHolder(ViewGroup container, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        View root = inflater.inflate(R.layout.image_strip_view, container, false);

        return new VerticalItemHolder(root, this);
    }

    @Override
    public void onBindViewHolder(VerticalItemHolder itemHolder, int position) {
        CustomGallery item = mItems.get(position);
        if (item.bitmap != null) {
            itemHolder.imageView.setImageBitmap(item.bitmap);
        } else
            itemHolder.setImage(item.sdcardPath);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    private void onItemHolderClick(VerticalItemHolder itemHolder) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(null, itemHolder.itemView,
                    itemHolder.getAdapterPosition(), itemHolder.getItemId());
        }
    }

    class VerticalItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView imageView;
        private ImageListRecycleAdapter mAdapter;

        public VerticalItemHolder(View itemView, ImageListRecycleAdapter adapter) {
            super(itemView);
            itemView.setOnClickListener(this);

            mAdapter = adapter;
            imageView = (ImageView) itemView.findViewById(R.id.strip_image);
        }

        @Override
        public void onClick(View v) {
            mAdapter.onItemHolderClick(this);
        }

        public void setImage(final String url) {

            Glide.with(mContext)
                    .load(Uri.parse("file://" + url))
                    .asBitmap()
                    .into(new SimpleTarget<Bitmap>(100, 100) {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                            MediaSingleTon.getInstance().getBitmapHashMap().put(url, resource);
                            imageView.setImageBitmap(resource); // Possibly runOnUiThread()
                        }
                    });
            /*if (MediaSingleTon.getInstance().getBitmapHashMap().containsKey(url)) {
                imageView.setImageBitmap(MediaSingleTon.getInstance().getBitmapHashMap().get(url));
            } else {
                Glide.with(mContext)
                        .load(Uri.parse("file://" + url))
                        .asBitmap()
                        .into(new SimpleTarget<Bitmap>(100, 100) {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                                MediaSingleTon.getInstance().getBitmapHashMap().put(url, resource);
                                imageView.setImageBitmap(resource); // Possibly runOnUiThread()
                            }
                        });
            }*/

        }
    }

}