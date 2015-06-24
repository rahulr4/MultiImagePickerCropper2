package com.luminous.pick;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.os.Environment;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.luminous.pick.utils.ViewPagerSwipeLess;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import crop.Crop;

/**
 * Created by rahul on 22/5/15.
 */
public class VideoPickActivity extends Activity {
    private static final int ACTION_REQUEST_VIDEO_FROM_CAMERA = 201;
    private static final int ACTION_REQUEST_VIDEO_FROM_GALLERY = 202;
    private static AlertDialog alertDialog;
    String action = Action.ACTION_PICK;
    private ImageLoader imageLoader;
    private ViewPagerSwipeLess mPager;
    private HashMap<String, CustomGallery> dataT;
    private CustomPagerAdapter adapter;
    private ImageListRecycleAdapter mImageListAdapter;

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
        initImageLoader();
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

        findViewById(R.id.navigate_crop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adapter != null && adapter.getCount() > 0) {
                    String imagePath = mImageListAdapter.mItems.get(mPager.getCurrentItem()).sdcardPath;
                    Crop.of((Uri.parse("file://" + imagePath)), (Uri.parse("file://" + imagePath))).
                            asSquare().start(VideoPickActivity.this);
                }
            }
        });

        findViewById(R.id.add_image_navigate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openVideoFromGallery();
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
        ((TextView) findViewById(R.id.btn_done_text)).setText("Send");
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
        findViewById(R.id.navigate_crop).setVisibility(View.GONE);

        if (getIntent().getAction() != null)
            action = getIntent().getAction();

        if (getIntent().getExtras().getBoolean("from"))
            openVideoFromGallery();
        else
            openVideoFromCamera();
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
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
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

    Bitmap getBitmapFromPath(final CustomGallery item) {

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

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            Log.w("imagePath", item.sdcardPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return thumbnail;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {

            if (requestCode == ACTION_REQUEST_VIDEO_FROM_CAMERA) {

                if (action.equals(Action.ACTION_PICK)) {
                    dataT.clear();
                }
                String[] projection = {MediaStore.Video.Media.DATA};
                @SuppressWarnings("deprecation")
                Cursor cursor = managedQuery(data.getData(), projection, null, null, null);
                int column_index_data = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                cursor.moveToFirst();
                String videoUriFromCamera = cursor.getString(column_index_data);

                String[] allPath = {videoUriFromCamera};

                Intent data2 = new Intent().putExtra("all_path", allPath);
                setResult(RESULT_OK, data2);
                finish();
                
                /*if (videoUriFromCamera != null) {
                    CustomGallery item = new CustomGallery();
                    item.sdcardPath = videoUriFromCamera;
                    item.sdCardUri = Uri.parse(videoUriFromCamera);
                    
//                    getBitmapFromPath(item);

                }*/

            } else if (requestCode == ACTION_REQUEST_VIDEO_FROM_GALLERY) {
                Cursor cursor = getContentResolver().query(
                        data.getData(),
                        new String[]{MediaStore.Video.Media.DATA, MediaStore.Video.Media.DURATION,
                                MediaStore.Video.Media.SIZE}, null, null, null);
                cursor.moveToFirst();
                String mVideoPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                if (mVideoPath == null)
                    mVideoPath = getPath(data.getData(), VideoPickActivity.this);

                String[] allPath = {mVideoPath};

                Intent data2 = new Intent().putExtra("all_path", allPath);
                setResult(RESULT_OK, data2);
                finish();
            }
        } else {
            if (dataT != null && dataT.size() > 0) {
            } else {
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
            path = cursor.getString(column_index).toString();
            cursor.close();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return path;
    }

    private void initImageLoader() {
        try {
            String CACHE_DIR = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + "/.temp_tmp";
            new File(CACHE_DIR).mkdirs();

            File cacheDir = StorageUtils.getOwnCacheDirectory(getBaseContext(),
                    CACHE_DIR);

            DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                    .cacheOnDisk(true).imageScaleType(ImageScaleType.EXACTLY)
                    .bitmapConfig(Bitmap.Config.RGB_565).build();
            ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(
                    getBaseContext())
                    .defaultDisplayImageOptions(defaultOptions)
                    .diskCache(new UnlimitedDiscCache(cacheDir));

            ImageLoaderConfiguration config = builder.build();
            imageLoader = ImageLoader.getInstance();
            imageLoader.init(config);

        } catch (Exception e) {
            e.printStackTrace();
        }
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
            return view == ((LinearLayout) object);
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View itemView = mLayoutInflater.inflate(R.layout.image_pager_item, container, false);

            final ImageView imageView = (ImageView) itemView.findViewById(R.id.full_screen_image);
            if (dataT.get(position).bitmap != null) {
                imageView.setImageBitmap(dataT.get(position).bitmap);
            } else {
                imageLoader.displayImage("file://" + dataT.get(position).sdcardPath,
                        imageView, new SimpleImageLoadingListener() {
                            @Override
                            public void onLoadingStarted(String imageUri, View view) {
                                imageView
                                        .setImageResource(R.drawable.placeholder_470x352);
                                super.onLoadingStarted(imageUri, view);
                            }
                        });
                container.addView(itemView);
            }
            return itemView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((LinearLayout) object);
        }
    }
}
