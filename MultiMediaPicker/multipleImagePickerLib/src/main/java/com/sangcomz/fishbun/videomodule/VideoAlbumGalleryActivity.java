package com.sangcomz.fishbun.videomodule;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.RelativeLayout;

import com.luminous.pick.R;
import com.sangcomz.fishbun.bean.MediaObject;
import com.sangcomz.fishbun.bean.MediaType;
import com.sangcomz.fishbun.define.Define;
import com.sangcomz.fishbun.permission.PermissionCheck;
import com.sangcomz.fishbun.util.ProcessGalleryFile;
import com.sangcomz.fishbun.videomodule.adapter.VideoGalleryAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class VideoAlbumGalleryActivity extends AppCompatActivity {

    private PermissionCheck permissionCheck;
    private RelativeLayout noAlbum;
    private ArrayList<MediaObject> mVideoArrayList = new ArrayList<>();
    private String bucketName;
    private GridView mGridView;
    Set<ProcessGalleryFile> tasks = new HashSet<ProcessGalleryFile>();
    private VideoGalleryAdapter mAdapter;
    private int mPickCount = 1;

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

        permissionCheck = new PermissionCheck(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionCheck.CheckStoragePermission())
                new GetVideoListAsync().execute();
        } else
            new GetVideoListAsync().execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Define.ENTER_ALBUM_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                setResult(RESULT_OK, data);
                finish();
            } else if (resultCode == Define.ADD_PHOTO_REQUEST_CODE) {
                new GetVideoListAsync().execute();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Define.PERMISSION_STORAGE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    new GetVideoListAsync().execute();
                    // permission was granted, yay! do the
                    // calendar task you need to do.
                } else {
                    permissionCheck.showPermissionDialog(mGridView);
                    finish();
                }
                return;
            }
        }
    }

    /**
     * find video list for given bucket name
     *
     * @param bucketName
     */
    private void initVideoImages(String bucketName) {
        try {
            final String orderBy = MediaStore.Video.Media.DATE_TAKEN;
            String searchParams = null;
            String bucket = bucketName;
            searchParams = "bucket_display_name = \"" + bucket + "\"";


            Cursor mVideoCursor = getContentResolver().query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null,
                    searchParams, null, orderBy + " DESC");

            if (mVideoCursor != null && mVideoCursor.moveToFirst()) {

                mVideoArrayList = new ArrayList<MediaObject>();

                do {
                    String filePath = mVideoCursor.getString(1);
                    long creationDate = getCreationDate(filePath);

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
            initVideoImages(bucketName);
            return !mVideoArrayList.isEmpty();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
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
                                            tasks = new HashSet<ProcessGalleryFile>();
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
            String[] allPath = mAdapter.getSelectedStringArray();
            if (allPath.length > 0) {

                if (allPath.length > mPickCount) {
                    Snackbar.make(findViewById(R.id.parent), "You can select only 1 video", Snackbar.LENGTH_SHORT).show();
                } else {
                    Intent data = new Intent().putExtra("all_path", allPath);
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
        }
        return super.onOptionsItemSelected(item);
    }
}
