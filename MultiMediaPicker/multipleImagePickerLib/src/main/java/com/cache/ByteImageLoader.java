package com.cache;

import android.content.Context;
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

    // Initialize MemoryCache
    private MemoryCache memoryCache = new MemoryCache();

    private FileCache fileCache;

    //Create Map (collection) to store image and image url in key value pair
    private Map<ImageView, String> imageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
    private ExecutorService executorService;

    //handler to display images in UI thread
    private Handler handler = new Handler();

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
            Glide.with(imageView.getContext())
                    .load(imageByte)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .into(imageView);
            // if image is stored in MemoryCache Map then
            // Show image in listview row
        } else {
            //queue Photo to download from url
            queuePhoto(url, imageView);

            //Before downloading image show default image 
            imageView.setImageResource(stub_id);
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
                byte[] thumbnail = getBitmap(photoToLoad.url);

                // set image data in Memory Cache
                memoryCache.put(photoToLoad.url, thumbnail);

                if (imageViewReused(photoToLoad))
                    return;

                // Get bitmap to display
                BitmapDisplayer bd = new BitmapDisplayer(thumbnail, photoToLoad);

                // Causes the Runnable bd (BitmapDisplayer) to be added to the message queue. 
                // The runnable will be run on the thread to which this handler is attached.
                // BitmapDisplayer run method will call
                handler.post(bd);

            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
    }

    private byte[] getBitmap(String url) {
        File f = fileCache.getFile(url);
        FileInputStream fin = null;
        byte[] fileContent;
        try {
            // create FileInputStream object
            fin = new FileInputStream(f);

            fileContent = new byte[(int) f.length()];

            // Reads up to certain bytes of data from this input stream into an array of bytes.
            fin.read(fileContent);
            //create string from byte array
            String s = new String(fileContent);
            System.out.println("File content: " + s);
            Log.i("ByteImageLoader", "Read file from cache :- " + f.getName());
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
                Log.i("ByteImageLoader", "Write to file in cache :- " + f.getName());
            }

            return thumbnail;

        } catch (Throwable ex) {
            ex.printStackTrace();
            if (ex instanceof OutOfMemoryError)
                memoryCache.clear();
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
        byte[] bitmap;
        PhotoToLoad photoToLoad;

        BitmapDisplayer(byte[] b, PhotoToLoad p) {
            bitmap = b;
            photoToLoad = p;
        }

        public void run() {
            if (imageViewReused(photoToLoad))
                return;

            // Show bitmap on UI
            if (bitmap != null) {
                Glide.with(photoToLoad.imageView.getContext())
                        .load(bitmap)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .into(photoToLoad.imageView);
            } else {
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
        fileCache.clear();
    }

}
