package com.rahul.media.videomodule;

import android.content.ContentValues;
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
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.msupport.MSupport;
import com.msupport.MSupportConstants;
import com.rahul.media.R;
import com.rahul.media.videomodule.adapter.VideoGalleryAdapter;
import com.rahul.media.model.MediaObject;
import com.rahul.media.model.MediaType;
import com.rahul.media.model.Define;
import com.rahul.media.utils.ProcessGalleryFile;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class VideoAlbumGalleryActivity extends AppCompatActivity {

    private RelativeLayout noAlbum;
    private ArrayList<MediaObject> mVideoArrayList = new ArrayList<>();
    private String bucketName;
    private GridView mGridView;
    Set<ProcessGalleryFile> tasks = new HashSet<ProcessGalleryFile>();
    private VideoGalleryAdapter mAdapter;
    private int mPickCount = 1;
    private String pathDir;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_gallery);
        bucketName = getIntent().getStringExtra("bucketName");
        mPickCount = getIntent().getIntExtra("pickCount", 1);
        noAlbum = (RelativeLayout) findViewById(R.id.no_album);
        mGridView = (GridView) findViewById(R.id.gridview);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setBackgroundColor(Define.ACTIONBAR_COLOR);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean isStoragePermissionGiven = MSupport.checkPermissionWithRationale(VideoAlbumGalleryActivity.this,
                    null, MSupportConstants.WRITE_EXTERNAL_STORAGE, MSupportConstants.REQUEST_STORAGE_READ_WRITE);
            if (isStoragePermissionGiven)
                new GetVideoListAsync().execute();
        } else
            new GetVideoListAsync().execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        switch (requestCode) {
            case MSupportConstants.REQUEST_STORAGE_READ_WRITE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    new GetVideoListAsync().execute();
                } else {
                    Toast.makeText(VideoAlbumGalleryActivity.this, "Storage permission not granted", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

    private void getAlbumVideos(String bucketName) {
        try {
            final String orderBy = MediaStore.Video.Media.DATE_TAKEN;
            String searchParams = null;
            String bucket = bucketName;
            searchParams = "bucket_display_name = \"" + bucket + "\"";


            Cursor mVideoCursor = getContentResolver().query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null,
                    searchParams, null, orderBy + " DESC");

            if (mVideoCursor != null && mVideoCursor.moveToFirst()) {

                mVideoArrayList = new ArrayList<>();

                do {
                    String filePath = mVideoCursor.getString(mVideoCursor.getColumnIndex(MediaStore.Video.Media.DATA));
                    long creationDate = getCreationDate(filePath);

                    setPathDir(filePath,
                            mVideoCursor.getString(mVideoCursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)));

                    MediaObject mediaObject = new MediaObject(mVideoCursor.getInt(0),
                            filePath, MediaType.VIDEO, creationDate);
                    mVideoArrayList.add(mediaObject);
                } while (mVideoCursor.moveToNext());
                mVideoCursor.close();

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static long getCreationDate(String filePath) {
        File file = new File(filePath);
        return file.lastModified();
    }

    public class GetVideoListAsync extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            mVideoArrayList.clear();
            getAlbumVideos(bucketName);
            return !mVideoArrayList.isEmpty();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            getPathDir();
            if (result) {
                noAlbum.setVisibility(View.GONE);
                mAdapter = new VideoGalleryAdapter(VideoAlbumGalleryActivity.this, mVideoArrayList);
                mGridView.setAdapter(mAdapter);
                mGridView
                        .setOnScrollListener(new AbsListView.OnScrollListener() {

                            public void onScroll(AbsListView view,
                                                 int firstVisibleItem,
                                                 int visibleItemCount,
                                                 int totalItemCount) {
                            }

                            public void onScrollStateChanged(
                                    AbsListView view, int scrollState) {
                                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                                    mAdapter.setFirstTime(false);
                                    int count = view.getChildCount();

                                    for (int i = 0; i < count; i++) {
                                        View convertView = view
                                                .getChildAt(i);
                                        VideoGalleryAdapter.ViewHolder holder = (VideoGalleryAdapter.ViewHolder) convertView
                                                .getTag();
                                        if (holder == null)
                                            return;
                                        final ProcessGalleryFile processGalleryFile = new ProcessGalleryFile(
                                                holder.imgThumb,
                                                holder.videoDuration,
                                                holder.object.getPath(),
                                                holder.object
                                                        .getMediaType());
                                        if (tasks == null) {
                                            tasks = new HashSet<>();
                                        }
                                        if (!tasks
                                                .contains(processGalleryFile)) {
                                            try {
                                                processGalleryFile
                                                        .execute();
                                                tasks.add(processGalleryFile);
                                            } catch (Exception ignored) {
                                            }
                                        }
                                    }
                                } else {
                                    try {
                                        cancelAll();
                                    } catch (Exception ignored) {
                                    }
                                }
                            }

                            public void cancelAll() throws Exception {
                                final Iterator<ProcessGalleryFile> iterator = tasks
                                        .iterator();
                                while (iterator.hasNext()) {
                                    iterator.next().cancel(true);
                                    iterator.remove();
                                }
                            }
                        });

                mGridView.setAdapter(mAdapter);

                mGridView
                        .setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(
                                    AdapterView<?> adapterView,
                                    View view, int i, long l) {

                            }
                        });
            } else {
                noAlbum.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_video_album, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_ok) {
            ArrayList<String> allPath = mAdapter.getSelectedStringArray();
            if (!allPath.isEmpty()) {

                if (allPath.size() > mPickCount) {
                    Snackbar.make(findViewById(R.id.parent), "You can select only 1 video", Snackbar.LENGTH_SHORT).show();
                } else {
                    Intent data = new Intent().putStringArrayListExtra(Define.INTENT_PATH, allPath);
                    setResult(RESULT_OK, data);
                    finish();
                }
            } else {
                Snackbar.make(findViewById(R.id.parent), "Please select a video.", Snackbar.LENGTH_SHORT).show();
            }
            return true;
        } else if (id == android.R.id.home) {
            Intent data = new Intent();
            setResult(RESULT_CANCELED, data);
            finish();
        } else if (id == R.id.take_video) {
            openVideoFromCamera();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setPathDir(String path, String fileName) {
        pathDir = path.replace("/" + fileName, "");
    }

    private String getPathDir() {

        if (TextUtils.isEmpty(pathDir))
            pathDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES).getAbsolutePath();
        return pathDir;
    }

    public void openVideoFromCamera() {
        String filePath = pathDir + "/" + System.currentTimeMillis() + "_video.mp4";
        File file = new File(filePath);
        try {

            ContentValues values = new ContentValues();
            values.put(MediaStore.Video.Media.TITLE, file.getName());
            values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
            values.put(MediaStore.Video.Media.BUCKET_DISPLAY_NAME, bucketName);

            Uri videoUriFromCamera = getContentResolver().insert(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);

            Log.i("Inserted ", videoUriFromCamera != null ? videoUriFromCamera.getPath() : "");
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.parse("file://" + filePath));
//            startActivityForResult(intent, Define.TAKE_A_VIDEO_REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
            Snackbar.make(findViewById(R.id.parent), R.string.sd_card_not_avail, Snackbar.LENGTH_SHORT).show();
        }
    }
}
