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

    private HashMap<String, Bitmap> bitmapHashMap = new HashMap<>();
    private HashMap<String, byte[]> byteBitmapHashMap = new HashMap<>();


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

    public byte[] getImageByte(String key) {
        byte[] aByte = byteBitmapHashMap.get(key);
        return aByte;
    }

    public void putImageByte(String key, byte[] value) {
        byteBitmapHashMap.put(key, value);
    }
}
