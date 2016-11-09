package com.rahul.media.utils;

import android.content.Context;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.rahul.media.R;
import com.rahul.media.activity.CameraPickActivity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.os.Environment.MEDIA_MOUNTED;

/**
 * Created by rahul on 6/5/2016.
 */

public class MediaUtility {
    public static String getUserImageDir(Context mContext) {
        return Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/MultipleImageCache/data/" + mContext.getPackageName() + "/images";
    }

    public static File initializeImageLoader(Context mContext) {
        File cacheDir = new File(getUserImageDir(mContext));
        if (!cacheDir.exists())
            cacheDir.mkdirs();
        return cacheDir;
    }

    public static Uri createImageFile(Context mContext) throws IOException {

        File image = null;

        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        if (MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            File storageDir = new File(Environment.getExternalStorageDirectory(),
                    mContext.getString(R.string.imagepicker_parent));
            boolean parentCreationResult = storageDir.mkdirs();
            image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );

        } else {

            File storageDir = mContext.getFilesDir();
            image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );

        }

        // Save a file: path for use with ACTION_VIEW intents
        Log.d(CameraPickActivity.class.getSimpleName(), "file:" + image.getAbsolutePath());
        return Uri.fromFile(image);
    }

    public static Uri createVideoFile(Context mContext) throws IOException {

        File image = null;

        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "VID_" + timeStamp + "_";

        if (MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            File storageDir = new File(Environment.getExternalStorageDirectory(),
                    mContext.getString(R.string.imagepicker_parent));
            boolean parentCreationResult = storageDir.mkdirs();
            image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".mp4",         /* suffix */
                    storageDir      /* directory */
            );

        } else {

            File storageDir = mContext.getFilesDir();
            image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".mp4",         /* suffix */
                    storageDir      /* directory */
            );

        }

        // Save a file: path for use with ACTION_VIEW intents
        Log.d(CameraPickActivity.class.getSimpleName(), "file:" + image.getAbsolutePath());
        return Uri.fromFile(image);
    }

    public static byte[] getThumbnail(String path) {
        ExifInterface exif;
        try {
            exif = new ExifInterface(path);
        } catch (IOException e) {
            return null;
        }
        byte[] imageData = exif.getThumbnail();
        if (imageData != null)
            return imageData;
        return null;
    }

}
