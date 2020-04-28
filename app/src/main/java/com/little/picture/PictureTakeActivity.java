package com.little.picture;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.fos.fosmvp.common.utils.LogUtils;
import com.fos.fosmvp.common.view.LoadViewUtil;
import com.little.picture.camera.BeepManager;
import com.little.picture.camera.CameraManager;
import com.little.picture.camera.CameraPreview;
import com.little.picture.camera.IOnCameraListener;
import com.little.picture.camera.InactivityTimer;
import com.little.picture.glide.GlideUtil;
import com.little.picture.listener.IOnCheckListener;
import com.little.picture.listener.IOnProgressListener;
import com.little.picture.model.ImageEntity;
import com.little.picture.model.ImageListEntity;
import com.little.picture.util.DensityUtils;
import com.little.picture.util.ImagePreviewUtil;
import com.little.picture.util.ImageUtil;
import com.little.picture.util.PermissionUtil;
import com.little.picture.util.VideoUtil;
import com.little.picture.view.CircularProgressView;
import com.little.picture.view.dialog.PAPopupManager;
import com.vincent.videocompressor.VideoCompress;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.little.picture.PicturePickActivity.PICK_AVATAR;
import static com.little.picture.PicturePickActivity.PICK_CROP_IMAGE;
import static com.little.picture.PicturePickActivity.PICK_IMAGE;

public class PictureTakeActivity extends AppCompatActivity implements SurfaceHolder.Callback{
    private ImageView ivFz;
    private ImageView ivXx;
    private CircularProgressView pvPz;
    private TextView tvTip;
    private CameraPreview surfaceView;

    private VideoView vvSp;
    private ImageView ivTp;
    private LinearLayout llPsYl;
    private ImageView ivFh;
    private TextView tvWc;

    private PAPopupManager popupManager;

    private SurfaceHolder surfaceHolder;
    private CameraManager cameraManager;// 相机控制
    private InactivityTimer inactivityTimer;// 电量控制
    private BeepManager beepManager;// 声音、震动控制
    private boolean hasSurface;
    private float previewWidth,previewHeight;
    private boolean isPreviewOpen = true;//预览是否打开
    private GestureDetector gestureDetector;
    private boolean isRecording = false;

    private int type = 0;//类型：0：拍摄，1：预览
    private int mode = 0;//模式：0：拍照，1：录像
    private int currentCameraType = 0;//类型：0：后置，1：前置
    private int currentCameraIndex = 0;//摄像头索引
    private String imagePath;//照片路径
    private String videoPath,outputVideoPath,videoThumbPath;//视频路径

    private String fromTag= "";//来源标志
    private ImageEntity videoEntity;

    private LoadViewUtil loadViewUtil;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐藏标题
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏

