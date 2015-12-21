package com.luminous.pick;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Collections;

public class CustomGalleryActivity extends Activity {

    GridView gridGallery;
    Handler handler;
    GalleryAdapter adapter;

    ImageView imgNoMedia;
    FrameLayout btnGalleryOk;
    String action;
    View.OnClickListener mOkClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            String[] allPath = adapter.getSelectedStringArray();
            if (allPath.length > 0) {

                if (allPath.length > 10) {
                    showAlertDialog(CustomGalleryActivity.this, "Please select only 10 images at a time");
                } else {
                    Intent data = new Intent().putExtra("all_path", allPath);
                    setResult(RESULT_OK, data);
                    finish();
                }
            } else {
                showAlertDialog(CustomGalleryActivity.this, "Please select an image.");
            }
        }
    };
    AdapterView.OnItemClickListener mItemMulClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> l, View v, int position, long id) {
            adapter.changeSelection(v, position);
            String[] allPath = adapter.getSelectedStringArray();
            if (allPath != null && allPath.length > 10) {
                showAlertDialog(CustomGalleryActivity.this, "Please select only 10 images at a time");
                adapter.changeSelection(v, position);
            }
        }
    };
    AdapterView.OnItemClickListener mItemSingleClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> l, View v, int position, long id) {
            CustomGallery item = adapter.getItem(position);
            String[] allPath = {item.sdcardPath};
            Intent data = new Intent().putExtra("all_path", allPath);
            setResult(RESULT_OK, data);
            finish();
        }
    };
    private FrameLayout btnGalleryCancel;
    private AlertDialog alertDialog;

    void showAlertDialog(Context mContext, String text) {


        alertDialog = new AlertDialog.Builder(mContext)
                .setMessage(text)
                .setCancelable(false).
                        setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                alertDialog.dismiss();
                            }
                        }).create();
        alertDialog.show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.gallery);

        action = getIntent().getAction();
        if (action == null) {
            finish();
        }
        init();
    }

    private void init() {

        handler = new Handler();
        gridGallery = (GridView) findViewById(R.id.gridGallery);
        gridGallery.setFastScrollEnabled(true);
        adapter = new GalleryAdapter(CustomGalleryActivity.this);

        if (action.equalsIgnoreCase(Action.ACTION_MULTIPLE_PICK)) {

            findViewById(R.id.llBottomContainer).setVisibility(View.VISIBLE);
            gridGallery.setOnItemClickListener(mItemMulClickListener);
            adapter.setMultiplePick(true);

        } else if (action.equalsIgnoreCase(Action.ACTION_PICK)) {

            findViewById(R.id.llBottomContainer).setVisibility(View.GONE);
            gridGallery.setOnItemClickListener(mItemSingleClickListener);
            adapter.setMultiplePick(false);

        }

        gridGallery.setAdapter(adapter);
        imgNoMedia = (ImageView) findViewById(R.id.imgNoMedia);

        btnGalleryOk = (FrameLayout) findViewById(R.id.btn_done);
        btnGalleryCancel = (FrameLayout) findViewById(R.id.btn_cancel);
        btnGalleryCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                setResult(RESULT_CANCELED, data);
                finish();
            }
        });
        btnGalleryOk.setOnClickListener(mOkClickListener);

        new Thread() {

            @Override
            public void run() {
                Looper.prepare();
                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        adapter.addAll(getGalleryPhotos());
                        checkImageStatus();
                    }
                });
                Looper.loop();
            }

            ;

        }.start();

    }

    private void checkImageStatus() {
        if (adapter.isEmpty()) {
            imgNoMedia.setVisibility(View.VISIBLE);
        } else {
            imgNoMedia.setVisibility(View.GONE);
        }
    }

    private ArrayList<CustomGallery> getGalleryPhotos() {
        ArrayList<CustomGallery> galleryList = new ArrayList<CustomGallery>();
        try {
            Cursor imagecursor = null;
            try {
//                final String[] columns = {MediaStore.Images.Media.DATA,
//                        MediaStore.Images.Media._ID};
                final String orderBy = MediaStore.Images.Media._ID;

                imagecursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        null, // Which columns to return
                        null,       // Return all rows
                        null,
                        null);
//                imagecursor = managedQuery(
//                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns,
//                        null, null, orderBy);

                if (imagecursor != null && imagecursor.getCount() > 0) {

                    while (imagecursor.moveToNext()) {
                        CustomGallery item = new CustomGallery();

                        int dataColumnIndex = imagecursor
                                .getColumnIndex(MediaStore.Images.Media.DATA);

                        item.sdcardPath = imagecursor.getString(dataColumnIndex);

                        galleryList.add(item);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (imagecursor != null)
                    imagecursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        // show newest photo at beginning of the list
        Collections.reverse(galleryList);
        return galleryList;
    }

}
