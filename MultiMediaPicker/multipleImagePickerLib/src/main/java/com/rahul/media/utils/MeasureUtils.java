package com.rahul.media.utils;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

/**
 * Created by rahul on 6/16/2016.
 */

public class MeasureUtils {
    public static int ALBUM_CARD_WIDTH = 1300;

    public static int getAlbumsColumns(Context c) {
        int n = Math.round(getDensity(c) / ALBUM_CARD_WIDTH);
        return n < 2 ? 2 : n;
    }

    public static int getScreenWidth(Context c) {
        WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    public static int getDensity(Context c) {
        return Math.round((getScreenWidth(c) * c.getResources().getDisplayMetrics().density));
    }
}
