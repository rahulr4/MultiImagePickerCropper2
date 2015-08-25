package com.luminous.pick.controller;

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

    public static void setMediaSingleTon(MediaSingleTon mediaSingleTon) {
        MediaSingleTon.mediaSingleTon = mediaSingleTon;
    }

    public HashMap<String, Bitmap> getBitmapHashMap() {
        return bitmapHashMap;
    }

    public void setBitmapHashMap(HashMap<String, Bitmap> bitmapHashMap) {
        this.bitmapHashMap = bitmapHashMap;
    }
}
