package com.app.multiimagepickercropper;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;

import com.luminous.pick.Action;
import com.luminous.pick.CameraPickActivity;
import com.luminous.pick.CustomGallery;
import com.luminous.pick.MultipleImagePreviewActivity;

import java.util.HashMap;


public class MainActivity extends AppCompatActivity {

    private static final int GALLERY_APP = 111;
    private static final int CAMERA_APP = 222;
    private ImageAdapter mImageAdapter;

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
                showSingleImageChooserDialog();
            }
        });

        findViewById(R.id.img_multiple_pick).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMultipleImageChooserDialog();
            }
        });
    }

    private void showSingleImageChooserDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Single Image");
        alertDialog.setMessage("Select picture source.");
        alertDialog.setIcon(R.drawable.ic_launcher);
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Gallery",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {

                        Intent intent = new Intent(MainActivity.this, MultipleImagePreviewActivity.class);
                        intent.setAction(Action.ACTION_PICK);
                        startActivityForResult(intent, GALLERY_APP);
                    }
                });

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Camera",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {

                        Intent intent = new Intent(MainActivity.this, CameraPickActivity.class);
                        intent.setAction(Action.ACTION_PICK);
                        startActivityForResult(intent, CAMERA_APP);
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

                        Intent intent = new Intent(MainActivity.this, MultipleImagePreviewActivity.class);
                        intent.setAction(Action.ACTION_MULTIPLE_PICK);
                        startActivityForResult(intent, GALLERY_APP);
                    }
                });

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Camera",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {

                        Intent intent = new Intent(MainActivity.this, CameraPickActivity.class);
                        intent.setAction(Action.ACTION_MULTIPLE_PICK);
                        startActivityForResult(intent, CAMERA_APP);
                    }
                });
        alertDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY_APP || requestCode == CAMERA_APP) {
                String[] all_path = data.getStringArrayExtra("all_path");

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
        }
    }

    private HashMap<String, MediaBean> mMediaPathArrayList;
}