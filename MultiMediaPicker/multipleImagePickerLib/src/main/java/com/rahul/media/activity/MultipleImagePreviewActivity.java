package com.rahul.media.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.luminous.pick.R;
import com.rahul.media.adapters.ImageListRecycleAdapter;
import com.rahul.media.model.CustomGallery;
import com.rahul.media.utils.BitmapDecoder;
import com.rahul.media.utils.MediaUtility;
import com.rahul.media.utils.ViewPagerSwipeLess;
import com.sangcomz.fishbun.define.Define;
import com.sangcomz.fishbun.ui.album.ImageAlbumListActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import crop.Crop;

/**
 * Created by rahul on 22/5/15.
 */
public class MultipleImagePreviewActivity extends AppCompatActivity {
    private static final int PICK_IMAGE = 200;
    private AlertDialog alertDialog;
    private ViewPagerSwipeLess mPager;
    private HashMap<String, CustomGallery> dataT;
    private CustomPagerAdapter adapter;
    private ImageListRecycleAdapter mImageListAdapter;
    private boolean isCrop;
    private int pickCount;

    public void showAlertDialog(Context mContext, String text) {

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
    public void onBackPressed() {
        Intent data2 = new Intent();
        setResult(RESULT_CANCELED, data2);
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        Fresco.shutDown();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Fresco.initialize(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiimage_preview);
        mPager = (ViewPagerSwipeLess) findViewById(R.id.pager);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(Define.ACTIONBAR_COLOR);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        dataT = new HashMap<>();
        adapter = new CustomPagerAdapter(dataT);
        mPager.setAdapter(adapter);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
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
            isCrop = getIntent().getExtras().getBoolean("crop");
        } catch (Exception e) {
            e.printStackTrace();
        }


        if (getIntent().getExtras().containsKey("pickCount")) {
            pickCount = getIntent().getIntExtra("pickCount", 1);
        }
        openGallery();
    }

    private void openGallery() {
        Intent i = new Intent(this, ImageAlbumListActivity.class);
        i.putExtra("pickCount", pickCount);
        startActivityForResult(i, PICK_IMAGE);
    }

    class ProcessImageView extends AsyncTask<Uri, Void, Void> {

        private ProgressDialog mProgressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(MultipleImagePreviewActivity.this);
            mProgressDialog.setMessage("Processing image ...");
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(Uri... params) {
            CustomGallery item = new CustomGallery();

            item.sdcardPath = params[0].getPath();
            item.sdCardUri = params[0];

            item.sdcardPath = BitmapDecoder.getBitmap(params[0].getPath(), MultipleImagePreviewActivity.this);
            item.sdCardUri = (Uri.parse(item.sdcardPath));

            dataT.put(item.sdcardPath, item);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mProgressDialog.dismiss();
            adapter.customNotify(dataT);
            mImageListAdapter.customNotify(dataT);
        }
    }

    private class ProcessAllImages extends AsyncTask<Void, Void, Void> {

        private ArrayList<String> stringArrayList;

        ProcessAllImages(ArrayList<String> stringArrayList) {
            this.stringArrayList = stringArrayList;
        }

        private ProgressDialog mProgressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(MultipleImagePreviewActivity.this);
            mProgressDialog.setMessage("Processing images ...");
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            for (int i = 0; i < stringArrayList.size(); i++) {
                CustomGallery item = new CustomGallery();

                item.sdcardPath = stringArrayList.get(i);
                item.sdCardUri = Uri.parse(stringArrayList.get(i));

                item.sdcardPath = BitmapDecoder.getBitmap(stringArrayList.get(i), MultipleImagePreviewActivity.this);
                item.sdCardUri = (Uri.parse(item.sdcardPath));

                dataT.put(item.sdcardPath, item);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mProgressDialog.dismiss();
            adapter.customNotify(dataT);
            mImageListAdapter.customNotify(dataT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {

            if (requestCode == PICK_IMAGE) {
                ArrayList<String> allPath = data.getStringArrayListExtra(Define.INTENT_PATH);

                if (allPath != null && !allPath.isEmpty()) {
                    if (pickCount == 1) {
                        dataT.clear();
                        new ProcessAllImages(allPath).execute();
                    } else {
                        new ProcessAllImages(allPath).execute();
//                        for (String string : allPath) {
//                            if (string != null) {
//                                CustomGallery item = new CustomGallery();
//                                item.sdcardPath = string;
//                                item.sdCardUri = Uri.parse(string);
//                                dataT.put(string, item);
//                            }
//                        }
                    }
//                    adapter.customNotify(dataT);
//                    mImageListAdapter.customNotify(dataT);
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
                        adapter.customNotify(dataT);
                        mImageListAdapter.customNotify(dataT);
                    }
                } catch (Exception e) {
                    String invalidImageText = (String) data.getExtras().get("invalid_image");
                    if (invalidImageText != null)
                        showAlertDialog(MultipleImagePreviewActivity.this, invalidImageText);
                }
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

    private class CustomPagerAdapter extends PagerAdapter {

        LayoutInflater mLayoutInflater;
        ArrayList<CustomGallery> dataT;

        CustomPagerAdapter(HashMap<String, CustomGallery> dataT) {
            this.dataT = new ArrayList<CustomGallery>(dataT.values());
            mLayoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        void customNotify(HashMap<String, CustomGallery> dataHashmap) {
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
            return view == object;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            View itemView = mLayoutInflater.inflate(R.layout.image_pager_item, container, false);

            final SimpleDraweeView imageView = (SimpleDraweeView) itemView.findViewById(R.id.full_screen_image);
            imageView.setImageURI(Uri.parse("file://" + dataT.get(position).sdcardPath));

            container.addView(itemView);
            return itemView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((LinearLayout) object);
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
            }
        } else if (id == android.R.id.home) {
            Intent data = new Intent();
            setResult(RESULT_CANCELED, data);
            finish();
        } else if (id == R.id.action_camera) {
            openGallery();
        } else if (id == R.id.action_crop) {
            if (adapter != null && adapter.getCount() > 0) {
                String imagePath = mImageListAdapter.mItems.get(mPager.getCurrentItem()).sdcardPath;

                Uri destination = null;
                try {
                    destination = MediaUtility.createImageFile(MultipleImagePreviewActivity.this);
                    Crop.of((Uri.parse("file://" + imagePath)), destination).
                            asSquare().start(MultipleImagePreviewActivity.this);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return super.onOptionsItemSelected(item);
    }
}
