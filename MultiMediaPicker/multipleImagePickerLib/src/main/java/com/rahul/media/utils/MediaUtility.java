package com.rahul.media.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

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
    public static Uri createImageFile(Context mContext) throws IOException {

        File image = null;

        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        if (MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

            File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
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
}
