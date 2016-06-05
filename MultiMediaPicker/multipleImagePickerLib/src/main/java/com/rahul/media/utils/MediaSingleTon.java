package com.rahul.media.utils;

import android.graphics.Bitmap;

import java.util.HashMap;

/**
 * Created by rahul on 20/8/15.
 */
public class MediaSingleTon {

    private static MediaSingleTon mediaSingleTon;

    private MediaSingleTon() {

    }

    public static MediaSingleTon getInstance() {
        if (mediaSingleTon == null)
            return mediaSingleTon = new MediaSingleTon();
        return mediaSingleTon;
    }

    HashMap<String, Bitmap> bitmapHashMap = new HashMap<>();


    public HashMap<String, Bitmap> getBitmapHashMap() {
        return bitmapHashMap;
    }

    public void setBitmapHashMap(HashMap<String, Bitmap> bitmapHashMap) {
        this.bitmapHashMap = bitmapHashMap;
    }


    public void putImage(String key, Bitmap value) {
        bitmapHashMap.put(key, value);
    }

    public Bitmap getImage(String key) {
        Bitmap bitmap = bitmapHashMap.get(key);
        if (bitmap != null && bitmap.isRecycled()) {
            return null;
        }
        return bitmap;
    }

}
