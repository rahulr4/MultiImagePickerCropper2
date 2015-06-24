package com.luminous.pick.controller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.luminous.pick.Action;
import com.luminous.pick.CameraPickActivity;
import com.luminous.pick.MultipleImagePreviewActivity;
import com.luminous.pick.VideoPickActivity;

/**
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
        private final Context mContext;

        public MediaBuilder(Context mContext) {
            this.mContext = mContext;
        }

        public MediaBuilder takeVideo() {
            takeVideo = true;
            return this;
        }

        public MediaBuilder fromGallery() {
            this.fromGallery = true;
            return this;
        }

        public MediaBuilder fromCamera() {
            this.fromGallery = false;
            return this;
        }

        public MediaBuilder doCropping() {
            this.isCrop = true;
            return this;
        }

        public MediaBuilder getSingleImage() {
            this.action = Action.ACTION_PICK;
            return this;
        }

        public MediaBuilder getMultipleImages() {
            this.action = Action.ACTION_MULTIPLE_PICK;
            return this;
        }
    }

    public MediaFactory start(MediaBuilder mediaBuilder) {
        if (mediaBuilder.takeVideo) {
            Intent intent = new Intent(mediaBuilder.mContext, VideoPickActivity.class);
            //TODO Give support of multiple video pick
            intent.setAction(Action.ACTION_PICK);
            intent.putExtra("from", mediaBuilder.fromGallery);
            ((Activity) mediaBuilder.mContext).startActivityForResult(intent, GALLERY_APP);
        } else {
            if (mediaBuilder.fromGallery) {
                Intent intent = new Intent(mediaBuilder.mContext, MultipleImagePreviewActivity.class);
                intent.setAction(mediaBuilder.action);
                ((Activity) mediaBuilder.mContext).startActivityForResult(intent, GALLERY_APP);
            } else {
                Intent intent = new Intent(mediaBuilder.mContext, CameraPickActivity.class);
                intent.setAction(Action.ACTION_PICK);
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
