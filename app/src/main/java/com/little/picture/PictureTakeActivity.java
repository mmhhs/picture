package com.little.picture;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
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
import com.little.picture.listener.IOnProgressListener;
import com.little.picture.model.ImageListEntity;
import com.little.picture.util.DensityUtils;
import com.little.picture.util.ImageUtil;
import com.little.picture.util.PermissionUtil;
import com.little.picture.util.VideoUtil;
import com.little.picture.view.CircularProgressView;
import com.little.picture.view.dialog.PAPopupManager;
import com.vincent.videocompressor.VideoCompress;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    private int type;//类型：0：拍摄，1：预览
    private int mode = 0;//模式：0：拍照，1：录像
    private int currentCameraType = 0;//类型：0：后置，1：前置
    private int currentCameraIndex = 0;//摄像头索引
    private String imagePath;//照片路径
    private String videoPath,outputVideoPath,videoThumbPath;//视频路径

    private String fromTag= "";//来源标志

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

    public static void startAction(Activity activity){
        Intent intent = new Intent(activity,PictureTakeActivity.class);
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
                type = 0;
                mode = 0;
                setType(type);
                setMode(mode);
                onResume();
            }
        });
        tvWc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mode==0){
                    imagePath = compressImage();
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
            popupManager.showTipDialog("","没有权限");
        }

        setType(0);
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
                MediaController mc = new MediaController(this);
                vvSp.setVisibility(View.VISIBLE);
                mc.setAnchorView(vvSp);
                mc.setMediaPlayer(vvSp);
                vvSp.setMediaController(mc);
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
                    setType(1);
                    setMode(0);
                }

                @Override
                public void onVideoResult(String path) {
                    type = 1;
                    videoPath = path;
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
        if (!hasSurface) {
            hasSurface = true;
            setCamera();
//            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

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
    }

    /**
     * 发送结果广播
     */
    public void sendTakeResult(){
        List<String> imageList = new ArrayList<>();
        ImageListEntity imageListEntity = new ImageListEntity();
        imageListEntity.setMode(mode);
        if (mode==0){
            imageList.add(imagePath);
        }else {
            imageListEntity.setVideoPath(outputVideoPath);
            imageListEntity.setVideoThumbPath(videoThumbPath);
        }
        imageListEntity.setChooseImageList(imageList);
        imageListEntity.setFromTag(fromTag);
        EventBus.getDefault().post(imageListEntity);
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
        inactivityTimer.shutdown();

    }
}
