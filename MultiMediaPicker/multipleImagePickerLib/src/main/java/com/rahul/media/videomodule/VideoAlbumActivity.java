package com.rahul.media.videomodule;

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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.msupport.MSupport;
import com.msupport.MSupportConstants;
import com.rahul.media.R;
import com.rahul.media.videomodule.adapter.VideoAlbumListAdapter;
import com.rahul.media.model.GalleryPhotoAlbum;
import com.rahul.media.model.Define;

import java.util.ArrayList;

import static com.rahul.media.model.Define.ENTER_ALBUM_REQUEST_CODE;


public class VideoAlbumActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RelativeLayout noAlbum;
    private ArrayList<GalleryPhotoAlbum> arrayListAlbums = new ArrayList<>();
    private int pickCount;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_album);
        pickCount = getIntent().getIntExtra("pickCount", 1);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        noAlbum = (RelativeLayout) findViewById(R.id.no_album);
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(Define.ACTIONBAR_COLOR);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(linearLayoutManager);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean isStoragePermissionGiven = MSupport.checkPermissionWithRationale(VideoAlbumActivity.this,
                    null, MSupportConstants.WRITE_EXTERNAL_STORAGE, MSupportConstants.REQUEST_STORAGE_READ_WRITE);
            if (isStoragePermissionGiven)
                new GetVideoListAsync().execute();
        } else
            new GetVideoListAsync().execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ENTER_ALBUM_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                setResult(RESULT_OK, data);
                finish();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        switch (requestCode) {
            case MSupportConstants.REQUEST_STORAGE_READ_WRITE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    new GetVideoListAsync().execute();
                } else {
                    Toast.makeText(VideoAlbumActivity.this, "Storage permission not granted", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

    private void getVideoList() {

        // which image properties are we querying
        String[] PROJECTION_BUCKET = {MediaStore.Video.VideoColumns.BUCKET_ID,
                MediaStore.Video.VideoColumns.BUCKET_DISPLAY_NAME, MediaStore.Video.VideoColumns.DATE_TAKEN,
                MediaStore.Video.VideoColumns.DATA};
        // We want to order the albums by reverse chronological order. We abuse
        // the
        // "WHERE" parameter to insert a "GROUP BY" clause into the SQL
        // statement.
        // The template for "WHERE" parameter is like:
        // SELECT ... FROM ... WHERE (%s)
        // and we make it look like:
        // SELECT ... FROM ... WHERE (1) GROUP BY 1,(2)
        // The "(1)" means true. The "1,(2)" means the first two columns
        // specified
        // after SELECT. Note that because there is a ")" in the template, we
        // use
        // "(2" to match it.
        String BUCKET_GROUP_BY = "1) GROUP BY 1,(2";
        String BUCKET_ORDER_BY = "MAX(datetaken) DESC";

        // Get the base URI for the People table in the Contacts content
        // provider.
        Uri images = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        Cursor cur = getContentResolver().query(images, PROJECTION_BUCKET,
                BUCKET_GROUP_BY, null, BUCKET_ORDER_BY);

        Log.v("ListingImages", " query count=" + cur.getCount());

        GalleryPhotoAlbum album;

        if (cur.moveToFirst()) {
            String bucket;
            String date;
            String data;
            long bucketId;

            int bucketColumn = cur
                    .getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);

            int dateColumn = cur
                    .getColumnIndex(MediaStore.Video.Media.DATE_TAKEN);
            int dataColumn = cur.getColumnIndex(MediaStore.Video.Media.DATA);

            int bucketIdColumn = cur
                    .getColumnIndex(MediaStore.Video.Media.BUCKET_ID);

            do {
                // Get the field values
                bucket = cur.getString(bucketColumn);
                date = cur.getString(dateColumn);
                data = cur.getString(dataColumn);
                bucketId = cur.getInt(bucketIdColumn);

                if (bucket != null && bucket.length() > 0) {
                    album = new GalleryPhotoAlbum();
                    album.setBucketId(bucketId);
                    album.setBucketName(bucket);
                    album.setDateTaken(date);
                    album.setData(data);
                    album.setTotalCount(videoCountByAlbum(bucket));
                    arrayListAlbums.add(album);
                    // Do something with the values.
                    Log.v("ListingImages", " bucket=" + bucket
                            + "  date_taken=" + date + "  _data=" + data
                            + " bucket_id=" + bucketId);
                }

            } while (cur.moveToNext());
        }
        cur.close();
    }

    private int videoCountByAlbum(String bucketName) {

        try {
            final String orderBy = MediaStore.Video.Media.DATE_TAKEN;
            String searchParams = null;
            String bucket = bucketName;
            searchParams = "bucket_display_name = \"" + bucket + "\"";

            // final String[] columns = { MediaStore.Video.Media.DATA,
            // MediaStore.Video.Media._ID };
            Cursor mVideoCursor = getContentResolver().query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null,
                    searchParams, null, orderBy + " DESC");

            if (mVideoCursor.getCount() > 0) {

                return mVideoCursor.getCount();
            }
            mVideoCursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;

    }


    private class GetVideoListAsync extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            arrayListAlbums.clear();
            getVideoList();
            return !arrayListAlbums.isEmpty();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {
                noAlbum.setVisibility(View.GONE);
                VideoAlbumListAdapter galleryAlbumAdapter = new VideoAlbumListAdapter(
                        VideoAlbumActivity.this, arrayListAlbums) {
                    @Override
                    public void onItemClick(ViewHolder holder) {
                        Intent intent = new Intent(VideoAlbumActivity.this, VideoAlbumGalleryActivity.class);
                        intent.putExtra("bucketName", arrayListAlbums.get(holder.getAdapterPosition()).getBucketName());
                        intent.putExtra("pickCount", pickCount);
                        startActivityForResult(intent, ENTER_ALBUM_REQUEST_CODE);
                    }
                };
                recyclerView.setAdapter(galleryAlbumAdapter);
            } else {
                noAlbum.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


}
