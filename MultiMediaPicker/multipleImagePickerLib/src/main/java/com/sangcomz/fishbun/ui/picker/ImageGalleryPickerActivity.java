package com.sangcomz.fishbun.ui.picker;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.luminous.pick.R;
import com.msupport.MSupport;
import com.msupport.MSupportConstants;
import com.sangcomz.fishbun.adapter.ImageGalleryGridAdapter;
import com.sangcomz.fishbun.bean.Album;
import com.sangcomz.fishbun.bean.MediaObject;
import com.sangcomz.fishbun.bean.MediaType;
import com.sangcomz.fishbun.define.Define;

import java.io.File;
import java.util.ArrayList;


public class ImageGalleryPickerActivity extends AppCompatActivity {

    private GridView gridView;
    private ArrayList<MediaObject> mGalleryImageArrayList = new ArrayList<>();
    private Album a;

    ImageGalleryGridAdapter adapter;

    private String pathDir = "";
    private int pickCount;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Glide.get(this).clearMemory();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_picker);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(Define.ACTIONBAR_COLOR);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        a = (Album) getIntent().getSerializableExtra("album");
        getSupportActionBar().setTitle(a.bucketname);
        pickCount = getIntent().getIntExtra("pickCount", 1);
        gridView = (GridView) findViewById(R.id.gridview);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean isStoragePermissionGiven = MSupport.checkPermissionWithRationale(ImageGalleryPickerActivity.this,
                    null, MSupportConstants.WRITE_EXTERNAL_STORAGE, MSupportConstants.REQUEST_STORAGE_READ_WRITE);
            if (isStoragePermissionGiven)
                new DisplayImage().execute();
        } else
            new DisplayImage().execute();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_photo_album, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_ok) {
            if (mGalleryImageArrayList.size() == 0) {
                Snackbar.make(gridView, getString(R.string.msg_no_slected), Snackbar.LENGTH_SHORT).show();
            } else {
                ArrayList<String> path = new ArrayList<>();
                for (int i = 0; i < mGalleryImageArrayList.size(); i++) {
                    if (mGalleryImageArrayList.get(i).isSelected)
                        path.add(mGalleryImageArrayList.get(i).getPath());
                }
                Intent i = new Intent();
                i.putStringArrayListExtra(Define.INTENT_PATH, path);
                setResult(RESULT_OK, i);
                finish();
            }
            return true;
        } else if (id == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }

    private class DisplayImage extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Thread t = new Thread(new Runnable() {
                public void run() {

                }
            });
            t.start();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            getAllMediaThumbnailsPath(a.bucketid);
            return true;
        }


        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result != null) {
                if (result) {
                    adapter = new ImageGalleryGridAdapter(ImageGalleryPickerActivity.this,
                            mGalleryImageArrayList, getPathDir(), pickCount, getSupportActionBar(), a.bucketname);
                    gridView.setAdapter(adapter);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        switch (requestCode) {
            case MSupportConstants.REQUEST_STORAGE_READ_WRITE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    new DisplayImage().execute();
                } else {
                    Toast.makeText(ImageGalleryPickerActivity.this, "Storage permission not granted", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

    private void getAllMediaThumbnailsPath(long id) {
        String path = "";
        String selection = MediaStore.Images.Media.BUCKET_ID + " = ?";
        String bucketid = String.valueOf(id);
        String sort = MediaStore.Images.Media._ID + " DESC";
        String[] selectionArgs = {bucketid};

        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor c;
        if (!bucketid.equals("0")) {
            c = getContentResolver().query(images, null,
                    selection, selectionArgs, sort);
        } else {
            c = getContentResolver().query(images, null,
                    null, null, sort);
        }


        if (c != null) {

            c.moveToFirst();
            setPathDir(c.getString(c.getColumnIndex(MediaStore.Images.Media.DATA)), c.getString(c.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)));
            int position = 0;
            while (true) {
                path = c.getString(c.getColumnIndex(MediaStore.Images.Thumbnails.DATA));
                long creationDate = getCreationDate(path);
                if (c.isLast()) {
                    MediaObject mediaObject = new MediaObject(c.getInt(0),
                            path, MediaType.PHOTO, creationDate);
                    mGalleryImageArrayList.add(mediaObject);

                    c.close();
                    break;
                } else {
                    MediaObject mediaObject = new MediaObject(c.getInt(0),
                            path, MediaType.PHOTO, creationDate);
                    mGalleryImageArrayList.add(mediaObject);
                    c.moveToNext();
                }
            }
        }
    }

    private static long getCreationDate(String filePath) {
        File file = new File(filePath);
        return file.lastModified();
    }

    //MediaScanning
    public void startFileMediaScan(String path) {
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + path)));
    }

    private void setPathDir(String path, String fileName) {
        pathDir = path.replace("/" + fileName, "");
    }

    private String getPathDir() {

        if (pathDir.equals("") || a.bucketid == 0)
            pathDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES).getAbsolutePath();
        return pathDir;
    }

}
