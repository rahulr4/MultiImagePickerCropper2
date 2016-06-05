package com.rahul.media.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.msupport.MSupport;
import com.msupport.MSupportConstants;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.rahul.media.R;
import com.rahul.media.adapters.ImageListRecycleAdapter;
import com.rahul.media.adapters.VideoPreviewAdapter;
import com.rahul.media.model.CustomGallery;
import com.rahul.media.model.VideoQuality;
import com.rahul.media.utils.ViewPagerSwipeLess;
import com.rahul.media.model.Define;
import com.rahul.media.utils.ProcessGalleryFile;
import com.rahul.media.videomodule.VideoAlbumActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by rahul on 22/5/15.
 */
public class VideoPickActivity extends AppCompatActivity {
    private static final int ACTION_REQUEST_VIDEO_FROM_CAMERA = 201;
    private static final int ACTION_REQUEST_VIDEO_FROM_GALLERY = 202;
    private AlertDialog alertDialog;
    private ViewPagerSwipeLess mPager;
    private HashMap<String, CustomGallery> dataT;
    private VideoPreviewAdapter adapter;
    private ImageListRecycleAdapter mImageListAdapter;
    private long videoSize;
    private int videoDuration;
    private int videoQuality = VideoQuality.HIGH_QUALITY.getQuality();
    private int pickCount;
    private boolean pickFromGallery;

