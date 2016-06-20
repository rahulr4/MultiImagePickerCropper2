package com.app.multiimagepickercropper;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.rahul.media.main.MediaFactory;
import com.rahul.media.model.CustomGallery;

import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity implements OnClickListener {

    private ImageAdapter mImageAdapter;
    private MediaFactory mediaFactory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Fresco.initialize(this);
        mMediaPathArrayList = new HashMap<>();
        mImageAdapter = new ImageAdapter(mMediaPathArrayList, this);
        ListView mListView = (ListView) findViewById(R.id.listview);
        mListView.setAdapter(mImageAdapter);

        findViewById(R.id.img_single_pick).setOnClickListener(this);
        findViewById(R.id.img_multiple_pick).setOnClickListener(this);

        findViewById(R.id.img_single_pick_camera).setOnClickListener(this);
        findViewById(R.id.img_multiple_pick_camera).setOnClickListener(this);

        findViewById(R.id.vid_single_pick).setOnClickListener(this);
        findViewById(R.id.vid_multiple_pick).setOnClickListener(this);

        findViewById(R.id.vid_single_pick_camera).setOnClickListener(this);
        findViewById(R.id.vid_multiple_pick_camera).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        MediaFactory.MediaBuilder mediaBuilder = null;
        switch (v.getId()) {
            case R.id.img_single_pick:
                mediaBuilder = new MediaFactory.MediaBuilder(MainActivity.this)
                        .setPickCount(1)
                        .doCropping()
                        .fromGallery();
                mediaFactory = MediaFactory.create().start(mediaBuilder);
                break;
            case R.id.img_multiple_pick:
                mediaBuilder = new MediaFactory.MediaBuilder(MainActivity.this)
                        .fromGallery()
                        .setPickCount(10)
                        .doCropping();
                mediaFactory = MediaFactory.create().start(mediaBuilder);
                break;
            case R.id.img_single_pick_camera:
                mediaBuilder = new MediaFactory.MediaBuilder(MainActivity.this)
                        .fromCamera();
                mediaFactory = MediaFactory.create().start(mediaBuilder);
                break;
            case R.id.img_multiple_pick_camera:
                mediaBuilder = new MediaFactory.MediaBuilder(MainActivity.this)
                        .fromCamera();
                mediaFactory = MediaFactory.create().start(mediaBuilder);
                break;
            case R.id.vid_single_pick:
                mediaBuilder = new MediaFactory.MediaBuilder(MainActivity.this)
                        .takeVideo()
                        .fromGallery();
                mediaFactory = MediaFactory.create().start(mediaBuilder);
                break;
            case R.id.vid_multiple_pick:
                mediaBuilder = new MediaFactory.MediaBuilder(MainActivity.this)
                        .takeVideo()
                        .setPickCount(5)
                        .fromGallery();
                mediaFactory = MediaFactory.create().start(mediaBuilder);
                break;
            case R.id.vid_single_pick_camera:
                mediaBuilder = new MediaFactory.MediaBuilder(MainActivity.this)
                        .takeVideo()
                        .fromCamera();
                mediaFactory = MediaFactory.create().start(mediaBuilder);
                break;
            case R.id.vid_multiple_pick_camera:
                mediaBuilder = new MediaFactory.MediaBuilder(MainActivity.this)
                        .takeVideo()
                        .fromCamera();
                mediaFactory = MediaFactory.create().start(mediaBuilder);
                break;

        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ArrayList<String> all_path = mediaFactory.onActivityResult(requestCode, resultCode, data);
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