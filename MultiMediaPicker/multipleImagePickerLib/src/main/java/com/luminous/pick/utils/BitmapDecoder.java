package com.luminous.pick.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class BitmapDecoder {

    public static String compressImage(String filePath, int outputQuality, Context mContext) {
        try {
            Bitmap scaledBitmap = null;

            BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
            options.inJustDecodeBounds = true;
            Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

            int actualHeight = options.outHeight;
            int actualWidth = options.outWidth;

//      max Height and width values of the compressed image is taken as 816x612

            float maxHeight = 450.0f;
            float maxWidth = 450.0f;
            float imgRatio = actualWidth / actualHeight;
            float maxRatio = maxWidth / maxHeight;

//      width and height values are set maintaining the aspect ratio of the image

            if (actualHeight > maxHeight || actualWidth > maxWidth) {
                if (imgRatio < maxRatio) {
                    imgRatio = maxHeight / actualHeight;
                    actualWidth = (int) (imgRatio * actualWidth);
                    actualHeight = (int) maxHeight;
                } else if (imgRatio > maxRatio) {
                    imgRatio = maxWidth / actualWidth;
                    actualHeight = (int) (imgRatio * actualHeight);
                    actualWidth = (int) maxWidth;
                } else {
                    actualHeight = (int) maxHeight;
                    actualWidth = (int) maxWidth;

                }
            }

//      setting inSampleSize value allows to load a scaled down version of the original image

            options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
            options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
            options.inPurgeable = true;
            options.inInputShareable = true;
            options.inTempStorage = new byte[16 * 1024];

            try {
//          load the bitmap from its path
                bmp = BitmapFactory.decodeFile(filePath, options);
            } catch (OutOfMemoryError exception) {
                exception.printStackTrace();

            }
            try {
                scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
            } catch (OutOfMemoryError exception) {
                exception.printStackTrace();
            }

            float ratioX = actualWidth / (float) options.outWidth;
            float ratioY = actualHeight / (float) options.outHeight;
            float middleX = actualWidth / 2.0f;
            float middleY = actualHeight / 2.0f;

            Matrix scaleMatrix = new Matrix();
            scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

            Canvas canvas = new Canvas(scaledBitmap);
            canvas.setMatrix(scaleMatrix);
            canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

//      check the rotation of the image and display it properly
            ExifInterface exif;
            try {
                exif = new ExifInterface(filePath);

                int orientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION, 0);
                Log.d("EXIF", "Exif: " + orientation);
                Matrix matrix = new Matrix();
                if (orientation == 6) {
                    matrix.postRotate(90);
                    Log.d("EXIF", "Exif: " + orientation);
                } else if (orientation == 3) {
                    matrix.postRotate(180);
                    Log.d("EXIF", "Exif: " + orientation);
                } else if (orientation == 8) {
                    matrix.postRotate(270);
                    Log.d("EXIF", "Exif: " + orientation);
                }
                scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                        scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                        true);
            } catch (IOException e) {
                e.printStackTrace();
            }

            String imageName = filePath.substring(filePath.lastIndexOf("/") + 1,
                    filePath.length());
            File fileBackup = new File(getUserImageDir(mContext) + "/" + imageName);
            initializeImageLoader(mContext);
            FileOutputStream fOut = null;
            try {
                fOut = new FileOutputStream(fileBackup);
                scaledBitmap.compress(Bitmap.CompressFormat.PNG, 70, fOut);
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
        } catch (Exception e) {
            Log.i(BitmapDecoder.class.getSimpleName(), e.getLocalizedMessage());
        }
        return filePath;

    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }

    /**
     * This method is depreciated. use compressImage()
     *
     * @param filePath
     * @param outputQuality
     * @param mContext
     * @return
     */
    public static String getBitmap(String filePath, int outputQuality, Context mContext) {
        return compressImage(filePath, outputQuality, mContext);
        /*if (filePath != null) {
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
        return filePath;*/

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
