package com.luminous.pick.controller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.luminous.pick.Action;
import com.luminous.pick.CameraPickActivity;
import com.luminous.pick.MultipleImagePreviewActivity;
import com.luminous.pick.VideoPickActivity;
import com.luminous.pick.utils.VideoQuality;

/**
 * Class to initiate media picker
 * Created by rahul on 24/6/15.
 */
public class MediaFactory {

    private static final int GALLERY_APP = 111;
    private static final int CAMERA_APP = 222;
    public static MediaFactory mMediaFactory;

    private MediaFactory() {

    }

    public static synchronized MediaFactory create() {
        if (mMediaFactory == null)
            mMediaFactory = new MediaFactory();
        return mMediaFactory;
    }

    public static class MediaBuilder {
        String action = Action.ACTION_PICK;
        boolean isCrop = false;
        boolean fromGallery = true;
        boolean takeVideo = false;
        long videoSize = -1;
        long videoDuration = -1;
        private final Context mContext;
        int imageQuality = 100;
        private VideoQuality videoQuality = VideoQuality.HIGH_QUALITY;

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
         * Sets the quality of image.
         *
         * @param imageQuality Quality of image between 0 to 100
         * @return current instance of MediaBuilder
         */
        public MediaBuilder setImageQuality(int imageQuality) {
            if (imageQuality < 0 || imageQuality > 100)
                imageQuality = 100;
            this.imageQuality = imageQuality;
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
         * Works only for image
         *
         * @return current instance of MediaBuilder
         */
        public MediaBuilder doCropping() {
            this.isCrop = true;
            return this;
        }

        /**
         * Flag to select only one media.
         *
         * @return current instance of MediaBuilder
         */
        public MediaBuilder getSingleMediaFiles() {
            this.action = Action.ACTION_PICK;
            return this;
        }

        /**
         * Flag to select only multiple media files.
         *
         * @return current instance of MediaBuilder
         */
        public MediaBuilder getMultipleMediaFiles() {
            this.action = Action.ACTION_MULTIPLE_PICK;
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
        if (mediaBuilder.takeVideo) {
            intent = new Intent(mediaBuilder.mContext, VideoPickActivity.class);
            intent.setAction(mediaBuilder.action);
            intent.putExtra("from", mediaBuilder.fromGallery);
            intent.putExtra("videoSize", mediaBuilder.videoSize);
            intent.putExtra("videoDuration", mediaBuilder.videoDuration);
            intent.putExtra("videoQuality", mediaBuilder.videoQuality.getQuality());
            ((Activity) mediaBuilder.mContext).startActivityForResult(intent, GALLERY_APP);
        } else {
            bundle.putBoolean("crop", mediaBuilder.isCrop);
            bundle.putInt("imageQuality", mediaBuilder.imageQuality);
            if (mediaBuilder.fromGallery) {
                intent = new Intent(mediaBuilder.mContext, MultipleImagePreviewActivity.class);
                intent.setAction(mediaBuilder.action);
                intent.putExtras(bundle);
                ((Activity) mediaBuilder.mContext).startActivityForResult(intent, GALLERY_APP);
            } else {
                intent = new Intent(mediaBuilder.mContext, CameraPickActivity.class);
                intent.setAction(mediaBuilder.action);
                intent.putExtras(bundle);
                ((Activity) mediaBuilder.mContext).startActivityForResult(intent, CAMERA_APP);
            }
        }

        return mMediaFactory;
    }

    public String[] onActivityResult(int requestCode, int resultCode, Intent data) {
        String[] all_path = new String[0];
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY_APP || requestCode == CAMERA_APP) {
                all_path = data.getStringArrayExtra("all_path");

            }
        }
        return all_path;
    }

}
