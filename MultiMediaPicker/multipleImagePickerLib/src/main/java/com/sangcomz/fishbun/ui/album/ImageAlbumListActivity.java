package com.sangcomz.fishbun.ui.album;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.luminous.pick.R;
import com.msupport.MSupport;
import com.msupport.MSupportConstants;
import com.sangcomz.fishbun.ItemDecoration.DividerItemDecoration;
import com.sangcomz.fishbun.adapter.ImageAlbumListAdapter;
import com.sangcomz.fishbun.bean.Album;
import com.sangcomz.fishbun.define.Define;

import java.util.ArrayList;
import java.util.List;


public class ImageAlbumListActivity extends AppCompatActivity {

    private List<Album> albumList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ImageAlbumListAdapter adapter;
    private List<String> thumbList;
    private RelativeLayout noAlbum;
    private int pickCount;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_album);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        noAlbum = (RelativeLayout) findViewById(R.id.no_album);
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(Define.ACTIONBAR_COLOR);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        pickCount = getIntent().getIntExtra("pickCount", 1);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean isStoragePermissionGiven = MSupport.checkPermissionWithRationale(ImageAlbumListActivity.this,
                    null, MSupportConstants.WRITE_EXTERNAL_STORAGE, MSupportConstants.REQUEST_STORAGE_READ_WRITE);
            if (isStoragePermissionGiven)
                new DisplayImage().execute();
        } else
            new DisplayImage().execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MSupportConstants.REQUEST_STORAGE_READ_WRITE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    new DisplayImage().execute();
                } else {
                    Toast.makeText(ImageAlbumListActivity.this, "Storage permission not granted", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

    public class DisplayImage extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {

            final String orderBy = MediaStore.Images.Media.BUCKET_ID;
            final ContentResolver resolver = getContentResolver();
            String[] projection = new String[]{
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                    MediaStore.Images.Media.BUCKET_ID};

            Cursor imagecursor = resolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                    null, null, orderBy);

            long previousid = 0;

            int bucketColumn = imagecursor
                    .getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);

            int bucketcolumnid = imagecursor
                    .getColumnIndex(MediaStore.Images.Media.BUCKET_ID);
            albumList = new ArrayList<Album>();
            Album totalAlbum = new Album();
            totalAlbum.bucketid = 0;
            totalAlbum.bucketname = getString(R.string.str_all_view);
            totalAlbum.counter = 0;
            albumList.add(totalAlbum);
            int totalCounter = 0;
            while (imagecursor.moveToNext()) {
                totalCounter++;
                long bucketid = imagecursor.getInt(bucketcolumnid);
                if (previousid != bucketid) {
                    Album album = new Album();
                    album.bucketid = bucketid;
                    album.bucketname = imagecursor.getString(bucketColumn);
                    album.counter++;
                    albumList.add(album);
                    previousid = bucketid;

                } else {
                    if (albumList.size() > 0)
                        albumList.get(albumList.size() - 1).counter++;
                }
                if (imagecursor.isLast()) {
                    albumList.get(0).counter = totalCounter;
                }
            }
            imagecursor.close();
            if (totalCounter == 0) {
                albumList.clear();
                return false;
            } else {
                return true;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {
                noAlbum.setVisibility(View.GONE);
//                if (adapter == null) {
                adapter = new ImageAlbumListAdapter(albumList, pickCount);
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                new DisplayThumbnail().execute();
            } else {
                noAlbum.setVisibility(View.VISIBLE);
            }
        }
    }

    public class DisplayThumbnail extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            thumbList = new ArrayList<String>();
        }

        @Override
        protected Void doInBackground(Void... params) {

            for (int i = 0; i < albumList.size(); i++) {
                Album album = albumList.get(i);

                String path = getAllMediaThumbnailsPath(album.bucketid);
                thumbList.add(path);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            adapter.setThumbList(thumbList);
        }
    }


    /*private String getAllMediaThumbnailsPath(long id) {
        String path = "";
        String selection = MediaStore.Images.Media.BUCKET_ID + " = ?";
        String bucketId = String.valueOf(id);
        String sort = MediaStore.Images.Thumbnails._ID + " DESC";
        String[] selectionArgs = {bucketId};

        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor c;
        if (!bucketId.equals("0")) {
            c = getContentResolver().query(images, null,
                    selection, selectionArgs, sort);
        } else {
            c = getContentResolver().query(images, null,
                    null, null, sort);
        }


        if (c != null) {
            if (c.moveToNext()) {
                selection = MediaStore.Images.Media._ID + " = ?";
                String photoID = c.getString(c.getColumnIndex(MediaStore.Images.Media._ID));
                selectionArgs = new String[]{photoID};

                images = MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI;
                Cursor cursor = getContentResolver().query(images, null,
                        selection, selectionArgs, sort);
                if (cursor != null && cursor.moveToNext()) {
                    path = c.getString(c.getColumnIndex(MediaStore.Images.Media.DATA));
                } else
                    path = c.getString(c.getColumnIndex(MediaStore.Images.Media.DATA));
                if (cursor != null) {
                    cursor.close();
                }
            } else {
                Log.e("id", "from else");
            }
        }

        if (c != null) {
            c.close();
        }
        return path;
    }*/

    private String getAllMediaThumbnailsPath(long id) {
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
            while (true) {
                path = c.getString(c.getColumnIndex(MediaStore.Images.Thumbnails.DATA));
                c.close();
                break;
            }
        }
        return path;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent i = new Intent();
                setResult(RESULT_CANCELED, i);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Define.ENTER_ALBUM_REQUEST_CODE
                && resultCode == Activity.RESULT_OK) {
            Intent i = new Intent();
            i.putStringArrayListExtra(Define.INTENT_PATH, data.getStringArrayListExtra(Define.INTENT_PATH));
            setResult(RESULT_OK, i);
            finish();
        }
    }
}
