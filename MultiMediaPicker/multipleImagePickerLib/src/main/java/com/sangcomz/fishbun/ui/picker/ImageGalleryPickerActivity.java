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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.luminous.pick.R;
import com.sangcomz.fishbun.adapter.ImageGalleryGridAdapter;
import com.sangcomz.fishbun.bean.Album;
import com.sangcomz.fishbun.bean.ImageBean;
import com.sangcomz.fishbun.bean.PickedImageBean;
import com.sangcomz.fishbun.define.Define;
import com.sangcomz.fishbun.permission.PermissionCheck;

import java.io.File;
import java.util.ArrayList;


public class ImageGalleryPickerActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ArrayList<PickedImageBean> pickedImageBeans;
    private PickerController pickerController;
    private Album a;
    //    boolean stop = false;
    private ImageBean[] imageBeans;
    PermissionCheck permissionCheck;

    ImageGalleryGridAdapter adapter;

    private String pathDir = "";

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
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(gridLayoutManager);
        pickedImageBeans = new ArrayList<>();

        pickerController = new PickerController(this, getSupportActionBar(), recyclerView, a.bucketname);

        ArrayList<String> path = getIntent().getStringArrayListExtra(Define.INTENT_PATH);
        if (path != null) {
            for (int i = 0; i < path.size(); i++) {
                pickedImageBeans.add(new PickedImageBean(i + 1, path.get(i), -1));
            }
        }
        pickerController.setActionbarTitle(pickedImageBeans.size());
        imageBeans = new ImageBean[a.counter];

        permissionCheck = new PermissionCheck(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionCheck.CheckStoragePermission())
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
            if (pickedImageBeans.size() == 0) {
                Toast.makeText(this, getString(R.string.msg_no_slected), Toast.LENGTH_SHORT).show();
//                Snackbar.make(recyclerView, getString(R.string.msg_no_slected), Snackbar.LENGTH_SHORT).show();
            } else {
                ArrayList<String> path = new ArrayList<>();
                for (int i = 0; i < pickedImageBeans.size(); i++) {
                    path.add(pickedImageBeans.get(i).getImgPath());
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
                    getAllMediaThumbnailsPath(a.bucketid);
                }
            });
            t.start();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean flag = true;
            while (flag) {
                if (imageBeans[0] != null && imageBeans[0].getImgPath().length() > 0) {
                    flag = false;
                }
            }
            return true;
        }


        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result != null) {
                if (result) {
                    adapter = new ImageGalleryGridAdapter(getApplicationContext(),
                            imageBeans, pickedImageBeans, pickerController, getPathDir());
                    recyclerView.setAdapter(adapter);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Define.PERMISSION_STORAGE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    new DisplayImage().execute();
                    // permission was granted, yay! do the
                    // calendar task you need to do.
                } else {
                    permissionCheck.showPermissionDialog(recyclerView);
                    finish();
                }
                return;
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
                if (c.isLast()) {
                    imageBeans[position] = new ImageBean(-1, path);
                    c.close();
                    break;
                } else {
                    imageBeans[position++] = new ImageBean(-1, path);
                    c.moveToNext();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Define.TAKE_A_PICK_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                startFileMediaScan(pickerController.getSavePath());
                adapter.addImage(pickerController.getSavePath());
                setResult(Define.ADD_PHOTO_REQUEST_CODE);
            } else {
                new File(pickerController.getSavePath()).delete();
            }
        }
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
