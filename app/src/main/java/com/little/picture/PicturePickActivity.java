package com.little.picture;


import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
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

import com.fos.fosmvp.common.utils.StringUtils;
import com.little.picture.adapter.PictureGridAdapter;
import com.little.picture.listener.IOnCheckListener;
import com.little.picture.listener.IOnItemClickListener;
import com.little.picture.model.ImageEntity;
import com.little.picture.model.ImageFolderEntity;
import com.little.picture.model.ImageListEntity;
import com.little.picture.util.ImageChooseUtil;
import com.little.picture.util.ImagePreviewUtil;
import com.little.picture.util.ImageUtil;
import com.little.picture.util.StatusBarUtils;
import com.little.picture.util.PaToastUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
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

    private Map<String, List<ImageEntity>> mGroupMap = new HashMap<>();//本地图片分组集合
    private List<ImageEntity> allImageList = new ArrayList<>();//所有图片路径集合
    private List<ImageEntity> allVideoList = new ArrayList<>();//所有视频路径集合
    private List<ImageEntity> chooseImageList = new ArrayList<>();//选中图片路径集合
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
    private int maxDuration = 15000;//支持最大视频时长

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.setContentView(R.layout.picture_ui_home);
        StatusBarUtils.setStatusBarTranslucent(this, true);
        init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            EventBus.getDefault().unregister(this);
        }catch (Exception e){

        }
    }

    /**
     * 打开选择照片
     * @param cxt
     * @param funcType 功能类型，0：头像选取；1：多照片选取；2：照片选取裁剪
     * @param maxSize 选取数量 1-9
     * @param fromTag 来源标志 用于区分哪个界面调用
     */
    public static void startAction(Context cxt, int funcType, int maxSize, String fromTag){
        Intent intent = new Intent(cxt,PicturePickActivity.class);
        intent.putExtra(PICTURE_PICK_TYPE,funcType);
        intent.putExtra(PICTURE_PICK_IMAGE,maxSize);
        intent.putExtra(PICTURE_FROM_TAG,fromTag);
        cxt.startActivity(intent);
    }

    /**
     * 打开选择照片
     * @param ctx
     * @param funcType 功能类型，0：头像选取；1：多照片选取；2：照片选取裁剪
     * @param maxSize 选取数量 1-9
     * @param fromTag 来源标志 用于区分哪个界面调用
     * @param rate 裁剪比例
     */
    public static void startAction(Context ctx,int funcType,int maxSize,String fromTag,float rate){
        Intent intent = new Intent(ctx,PicturePickActivity.class);
        intent.putExtra(PICTURE_PICK_TYPE,funcType);
        intent.putExtra(PICTURE_PICK_IMAGE,maxSize);
        intent.putExtra(PICTURE_FROM_TAG,fromTag);
        intent.putExtra("rate",rate);
        ctx.startActivity(intent);
    }

    public void init() {
        bindView();
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

        EventBus.getDefault().register(this);

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

    private void bindView(){
        gridView = findViewById(R.id.picture_ui_home_gridview);
        doneText = findViewById(R.id.picture_ui_title_done);
        backLayout = findViewById(R.id.picture_ui_title_back_layout);
        folderText = findViewById(R.id.picture_ui_footer_folder);
        previewText = findViewById(R.id.picture_ui_footer_preview);
        footerLayout = findViewById(R.id.picture_ui_footer_layout);
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
    private List<ImageFolderEntity> subGroupOfImage(Map<String, List<ImageEntity>> mGroupMap) {
        List<ImageFolderEntity> list = new ArrayList<>();
        String keyAll = getString(R.string.picture_all);
        String keyAllVideo = getString(R.string.picture_all_video);
        if (allImageList.size()>0){
            mGroupMap.put(keyAll, allImageList);
        }
        if (allVideoList.size()>0){
            mGroupMap.put(keyAllVideo, allVideoList);
        }

        if (mGroupMap.size() == 0) {
            return list;
        }
        Iterator<Map.Entry<String, List<ImageEntity>>> it = mGroupMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, List<ImageEntity>> entry = it.next();
            ImageFolderEntity mImageFolderEntity = new ImageFolderEntity();
            String key = entry.getKey();
            List<ImageEntity> value = entry.getValue();
            mImageFolderEntity.setSelected(false);
            mImageFolderEntity.setFolderName(key);
            mImageFolderEntity.setImageCounts(value.size());
            if (value.size() > 0){
                mImageFolderEntity.setTopImagePath(value.get(0));//获取该组的第一张图片
            }
            mImageFolderEntity.setImagePathList(value);

            if (key.equals(keyAll)){
                mImageFolderEntity.setSelected(true);
                if (value.size() > 0){
                    mImageFolderEntity.setTopImagePath(value.get(0));
                }
                list.add(0, mImageFolderEntity);
            }else if (key.equals(keyAllVideo)){
                if (value.size() > 0){
                    mImageFolderEntity.setTopImagePath(value.get(0));
                }
                if (list.size()>0){
                    list.add(1, mImageFolderEntity);
                }else {
                    list.add(0, mImageFolderEntity);
                }

            }else {
                list.add(mImageFolderEntity);
            }

        }
        if (funcType==PICK_AVATAR||funcType==PICK_CROP_IMAGE){
            ImageEntity pzEntity = new ImageEntity();
            pzEntity.setAddTime("949494949494");
            allImageList.add(0,pzEntity);
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

        queryImages();
        if (funcType==PICK_IMAGE){
            queryVideo();
        }


        return true;
    }

    /**
     * 获取图像列表
     */
    private void queryImages() {
        String[] projection = { MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.MIME_TYPE,
                MediaStore.Images.Media.MINI_THUMB_MAGIC,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.DATE_ADDED
        };
        String orderBy = MediaStore.Images.Media.DATE_ADDED+ " DESC";
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        getContentProvider(0,uri,projection, orderBy);
    }

    /**
     * 获取视频列表
     */
    void queryVideo() {
        String []projection = { MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.MIME_TYPE,
                MediaStore.Video.Media.MINI_THUMB_MAGIC,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Video.Media.DURATION
        };
        String orderBy = MediaStore.Video.Media.DATE_ADDED+ " DESC";
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        getContentProvider(1,uri,projection, orderBy);
    }

    /**
     * 获取音频列表
     */
    private void queryAudio() {
        String []projection = { MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.MIME_TYPE,
                MediaStore.Audio.Media.DATE_ADDED,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Audio.Media.DURATION
        };
        String orderBy = MediaStore.Audio.Media.DATE_ADDED+ " DESC";
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        getContentProvider(2,uri,projection, orderBy);
    }

    /**
     * 获取缩略图列表
     */
    private void queryThumbnails() {

    }


    /**
     * 获取ContentProvider
     * @param projection
     * @param orderBy
     */
    public void getContentProvider(int type,Uri uri,String[] projection, String orderBy) {
        Cursor cursor = getContentResolver().query(uri, projection, null,
                null, orderBy);
        if (null == cursor) {
            return;
        }
        while (cursor.moveToNext()) {
            ImageEntity imageEntity = new ImageEntity();
            imageEntity.setDisplayName(cursor.getString(1));
            imageEntity.setImagePath(cursor.getString(2));
//            imageEntity.setWidth(cursor.getString(3));
            imageEntity.setMimeType(cursor.getString(4));
            imageEntity.setThumbPath(cursor.getString(5));
            imageEntity.setAddTime(cursor.getString(6));
            String duration = cursor.getString(7);
            imageEntity.setDuration(duration);
            imageEntity.setType(type);
            if (type==0){
                allImageList.add(imageEntity);
                //获取该文件的父路径名
                String parentName = new File(imageEntity.getImagePath()).getParentFile().getName();
                //根据父路径名将图片放入到mGroupMap中
                if (!mGroupMap.containsKey(parentName)) {
                    List<ImageEntity> childList = new ArrayList();
                    childList.add(imageEntity);
                    mGroupMap.put(parentName, childList);
                } else {
                    mGroupMap.get(parentName).add(imageEntity);
                }
            }else if (type==1){
                if (!StringUtils.isEmpty(duration)){
                    int d = Integer.parseInt(duration);
                    if (d<maxDuration){
                        allImageList.add(imageEntity);
                        allVideoList.add(imageEntity);
                    }
                }


            }

        }
        cursor.close();

    }

    /**
     * 完成
     */
    public void onDone() {
        if (chooseImageList.size() > 0) {
            if (!imagePreviewUtil.isOriginal()) {
                for (ImageEntity path : chooseImageList) {
                    String imagePath = ImageUtil.saveScaleImage(path.getImagePath(), PictureStartManager.getImageFolder(), PictureStartManager.SCALE_WIDTH, PictureStartManager.SCALE_HEIGHT, PictureStartManager.QUALITY);
                    path.setScalePath(imagePath);
                }
                imagePreviewUtil.sendPicturePickBroadcast(chooseImageList);
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
            imagePreviewUtil.setChooseImageList(chooseImageList);
            List<ImageEntity> list = new ArrayList<>();
            list.addAll(chooseImageList);
            imagePreviewUtil.showPicturePreview(ImagePreviewUtil.PREVIEW_CHOOSE, list, 0);
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
        try {
            if (position>=folderImageFolderEntityList.size()){
                return;
            }
            folderShowIndex = position;
            for (ImageFolderEntity imageFolderEntity : folderImageFolderEntityList) {
                imageFolderEntity.setSelected(false);
            }
            folderImageFolderEntityList.get(position).setSelected(true);
            folderText.setText(folderImageFolderEntityList.get(position).getFolderName());
            //排序
            Collections.sort(folderImageFolderEntityList.get(position).getImagePathList());
            pictureGridAdapter = new PictureGridAdapter(PicturePickActivity.this, folderImageFolderEntityList.get(position).getImagePathList(), chooseImageList, screenWidth, maxSize, folderShowIndex, funcType);
            pictureGridAdapter.setOnCheckListener(onCheckListener);
            pictureGridAdapter.setOnItemClickListener(onItemClickListener);
            gridView.setAdapter(pictureGridAdapter);
            pictureGridAdapter.notifyDataSetChanged();
        }catch (Exception e){

        }
    }


    private IOnCheckListener onCheckListener = new IOnCheckListener() {
        @Override
        public void onCheck(List<ImageEntity> chooseList) {
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
                ImageEntity entity = folderImageFolderEntityList.get(folderShowIndex).getImagePathList().get(position);
                if (entity.getType()==1){
                    PictureTakeActivity.startAction(PicturePickActivity.this,1,fromTag,entity);
//                    entity.setShowDelete(true);
//                    imagePreviewUtil.showVideoDialog(entity);
                    return;
                }
                if (funcType == PICK_IMAGE) {
                    if (folderImageFolderEntityList.get(folderShowIndex).getImagePathList().size() > 0) {
                        imagePreviewUtil.setChooseImageList(chooseImageList);
                        imagePreviewUtil.setFolderShowIndex(folderShowIndex);
                        imagePreviewUtil.setOnItemClickListener(onItemClickListener);
                        imagePreviewUtil.setOnCheckListener(onCheckListener);
                        imagePreviewUtil.setPictureGridAdapter(pictureGridAdapter);
                        imagePreviewUtil.showPicturePreview(ImagePreviewUtil.PREVIEW_FOLDER, folderImageFolderEntityList.get(folderShowIndex).getImagePathList(), position);
                    }
                } else if (funcType == PICK_AVATAR) {
                    if (position == 0 && folderShowIndex == 0) {
                        imageChooseUtil.doTakePhoto();
                        return;
                    }
                    imagePreviewUtil.setChooseImageList(chooseImageList);
                    imagePreviewUtil.setOnItemClickListener(onItemClickListener);
                    imagePreviewUtil.setOnCheckListener(onCheckListener);
                    imagePreviewUtil.setPictureGridAdapter(pictureGridAdapter);
                    imagePreviewUtil.showPicturePreview(ImagePreviewUtil.PREVIEW_EDIT, folderImageFolderEntityList.get(folderShowIndex).getImagePathList(), position);
                } else if (funcType == PICK_CROP_IMAGE) {
                    if (position == 0 && folderShowIndex == 0) {
                        imageChooseUtil.doTakePhoto();
                        return;
                    }
                    imagePreviewUtil.setRate(rate);
                    imagePreviewUtil.setChooseImageList(chooseImageList);
                    imagePreviewUtil.setOnItemClickListener(onItemClickListener);
                    imagePreviewUtil.setOnCheckListener(onCheckListener);
                    imagePreviewUtil.setPictureGridAdapter(pictureGridAdapter);
                    imagePreviewUtil.showPicturePreview(ImagePreviewUtil.PREVIEW_EDIT, folderImageFolderEntityList.get(folderShowIndex).getImagePathList(), position);
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
                    List<ImageEntity> pathList = new ArrayList<>();
                    ImageEntity ie = new ImageEntity();
                    ie.setImagePath(path);
                    pathList.add(ie);
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ImageListEntity entity) {
        try {
            finish();
        }catch (Exception e){

        }
    }

}