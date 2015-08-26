package com.luminous.pick.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class BitmapDecoder {

    public static String getBitmap(String filePath, int outputQuality, Context mContext) {
        if (filePath != null) {
            FileInputStream stream = null;
            File file = null;
            try {
                file = new File(filePath);
                stream = new FileInputStream(file);
            } catch (FileNotFoundException e2) {
                e2.printStackTrace();
            }
            BitmapFactory.Options tmpOptions = new BitmapFactory.Options();
            tmpOptions.inJustDecodeBounds = true;
            tmpOptions.inSampleSize = 2;
            BitmapRegionDecoder decoder = null;
            try {
                decoder = BitmapRegionDecoder.newInstance(stream, false);
            } catch (IOException e1) {
                e1.printStackTrace();
            } finally {
                try {
                    if (stream != null) {
                        stream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (decoder != null) {
                Bitmap region;
                if (decoder.getWidth() > 2000)
                    region = decoder.decodeRegion(new Rect(0, 0, decoder.getWidth(), decoder.getHeight()), tmpOptions);
                else
                    region = decoder.decodeRegion(new Rect(0, 0, decoder.getWidth(), decoder.getHeight()), null);

                ExifInterface ei = null;
                try {
                    ei = new ExifInterface(file.getAbsolutePath());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                int orientation = 0;
                if (ei != null) {
                    orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL);
                }

                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        region = rotateImage(region, 90);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        region = rotateImage(region, 180);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        region = rotateImage(region, 270);
                        break;
                }

                String imageName = filePath.substring(filePath.lastIndexOf("/") + 1,
                        filePath.length());
                File fileBackup = new File(getUserImageDir(mContext) + "/" + imageName);
                initializeImageLoader(mContext);
                FileOutputStream fOut = null;
                try {
                    fOut = new FileOutputStream(fileBackup);
                    region.compress(Bitmap.CompressFormat.JPEG, outputQuality, fOut);
                    filePath = fileBackup.getPath();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (fOut != null) {
                            fOut.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
//                return region;
                return filePath;
            }
        }
        return filePath;

    }

    private static String getUserImageDir(Context mContext) {
        return Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/MultipleImageCache/data/" + mContext.getPackageName() + "/images";
    }

    private static void initializeImageLoader(Context mContext) {
        File cacheDir = new File(getUserImageDir(mContext));
        if (!cacheDir.exists())
            cacheDir.mkdirs();

    }

    private static Bitmap rotateImage(Bitmap source, int angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        source = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
        return source;
    }
}
