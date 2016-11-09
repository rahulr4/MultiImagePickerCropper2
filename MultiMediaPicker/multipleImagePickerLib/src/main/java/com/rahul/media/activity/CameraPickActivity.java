package com.rahul.media.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.msupport.MSupport;
import com.msupport.MSupportConstants;
import com.rahul.media.R;
import com.rahul.media.adapters.ImageListRecycleAdapter;
import com.rahul.media.adapters.ImagePreviewAdapter;
import com.rahul.media.model.CustomGallery;
import com.rahul.media.model.Define;
import com.rahul.media.utils.BitmapDecoder;
import com.rahul.media.utils.MediaUtility;
import com.rahul.media.utils.ViewPagerSwipeLess;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import crop.Crop;

/**
 * Class to pick image from camera
 * Created by rahul on 22/5/15.
 */
public class CameraPickActivity extends AppCompatActivity {
    private static final int ACTION_REQUEST_CAMERA = 201;
    private ViewPagerSwipeLess mPager;
    private HashMap<String, CustomGallery> dataT;
    private ImagePreviewAdapter imagePreviewAdapter;
    private ImageListRecycleAdapter mImageListAdapter;
    private Uri userPhotoUri;
    private boolean isCrop, isSquareCrop;
    private int pickCount = 1;
    private AlertDialog alertDialog;
    private int aspectX, aspectY;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_preview);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(Define.ACTIONBAR_COLOR);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mPager = (ViewPagerSwipeLess) findViewById(R.id.pager);
        dataT = new HashMap<>();
        imagePreviewAdapter = new ImagePreviewAdapter(CameraPickActivity.this, dataT);
        mPager.setAdapter(imagePreviewAdapter);
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
            aspectX = getIntent().getIntExtra("aspect_x", 1);
            aspectY = getIntent().getIntExtra("aspect_y", 1);
        } catch (Exception ignored) {

        }
        try {
            isCrop = getIntent().getExtras().getBoolean("crop");
            isSquareCrop = getIntent().getExtras().getBoolean("isSquareCrop");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (getIntent().getExtras().containsKey("pickCount")) {
            pickCount = getIntent().getIntExtra("pickCount", 1);
        }
        openCamera(false);
    }

    @Override
    public void onBackPressed() {
        Intent data2 = new Intent();
        setResult(RESULT_CANCELED, data2);
        super.onBackPressed();
    }

    private void openCamera(boolean isPermission) {
        String[] permissionSet = {MSupportConstants.WRITE_EXTERNAL_STORAGE, MSupportConstants.CAMERA};
        if (isPermission) {
            try {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    Uri imageFile = MediaUtility.createImageFile(CameraPickActivity.this);
                    userPhotoUri = FileProvider.getUriForFile(CameraPickActivity.this, Define.MEDIA_PROVIDER,
                            new File(imageFile.getPath()));

                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, userPhotoUri);
                    startActivityForResult(takePictureIntent, ACTION_REQUEST_CAMERA);
                    userPhotoUri = imageFile;
                }
            } catch (Exception e) {
                showAlertDialog(CameraPickActivity.this, "Device does not support camera.");
            }
        } else {
            boolean isCameraPermissionGranted;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                isCameraPermissionGranted = MSupport.checkMultiplePermission(CameraPickActivity.this, permissionSet, MSupportConstants.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            } else
                isCameraPermissionGranted = true;
            if (isCameraPermissionGranted) {
                try {
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        Uri imageFile = MediaUtility.createImageFile(CameraPickActivity.this);
                        userPhotoUri = FileProvider.getUriForFile(CameraPickActivity.this, Define.MEDIA_PROVIDER,
                                new File(imageFile.getPath()));

                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, userPhotoUri);
                        startActivityForResult(takePictureIntent, ACTION_REQUEST_CAMERA);
                        userPhotoUri = imageFile;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlertDialog(CameraPickActivity.this, "Device does not support camera.");
                }
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
                    openCamera(true);
                } else {
                    String message = "Requested Permission not granted";
                    if (!deniedPermissionList.isEmpty()) {
                        message = "You need to grant access to " + deniedPermissionList.get(0);
                        for (int i = 1; i < deniedPermissionList.size(); i++) {
                            message = message + ", " + deniedPermissionList.get(i);
                        }
                        message = message + " to access app features";
                    }
                    Toast.makeText(CameraPickActivity.this, message, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

    private class ProcessImageView extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            CustomGallery item = new CustomGallery();

            item.sdcardPath = userPhotoUri.getPath();
            item.sdCardUri = userPhotoUri;

            item.sdcardPath = BitmapDecoder.getBitmap(userPhotoUri.getPath(), CameraPickActivity.this);
            item.sdCardUri = (Uri.parse(item.sdcardPath));

            dataT.put(item.sdcardPath, item);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            imagePreviewAdapter.customNotify(dataT);
            mImageListAdapter.customNotify(dataT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {

            if (requestCode == ACTION_REQUEST_CAMERA) {

                if (pickCount == 1) {
                    dataT.clear();
                }
                if (userPhotoUri != null) {
                    new ProcessImageView().execute();
                }

            } else if (requestCode == Crop.REQUEST_CROP) {
                try {
                    Uri mTargetImageUri = (Uri) data.getExtras().get(MediaStore.EXTRA_OUTPUT);
                    if (mTargetImageUri != null) {

                        String imagePath = mImageListAdapter.mItems.get(mPager.getCurrentItem()).sdcardPath;

                        CustomGallery item = new CustomGallery();
                        item.sdcardPath = mTargetImageUri.getPath();
                        item.sdCardUri = mTargetImageUri;
                        dataT.remove(imagePath);
                        dataT.put(mTargetImageUri.getPath(), item);
                        imagePreviewAdapter.customNotify(dataT);
                        mImageListAdapter.customNotify(dataT);
                    }
                } catch (Exception e) {
                    String invalidImageText = (String) data.getExtras().get("invalid_image");
                    if (invalidImageText != null)
                        showAlertDialog(CameraPickActivity.this, invalidImageText);
                }
            }
        } else {
            if (dataT == null || dataT.size() == 0) {
                Intent data2 = new Intent();
                setResult(RESULT_CANCELED, data2);
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_camera, menu);
        menu.findItem(R.id.action_crop).setVisible(isCrop);
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
                showAlertDialog(CameraPickActivity.this, "Please select an image.");
            }
        } else if (id == android.R.id.home) {
            Intent data = new Intent();
            setResult(RESULT_CANCELED, data);
            finish();
        } else if (id == R.id.action_camera) {
            openCamera(false);
        } else if (id == R.id.action_crop) {
            if (imagePreviewAdapter != null && imagePreviewAdapter.getCount() > 0) {
                String imagePath = mImageListAdapter.mItems.get(mPager.getCurrentItem()).sdcardPath;

                Uri destination;
                try {
                    destination = MediaUtility.createImageFile(CameraPickActivity.this);
                    if (isSquareCrop) {
                        Crop.of((Uri.parse("file://" + imagePath)), destination).
                                asSquare().start(CameraPickActivity.this);
                    } else {
                        Crop.of((Uri.parse("file://" + imagePath)), destination).
                                withAspect(aspectX, aspectY).
                                start(CameraPickActivity.this);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return super.onOptionsItemSelected(item);
    }
}
