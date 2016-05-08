package com.app.multiimagepickercropper;

import android.net.Uri;

/**
 * Created by rahul on 25/5/15.
 */
public class MediaBean {
    String imagePath, videoPath;
    Uri imageUri;
    boolean isImage;

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }

    public boolean isImage() {
        return isImage;
    }

    public void setImage(boolean isImage) {
        this.isImage = isImage;
    }
}
