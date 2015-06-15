package com.luminous.pick;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.HashMap;

public class ImageListRecycleAdapter extends RecyclerView.Adapter<ImageListRecycleAdapter.VerticalItemHolder> {

    private final Context mContext;
    private final ImageLoader imageLoader;
    private final DisplayImageOptions imageOptions;
    public ArrayList<CustomGallery> mItems;


    private AdapterView.OnItemClickListener mOnItemClickListener;

    public ImageListRecycleAdapter(Context context,
                                   HashMap<String, CustomGallery> imagesUri) {
        mContext = context;

        this.mItems = new ArrayList<CustomGallery>(imagesUri.values());

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

        public void setImage(String url) {
            imageLoader.displayImage("file://" + url,
                    imageView, new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String imageUri, View view) {
                            imageView
                                    .setImageResource(R.drawable.placeholder_470x352);
                            super.onLoadingStarted(imageUri, view);
                        }
                    });
        }
    }

}