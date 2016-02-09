package com.app.multiimagepickercropper;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;

import com.luminous.pick.CustomGallery;
import com.luminous.pick.controller.MediaFactory;
import com.luminous.pick.utils.VideoQuality;

import java.util.HashMap;


public class MainActivity extends AppCompatActivity {

    private static final int GALLERY_APP = 111;
    private static final int CAMERA_APP = 222;
    private ImageAdapter mImageAdapter;
    private MediaFactory mediaFactory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMediaPathArrayList = new HashMap<>();
        mImageAdapter = new ImageAdapter(mMediaPathArrayList, this);
        ListView mListView = (ListView) findViewById(R.id.listview);
        mListView.setAdapter(mImageAdapter);
        findViewById(R.id.img_single_pick).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaFactory.MediaBuilder mediaBuilder = new MediaFactory.MediaBuilder(MainActivity.this)
                        .getSingleMediaFiles()
                        .takeVideo()
                        .fromCamera();
                mediaFactory = MediaFactory.create().start(mediaBuilder);
            }
        });

        findViewById(R.id.img_multiple_pick).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMultipleImageChooserDialog();
            }
        });
        findViewById(R.id.vid_single_pick).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSingleVideoChooserDialog();
            }
        });
        findViewById(R.id.vid_multiple_pick).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMultipleVideoChooserDialog();
            }
        });
    }

    private void showSingleVideoChooserDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Single Video");
        alertDialog.setMessage("Select video source.");
        alertDialog.setIcon(R.drawable.ic_launcher);
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Gallery",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {

                        MediaFactory.MediaBuilder mediaBuilder = new MediaFactory.MediaBuilder(MainActivity.this)
                                .getSingleMediaFiles()
                                .takeVideo()
                                .fromGallery();
                        mediaFactory = MediaFactory.create().start(mediaBuilder);

                    }
                });

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Camera",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {

                        MediaFactory.MediaBuilder mediaBuilder = new MediaFactory.MediaBuilder(MainActivity.this)
                                .getSingleMediaFiles()
                                .fromCamera()
                                .setVideoDuration(300)
                                .setVideoSize(15)
                                .setVideoQuality(VideoQuality.HIGH_QUALITY)
                                .takeVideo();
                        mediaFactory = MediaFactory.create().start(mediaBuilder);

                    }
                });
        alertDialog.show();
    }

    private void showMultipleVideoChooserDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Multiple Video");
        alertDialog.setMessage("Select video source.");
        alertDialog.setIcon(R.drawable.ic_launcher);
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Gallery",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {

                        MediaFactory.MediaBuilder mediaBuilder = new MediaFactory.MediaBuilder(MainActivity.this)
                                .getMultipleMediaFiles().fromGallery()
                                .takeVideo();
                        mediaFactory = MediaFactory.create().start(mediaBuilder);

                    }
                });

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Camera",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {

                        MediaFactory.MediaBuilder mediaBuilder = new MediaFactory.MediaBuilder(MainActivity.this)
                                .getMultipleMediaFiles().fromCamera()
                                .setVideoSize(2)
                                .takeVideo();
                        mediaFactory = MediaFactory.create().start(mediaBuilder);
                    }
                });
        alertDialog.show();
    }

    private void showSingleImageChooserDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Single Image");
        alertDialog.setMessage("Select picture source.");
        alertDialog.setIcon(R.drawable.ic_launcher);
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Gallery",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {

                        MediaFactory.MediaBuilder mediaBuilder = new MediaFactory.MediaBuilder(MainActivity.this)
                                .getSingleMediaFiles()
                                .doCropping()
                                .setImageQuality(50)
                                .fromGallery();
                        mediaFactory = MediaFactory.create().start(mediaBuilder);

                    }
                });

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Camera",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {

                        MediaFactory.MediaBuilder mediaBuilder = new MediaFactory.MediaBuilder(MainActivity.this)
                                .getSingleMediaFiles()
                                .fromCamera()
                                .doCropping();
                        mediaFactory = MediaFactory.create().start(mediaBuilder);

                    }
                });
        alertDialog.show();
    }

    private void showMultipleImageChooserDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Multiple Image");
        alertDialog.setMessage("Select picture source.");
        alertDialog.setIcon(R.drawable.ic_launcher);
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Gallery",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {

                        MediaFactory.MediaBuilder mediaBuilder = new MediaFactory.MediaBuilder(MainActivity.this)
                                .getMultipleMediaFiles().fromGallery()
                                .doCropping();
                        mediaFactory = MediaFactory.create().start(mediaBuilder);

                    }
                });

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Camera",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {

                        MediaFactory.MediaBuilder mediaBuilder = new MediaFactory.MediaBuilder(MainActivity.this)
                                .getMultipleMediaFiles().fromCamera()
                                .setImageQuality(50)
                                .doCropping();
                        mediaFactory = MediaFactory.create().start(mediaBuilder);
                    }
                });
        alertDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String[] all_path = mediaFactory.onActivityResult(requestCode, resultCode, data);
        for (String string : all_path) {
            CustomGallery item = new CustomGallery();
            item.sdcardPath = string;
            item.sdCardUri = Uri.parse(string);

            MediaBean bean = new MediaBean();
            bean.setImage(true);
            bean.setImagePath(item.sdcardPath);
            bean.setImageUri(item.sdCardUri);
            mMediaPathArrayList.put(item.sdcardPath, bean);
        }
        mImageAdapter.customNotify(mMediaPathArrayList);
    }

    private HashMap<String, MediaBean> mMediaPathArrayList;
}