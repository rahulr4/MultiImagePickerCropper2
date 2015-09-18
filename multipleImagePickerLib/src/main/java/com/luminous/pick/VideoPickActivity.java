package com.luminous.pick;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.luminous.pick.controller.MediaSingleTon;
import com.luminous.pick.utils.VideoQuality;
import com.luminous.pick.utils.ViewPagerSwipeLess;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by rahul on 22/5/15.
 */
public class VideoPickActivity extends Activity {
    private static final int ACTION_REQUEST_VIDEO_FROM_CAMERA = 201;
    private static final int ACTION_REQUEST_VIDEO_FROM_GALLERY = 202;
    private static AlertDialog alertDialog;
    String action = Action.ACTION_PICK;
    private ViewPagerSwipeLess mPager;
    private HashMap<String, CustomGallery> dataT;
    private CustomPagerAdapter adapter;
    private ImageListRecycleAdapter mImageListAdapter;
    private long videoSize;
    private int videoDuration;
    private int videoQuality = VideoQuality.HIGH_QUALITY.getQuality();


    public static void showAlertDialog(Context mContext, String text) {

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
        setContentView(R.layout.activity_video_preview);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait ...");
        progressDialog.setCancelable(false);
        mPager = (ViewPagerSwipeLess) findViewById(R.id.pager);
        dataT = new HashMap<String, CustomGallery>();
        adapter = new CustomPagerAdapter(dataT);
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

        findViewById(R.id.add_image_navigate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showVideoChooserDialog();
            }
        });

        findViewById(R.id.delete_video).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adapter != null && adapter.getCount() > 0) {
                    String imagePath = mImageListAdapter.mItems.get(mPager.getCurrentItem()).sdcardPath;
                    CustomGallery customGallery = dataT.remove(imagePath);
                    if (customGallery != null) {
                        mImageListAdapter.customNotify(dataT);
                        adapter.customNotify(dataT);
                    }
                }
            }
        });

        findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data2 = new Intent();
                setResult(RESULT_CANCELED, data2);
                finish();
            }
        });
        ((TextView) findViewById(R.id.btn_done_text)).setText(getString(R.string.crop__done));
        findViewById(R.id.btn_done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<CustomGallery> mArrayList = new ArrayList<CustomGallery>(dataT.values());
                if (mArrayList.size() > 0) {
                    String[] allPath = new String[mArrayList.size()];
                    for (int i = 0; i < allPath.length; i++) {
                        allPath[i] = mArrayList.get(i).sdcardPath;
                    }

                    Intent data = new Intent().putExtra("all_path", allPath);
                    setResult(RESULT_OK, data);
                    finish();
                } else {
                    showAlertDialog(VideoPickActivity.this, "Please select a video.");
                }
            }
        });
        if (getIntent().getAction() != null)
            action = getIntent().getAction();

        try {
            videoSize = getIntent().getExtras().getLong("videoSize");
            videoDuration = (int) getIntent().getExtras().getLong("videoDuration");
            videoQuality = getIntent().getExtras().getInt("videoQuality");

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (getIntent().getExtras().getBoolean("from"))
            openVideoFromGallery();
        else
            openVideoFromCamera();
    }

    private void showVideoChooserDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Select Video");
        alertDialog.setMessage("Select video source.");
        alertDialog.setIcon(R.drawable.ic_launcher);
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Gallery",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        openVideoFromGallery();
                    }
                });

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Camera",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        openVideoFromCamera();
                    }
                });
        alertDialog.show();
    }

    public void openVideoFromCamera() {
        ContentValues values = new ContentValues();
        String fileName = System.currentTimeMillis() + ".mp4";
        values.put(MediaStore.Video.Media.TITLE, fileName);
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        try {
            Uri videoUriFromCamera = getContentResolver().insert(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
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

    void openVideoFromGallery() {
        try {
            Intent intent = new Intent();
            intent.setType("video/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(
                    Intent.createChooser(intent, "Select Video"),
                    ACTION_REQUEST_VIDEO_FROM_GALLERY);

        } catch (Exception e) {
            showAlertDialog(VideoPickActivity.this, "Device does not support video pick.");
        }
    }

    Bitmap thumbnail = null;
    ProgressDialog progressDialog;

    void getBitmapFromPath(final CustomGallery item) {
        progressDialog.show();
        try {
            final Handler mHandler = new Handler();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        thumbnail = ThumbnailUtils.createVideoThumbnail(item.sdcardPath,
                                MediaStore.Images.Thumbnails.MINI_KIND);
                        item.bitmap = thumbnail;
                        dataT.put(item.sdcardPath, item);
                        mImageListAdapter.customNotify(dataT);
                        adapter.customNotify(dataT);
                        progressDialog.dismiss();

                    } catch (Exception e) {
                        progressDialog.dismiss();
                        e.printStackTrace();
                    }
                }
            });
            Log.w("videoPath", item.sdcardPath);
        } catch (Exception e) {
            progressDialog.dismiss();
            e.printStackTrace();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            String videoUriFromCamera = "";
            if (action.equals(Action.ACTION_PICK)) {
                dataT.clear();
            }
            if (requestCode == ACTION_REQUEST_VIDEO_FROM_CAMERA) {

                String[] projection = {MediaStore.Video.Media.DATA};
                Cursor cursor = getContentResolver().query(data.getData(), projection, null, null, null);
                int column_index_data = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                cursor.moveToFirst();
                videoUriFromCamera = cursor.getString(column_index_data);


            } else if (requestCode == ACTION_REQUEST_VIDEO_FROM_GALLERY) {
                Cursor cursor = getContentResolver().query(
                        data.getData(),
                        new String[]{MediaStore.Video.Media.DATA, MediaStore.Video.Media.DURATION,
                                MediaStore.Video.Media.SIZE}, null, null, null);
                cursor.moveToFirst();
                videoUriFromCamera = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                if (videoUriFromCamera == null)
                    videoUriFromCamera = getPath(data.getData(), VideoPickActivity.this);
            }
            if (videoUriFromCamera != null) {
                CustomGallery item = new CustomGallery();
                item.sdcardPath = videoUriFromCamera;
                item.sdCardUri = Uri.parse(videoUriFromCamera);
                getBitmapFromPath(item);
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

    class CustomPagerAdapter extends PagerAdapter {

        LayoutInflater mLayoutInflater;
        ArrayList<CustomGallery> dataT;

        public CustomPagerAdapter(HashMap<String, CustomGallery> dataT) {
            this.dataT = new ArrayList<CustomGallery>(dataT.values());
            mLayoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void customNotify(HashMap<String, CustomGallery> dataHashmap) {
            dataT.clear();
            ArrayList<CustomGallery> dataT2 = new ArrayList<CustomGallery>(dataHashmap.values());
            this.dataT.addAll(dataT2);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return dataT.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((FrameLayout) object);
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            View itemView = mLayoutInflater.inflate(R.layout.video_image_pager_item, container, false);

            final ImageView imageView = (ImageView) itemView.findViewById(R.id.full_screen_image);

            if (dataT.get(position).bitmap != null) {
                imageView.setImageBitmap(dataT.get(position).bitmap);
            } else {
                if (MediaSingleTon.getInstance().getBitmapHashMap().containsKey(dataT.get(position).sdcardPath)) {
                    imageView.setImageBitmap(MediaSingleTon.getInstance().getBitmapHashMap().get((dataT.get(position).sdcardPath)));
                } else
                    Glide.with(VideoPickActivity.this)
                            .load(Uri.parse("file://" + dataT.get(position).sdcardPath))
                            .asBitmap()
                            .into(new SimpleTarget<Bitmap>(100, 100) {
                                @Override
                                public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                                    MediaSingleTon.getInstance().getBitmapHashMap().put(dataT.get(position).sdcardPath, resource);
                                    imageView.setImageBitmap(resource); // Possibly runOnUiThread()
                                }
                            });
            }

            container.addView(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.parse(dataT.get(position).sdcardPath), "video/*");
                        startActivity(Intent.createChooser(intent, "Complete action using .."));

                    } catch (Exception e) {
                        e.printStackTrace();
                        if (e instanceof ActivityNotFoundException) {
                            showAlertDialog(VideoPickActivity.this, "Video Player not found");
                        }

                    }
                }
            });
            return itemView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((FrameLayout) object);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dataT != null && dataT.size() > 0) {
            ArrayList<CustomGallery> dataT2 = new ArrayList<CustomGallery>(dataT.values());
            for (int i = 0; i < dataT2.size(); i++) {
                if (null != dataT2.get(i).bitmap) {
                    dataT2.get(i).bitmap.recycle();
                }
            }
        }
    }
}
