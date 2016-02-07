package com.sangcomz.fishbun.util;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.luminous.pick.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.sangcomz.fishbun.bean.MediaType;

import java.io.File;

public class ProcessGalleryFile extends AsyncTask<Void, Void, Bitmap> {

    private static int WIDTH = 80;
    private static int HEIGHT = 80;

    ImageView photoHolder;
    TextView durationHolder;
    MediaType type;
    String filePath;
    MediaMetadataRetriever retriever = new MediaMetadataRetriever();

    public ProcessGalleryFile(ImageView photoHolder, TextView durationHolder, String filePath, MediaType type) {
        HEIGHT = WIDTH = (int) photoHolder.getContext().getResources().getDimension(R.dimen.thumbnail_width);
        this.filePath = filePath;
        this.durationHolder = durationHolder;
        this.photoHolder = photoHolder;
        this.type = type;
    }

    @Override
    protected Bitmap doInBackground(Void... params) {

        Bitmap bmp = null;
        Log.d(getClass().getSimpleName(), "" + Thread.getAllStackTraces().keySet().size());
        if (type != MediaType.PHOTO) {
            try {
                bmp = ImageLoader.getInstance().getMemoryCache().get(Uri.fromFile(new File(filePath)).toString() + "_");
            } catch (Exception e) {
                Log.e(ProcessGalleryFile.class.getSimpleName(), "" + e);
            }
            if (bmp == null) {
                try {
                    bmp = ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Images.Thumbnails.MINI_KIND);
                    if (bmp != null) {
                        ImageLoader.getInstance().getMemoryCache().put(Uri.fromFile(new File(filePath)).toString() + "_", bmp);
                    }
                } catch (Exception e) {
                    Log.e(getClass().getSimpleName(), "Exception when rotating thumbnail for gallery", e);
                } catch (OutOfMemoryError e) {
                    Log.e(ProcessGalleryFile.class.getSimpleName(), "" + e);
                }
            }
        }
        return bmp;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        super.onPostExecute(result);
        if (type == MediaType.PHOTO) {
            durationHolder.setVisibility(View.GONE);
            ImageAware aware = new ImageViewAware(photoHolder) {

                @Override
                public int getWidth() {
                    return WIDTH;
                }

                @Override
                public int getHeight() {
                    return HEIGHT;
                }
            };
            ImageLoader.getInstance().displayImage(Uri.fromFile(new File(filePath)).toString(), aware,
                    ImageOption.GALLERY_OPTIONS.getDisplayImageOptions());
        } else {
            durationHolder.setText(getDurationMark(filePath, retriever));
            durationHolder.setVisibility(View.VISIBLE);
            photoHolder.setImageBitmap(result);
        }
    }

    public static String getDurationMark(String filePath, MediaMetadataRetriever retriever) {
        try {
            retriever.setDataSource(filePath);
        } catch (Exception e) {
            Log.e("getDurationMark", e.toString());
            return "?:??";
        }
        String time = null;

        //fix for the gallery picker crash
        // if it couldn't detect the media file
        try {
            Log.e("file", filePath);

            time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        } catch (Exception ex) {
            Log.e("getDurationMark", ex.toString());
        }

        //fix for the gallery picker crash
        // if it couldn't extractMetadata() of a media file
        //time was null
        time = time == null ? "0" : time.isEmpty() ? "0" : time;
        //bam crash - no more :)
        int timeInMillis = Integer.parseInt(time);
        int duration = timeInMillis / 1000;
        int hours = duration / 3600;
        int minutes = (duration % 3600) / 60;
        int seconds = duration % 60;
        StringBuilder sb = new StringBuilder();
        if (hours > 0) {
            sb.append(hours).append(":");
        }
        if (minutes < 10) {
            sb.append("0").append(minutes);
        } else {
            sb.append(minutes);
        }
        sb.append(":");
        if (seconds < 10) {
            sb.append("0").append(seconds);
        } else {
            sb.append(seconds);
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return filePath != null ? filePath.hashCode() : super.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof ProcessGalleryFile)) return false;
        ProcessGalleryFile file = (ProcessGalleryFile) o;
        return filePath != null && file.filePath != null && filePath.equals(file.filePath);
    }
}


