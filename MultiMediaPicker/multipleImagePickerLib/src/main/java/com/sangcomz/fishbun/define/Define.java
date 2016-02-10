package com.sangcomz.fishbun.define;

import android.graphics.Color;

/**
 * Created by sangc on 2015-11-02.
 */
public class Define {
    public static int ALBUM_THUMBNAIL_SIZE = 70;


    //    public static int ACTIONBAR_COLOR = "#3F51B5";
    public static int ACTIONBAR_COLOR = Color.parseColor("#3F51B5");
    //    public static int STATUSBAR_COLOR = "#303F9F";
    public static int STATUSBAR_COLOR = Color.parseColor("#303F9F");

    public static final String INTENT_PATH = "all_path";
    public static int ENTER_ALBUM_REQUEST_CODE = 123;

    /*public static void setActionbarTitle(int total) {
        if (pickCount == 1)
            actionBar.setTitle(bucketTitle);
        else
            actionBar.setTitle(bucketTitle + "(" + String.valueOf(total) + "/" + pickCount + ")");
    }*/
}
