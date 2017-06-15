package com.rahul.media.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.rahul.media.R;
import com.rahul.media.activity.CameraPickActivity;
import com.rahul.media.activity.MultipleImagePreviewActivity;
import com.rahul.media.activity.VideoPickActivity;
import com.rahul.media.model.Define;
import com.rahul.media.model.VideoQuality;
import com.rahul.media.utils.MediaSingleTon;
import com.rahul.media.utils.MediaUtility;

import java.util.ArrayList;

/**
 * Class to initiate media picker
 * Created by rahul on 24/6/15.
 */
public class MediaFactory {

    private static final int MEDIA_REQUEST_CODE = 222;
    private static MediaFactory mMediaFactory;
    private boolean isVideo;

    public boolean isVideo() {
        return isVideo;
    }

    private MediaFactory() {
    }

    public static synchronized MediaFactory create() {
        if (mMediaFactory == null)
            mMediaFactory = new MediaFactory();
        return mMediaFactory;
    }

    /**
     * Method to clear cached images stored in sd card
     *
     * @param context
     */
    public void clearCache(Context context) {
        try {
            MediaUtility.initializeImageLoader(context).delete();
        } catch (Exception ignored) {
        }
    }

    public static class MediaBuilder {
        boolean isCrop = false;
        boolean isSquareCrop = true;
        boolean fromGallery = true;
        boolean takeVideo = false;
        long videoSize = -1;
        long videoDuration = -1;
        private final Context mContext;
        private VideoQuality videoQuality = VideoQuality.HIGH_QUALITY;
        private int pickCount = Define.MIN_MEDIA_COUNT;
        private int x = 1;
        private int y = 1;
        private int playResId = R.drawable.video_play;

        public MediaBuilder(Context mContext) {
            this.mContext = mContext;
        }

        /**
         * Sets type of media to be video
         *
         * @return current instance of MediaBuilder
         */
        public MediaBuilder takeVideo() {
            takeVideo = true;
            return this;
        }

        /**
         * Sets type of media to be video
         *
         * @return current instance of MediaBuilder
         */
        public MediaBuilder setPlayIcon(int playResId) {
            if (playResId > 0)
                this.playResId = playResId;
            return this;
        }


        /**
         * Sets the size of video.
         * Will work only for camera videos
         *
         * @param videoQuality Quality of video
         * @return current instance of MediaBuilder
         */
        public MediaBuilder setVideoQuality(VideoQuality videoQuality) {
            this.videoQuality = videoQuality;
            return this;
        }

        /**
         * Sets the size of video.
         * Will work only for camera videos
         *
         * @param size Size of video in MB
         * @return current instance of MediaBuilder
         */
        public MediaBuilder setVideoSize(int size) {
            videoSize = size * 1000000L;
            return this;
        }

        /**
         * Sets the duration of video
         * Will work only for camera videos
         *
         * @param seconds Duration of the video in seconds
         * @return current instance of MediaBuilder
         */
        public MediaBuilder setVideoDuration(long seconds) {
            videoDuration = seconds;
            return this;
        }

        /**
         * Sets type of media to be taken from gallery
         *
         * @return current instance of MediaBuilder
         */
        public MediaBuilder fromGallery() {
            this.fromGallery = true;
            return this;
        }

        /**
         * Sets type of media to be taken from camera
         *
         * @return current instance of MediaBuilder
         */
        public MediaBuilder fromCamera() {
            this.fromGallery = false;
            return this;
        }

        /**
         * Sets the cropping feature enabled or disabled.
         * Works only for camera image
         *
         * @return current instance of MediaBuilder
         */
        public MediaBuilder doCropping() {
            this.isCrop = true;
            return this;
        }

        /**
         * Sets the cropping feature enabled or disabled.
         * Works only for camera image
         *
         * @return current instance of MediaBuilder
         */
        public MediaBuilder isSquareCrop(boolean isSquareCrop) {
            this.isSquareCrop = isSquareCrop;
            return this;
        }

        /**
         * Method to select max media files from mobile
         *
         * @param count Count
         * @return MediaBuilder instance
         */
        public MediaBuilder setPickCount(int count) {
            if (count <= 0)
                count = 1;
            this.pickCount = count;
            return this;
        }

        public MediaBuilder withAspectRatio(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

    }

    /**
     * Starts the media picking functionality
     *
     * @param mediaBuilder MediaBuilder object
     * @return current instance of MediaFactory
     */

    public MediaFactory start(MediaBuilder mediaBuilder) {
        MediaSingleTon.getInstance();
        Intent intent;
        Bundle bundle = new Bundle();
        Fresco.initialize(mediaBuilder.mContext.getApplicationContext());
        if (mediaBuilder.takeVideo) {
            isVideo = true;

            intent = new Intent(mediaBuilder.mContext, VideoPickActivity.class);
            intent.putExtra("from", mediaBuilder.fromGallery);
            intent.putExtra("videoSize", mediaBuilder.videoSize);
            intent.putExtra("videoDuration", mediaBuilder.videoDuration);
            intent.putExtra("videoQuality", mediaBuilder.videoQuality.getQuality());
            intent.putExtra("pickCount", mediaBuilder.pickCount);
            intent.putExtra("playResId", mediaBuilder.playResId);

            ((Activity) mediaBuilder.mContext).startActivityForResult(intent, MEDIA_REQUEST_CODE);
        } else {
            isVideo = false;
            bundle.putBoolean("crop", mediaBuilder.isCrop);
            bundle.putBoolean("isSquareCrop", mediaBuilder.isSquareCrop);
            bundle.putInt("pickCount", mediaBuilder.pickCount);
            bundle.putInt("aspect_x", mediaBuilder.x);
            bundle.putInt("aspect_y", mediaBuilder.y);
            if (mediaBuilder.fromGallery) {
                intent = new Intent(mediaBuilder.mContext, MultipleImagePreviewActivity.class);
                intent.putExtras(bundle);
                ((Activity) mediaBuilder.mContext).startActivityForResult(intent, MEDIA_REQUEST_CODE);

            } else {
                intent = new Intent(mediaBuilder.mContext, CameraPickActivity.class);
                intent.putExtras(bundle);
                ((Activity) mediaBuilder.mContext).startActivityForResult(intent, MEDIA_REQUEST_CODE);
            }
        }

        return mMediaFactory;
    }

    public ArrayList<String> onActivityResult(int requestCode, int resultCode, Intent data) {
        ArrayList<String> all_path = new ArrayList<>();
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == MEDIA_REQUEST_CODE) {
                all_path = data.getStringArrayListExtra(Define.INTENT_PATH);
            }
        }
        return all_path;
    }

}
