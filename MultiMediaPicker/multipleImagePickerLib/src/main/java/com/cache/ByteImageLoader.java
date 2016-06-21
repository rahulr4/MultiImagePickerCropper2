package com.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.rahul.media.R;
import com.rahul.media.utils.MediaUtility;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ByteImageLoader {

    private final MemoryCache memoryCache = new MemoryCache();
    // Initialize MemoryCache
    private BitmapMemoryCache bitmapMemoryCache = new BitmapMemoryCache();

    private FileCache fileCache;

    //Create Map (collection) to store image and image url in key value pair
    private Map<ImageView, String> imageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
    private ExecutorService executorService;

    //handler to display images in UI thread
    private Handler handler = new Handler();
    private int reqWidth = 100;
    private int reqHeight = 100;

    public ByteImageLoader(Context context) {

        fileCache = new FileCache(context);
        // Creates a thread pool that reuses a fixed number of
        // threads operating off a shared unbounded queue.
        executorService = Executors.newFixedThreadPool(5);

    }

    // default image show in list (Before online image download)
    private final int stub_id = R.drawable.ic_empty_amoled;

    public void DisplayImage(String url, ImageView imageView) {
        //Store image and url in Map
        imageViews.put(imageView, url);

        //Check image is stored in MemoryCache Map or not (see MemoryCache.java)
        byte[] imageByte = memoryCache.get(url);

        if (imageByte != null) {
            Log.i("ByteImageLoader", "Load from byte cache");
            Glide.with(imageView.getContext())
                    .load(imageByte)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .into(imageView);
            // if image is stored in MemoryCache Map then
            // Show image in listview row
        } else {
            Bitmap bitmap = bitmapMemoryCache.get(url);
            if (bitmap == null) {
                //queue Photo to download from url
                queuePhoto(url, imageView);

                //Before downloading image show default image
                imageView.setImageResource(stub_id);
            } else {
                Log.i("ByteImageLoader", "Load from bitmap cache");
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    private void queuePhoto(String url, ImageView imageView) {
        // Store image and url in PhotoToLoad object
        PhotoToLoad p = new PhotoToLoad(url, imageView);

        // pass PhotoToLoad object to PhotosLoader runnable class
        // and submit PhotosLoader runnable to executers to run runnable
        // Submits a PhotosLoader runnable task for execution  

        executorService.submit(new PhotosLoader(p));
    }

    //Task for the queue
    private class PhotoToLoad {
        String url;
        ImageView imageView;

        PhotoToLoad(String u, ImageView i) {
            url = u;
            imageView = i;
        }
    }

    private class PhotosLoader implements Runnable {
        PhotoToLoad photoToLoad;

        PhotosLoader(PhotoToLoad photoToLoad) {
            this.photoToLoad = photoToLoad;
        }

        @Override
        public void run() {
            try {
                //Check if image already downloaded
                if (imageViewReused(photoToLoad))
                    return;
                // download image from web url
//                byte[] thumbnail = MediaUtility.getThumbnail(photoToLoad.url);

                // download image from web url
                byte[] thumbnail = getBitmapByte(photoToLoad.url);

                // set image data in Memory Cache
                if (thumbnail != null) {
                    memoryCache.put(photoToLoad.url, thumbnail);
                    // Get bitmap to display
                    BitmapDisplayer bd = new BitmapDisplayer(thumbnail, photoToLoad);
                    // Causes the Runnable bd (BitmapDisplayer) to be added to the message queue.
                    // The runnable will be run on the thread to which this handler is attached.
                    // BitmapDisplayer run method will call
                    handler.post(bd);
                } else {
                    // Get bitmap and store it in pool
                    // download image from web url
                    Bitmap bmp = getBitmap(photoToLoad.url);
                    // set image data in Memory Cache
                    bitmapMemoryCache.put(photoToLoad.url, bmp);
                    if (imageViewReused(photoToLoad))
                        return;

                    // Get bitmap to display
                    BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);

                    // Causes the Runnable bd (BitmapDisplayer) to be added to the message queue.
                    // The runnable will be run on the thread to which this handler is attached.
                    // BitmapDisplayer run method will call
                    handler.post(bd);
                }


            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
    }

    private static String PREFIX_BYTE = "byte_";
    private static String PREFIX_BITMAP = "bitmap_";

    private Bitmap getBitmap(String url) {
        File f = fileCache.getFile(url, PREFIX_BITMAP);

        //from SD cache
        //CHECK : if trying to decode file which not exist in cache return null
        Bitmap b = decodeFile(f);
        if (b != null) {
            Log.i("ByteImageLoader", "Read bitmap file from cache :- " + f.getName());
            return b;
        }

        try {
            Bitmap bitmap = decodeFile(new File(url));
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(f);
                Log.i("ByteImageLoader", "Write bitmap file from cache :- " + f.getName());
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
                // PNG is a lossless format, the compression factor (100) is ignored
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            /*final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(url, options);
        options.inSampleSize = MediaUtility.calculateInSampleSize(options, reqWidth, reqHeight);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            options.inMutable = true;
            Bitmap inBitmap = GlideBitmapPool.getBitmap(options.outWidth, options.outHeight, options.inPreferredConfig);
            if (inBitmap != null && Util.canUseForInBitmap(inBitmap, options)) {
                options.inBitmap = inBitmap;
            }
        }
        options.inJustDecodeBounds = false;
        try {
            return BitmapFactory.decodeFile(url, options);
        } catch (Exception e) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
                options.inBitmap = null;
            }
            return BitmapFactory.decodeFile(url, options);
        }*/

            return bitmap;
        } catch (Throwable ex) {
            ex.printStackTrace();
            if (ex instanceof OutOfMemoryError)
                memoryCache.clear();
            return null;
        }
    }

    //Decodes image and scales it to reduce memory consumption
    private Bitmap decodeFile(File f) {

        try {

            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            FileInputStream stream1 = new FileInputStream(f);
            BitmapFactory.decodeStream(stream1, null, o);
            stream1.close();

            //Find the correct scale value. It should be the power of 2.

            // Set width/height of recreated image
            final int REQUIRED_SIZE = reqWidth;

            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;
            while (true) {
                if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
                    break;
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }

            //decode with current scale values
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            FileInputStream stream2 = new FileInputStream(f);
            Bitmap bitmap = BitmapFactory.decodeStream(stream2, null, o2);
            stream2.close();
            return bitmap;

        } catch (FileNotFoundException ignored) {
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private byte[] getBitmapByte(String url) {
        File f = fileCache.getFile(url, PREFIX_BYTE);
        FileInputStream fin = null;
        byte[] fileContent;
        try {
            // create FileInputStream object
            fin = new FileInputStream(f);

            fileContent = new byte[(int) f.length()];

            // Reads up to certain bytes of data from this input stream into an array of bytes.
            fin.read(fileContent);
            //create string from byte array
            Log.i("ByteImageLoader", "Read byte file from cache :- " + f.getName());
            return fileContent;
        } catch (FileNotFoundException e) {
            System.out.println("File not found" + e);
        } catch (IOException ioe) {
            System.out.println("Exception while reading file " + ioe);
        } catch (Exception ioe) {
            System.out.println("Other File Exception while reading file " + ioe);
        } finally {
            // close the streams using close method
            try {
                if (fin != null) {
                    fin.close();
                }
            } catch (IOException ioe) {
                System.out.println("Error while closing stream: " + ioe);
            }
        }

        // Download image file from web
        try {
            byte[] thumbnail = MediaUtility.getThumbnail(url);
            if (thumbnail != null) {
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(f));
                bos.write(thumbnail);
                bos.flush();
                bos.close();
                Log.i("ByteImageLoader", "Write byte to file in cache :- " + f.getName());
            }

            return thumbnail;

        } catch (Throwable ex) {
            ex.printStackTrace();
            if (ex instanceof OutOfMemoryError) {
                memoryCache.clear();
                bitmapMemoryCache.clear();
            }
            return null;
        }
    }

    private boolean imageViewReused(PhotoToLoad photoToLoad) {

        String tag = imageViews.get(photoToLoad.imageView);
        //Check url is already exist in imageViews MAP
        return tag == null || !tag.equals(photoToLoad.url);
    }

    //Used to display bitmap in the UI thread
    private class BitmapDisplayer implements Runnable {
        private Bitmap bitmap;
        byte[] byteBitmap;
        PhotoToLoad photoToLoad;

        BitmapDisplayer(byte[] byteBitmap, PhotoToLoad p) {
            this.byteBitmap = byteBitmap;
            photoToLoad = p;
        }

        BitmapDisplayer(Bitmap bitmap, PhotoToLoad p) {
            this.bitmap = bitmap;
            photoToLoad = p;
        }

        @Override
        public void run() {
            if (imageViewReused(photoToLoad))
                return;

            // Show bitmap on UI
            if (byteBitmap != null) {
                Log.i("ByteImageLoader", "Load from byte");
                Glide.with(photoToLoad.imageView.getContext())
                        .load(byteBitmap)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .into(photoToLoad.imageView);
            } else if (bitmap != null) {
                Log.i("ByteImageLoader", "Load from bitmap");
                photoToLoad.imageView.setImageBitmap(bitmap);
            } else {
                Log.i("ByteImageLoader", "Load from glide uri direct");
                Glide.with(photoToLoad.imageView.getContext())
                        .load(Uri.parse("file://" + photoToLoad.url))
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .override(200, 200) // resizes the image to these dimensions (in pixel). does not respect aspect ratio
                        .centerCrop() // this cropping technique scales the image so that it fills the requested bounds and then crops the extra.
                        .into(photoToLoad.imageView);
            }
        }
    }

    public void clearCache() {
        //Clear cache directory downloaded images and stored data in maps
        memoryCache.clear();
        bitmapMemoryCache.clear();
        fileCache.clear();
    }

}
