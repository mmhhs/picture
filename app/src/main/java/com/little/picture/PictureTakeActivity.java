package com.little.picture;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.fos.fosmvp.common.utils.LogUtils;
import com.little.picture.camera.BeepManager;
import com.little.picture.camera.CameraManager;
import com.little.picture.camera.CameraPreview;
import com.little.picture.camera.InactivityTimer;
import com.little.picture.camera.OnHandlerListener;
import com.little.picture.util.DensityUtils;
import com.little.picture.util.PermissionUtil;
import com.little.picture.view.CircularProgressView;
import com.little.picture.view.dialog.PAPopupManager;

import java.io.IOException;

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

    private int type;//类型：0：拍摄，1：预览

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    }

    private void initView(){
        popupManager = new PAPopupManager(this);

        previewWidth = DensityUtils.getWidthInPx(this);
        previewHeight = DensityUtils.getHeightInPx(this)-DensityUtils.getStatusBarHeight(this);
        inactivityTimer = new InactivityTimer(this);
        beepManager = new BeepManager(this);
        beepManager.updatePrefs();
        inactivityTimer.onResume();

        if (!PermissionUtil.hasPicturePermission(this,true)){
            popupManager.showTipDialog("","没有权限");
        }
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

    private void setCamera(){
        // CameraManager必须在这里初始化，而不是在onCreate()中。
        // 这是必须的，因为当我们第一次进入时需要显示帮助页，我们并不想打开Camera,测量屏幕大小
        // 当扫描框的尺寸不正确时会出现bug
        cameraManager = new CameraManager(this);
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
        if (cameraManager.isOpen()) {
            return;
        }
        try {
            // 打开Camera硬件设备
            cameraManager.openDriver(surfaceHolder,(int)previewWidth,(int)previewHeight);
            surfaceView.setmCamera(cameraManager.camera);
            // 创建一个handler来打开预览，并抛出一个运行时异常
            cameraManager.startPreview();
        } catch (IOException ioe) {
            LogUtils.e(ioe.getMessage());
        } catch (RuntimeException e) {
            LogUtils.e(e.getMessage());
        }
    }

    private OnHandlerListener onHandlerListener = new OnHandlerListener() {
        @Override
        public Handler getActivityHandler() {
            return null;
        }

        @Override
        public CameraManager getActivityCameraManager() {
            return cameraManager;
        }

        @Override
        public Activity getActivity() {
            return PictureTakeActivity.this;
        }
    };

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
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
//            SurfaceHolder surfaceHolder = surfaceView.getHolder();
//            surfaceHolder.removeCallback(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        inactivityTimer.shutdown();

    }
}
