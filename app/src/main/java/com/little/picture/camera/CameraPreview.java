package com.little.picture.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.ExifInterface;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.fos.fosmvp.common.utils.LogUtils;
import com.little.picture.PictureStartManager;
import com.little.picture.util.ImageUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private static final String TAG = "CameraDemo";
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private MediaRecorder mMediaRecorder;
    private Uri outputMediaFileUri;
    private String outputMediaFileType;
    private IOnCameraListener onCameraListener;

    private float oldDist = 1f;

    private int currentCameraType = 0;//类型：0：后置，1：前置
    private int cameraId = 0;//摄像头索引
    public int rotation = 0;//手机屏幕方向
    private int VIDEO_WIDTH,VIDEO_HEIGHT;

    public void takePicture() {
        mCamera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                if (pictureFile == null) {
                    Log.d(TAG, "Error creating media file, check storage permissions");
                    return;
                }
                try {
                    Bitmap bitmap= BitmapFactory.decodeByteArray(data,0,data.length);
                    ImageUtil.saveJPGE_After(bitmap,100,pictureFile.getAbsolutePath());
//                    FileOutputStream fos = new FileOutputStream(pictureFile);
//                    fos.write(data);
//                    fos.close();
                    LogUtils.e(TAG+ " pictureFile.getAbsolutePath(): " + pictureFile.getAbsolutePath());
                    ImageUtil.setPictureDegreeZero(currentCameraType,getDisplayOrientation(),pictureFile.getAbsolutePath());
                    if (onCameraListener!=null){
                        onCameraListener.onPictureResult(pictureFile.getAbsolutePath());
                    }
                    camera.startPreview();
                } catch (Exception e) {
                    Log.d(TAG, "File not found: " + e.getMessage());
                }
            }
        });
    }

    public boolean startRecording() {
        if (prepareVideoRecorder()) {
            mMediaRecorder.start();
            return true;
        } else {
            releaseMediaRecorder();
        }
        return false;
    }

    public void stopRecording() {
        try {
            if (mMediaRecorder != null) {
                mMediaRecorder.setOnErrorListener(null);
                mMediaRecorder.setPreviewDisplay(null);

                try {
                    mMediaRecorder.stop();
                } catch (RuntimeException e) {
                    Log.e(TAG, "stopRecord", e);
                }
            }
            
        }catch (Exception e){
            e.printStackTrace();
        }
        releaseMediaRecorder();
    }

    public boolean isRecording() {
        return mMediaRecorder != null;
    }

    public CameraPreview(Context context) {
        super(context);
        mHolder = getHolder();
//        mHolder.addCallback(this);
    }

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHolder = getHolder();
//        mHolder.addCallback(this);
    }

    public Camera getCameraInstance() {
        if (mCamera == null) {
            try {
                mCamera = Camera.open();
            } catch (Exception e) {
                LogUtils.e(TAG+ "mCamera is not available");
            }
        }
        return mCamera;
    }

    public Camera getmCamera() {
        return mCamera;
    }

    public void setmCamera(Camera mCamera) {
        this.mCamera = mCamera;
    }

    public void surfaceCreated(SurfaceHolder holder) {
        getCameraInstance();
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting mCamera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        mHolder.removeCallback(this);
        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        int rotation = getDisplayOrientation();
        LogUtils.e("-------------rotation= "+rotation);
//        mCamera.setDisplayOrientation(rotation);
        setCameraDisplayOrientation(mCamera,rotation);
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setRotation(rotation);
        mCamera.setParameters(parameters);
        adjustDisplayRatio(rotation);

    }

    /**
     * 2.1之前的版本设置摄像头旋转90度
     * @param orientation
     */
    @SuppressLint("NewApi")
    public void setCameraDisplayOrientation(Camera camera, int orientation) {
        if(android.os.Build.VERSION.SDK_INT>=8){

            setDisplayOrientation(camera,orientation);
//            setDisplayOrientation(camera,90);
        }

        else {
            if(null == camera)
                return;
            Method method;
            try {
                //通过反射机制获取相应的方法
                method = camera.getClass().getMethod("setDisplayOrientation", new Class[]{int.class});
                if(null != method){
                    method.invoke(camera, new Object[]{orientation});
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    /**
     *
     * @param camera
     * @param angle
     */
    protected void setDisplayOrientation(Camera camera, int angle){
        Method downPolymorphic;
        try
        {
            downPolymorphic = camera.getClass().getMethod("setDisplayOrientation", new Class[] { int.class });
            if (downPolymorphic != null)
                downPolymorphic.invoke(camera, new Object[] { angle });
        }
        catch (Exception e1)
        {
        }
    }


    private void adjustDisplayRatio(int rotation) {
        ViewGroup parent = ((ViewGroup) getParent());
        Rect rect = new Rect();
        parent.getLocalVisibleRect(rect);
        int width = rect.width();
        int height = rect.height();
        Camera.Size previewSize = mCamera.getParameters().getPreviewSize();
        int previewWidth;
        int previewHeight;
        if (rotation == 90 || rotation == 270) {
            previewWidth = previewSize.height;
            previewHeight = previewSize.width;
        } else {
            previewWidth = previewSize.width;
            previewHeight = previewSize.height;
        }

        if (width * previewHeight > height * previewWidth) {
            final int scaledChildWidth = previewWidth * height / previewHeight;

            layout((width - scaledChildWidth) / 2, 0,
                    (width + scaledChildWidth) / 2, height);
        } else {
            final int scaledChildHeight = previewHeight * width / previewWidth;
            layout(0, (height - scaledChildHeight) / 2,
                    width, (height + scaledChildHeight) / 2);
        }
    }

    public int getDisplayOrientation() {

        Camera.CameraInfo camInfo =
                new Camera.CameraInfo();
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, camInfo);

        int result = ImageUtil.getCameraOri(rotation, cameraId);
//        camera.setDisplayOrientation(angel);
        LogUtils.e(TAG+" rotation="+rotation+" result="+result);

        return result;
    }

    private boolean prepareVideoRecorder() {

        mCamera = getCameraInstance();
        mMediaRecorder = new MediaRecorder();

        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        setCamcorderProfile(mMediaRecorder);

        File videoFile = getOutputMediaFile(MEDIA_TYPE_VIDEO);
        mMediaRecorder.setOutputFile(videoFile.getAbsolutePath());

        mMediaRecorder.setPreviewDisplay(mHolder.getSurface());

        int rotation = getDisplayOrientation();
        rotation = getRecorderOrientation(rotation);

        mMediaRecorder.setOrientationHint(rotation);
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    private int getRecorderOrientation(int rotation){
        int rotate = 0;
        switch (rotation) {
            case 0:
                if (currentCameraType==0){
                    rotate = 180;
                }else {
                    rotate = 180;
                }
                break;
            case 90://竖屏
                if (currentCameraType==0){
                    rotate = 90;
                }else {
                    rotate = 270;
                }
                break;
            case 180://左横屏
                if (currentCameraType==0){
                    rotate = 0;
                }else {
                    rotate = 0;
                }
                break;
            case 270:
                if (currentCameraType==0){
                    rotate = 90;
                }else {
                    rotate = 270;
                }
                break;
        }
        return rotate;
    }

    private CamcorderProfile getProfile(int cameraId, int quality) {
        if (CamcorderProfile.hasProfile(cameraId, quality)) {
            return CamcorderProfile.get(cameraId, quality);
        }
        return null;
    }


    @SuppressLint("NewApi")
    private void setCamcorderProfile(MediaRecorder mediaRecorder) {
        CamcorderProfile profile = getProfile(cameraId, CamcorderProfile.QUALITY_HIGH);
        if (profile == null) {
            profile = getProfile(cameraId, CamcorderProfile.QUALITY_LOW);
        }

        if (profile != null) {
            profile.fileFormat = MediaRecorder.OutputFormat.MPEG_4;

            if (Build.MODEL.equalsIgnoreCase("MB525") || Build.MODEL.equalsIgnoreCase("C8812") || Build.MODEL
                    .equalsIgnoreCase("C8650")) {
                profile.videoCodec = MediaRecorder.VideoEncoder.H263;
            } else {
                profile.videoCodec = MediaRecorder.VideoEncoder.H264;
            }

            if (android.os.Build.VERSION.SDK_INT >= 14) {
                profile.audioCodec = MediaRecorder.AudioEncoder.AAC;
            } else {
                if (android.os.Build.DISPLAY != null && android.os.Build.DISPLAY.indexOf("MIUI") >= 0) {
                    profile.audioCodec = MediaRecorder.AudioEncoder.AAC;
                } else {
                    profile.audioCodec = MediaRecorder.AudioEncoder.AMR_NB;
                }
            }

            mediaRecorder.setProfile(profile);
        } else {
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
//            mediaRecorder.setVideoSize(VIDEO_WIDTH, VIDEO_HEIGHT);
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        }
    }


    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
            mCamera.lock();
        }
    }

    public File getOutputMediaFile(int type) {
        File mediaStorageDir = new File(PictureStartManager.IMAGE_FOLDER);
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "failed to create directory");
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath()  +  "/IMG_" + timeStamp + ".jpg");
            outputMediaFileType = "image/*";
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + "/VID_" + timeStamp + ".mp4");
            outputMediaFileType = "video/*";
        } else {
            return null;
        }
        outputMediaFileUri = Uri.fromFile(mediaFile);
        return mediaFile;
    }

    public Uri getOutputMediaFileUri() {
        return outputMediaFileUri;
    }

    public String getOutputMediaFileType() {
        return outputMediaFileType;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getPointerCount() == 1) {
            handleFocusMetering(event, mCamera);
        } else {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_POINTER_DOWN:
                    oldDist = getFingerSpacing(event);
                    break;
                case MotionEvent.ACTION_MOVE:
                    float newDist = getFingerSpacing(event);
                    if (newDist > oldDist) {
                        handleZoom(true, mCamera);
                    } else if (newDist < oldDist) {
                        handleZoom(false, mCamera);
                    }
                    oldDist = newDist;
                    break;
            }
        }
        return true;
    }

    private void handleZoom(boolean isZoomIn, Camera camera) {
        Camera.Parameters params = camera.getParameters();
        if (params.isZoomSupported()) {
            int maxZoom = params.getMaxZoom();
            int zoom = params.getZoom();
            if (isZoomIn && zoom < maxZoom) {
//                zoom++;
                zoom = zoom + 4;

            } else if (zoom > 0) {
//                zoom--;
                zoom = zoom - 4;
            }
            //TODO 解决崩溃
            if (zoom>maxZoom){
                zoom = maxZoom;
            }
            if (zoom<=0){
                zoom = 1;
            }
            params.setZoom(zoom);
            camera.setParameters(params);
        } else {
            Log.i(TAG, "zoom not supported");
        }
    }

    public static void resetZoom(Camera camera){
        Camera.Parameters params = camera.getParameters();
        int zoom = 1;
        params.setZoom(zoom);
        camera.setParameters(params);
    }

    private void handleFocusMetering(MotionEvent event, Camera camera) {
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        Rect focusRect = calculateTapArea(event.getX(), event.getY(), 1f, viewWidth, viewHeight);
        Rect meteringRect = calculateTapArea(event.getX(), event.getY(), 1.5f, viewWidth, viewHeight);

        camera.cancelAutoFocus();
        Camera.Parameters params = camera.getParameters();
        if (params.getMaxNumFocusAreas() > 0) {
            List<Camera.Area> focusAreas = new ArrayList<>();
            focusAreas.add(new Camera.Area(focusRect, 800));
            params.setFocusAreas(focusAreas);
        } else {
            Log.i(TAG, "focus areas not supported");
        }
        if (params.getMaxNumMeteringAreas() > 0) {
            List<Camera.Area> meteringAreas = new ArrayList<>();
            meteringAreas.add(new Camera.Area(meteringRect, 800));
            params.setMeteringAreas(meteringAreas);
        } else {
            Log.i(TAG, "metering areas not supported");
        }
        final String currentFocusMode = params.getFocusMode();
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
        try {
            camera.setParameters(params);
        }catch (Exception e){
            e.printStackTrace();
        }

        camera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                Camera.Parameters params = camera.getParameters();
                params.setFocusMode(currentFocusMode);
                camera.setParameters(params);
            }
        });
    }

    private static float getFingerSpacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private static Rect calculateTapArea(float x, float y, float coefficient, int width, int height) {
        float focusAreaSize = 300;
        int areaSize = Float.valueOf(focusAreaSize * coefficient).intValue();
        int centerX = (int) (x / width * 2000 - 1000);
        int centerY = (int) (y / height * 2000 - 1000);

        int halfAreaSize = areaSize / 2;
        RectF rectF = new RectF(clamp(centerX - halfAreaSize, -1000, 1000)
                , clamp(centerY - halfAreaSize, -1000, 1000)
                , clamp(centerX + halfAreaSize, -1000, 1000)
                , clamp(centerY + halfAreaSize, -1000, 1000));
        return new Rect(Math.round(rectF.left), Math.round(rectF.top), Math.round(rectF.right), Math.round(rectF.bottom));
    }

    private static int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

    public void setFlash(boolean on){
        Camera.Parameters parameters = mCamera.getParameters();
        CameraConfigurationUtils.setTorch(parameters,on);
        mCamera.setParameters(parameters);
    }

    public int getCurrentCameraType() {
        return currentCameraType;
    }

    public void setCurrentCameraType(int currentCameraType) {
        this.currentCameraType = currentCameraType;
    }

    public int getCameraId() {
        return cameraId;
    }

    public void setCameraId(int cameraId) {
        this.cameraId = cameraId;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
    }

    public void setVideoSize(int VIDEO_WIDTH, int VIDEO_HEIGHT) {
        this.VIDEO_WIDTH = VIDEO_WIDTH;
        this.VIDEO_HEIGHT = VIDEO_HEIGHT;
    }



    public IOnCameraListener getOnCameraListener() {
        return onCameraListener;
    }

    public void setOnCameraListener(IOnCameraListener onCameraListener) {
        this.onCameraListener = onCameraListener;
    }
}
