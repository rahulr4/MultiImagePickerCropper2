package com.luminous.pick;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
public class MultipleImagePreviewActivity extends Activity {
    private static final int PICK_IMAGE = 200;
    private static AlertDialog alertDialog;
    String action = Action.ACTION_PICK;
    private ImageLoader imageLoader;
    //    private HListView hListView;
    private ViewPagerSwipeLess mPager;
    private HashMap<String, CustomGallery> dataT;
    private CustomPagerAdapter adapter;
    private ImageListRecycleAdapter mImageListAdapter;
    private RecyclerView mRecycleView;

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
        setContentView(R.layout.activity_multiimage_preview);
        initImageLoader();
        mPager = (ViewPagerSwipeLess) findViewById(R.id.pager);
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

        mRecycleView = (RecyclerView) findViewById(R.id.image_hlistview);
        mRecycleView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mRecycleView.setAdapter(mImageListAdapter);
        mImageListAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mPager.setCurrentItem(position);
            }
        });
//        hListView = (HListView) findViewById(R.id.image_hlistview);
//        hListView.setSelector(R.drawable.list_selector);

//        hListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                mPager.setCurrentItem(position);
//            }
//        });
//        hListView.setAdapter(mImageListAdapter);

        findViewById(R.id.navigate_crop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adapter != null && adapter.getCount() > 0) {
                    String imagePath = mImageListAdapter.mItems.get(mPager.getCurrentItem()).sdcardPath;
                    Crop.of((Uri.parse("file://" + imagePath)), (Uri.parse("file://" + imagePath))).
                            asSquare().start(MultipleImagePreviewActivity.this);
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
                    showAlertDialog(MultipleImagePreviewActivity.this, "Please select an image.");
                }
            }
        });
        Intent i = new Intent(this,
                CustomGalleryActivity.class);

        if (getIntent().getAction() != null)
            action = getIntent().getAction();
        i.setAction(action);
        startActivityForResult(i, PICK_IMAGE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {

            if (requestCode == PICK_IMAGE && action.equals(Action.ACTION_MULTIPLE_PICK)) {
                String[] all_path = data.getStringArrayExtra("all_path");


                for (String string : all_path) {
                    if (string != null) {
                        CustomGallery item = new CustomGallery();
                        item.sdcardPath = string;
                        item.sdCardUri = Uri.parse(string);
                        dataT.put(string, item);
                    }
                }
//                if (hListView.getCheckedItemCount() == 0) {
//                    hListView.setItemChecked(0, true);
//                }

                adapter.customNotify(dataT);
                mImageListAdapter.customNotify(dataT);

            } else if (requestCode == PICK_IMAGE && action.equals(Action.ACTION_PICK)) {
                String single_path = data.getExtras().getString("single_path");
                dataT.clear();
                if (single_path != null) {
                    CustomGallery item = new CustomGallery();
                    item.sdcardPath = single_path;
                    item.sdCardUri = Uri.parse(single_path);
                    dataT.put(single_path, item);
                }

//                if (hListView.getCheckedItemCount() == 0) {
//                    hListView.setItemChecked(0, true);
//                }

                adapter.customNotify(dataT);
                mImageListAdapter.customNotify(dataT);

            } else if (requestCode == Crop.REQUEST_CROP) {
                try {
                    Uri mTargetImageUri = (Uri) data.getExtras().get(MediaStore.EXTRA_OUTPUT);

                    CustomGallery item = new CustomGallery();
                    item.sdcardPath = mTargetImageUri.getPath();
                    item.sdCardUri = mTargetImageUri;

                    imageLoader.clearDiskCache();
                    imageLoader.getDiskCache().remove("file://" + item.sdcardPath);
                    imageLoader.getMemoryCache().remove("file://" + item.sdcardPath);
                    dataT.put(mTargetImageUri.getPath(), item);
                    adapter.customNotify(dataT);
                    mImageListAdapter.customNotify(dataT);
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
            return itemView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((LinearLayout) object);
        }
    }
}
