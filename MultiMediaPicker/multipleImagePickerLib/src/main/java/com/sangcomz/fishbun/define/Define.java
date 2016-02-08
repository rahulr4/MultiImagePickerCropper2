package com.sangcomz.fishbun.define;

import android.graphics.Color;

/**
 * Created by sangc on 2015-11-02.
 */
public class Define {
    public static int ALBUM_THUMNAIL_SIZE = -1;
    public static int ALBUM_PICKER_COUNT = 1;

    //    public static int ACTIONBAR_COLOR = "#3F51B5";
    public static int ACTIONBAR_COLOR = Color.parseColor("#3F51B5");
    //    public static int STATUSBAR_COLOR = "#303F9F";
    public static int STATUSBAR_COLOR = Color.parseColor("#303F9F");

    public static boolean IS_CAMERA = false;

    public static int ADD_PHOTO_REQUEST_CODE = 127;
    public static int TAKE_A_PICK_REQUEST_CODE = 128;
    public static int TAKE_A_VIDEO_REQUEST_CODE = 129;
    public static int ENTER_ALBUM_REQUEST_CODE = 128;
    public final static int ALBUM_REQUEST_CODE = 27;
    public final static int PERMISSION_STORAGE = 28;

    public static String INTENT_PATH = "intent_path";

}
