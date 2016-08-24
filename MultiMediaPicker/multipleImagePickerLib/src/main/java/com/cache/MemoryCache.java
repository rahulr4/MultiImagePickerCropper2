package com.cache;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

class MemoryCache {

    private static final String TAG = "MemoryCache";

    //Last argument true for LRU ordering
    private Map<String, byte[]> cache = Collections.synchronizedMap(
            new LinkedHashMap<String, byte[]>(10, 1.5f, true));

    //current allocated size
    private long size = 0;

    //max memory cache folder used to download images in bytes
    private long limit = 1000000;

    MemoryCache() {

        //use 25% of available heap size
        setLimit(Runtime.getRuntime().maxMemory() / 4);
    }

    private void setLimit(long new_limit) {

        limit = new_limit;
        Log.i(TAG, "MemoryCache will use up to " + limit / 1024. / 1024. + "MB");
    }

    public byte[] get(String id) {
        try {
            if (!cache.containsKey(id))
                return null;
            //NullPointerException sometimes happen here http://code.google.com/p/osmdroid/issues/detail?id=78 
            return cache.get(id);
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public void put(String id, byte[] bitmap) {
        try {
            if (cache.containsKey(id))
                size -= getSizeInBytes(cache.get(id));
            cache.put(id, bitmap);
            size += getSizeInBytes(bitmap);
            checkSize();
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    private void checkSize() {
        Log.i(TAG, "cache size=" + size + " length=" + cache.size());
        if (size > limit) {
            Iterator<Entry<String, byte[]>> iter = cache.entrySet().iterator();//least recently accessed item will be the first one iterated
            while (iter.hasNext()) {
                Entry<String, byte[]> entry = iter.next();
                size -= getSizeInBytes(entry.getValue());
                iter.remove();
                if (size <= limit)
                    break;
            }
            Log.i(TAG, "Clean cache. New size " + cache.size());
        }
    }

    public void clear() {
        try {
            //NullPointerException sometimes happen here http://code.google.com/p/osmdroid/issues/detail?id=78 
            cache.clear();
            size = 0;
            cache = null;
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    private long getSizeInBytes(byte[] imageData) {
        if(imageData == null)
            return 0;
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);

        if (bitmap == null)
            return 0;
        int i = bitmap.getRowBytes() * bitmap.getHeight();
        bitmap.recycle();
        bitmap = null;
        return i;
    }
}