    private void showAlertDialog(Context mContext, String text) {

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
    protected void onResume() {
        super.onResume();
        Fresco.initialize(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_preview);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(Define.ACTIONBAR_COLOR);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait ...");
        progressDialog.setCancelable(false);
        mPager = (ViewPagerSwipeLess) findViewById(R.id.pager);
        dataT = new HashMap<>();
        adapter = new VideoPreviewAdapter(VideoPickActivity.this, dataT);
        mPager.setAdapter(adapter);
        mImageListAdapter = new ImageListRecycleAdapter(this, dataT);
        RecyclerView mRecycleView = (RecyclerView) findViewById(R.id.image_hlistview);
        mRecycleView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mRecycleView.setAdapter(mImageListAdapter);
        mImageListAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mPager.setCurrentItem(position);
            }
        });

        try {
            videoSize = getIntent().getExtras().getLong("videoSize");
            videoDuration = (int) getIntent().getExtras().getLong("videoDuration");
            videoQuality = getIntent().getExtras().getInt("videoQuality");
            pickCount = getIntent().getIntExtra("pickCount", 1);
            pickFromGallery = getIntent().getBooleanExtra("from", false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (pickFromGallery) {
            Intent intent = new Intent(VideoPickActivity.this, VideoAlbumActivity.class);
            intent.putExtra("pickCount", pickCount);
            startActivityForResult(intent, ACTION_REQUEST_VIDEO_FROM_GALLERY);
        } else {
            openVideoFromCamera(false);
        }
    }

    private void openVideoFromCamera(boolean isPermission) {
        String[] permissionSet = {MSupportConstants.WRITE_EXTERNAL_STORAGE, MSupportConstants.CAMERA};
        if (isPermission) {
            openVideoCamera();
        } else {
            boolean isCameraPermissionGranted;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                isCameraPermissionGranted = MSupport.checkMultiplePermission(VideoPickActivity.this, permissionSet, MSupportConstants.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            } else
                isCameraPermissionGranted = true;

            if (isCameraPermissionGranted) {
                openVideoCamera();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode) {
            case MSupportConstants.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                ArrayList<String> deniedPermissionList = new ArrayList<>();
                boolean isAllPermissionGranted = true;
                for (int i = 0; i < grantResults.length; i++) {
                    int results = grantResults[i];
                    String permission = permissions[i];
                    if (results != PackageManager.PERMISSION_GRANTED) {
                        isAllPermissionGranted = false;
                        deniedPermissionList.add(MSupportConstants.getPermissionRationaleMessage(permission));
                    }
                }
                if (isAllPermissionGranted) {
                    if (!pickFromGallery) {
                        openVideoFromCamera(true);
                    }
                } else {
                    String message = "Requested Permission not granted";
                    if (!deniedPermissionList.isEmpty()) {
                        message = "You need to grant access to " + deniedPermissionList.get(0);
                        for (int i = 1; i < deniedPermissionList.size(); i++) {
                            message = message + ", " + deniedPermissionList.get(i);
                        }
                        message = message + " to access app features";
                    }
                    Toast.makeText(VideoPickActivity.this, message, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

    private void openVideoCamera() {
        ContentValues values = new ContentValues();
        String fileName = System.currentTimeMillis() + ".mp4";
        values.put(MediaStore.Video.Media.TITLE, fileName);
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        try {
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, videoQuality);
            if (videoSize != -1)
                intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, videoSize);
            if (videoDuration != -1)
                intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, videoDuration);

            startActivityForResult(intent, ACTION_REQUEST_VIDEO_FROM_CAMERA);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(VideoPickActivity.this, "SD-Card not available", Toast.LENGTH_LONG).show();
        }
    }

    private ProgressDialog progressDialog;

    private void getBitmapFromPath(final ArrayList<String> stringArrayList) {
        progressDialog.show();
        try {
            final Handler mHandler = new Handler();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < stringArrayList.size(); i++) {

                        CustomGallery item = new CustomGallery();
                        item.sdcardPath = stringArrayList.get(i);
                        item.sdCardUri = Uri.parse(stringArrayList.get(i));

                        Bitmap bmp = null;
                        try {
                            bmp = ImageLoader.getInstance().getMemoryCache().get(Uri.fromFile(new File(item.sdcardPath)).toString() + "_");
                        } catch (Exception e) {
                            Log.e(ProcessGalleryFile.class.getSimpleName(), "" + e);
                        }
                        if (bmp == null) {
                            try {
                                bmp = ThumbnailUtils.createVideoThumbnail(item.sdcardPath, MediaStore.Images.Thumbnails.MINI_KIND);
                                if (bmp != null) {
                                    ImageLoader.getInstance().getMemoryCache().put(Uri.fromFile(new File(item.sdcardPath)).toString() + "_", bmp);
                                }
                            } catch (Exception e) {
                                Log.e(getClass().getSimpleName(), "Exception when rotating thumbnail for gallery", e);
                            } catch (OutOfMemoryError e) {
                                Log.e(ProcessGalleryFile.class.getSimpleName(), "" + e);
                            }
                        }
                        dataT.put(item.sdcardPath, item);
                    }
                    try {

                        mImageListAdapter.customNotify(dataT);
                        adapter.customNotify(dataT);
                        progressDialog.dismiss();

                    } catch (Exception e) {
                        progressDialog.dismiss();
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            progressDialog.dismiss();
            e.printStackTrace();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            ArrayList<String> pickedVideoList = new ArrayList<>();
            if (pickCount == 1) {
                dataT.clear();
            }
            if (requestCode == ACTION_REQUEST_VIDEO_FROM_CAMERA) {

                String[] projection = {MediaStore.Video.Media.DATA};
                Cursor cursor = getContentResolver().query(data.getData(), projection, null, null, null);
                if (cursor != null) {
                    int column_index_data = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                    cursor.moveToFirst();
                    pickedVideoList.add(cursor.getString(column_index_data));
                    cursor.close();
                }

            } else if (requestCode == ACTION_REQUEST_VIDEO_FROM_GALLERY) {
                ArrayList<String> allPath = data.getStringArrayListExtra(Define.INTENT_PATH);
                if (allPath != null)
                    pickedVideoList.addAll(allPath);
            }
            if (!pickedVideoList.isEmpty()) {
                getBitmapFromPath(pickedVideoList);
            }
        } else {
            if (dataT == null || dataT.size() == 0) {
                Intent data2 = new Intent();
                setResult(RESULT_CANCELED, data2);
                finish();
            }
        }
    }

    @SuppressLint("NewApi")
    private String getPath(Uri uri, Context context) {
        if (uri == null) {
            return null;
        }

        String[] projection = {MediaStore.Video.Media.DATA};

        Cursor cursor;
        if (Build.VERSION.SDK_INT > 19) {
            // Will return "image:x*"
            String wholeID = DocumentsContract.getDocumentId(uri);
            // Split at colon, use second item in the array
            String id = wholeID.split(":")[1];
            // where id is equal to             
            String sel = MediaStore.Video.Media._ID + "=?";

            cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    projection, sel, new String[]{id}, null);
        } else {
            cursor = context.getContentResolver().query(uri, projection, null, null, null);
        }
        String path = null;
        try {
            int column_index = cursor
                    .getColumnIndex(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            path = cursor.getString(column_index);
        } catch (NullPointerException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return path;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        Fresco.shutDown();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_video_pick, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_ok) {
            ArrayList<CustomGallery> mArrayList = new ArrayList<>(dataT.values());
            if (mArrayList.size() > 0) {
                ArrayList<String> allPath = new ArrayList<>();
                for (int i = 0; i < mArrayList.size(); i++) {
                    allPath.add(mArrayList.get(i).sdcardPath);
                }

                Intent data = new Intent().putStringArrayListExtra(Define.INTENT_PATH, allPath);
                setResult(RESULT_OK, data);
                finish();
            } else {
                showAlertDialog(VideoPickActivity.this, "Please select a video.");
            }
        } else if (id == android.R.id.home) {
            Intent data = new Intent();
            setResult(RESULT_CANCELED, data);
            finish();
        } else if (id == R.id.action_pick) {
            if (pickFromGallery) {
                Intent intent = new Intent(VideoPickActivity.this, VideoAlbumActivity.class);
                intent.putExtra("pickCount", pickCount);
                startActivityForResult(intent, ACTION_REQUEST_VIDEO_FROM_GALLERY);
            } else {
                openVideoFromCamera(false);
            }
        } else if (id == R.id.delete) {
            if (adapter != null && adapter.getCount() > 0) {
                String imagePath = mImageListAdapter.mItems.get(mPager.getCurrentItem()).sdcardPath;
                CustomGallery customGallery = dataT.remove(imagePath);
                if (customGallery != null) {
                    mImageListAdapter.customNotify(dataT);
                    adapter.customNotify(dataT);
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
