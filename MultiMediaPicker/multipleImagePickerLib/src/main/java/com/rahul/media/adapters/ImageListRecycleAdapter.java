package com.rahul.media.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.rahul.media.R;
import com.rahul.media.model.CustomGallery;
import com.rahul.media.utils.MediaSingleTon;
import com.rahul.media.utils.MediaUtility;
import com.rahul.media.utils.SquareImageView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Adapter to show bottom strip view
 */
public class ImageListRecycleAdapter extends RecyclerView.Adapter<ImageListRecycleAdapter.VerticalItemHolder> {

    public ArrayList<CustomGallery> mItems;

    private AdapterView.OnItemClickListener mOnItemClickListener;

    public ImageListRecycleAdapter(Context context, HashMap<String, CustomGallery> imagesUri) {
        this.mItems = new ArrayList<>(imagesUri.values());
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
        private SquareImageView imageView;
        private ImageListRecycleAdapter mAdapter;

        VerticalItemHolder(View itemView, ImageListRecycleAdapter adapter) {
            super(itemView);
            itemView.setOnClickListener(this);

            mAdapter = adapter;
            imageView = (SquareImageView) itemView.findViewById(R.id.strip_image);
        }

        @Override
        public void onClick(View v) {
            mAdapter.onItemHolderClick(this);
        }

        public void setImage(final String thumbPath) {
            Context mContext = imageView.getContext();
            byte[] imageByte = MediaSingleTon.getInstance().getImageByte(thumbPath);
            if (imageByte != null) {
                Glide.with(mContext)
                        .load(imageByte)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .override(200, 200)
                        .into(imageView);
            } else {
                byte[] thumbnail = MediaUtility.getThumbnail(thumbPath);
                if (thumbnail != null) {
                    MediaSingleTon.getInstance().putImageByte(thumbPath, thumbnail);
                    Glide.with(mContext)
                            .load(thumbnail)
                            .centerCrop()
                            .diskCacheStrategy(DiskCacheStrategy.RESULT)
                            .override(200, 200)
                            .into(imageView);
                } else {
                    Glide.with(mContext)
                            .load(Uri.parse("file://" + thumbPath))
                            .asBitmap()
                            .diskCacheStrategy(DiskCacheStrategy.RESULT)
                            .override(200, 200) // resizes the image to these dimensions (in pixel). does not respect aspect ratio
                            .centerCrop() // this cropping technique scales the image so that it fills the requested bounds and then crops the extra.
                            .into(imageView);

                }
            }
//            imageView.setImageURI(Uri.parse("file://" + url));
        }
    }

}