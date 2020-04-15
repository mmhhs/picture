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
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.little.picture.adapter.PictureGridAdapter;
import com.little.picture.listener.IOnCheckListener;
import com.little.picture.listener.IOnItemClickListener;
import com.little.picture.model.ImageFolderEntity;
import com.little.picture.util.ImageChooseUtil;
import com.little.picture.util.ImagePreviewUtil;
import com.little.picture.util.ImageUtil;
import com.little.picture.util.StatusBarUtils;
import com.little.picture.util.PaToastUtils;

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
    public static final String PICTURE_PICK_TYPE = "PICTURE_PICK_TYPE";//功能类型，0：头像选取；1：多照片选取
    public static final String PICTURE_FROM_TAG = "PICTURE_FROM_TAG";//来源标志
    public static final int PICK_AVATAR = 0;//头像选取
    public static final int PICK_IMAGE = 1;//多照片选取
    public static final int PICK_CROP_IMAGE = 2;//照片选取裁剪

    private int funcType = PICK_IMAGE;//功能类型 默认为多照片选取
    private String fromTag= "";//来源标志

    private HashMap<String, List<String>> mGroupMap = new HashMap<>();//本地图片分组集合
    private List<String> allImageList = new ArrayList<String>();//所有图片路径集合
    private ArrayList<String> chooseImageList = new ArrayList<>();//选中图片路径集合
    private List<ImageFolderEntity> folderImageFolderEntityList = new ArrayList<>();//图片文件夹集合

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
    private float rate = 1;//裁剪图片宽高比

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.setContentView(R.layout.picture_ui_home);
        StatusBarUtils.setStatusBarTranslucent(this, true);
        init();


    }

    /**
     * 打开选择照片
     * @param activity
     * @param funcType 功能类型，0：头像选取；1：多照片选取
     * @param maxSize 选取数量 1-9
     * @param fromTag 来源标志 用于区分哪个界面调用
     */
    public static void startAction(Activity activity,int funcType,int maxSize,String fromTag){
        Intent intent = new Intent(activity,PicturePickActivity.class);
        intent.putExtra(PICTURE_PICK_TYPE,funcType);
        intent.putExtra(PICTURE_PICK_IMAGE,maxSize);
        intent.putExtra(PICTURE_FROM_TAG,fromTag);
        activity.startActivity(intent);
    }

    public static void startAction(Activity activity,int funcType,int maxSize,String fromTag,float rate){
        Intent intent = new Intent(activity,PicturePickActivity.class);
        intent.putExtra(PICTURE_PICK_TYPE,funcType);
        intent.putExtra(PICTURE_PICK_IMAGE,maxSize);
        intent.putExtra(PICTURE_FROM_TAG,fromTag);
        intent.putExtra("rate",rate);
        activity.startActivity(intent);
    }

    public void init() {
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
                imagePreviewUtil.showFolderWindow(folderImageFolderEntityList);
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
            fromTag = getIntent().getExtras().getString(PICTURE_FROM_TAG);
            maxSize = getIntent().getExtras().getInt(PICTURE_PICK_IMAGE, 9);
            rate = getIntent().getExtras().getFloat("rate", 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (funcType == PICK_IMAGE) {
            previewText.setVisibility(View.VISIBLE);
        } else if (funcType == PICK_AVATAR) {
            previewText.setVisibility(View.GONE);
        }else if (funcType == PICK_CROP_IMAGE){
            previewText.setVisibility(View.GONE);
        }

        imageChooseUtil = new ImageChooseUtil(this);

        imagePreviewUtil = new ImagePreviewUtil(this, gridView);
        imagePreviewUtil.setActivity(this);
        imagePreviewUtil.setMaxSize(maxSize);
        imagePreviewUtil.setStatusBarHeight(statusBarHeight);
        imagePreviewUtil.setFromTag(fromTag);

        handler = new Handler() {
            @Override
            //当有消息发送出来的时候就执行Handler的这个方法
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                boolean res = msg.getData().getBoolean("result");
                if (res) {
                    folderImageFolderEntityList = subGroupOfImage(mGroupMap);
                    setFolderShow(0);
                } else {
                    PaToastUtils.addToast(PicturePickActivity.this, getString(R.string.picture_fail));
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
    private List<ImageFolderEntity> subGroupOfImage(HashMap<String, List<String>> mGroupMap) {

        List<ImageFolderEntity> list = new ArrayList<ImageFolderEntity>();
        String keyAll = getString(R.string.picture_all);
        mGroupMap.put(keyAll, allImageList);

        if (mGroupMap.size() == 0) {
            return list;
        }
        Iterator<Map.Entry<String, List<String>>> it = mGroupMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, List<String>> entry = it.next();
            ImageFolderEntity mImageFolderEntity = new ImageFolderEntity();
            String key = entry.getKey();
            List<String> value = entry.getValue();
            mImageFolderEntity.setSelected(false);
            mImageFolderEntity.setFolderName(key);
            mImageFolderEntity.setImageCounts(value.size());
            if (value.size() > 0)
                mImageFolderEntity.setTopImagePath(value.get(0));//获取该组的第一张图片
            mImageFolderEntity.setImagePathList(value);
            if (!key.equals(keyAll)) {
                list.add(mImageFolderEntity);
            } else {
                mImageFolderEntity.setSelected(true);
                if (value.size() > 1)
                    mImageFolderEntity.setTopImagePath(value.get(1));
                list.add(0, mImageFolderEntity);
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
            PaToastUtils.addToast(this, getString(R.string.picture_sd));
            return false;
        }

        Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        ContentResolver mContentResolver = PicturePickActivity.this.getContentResolver();
        //只查询jpeg的图片
        Cursor mCursor = mContentResolver.query(mImageUri, null,
                MediaStore.Images.Media.MIME_TYPE + "=? or "
                        + MediaStore.Images.Media.MIME_TYPE + "=?",
                new String[]{"image/jpeg", "image/png"}, MediaStore.Images.Media.DATE_MODIFIED + " desc");
//                new String[] { "image/jpeg", "image/png" }, MediaStore.Images.Media.DATE_MODIFIED+ " desc");不支持PNG

        allImageList.add("takePhoto");//为拍摄照片按钮预留位置

        if (mCursor!=null){
            while (mCursor.moveToNext()) {
                //获取图片的路径
                String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA));
                allImageList.add(path);
                //获取该图片的父路径名
                String parentName = new File(path).getParentFile().getName();
                //根据父路径名将图片放入到mGroupMap中
                if (!mGroupMap.containsKey(parentName)) {
                    List<String> childList = new ArrayList();
                    childList.add(path);
                    mGroupMap.put(parentName, childList);
                } else {
                    mGroupMap.get(parentName).add(path);
                }
            }
            mCursor.close();
        }

        return true;
    }

    /**
     * 完成
     */
    public void onDone() {
        if (chooseImageList.size() > 0) {
            if (!imagePreviewUtil.isOriginal()) {
                ArrayList<String> imageList = new ArrayList<String>();
                for (String path : chooseImageList) {
                    String imagePath = ImageUtil.saveScaleImage(path, PictureStartManager.getImageFolder(), PictureStartManager.SCALE_WIDTH, PictureStartManager.SCALE_HEIGHT, PictureStartManager.QUALITY);
                    imageList.add(imagePath);
                }
                imagePreviewUtil.sendPicturePickBroadcast(imageList);
            } else {
                imagePreviewUtil.sendPicturePickBroadcast(chooseImageList);
            }
            finish();
        }
    }

    /**
     * 预览大图
     */
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
        for (ImageFolderEntity imageFolderEntity : folderImageFolderEntityList) {
            imageFolderEntity.setSelected(false);
        }
        folderImageFolderEntityList.get(position).setSelected(true);
        folderText.setText(folderImageFolderEntityList.get(position).getFolderName());
        pictureGridAdapter = new PictureGridAdapter(PicturePickActivity.this, folderImageFolderEntityList.get(position).getImagePathList(), chooseImageList, screenWidth, maxSize, folderShowIndex, funcType);
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
                        if (folderImageFolderEntityList.get(folderShowIndex).getImagePathList().size() > 0) {
                            imagePreviewUtil.setChooseImageList(chooseImageList);
                            imagePreviewUtil.setFolderShowIndex(folderShowIndex);
                            imagePreviewUtil.setOnItemClickListener(onItemClickListener);
                            imagePreviewUtil.setOnCheckListener(onCheckListener);
                            imagePreviewUtil.setPictureGridAdapter(pictureGridAdapter);
                            if (folderShowIndex == 0) {
                                imagePreviewUtil.showPicturePreview(ImagePreviewUtil.PREVIEW_FOLDER, folderImageFolderEntityList.get(folderShowIndex).getImagePathList(), position - 1);

                            } else {
                                imagePreviewUtil.showPicturePreview(ImagePreviewUtil.PREVIEW_FOLDER, folderImageFolderEntityList.get(folderShowIndex).getImagePathList(), position);
                            }
                        }
                    } else if (funcType == PICK_AVATAR) {
                        imagePreviewUtil.setChooseImageList(chooseImageList);
                        imagePreviewUtil.setOnItemClickListener(onItemClickListener);
                        imagePreviewUtil.setOnCheckListener(onCheckListener);
                        imagePreviewUtil.setPictureGridAdapter(pictureGridAdapter);
                        imagePreviewUtil.showPicturePreview(ImagePreviewUtil.PREVIEW_EDIT, folderImageFolderEntityList.get(folderShowIndex).getImagePathList(), position);
                    } else if (funcType == PICK_CROP_IMAGE) {
                        imagePreviewUtil.setRate(rate);
                        imagePreviewUtil.setChooseImageList(chooseImageList);
                        imagePreviewUtil.setOnItemClickListener(onItemClickListener);
                        imagePreviewUtil.setOnCheckListener(onCheckListener);
                        imagePreviewUtil.setPictureGridAdapter(pictureGridAdapter);
                        imagePreviewUtil.showPicturePreview(ImagePreviewUtil.PREVIEW_EDIT, folderImageFolderEntityList.get(folderShowIndex).getImagePathList(), position);
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
                    }else if (funcType == PICK_CROP_IMAGE) {
                        imagePreviewUtil.setRate(rate);
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