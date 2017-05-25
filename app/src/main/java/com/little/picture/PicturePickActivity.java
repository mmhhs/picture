package com.little.picture;


import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.little.picture.adapter.PictureGridAdapter;
import com.little.picture.listener.IOnCheckListener;
import com.little.picture.listener.IOnItemClickListener;
import com.little.picture.model.ImageEntity;
import com.little.picture.util.ImageChooseUtil;
import com.little.picture.util.ImagePreviewUtil;
import com.little.picture.util.ImageUtil;
import com.little.picture.util.ToastUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class PicturePickActivity extends Activity {
    /**
     * 传值key
     * 可选取最大数量、返回选中的图片集合
     */
    public static final String PICTURE_PICK_IMAGE = "PICTURE_PICK_IMAGE";//选取数量，返回值传值
    public static final String PICTURE_PICK_TYPE = "PICTURE_PICK_TYPE";//功能类型
    public static final int PICK_AVATAR = 0;//头像选取
    public static final int PICK_IMAGE = 1;//多照片选取

    private int funcType = PICK_IMAGE;//功能类型 默认为多照片选取

    private HashMap<String, List<String>> mGroupMap = new HashMap<String, List<String>>();//本地图片分组集合
    private List<String> allImageList = new ArrayList<String>();//所有图片路径集合
    private ArrayList<String> chooseImageList = new ArrayList<String>();//选中图片路径集合
    private List<ImageEntity> folderImageEntityList = new ArrayList<ImageEntity>();//图片文件夹集合

    public GridView gridView;
    public TextView doneText;
    public TextView folderText;
    public TextView previewText;
    public LinearLayout footerLayout;
    public LinearLayout backLayout;

    private int screenWidth = 0;//屏幕宽度
    private int statusBarHeight = 0;//状态栏高度
    private PictureGridAdapter pictureGridAdapter;
    private int maxSize = 9;//最多能选择的图片数
    private int folderShowIndex = 0;//当前文件夹索引
    private ImageChooseUtil imageChooseUtil;//选取图片工具
    private Handler handler;
    private ImagePreviewUtil imagePreviewUtil;//图片预览弹窗

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.picture_ui_home);
        init();
    }

    public void init() {
//        FrescoUtils.init(this);
        gridView = (GridView) findViewById(R.id.picture_ui_home_gridview);
        doneText = (TextView) findViewById(R.id.picture_ui_title_done);
        backLayout = (LinearLayout) findViewById(R.id.picture_ui_title_back_layout);
        folderText = (TextView) findViewById(R.id.picture_ui_footer_folder);
        previewText = (TextView) findViewById(R.id.picture_ui_footer_preview);
        footerLayout = (LinearLayout) findViewById(R.id.picture_ui_footer_layout);
        doneText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDone();
            }
        });
        previewText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPreview();
            }
        });
        folderText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagePreviewUtil.showFolderWindow(folderImageEntityList);
                imagePreviewUtil.setOnItemClickListener(new IOnItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        setFolderShow(position);
                    }
                });
            }
        });
        backLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        doneText.setEnabled(false);

        screenWidth = ImageUtil.getScreenWidth(this);
        statusBarHeight = ImageUtil.getStatusBarHeight(this);
        try {
            funcType = getIntent().getExtras().getInt(PICTURE_PICK_TYPE, PICK_IMAGE);
            maxSize = getIntent().getExtras().getInt(PICTURE_PICK_IMAGE, 9);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (funcType == PICK_IMAGE) {
            previewText.setVisibility(View.VISIBLE);
        } else if (funcType == PICK_AVATAR) {
            previewText.setVisibility(View.GONE);
        }

        imageChooseUtil = new ImageChooseUtil(this);

        imagePreviewUtil = new ImagePreviewUtil(this, gridView);
        imagePreviewUtil.setActivity(this);
        imagePreviewUtil.setMaxSize(maxSize);
        imagePreviewUtil.setStatusBarHeight(statusBarHeight);

        handler = new Handler() {
            @Override
            //当有消息发送出来的时候就执行Handler的这个方法
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                boolean res = msg.getData().getBoolean("result");
                if (res) {
                    folderImageEntityList = subGroupOfImage(mGroupMap);
                    setFolderShow(0);
                } else {
                    ToastUtil.addToast(PicturePickActivity.this, getString(R.string.picture_fail));
                }
            }
        };

        queryData();
    }


    private void queryData() {
        new Thread() {
            @Override
            public void run() {
                //在新线程里执行长耗时方法
                boolean res = queryLocalImages();
                //执行完毕后给handler发送一个空消息
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putBoolean("result", res);
                message.setData(bundle);
                handler.sendMessage(message);
            }
        }.start();
    }


    /**
     * 组装分组界面GridView的数据源，因为我们扫描手机的时候将图片信息放在HashMap中
     * 所以需要遍历HashMap将数据组装成List
     *
     * @param mGroupMap
     * @return
     */
    private List<ImageEntity> subGroupOfImage(HashMap<String, List<String>> mGroupMap) {
        if (mGroupMap.size() == 0) {
            return null;
        }
        List<ImageEntity> list = new ArrayList<ImageEntity>();
        String keyAll = getString(R.string.picture_all);
        mGroupMap.put(keyAll, allImageList);
        Iterator<Map.Entry<String, List<String>>> it = mGroupMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, List<String>> entry = it.next();
            ImageEntity mImageEntity = new ImageEntity();
            String key = entry.getKey();
            List<String> value = entry.getValue();
            mImageEntity.setSelected(false);
            mImageEntity.setFolderName(key);
            mImageEntity.setImageCounts(value.size());
            if (value.size() > 0)
                mImageEntity.setTopImagePath(value.get(0));//获取该组的第一张图片
            mImageEntity.setImagePathList(value);
            if (!key.equals(keyAll)) {
                list.add(mImageEntity);
            } else {
                mImageEntity.setSelected(true);
                if (value.size() > 1)
                    mImageEntity.setTopImagePath(value.get(1));
                list.add(0, mImageEntity);
            }
        }
        return list;
    }

    /**
     * 查询SD卡中的图片
     *
     * @return
     */
    private Boolean queryLocalImages() {
        if (!ImageUtil.sdCardExist()) {
            ToastUtil.addToast(this, getString(R.string.picture_sd));
            return false;
        }
        Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        ContentResolver mContentResolver = PicturePickActivity.this.getContentResolver();
        //只查询jpeg的图片
        Cursor mCursor = mContentResolver.query(mImageUri, null,
                MediaStore.Images.Media.MIME_TYPE + "=? or "
                        + MediaStore.Images.Media.MIME_TYPE + "=?",
                new String[]{"image/jpeg"}, MediaStore.Images.Media.DATE_MODIFIED + " desc");
//                new String[] { "image/jpeg", "image/png" }, MediaStore.Images.Media.DATE_MODIFIED+ " desc");不支持PNG
        allImageList.add("takePhoto");//为拍摄照片按钮预留位置
        while (mCursor.moveToNext()) {
            //获取图片的路径
            String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA));
            allImageList.add(path);
            //获取该图片的父路径名
            String parentName = new File(path).getParentFile().getName();
            //根据父路径名将图片放入到mGroupMap中
            if (!mGroupMap.containsKey(parentName)) {
                List<String> childList = new ArrayList<String>();
                childList.add(path);
                mGroupMap.put(parentName, childList);
            } else {
                mGroupMap.get(parentName).add(path);
            }
        }
        mCursor.close();
        return true;
    }

    public void onDone() {
        if (chooseImageList.size() > 0) {
            if (!imagePreviewUtil.isOriginal()) {
                ArrayList<String> imageList = new ArrayList<String>();
                for (String path : chooseImageList) {
                    String imagePath = ImageUtil.saveScaleImage(path, ImageChooseUtil.getImagePathFolder(), ImageChooseUtil.SCALE_WIDTH, ImageChooseUtil.SCALE_HEIGHT, 100);
                    imageList.add(imagePath);
                }
                imagePreviewUtil.sendPicturePickBroadcast(imageList);
            } else {
                imagePreviewUtil.sendPicturePickBroadcast(chooseImageList);
            }
            finish();
        }
    }

    public void onPreview() {
        if (chooseImageList.size() > 0) {
            List<String> preList = new ArrayList<String>();
            for (String pre : chooseImageList) {
                preList.add(pre);
            }
            imagePreviewUtil.setChooseImageList(chooseImageList);
            imagePreviewUtil.showPicturePreview(ImagePreviewUtil.PREVIEW_CHOOSE, preList, 0);
            imagePreviewUtil.setOnItemClickListener(onItemClickListener);
            imagePreviewUtil.setOnCheckListener(onCheckListener);
            imagePreviewUtil.setPictureGridAdapter(pictureGridAdapter);
        }
    }

    /**
     * 切换显示文件夹图片
     *
     * @param position
     */
    private void setFolderShow(int position) {
        folderShowIndex = position;
        for (ImageEntity imageEntity : folderImageEntityList) {
            imageEntity.setSelected(false);
        }
        folderImageEntityList.get(position).setSelected(true);
        folderText.setText(folderImageEntityList.get(position).getFolderName());
        pictureGridAdapter = new PictureGridAdapter(PicturePickActivity.this, folderImageEntityList.get(position).getImagePathList(), chooseImageList, screenWidth, maxSize, folderShowIndex, funcType);
        pictureGridAdapter.setOnCheckListener(onCheckListener);
        pictureGridAdapter.setOnItemClickListener(onItemClickListener);
        gridView.setAdapter(pictureGridAdapter);
        pictureGridAdapter.notifyDataSetChanged();
    }


    private IOnCheckListener onCheckListener = new IOnCheckListener() {
        @Override
        public void onCheck(List<String> chooseList) {
            if (chooseList != null && chooseList.size() > 0) {
                doneText.setText("" + getString(R.string.picture_done) + "(" + chooseList.size() + "/" + maxSize + ")");
                previewText.setText("" + getString(R.string.picture_preview) + "(" + chooseList.size() + ")");
                doneText.setEnabled(true);
            } else {
                doneText.setText("" + getString(R.string.picture_done));
                previewText.setText("" + getString(R.string.picture_preview));
                doneText.setEnabled(false);
            }
        }
    };

    private IOnItemClickListener onItemClickListener = new IOnItemClickListener() {
        @Override
        public void onItemClick(int position) {
            try {
                if (position == 0 && folderShowIndex == 0) {
                    imageChooseUtil.doTakePhoto();
                } else {
                    if (funcType == PICK_IMAGE) {
                        if (folderImageEntityList.get(folderShowIndex).getImagePathList().size() > 0) {
                            imagePreviewUtil.setChooseImageList(chooseImageList);
                            imagePreviewUtil.setFolderShowIndex(folderShowIndex);
                            imagePreviewUtil.setOnItemClickListener(onItemClickListener);
                            imagePreviewUtil.setOnCheckListener(onCheckListener);
                            imagePreviewUtil.setPictureGridAdapter(pictureGridAdapter);
                            if (folderShowIndex == 0) {
                                imagePreviewUtil.showPicturePreview(ImagePreviewUtil.PREVIEW_FOLDER, folderImageEntityList.get(folderShowIndex).getImagePathList(), position - 1);

                            } else {
                                imagePreviewUtil.showPicturePreview(ImagePreviewUtil.PREVIEW_FOLDER, folderImageEntityList.get(folderShowIndex).getImagePathList(), position);
                            }
                        }
                    } else if (funcType == PICK_AVATAR) {
                        imagePreviewUtil.setChooseImageList(chooseImageList);
                        imagePreviewUtil.setOnItemClickListener(onItemClickListener);
                        imagePreviewUtil.setOnCheckListener(onCheckListener);
                        imagePreviewUtil.setPictureGridAdapter(pictureGridAdapter);
                        imagePreviewUtil.showPicturePreview(ImagePreviewUtil.PREVIEW_EDIT, folderImageEntityList.get(folderShowIndex).getImagePathList(), position);
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ImageChooseUtil.PHOTO_WITH_CAMERA:
                    String path = imageChooseUtil.getTakePhotoScaleUrl();//获取拍照压缩路径
                    List<String> pathList = new ArrayList<String>();
                    pathList.add(path);
                    if (funcType == PICK_IMAGE) {
                        imagePreviewUtil.setChooseImageList(chooseImageList);
                        imagePreviewUtil.showPicturePreview(ImagePreviewUtil.PREVIEW_TAKE, pathList, 0);
                        imagePreviewUtil.setOnItemClickListener(onItemClickListener);
                        imagePreviewUtil.setOnCheckListener(onCheckListener);
                        imagePreviewUtil.setPictureGridAdapter(pictureGridAdapter);
                    } else if (funcType == PICK_AVATAR) {
                        imagePreviewUtil.setChooseImageList(chooseImageList);
                        imagePreviewUtil.showPicturePreview(ImagePreviewUtil.PREVIEW_EDIT, pathList, 0);
                        imagePreviewUtil.setOnItemClickListener(onItemClickListener);
                        imagePreviewUtil.setOnCheckListener(onCheckListener);
                        imagePreviewUtil.setPictureGridAdapter(pictureGridAdapter);
                    }
                    break;
                case ImageChooseUtil.CHOOSE_PICTURE:
                    break;
            }

        }
    }

}