        setContentView(R.layout.activity_picture_take);
        bindView();
        initView();
    }

    public static void startAction(Activity activity,int type,ImageEntity entity){
        Intent intent = new Intent(activity,PictureTakeActivity.class);
        intent.putExtra("type",type);
        intent.putExtra("data",entity);
        activity.startActivity(intent);
    }

    private void bindView(){
        ivFz = findViewById(R.id.iv_fz);
        ivXx = findViewById(R.id.iv_xx);
        pvPz = findViewById(R.id.pv_pz);
        tvTip = findViewById(R.id.tv_tip);
        surfaceView = findViewById(R.id.cp_view);

        vvSp = findViewById(R.id.vv_sp);
        ivTp = findViewById(R.id.iv_tp);
        llPsYl = findViewById(R.id.ll_ps_yl);
        ivFh = findViewById(R.id.iv_fh);
        tvWc = findViewById(R.id.tv_wc);

        pvPz.setLongClickable(true);
        setGestureDetector();
        pvPz.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                    case MotionEvent.ACTION_UP:
                        //抬起
                        if (isRecording){
//                            stopRecording();
                            pvPz.setRecording(false);
                        }
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        break;
                }
                return gestureDetector.onTouchEvent(event);
            }
        });
        pvPz.setOnProgressListener(new IOnProgressListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onProgress(int progress) {

            }

            @Override
            public void onFinish() {
                if (isRecording){
                    isRecording= false;
                    stopRecording();
                }
            }
        });
        ivFh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back();
            }
        });
        tvWc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mode==0){
                    sendTakeResult();
                    finish();
                }else {
                    compressVideo();
                }

            }
        });
        ivFz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCamera();
            }
        });
        ivXx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initView(){
        type = getIntent().getIntExtra("type",0);
        videoEntity = (ImageEntity) getIntent().getSerializableExtra("data");
        if (videoEntity!=null){
            videoPath = videoEntity.getImagePath();
        }

        EventBus.getDefault().register(this);
        popupManager = new PAPopupManager(this);
        loadViewUtil = new LoadViewUtil(this,ivXx,"",1);

        previewWidth = DensityUtils.getWidthInPx(this);
        previewHeight = DensityUtils.getHeightInPx(this);
        inactivityTimer = new InactivityTimer(this);
        beepManager = new BeepManager(this);
        beepManager.updatePrefs();
        inactivityTimer.onResume();

        LogUtils.e("previewWidth="+previewWidth+" previewHeight="+previewHeight);

        surfaceView.setVideoSize((int)previewWidth,(int)previewHeight);

        if (!PermissionUtil.hasPicturePermission(this,true)){
            popupManager.setOptionCount(1);
            popupManager.showTipDialog("","没有权限");
        }

        setType(type);
        if (type==1){
            setMode(1);
        }

        startWatch();
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }

    public void setType(int type) {
        this.type = type;

        ivFz.setVisibility(View.INVISIBLE);
        ivXx.setVisibility(View.INVISIBLE);
        pvPz.setVisibility(View.INVISIBLE);
        tvTip.setVisibility(View.INVISIBLE);
        surfaceView.setVisibility(View.INVISIBLE);
        vvSp.setVisibility(View.INVISIBLE);
        ivTp.setVisibility(View.INVISIBLE);
        llPsYl.setVisibility(View.INVISIBLE);

        if (type==0){
            ivFz.setVisibility(View.VISIBLE);
            ivXx.setVisibility(View.VISIBLE);
            pvPz.setVisibility(View.VISIBLE);
            tvTip.setVisibility(View.VISIBLE);
            surfaceView.setVisibility(View.VISIBLE);
        }else {
            vvSp.setVisibility(View.VISIBLE);
            ivTp.setVisibility(View.VISIBLE);
            llPsYl.setVisibility(View.VISIBLE);
        }
    }

    private void setMode(int m){
        mode = m;
        if (type==0){
            pvPz.setMode(mode);
            if (mode==0){
                isRecording = false;
                tvTip.setVisibility(View.VISIBLE);
                ivXx.setVisibility(View.VISIBLE);
            }else {
                isRecording = true;
                tvTip.setVisibility(View.INVISIBLE);
                ivXx.setVisibility(View.INVISIBLE);
            }
        }else {
            ivTp.setVisibility(View.GONE);
            vvSp.setVisibility(View.GONE);
            if (mode==0){
                ivTp.setVisibility(View.VISIBLE);
                GlideUtil.getInstance().display(this,imagePath,ivTp);
            }else {
                vvSp.setVisibility(View.VISIBLE);
                vvSp.setVideoPath(videoPath);
                vvSp.start();
                vvSp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mp.start();
                        mp.setLooping(true);
                    }
                });

            }
        }
    }




    private void setGestureDetector(){
        gestureDetector = new GestureDetector(PictureTakeActivity.this, new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                // 抬起，手指离开触摸屏时触发(长按、滚动、滑动时，不会触发这个手势)
//                setMode(0);
                if (!isRecording)
                    takePicture();
                return true;
            }


            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                // 长按，触摸屏按下后既不抬起也不移动，过一段时间后触发
                setMode(1);
                startRecording();
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return false;
            }
        });
    }



    private void setCamera(){
        // CameraManager必须在这里初始化，而不是在onCreate()中。
        // 这是必须的，因为当我们第一次进入时需要显示帮助页，我们并不想打开Camera,测量屏幕大小
        // 当扫描框的尺寸不正确时会出现bug
        cameraManager = new CameraManager(this);
        LogUtils.e("currentCameraIndex= "+currentCameraIndex+" hasSurface= "+hasSurface);
        cameraManager.setManualCameraId(currentCameraIndex);
        cameraManager.setmCamera(surfaceView.getmCamera());

        surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            // activity在paused时但不会stopped,因此surface仍旧存在；
            // surfaceCreated()不会调用，因此在这里初始化camera
            initCamera(surfaceHolder);
        } else {
            // 重置callback，等待surfaceCreated()来初始化camera
            surfaceHolder.addCallback(this);
        }

    }

    /**
     * 初始化Camera
     *
     * @param surfaceHolder
     */
    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        LogUtils.e("cameraManager.isOpen()= "+cameraManager.isOpen());
        if (cameraManager.isOpen()) {
            cameraManager.stopPreview();
            cameraManager.closeDriver();

//            return;
        }
        try {
            // 打开Camera硬件设备
            cameraManager.openDriver(surfaceHolder,(int)previewWidth,(int)previewHeight);
            surfaceView.setmCamera(cameraManager.mCamera);
            // 创建一个handler来打开预览，并抛出一个运行时异常
            cameraManager.getmCamera().setPreviewDisplay(surfaceView.getHolder());
            cameraManager.startPreview();
            surfaceView.setOnCameraListener(new IOnCameraListener() {
                @Override
                public void onPictureResult(String path) {
                    type = 1;
                    imagePath = path;
                    imagePath = compressImage();
                    setType(1);
                    setMode(0);
                }

                @Override
                public void onVideoResult(String path) {
                    type = 1;
                    videoPath = path;
//                    ImageUtil.setPictureDegreeZero(surfaceView.getDisplayOrientation(),videoPath);
                    setType(1);
                    setMode(1);
//                    compressVideo();

                }
            });
        } catch (IOException ioe) {
            LogUtils.e(""+ioe.getMessage());
        } catch (RuntimeException e) {
            LogUtils.e("RuntimeException= "+e.getMessage());
        }
    }

    public void takePicture() {
        surfaceView.takePicture();
    }

    public void startRecording() {
        surfaceView.startRecording();
    }

    public void stopRecording() {
        surfaceView.stopRecording();
        surfaceView.getOnCameraListener().onVideoResult(surfaceView.getOutputMediaFileUri().getPath());

    }

    private String compressVideo(){
        try {
            LogUtils.e("compressVideo length = "+new File(videoPath).length()/1024);
            outputVideoPath = PictureStartManager.IMAGE_FOLDER+"VID_" + System.currentTimeMillis() + ".mp4";
            VideoCompress.compressVideoLow(videoPath, outputVideoPath, new VideoCompress.CompressListener() {
                @Override
                public void onStart() {
                    LogUtils.e("---------VideoCompress onStart----------");
                    loadViewUtil.showLoadView();
                }

                @Override
                public void onSuccess() {
                    LogUtils.e("---------VideoCompress onSuccess----------");
                    LogUtils.e("compressVideo after length = "+new File(outputVideoPath).length()/1024);
                    videoThumbPath = PictureStartManager.IMAGE_FOLDER+"THUMB_" + System.currentTimeMillis() + ".jpg";
                    ImageUtil.saveJPGE_After(VideoUtil.getVideoThumb2(outputVideoPath),100,videoThumbPath);
                    LogUtils.e("compressVideo videoThumbPath length = "+new File(videoThumbPath).length()/1024);
                    loadViewUtil.hideLoadView();
                    sendTakeResult();
                    finish();
                }

                @Override
                public void onFail() {
                    LogUtils.e("---------VideoCompress onFail----------");
                    loadViewUtil.hideLoadView();
                }

                @Override
                public void onProgress(float percent) {
                    LogUtils.e("---------VideoCompress percent---------- "+percent);
                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }
        return outputVideoPath;
    }

    private String compressImage(){
        String filePath = "";
        try {
            filePath = ImageUtil.saveScaleImage(imagePath, PictureStartManager.getImageFolder(), PictureStartManager.SCALE_WIDTH, PictureStartManager.SCALE_HEIGHT, PictureStartManager.QUALITY);
        }catch (Exception e){
            e.printStackTrace();
        }
        return filePath;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
//        surfaceView.surfaceCreated(holder);
        if (!hasSurface) {
            hasSurface = true;
            setCamera();
//            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
//        surfaceView.surfaceDestroyed(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
//        surfaceView.surfaceChanged(holder,format,width,height);
    }

    public void changeCamera(){
        try {
            if(currentCameraType == 0){
                setManualCameraId(1);
            }else if(currentCameraType == 1){
                setManualCameraId(0);
            }
            setCamera();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * 设置前后摄像头
     * @param type
     */
    private void setManualCameraId(int type){
        int frontIndex =-1;//前置摄像头的ID
        int backIndex = -1;//后置摄像头的ID
        int cameraCount = Camera.getNumberOfCameras();
        Camera.CameraInfo info = new Camera.CameraInfo();
        for(int cameraIndex = 0; cameraIndex<cameraCount; cameraIndex++){
            Camera.getCameraInfo(cameraIndex, info);
            if(info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT){
                frontIndex = cameraIndex;
            }else if(info.facing == Camera.CameraInfo.CAMERA_FACING_BACK){
                backIndex = cameraIndex;
            }
        }
        LogUtils.e("frontIndex= "+frontIndex+" backIndex= "+backIndex);
        currentCameraType = type;
        if(type == 1 && frontIndex != -1){
            currentCameraIndex = frontIndex;
        }else if(type == 0 && backIndex != -1){
            currentCameraIndex = backIndex;
        }
        surfaceView.setCurrentCameraType(currentCameraType);
        surfaceView.setCameraId(currentCameraIndex);
        if (type==1){

        }
    }



    public void startWatch(){
        OrientationEventListener orientationEventListener = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int orientation) {
                int rotation = 0;
                if (orientation < 100 && orientation > 80){
                    rotation = Surface.ROTATION_90;
                }else if (orientation < 280 && orientation > 260){
                    rotation = Surface.ROTATION_270;
                }else if (orientation < 10 || orientation > 350){
                    rotation = Surface.ROTATION_0;
                }else if (orientation < 190 && orientation > 170){
                    rotation = Surface.ROTATION_180;
                }

                surfaceView.setRotation(rotation);

            }
        };
        orientationEventListener.enable();
    }



    /**
     * 发送结果广播
     */
    public void sendTakeResult(){
        List<ImageEntity> imageList = new ArrayList<>();
        ImageListEntity imageListEntity = new ImageListEntity();
        imageListEntity.setMode(mode);
        if (mode==0){
            ImageEntity ie = new ImageEntity();
            ie.setImagePath(imagePath);
            imageList.add(ie);
        }else {
            imageListEntity.setVideoPath(outputVideoPath);
            imageListEntity.setVideoThumbPath(videoThumbPath);
        }
        imageListEntity.setChooseImageList(imageList);
        imageListEntity.setFromTag(fromTag);
        EventBus.getDefault().post(imageListEntity);
    }

    private void back(){
        if (videoEntity!=null||type == 0){
            finish();
        }else {
            type = 0;
            mode = 0;
            setType(type);
            setMode(mode);
            onResume();
        }
    }

    @Override
    public void onBackPressed() {
        back();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isPreviewOpen){
            setCamera();
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        cameraManager.stopPreview();
        inactivityTimer.onPause();
        beepManager.close();
        cameraManager.closeDriver();
        if (!hasSurface) {
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            surfaceHolder.removeCallback(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        inactivityTimer.shutdown();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ImageEntity entity) {
        try {
            LogUtils.e("getWidth="+entity.getWidth()+" getHeight="+entity.getHeight());
            previewHeight = entity.getHeight()*previewWidth/entity.getWidth();
//            surfaceView.getLayoutParams().height = (int)previewHeight;
            LogUtils.e("-------previewHeight="+previewHeight+"-------previewWidth="+previewWidth);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
