package com.luminous.pick;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.luminous.pick.controller.MediaSingleTon;
import com.luminous.pick.utils.BitmapDecoder;
import com.luminous.pick.utils.CameraUtils;
import com.luminous.pick.utils.ViewPagerSwipeLess;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import crop.Crop;

/**
 * Created by rahul on 22/5/15.
 */
public class MultipleImagePreviewActivity extends Activity {
    private static final int PICK_IMAGE = 200;
    private static AlertDialog alertDialog;
    String action = Action.ACTION_PICK;
    private ViewPagerSwipeLess mPager;
    private HashMap<String, CustomGallery> dataT;
    private CustomPagerAdapter adapter;
    private ImageListRecycleAdapter mImageListAdapter;
    private FrameLayout mDone;
    private int imageQuality;

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
    public void onBackPressed() {
        Intent data2 = new Intent();
        setResult(RESULT_CANCELED, data2);
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiimage_preview);
        mPager = (ViewPagerSwipeLess) findViewById(R.id.pager);
        mDone = (FrameLayout) findViewById(R.id.btn_done);
        dataT = new HashMap<String, CustomGallery>();
        adapter = new CustomPagerAdapter(dataT);
        mPager.setAdapter(adapter);
        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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

        findViewById(R.id.navigate_crop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adapter != null && adapter.getCount() > 0) {
                    String imagePath = mImageListAdapter.mItems.get(mPager.getCurrentItem()).sdcardPath;

                    Uri destination = null;
                    try {
                        destination = CameraUtils.createImageFile(MultipleImagePreviewActivity.this);
                        Crop.of((Uri.parse("file://" + imagePath)), destination).
                                asSquare().start(MultipleImagePreviewActivity.this);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        findViewById(R.id.add_image_navigate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MultipleImagePreviewActivity.this,
                        CustomGalleryActivity.class);
                i.setAction(action);
                startActivityForResult(i, PICK_IMAGE);
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
                    showAlertDialog(MultipleImagePreviewActivity.this, "Please select an image.");
                }
            }
        });

        try {
            boolean isCrop = getIntent().getExtras().getBoolean("crop");
            if (isCrop)
                findViewById(R.id.navigate_crop).setVisibility(View.VISIBLE);
            else
                findViewById(R.id.navigate_crop).setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Intent i = new Intent(this,
                CustomGalleryActivity.class);

        if (getIntent().getExtras().containsKey("imageQuality")) {
            imageQuality = getIntent().getExtras().getInt("imageQuality");
        }

        if (getIntent().getAction() != null)
            action = getIntent().getAction();
        i.setAction(action);
        startActivityForResult(i, PICK_IMAGE);

    }

    class ProcessImageView extends AsyncTask<Uri, Void, Void> {

        @Override
        protected Void doInBackground(Uri... params) {
            CustomGallery item = new CustomGallery();

            item.sdcardPath = params[0].getPath();
            item.sdCardUri = params[0];

            item.sdcardPath = BitmapDecoder.getBitmap(params[0].getPath(), imageQuality, MultipleImagePreviewActivity.this);
            item.sdCardUri = (Uri.parse(item.sdcardPath));

            dataT.put(item.sdcardPath, item);
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDone.setEnabled(false);
            mDone.setClickable(false);
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            adapter.customNotify(dataT);
            mImageListAdapter.customNotify(dataT);
            mDone.setEnabled(true);
            mDone.setClickable(true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {

            if (action.equals(Action.ACTION_PICK)) {
                dataT.clear();
            }
            if (requestCode == PICK_IMAGE) {
                String[] all_path = data.getStringArrayExtra("all_path");

                if (all_path != null) {
                    if (action.equals(Action.ACTION_PICK) && all_path.length > 0) {
                        new ProcessImageView().execute(Uri.parse(all_path[0]));
                    } else {
                        for (String string : all_path) {
                            if (string != null) {
                                CustomGallery item = new CustomGallery();
                                item.sdcardPath = string;
                                item.sdCardUri = Uri.parse(string);
                                dataT.put(string, item);
                            }
                        }
                    }
                    adapter.customNotify(dataT);
                    mImageListAdapter.customNotify(dataT);
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
        public Object instantiateItem(ViewGroup container, final int position) {
            View itemView = mLayoutInflater.inflate(R.layout.image_pager_item, container, false);

            final ImageView imageView = (ImageView) itemView.findViewById(R.id.full_screen_image);

            if (MediaSingleTon.getInstance().getBitmapHashMap().containsKey(dataT.get(position).sdcardPath)) {
                imageView.setImageBitmap(MediaSingleTon.getInstance().getBitmapHashMap().get((dataT.get(position).sdcardPath)));
            } else
                Glide.with(MultipleImagePreviewActivity.this)
                        .load(Uri.parse("file://" + dataT.get(position).sdcardPath))
                        .asBitmap()
                        .into(new SimpleTarget<Bitmap>(100, 100) {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                                MediaSingleTon.getInstance().getBitmapHashMap().put(dataT.get(position).sdcardPath, resource);
                                imageView.setImageBitmap(resource); // Possibly runOnUiThread()
                            }
                        });

            container.addView(itemView);
            return itemView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((LinearLayout) object);
        }
    }
}
