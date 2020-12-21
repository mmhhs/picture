package com.little.picture.util;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import permissions.dispatcher.PermissionUtils;


/**
 * 权限管理类
 */
public class PermissionUtil {
    public static final int PERMISSION_REQUEST_CODE = 0;
    //定位
    public final static int CHECK_LOCTION_PERMISSION_CODE = 1;
    public final static String CHECK_LOCTION_PERMISSION_NAME = "android.permission.ACCESS_F" +
            "INE_LOCATION";
    //拨打电话及联系人
    public final static int CHECK_PHONE_PERMISSION_CODE = 2;
    public final static String CHECK_PHONE_PERMISSION_NAME = "android.permission.READ_PHONE_STATE";
    //读写文件
    public final static int CHECK_FILE_PERMISSION_CODE = 3;
    public final static String CHECK_FILE_PERMISSION_NAME = "android.permission.WRITE_EXTERNAL_STORAGE";
    //录音
    public final static int CHECK_AUDIO_PERMISSION_CODE = 4;
    public final static String CHECK_AUDIO_PERMISSION_NAME = "android.permission.RECORD_AUDIO";
    //拍照
    public final static int CHECK_CAMERA_PERMISSION_CODE = 5;
    public final static String CHECK_CAMERA_PERMISSION_NAME = "android.permission.CAMERA";

    //定义的权限编码,当PERMISSION_DOACACHENEEDSPERMISSION有N个权限，那么REQUEST_DOACACHENEEDSPERMISSION就会有多少值
    private static final int REQUEST_DOACACHENEEDSPERMISSION = 1;
    //需要请求的权限名称
    private static String[] PERMISSION_DOACACHENEEDSPERMISSION = new String[]{CHECK_LOCTION_PERMISSION_NAME,CHECK_FILE_PERMISSION_NAME,CHECK_PHONE_PERMISSION_NAME};

    private Activity mActivity;

    /**
     * 需要进行检测的权限数组
     */
    public static String[] needPermissions = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_PHONE_STATE
    };

    public PermissionUtil(Activity activity) {
        mActivity = activity;
    }



    /**
     * 判断GPS是否开启，GPS或者AGPS开启一个就认为是开启的
     *
     * @param Activity
     * @return true 表示开启
     */
    public static final boolean isOpenGps(final Activity Activity) {
        LocationManager locationManager
                = (LocationManager) Activity.getSystemService(Activity.LOCATION_SERVICE);
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gps || network) {
            return true;
        }

        return false;
    }

    /**
     * 强制帮用户打开GPS
     *
     * @param Activity
     */
    public static final void openGPS(Activity Activity) {
        Intent GPSIntent = new Intent();
        GPSIntent.setClassName("com.android.settings",
                "com.android.settings.widget.SettingsAppWidgetProvider");
        GPSIntent.addCategory("android.intent.category.ALTERNATIVE");
        GPSIntent.setData(Uri.parse("custom:3"));
        try {
            PendingIntent.getBroadcast(Activity, 0, GPSIntent, 0).send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    public static boolean hasAllPermission(Activity activity,boolean needRetry){
        if (PermissionUtils.hasSelfPermissions(activity, CHECK_LOCTION_PERMISSION_NAME)&& PermissionUtils.hasSelfPermissions(activity, CHECK_FILE_PERMISSION_NAME)&& PermissionUtils.hasSelfPermissions(activity, CHECK_PHONE_PERMISSION_NAME)) {
            return true;
        }else {
            if (needRetry){
                String[] PERMISSION_DOACACHENEEDSPERMISSION = new String[]{CHECK_LOCTION_PERMISSION_NAME,CHECK_FILE_PERMISSION_NAME,CHECK_PHONE_PERMISSION_NAME};
                ActivityCompat.requestPermissions(activity, PERMISSION_DOACACHENEEDSPERMISSION, CHECK_CAMERA_PERMISSION_CODE);
            }
            return false;
        }
    }

    public static boolean hasPhonePermission(Activity activity,boolean needRetry){
        if (PermissionUtils.hasSelfPermissions(activity, CHECK_PHONE_PERMISSION_NAME)) {
            return true;
        }else {
            if (needRetry){
                String[] PERMISSION_DOACACHENEEDSPERMISSION = new String[]{CHECK_PHONE_PERMISSION_NAME};
                ActivityCompat.requestPermissions(activity, PERMISSION_DOACACHENEEDSPERMISSION, CHECK_PHONE_PERMISSION_CODE);
            }
            return false;
        }
    }

    public static boolean hasFilePermission(Activity activity,boolean needRetry){
        if (PermissionUtils.hasSelfPermissions(activity, CHECK_FILE_PERMISSION_NAME)) {
            return true;
        }else {
            if (needRetry){
                String[] PERMISSION_DOACACHENEEDSPERMISSION = new String[]{CHECK_FILE_PERMISSION_NAME};
                ActivityCompat.requestPermissions(activity, PERMISSION_DOACACHENEEDSPERMISSION, CHECK_FILE_PERMISSION_CODE);
            }
            return false;
        }
    }

    public static boolean hasCameraPermission(Activity activity,boolean needRetry){
        if (PermissionUtils.hasSelfPermissions(activity, CHECK_CAMERA_PERMISSION_NAME)) {
            return true;
        }else {
            if (needRetry){
                String[] PERMISSION_DOACACHENEEDSPERMISSION = new String[]{CHECK_CAMERA_PERMISSION_NAME};
                ActivityCompat.requestPermissions(activity, PERMISSION_DOACACHENEEDSPERMISSION, CHECK_CAMERA_PERMISSION_CODE);
            }
            return false;
        }
    }

    public static boolean hasPicturePermission(Activity activity,boolean needRetry){
        if (PermissionUtils.hasSelfPermissions(activity, CHECK_FILE_PERMISSION_NAME)&& PermissionUtils.hasSelfPermissions(activity, CHECK_CAMERA_PERMISSION_NAME)&& PermissionUtils.hasSelfPermissions(activity, CHECK_AUDIO_PERMISSION_NAME)) {
            return true;
        }else {
            if (needRetry){
                String[] PERMISSION_DOACACHENEEDSPERMISSION = new String[]{CHECK_FILE_PERMISSION_NAME,CHECK_CAMERA_PERMISSION_NAME,CHECK_AUDIO_PERMISSION_NAME};
                ActivityCompat.requestPermissions(activity, PERMISSION_DOACACHENEEDSPERMISSION, CHECK_CAMERA_PERMISSION_CODE);
            }
            return false;
        }
    }

    public static boolean hasLocationPermission(Activity activity,boolean needRetry){
        if (PermissionUtils.hasSelfPermissions(activity, CHECK_LOCTION_PERMISSION_NAME)) {
            return true;
        }else {
            if (needRetry){
                String[] PERMISSION_DOACACHENEEDSPERMISSION = new String[]{CHECK_LOCTION_PERMISSION_NAME};
                ActivityCompat.requestPermissions(activity, PERMISSION_DOACACHENEEDSPERMISSION, CHECK_LOCTION_PERMISSION_CODE);
            }
            return false;
        }
    }

    public static boolean hasVideoPermission(Activity activity,boolean needRetry){
        if (PermissionUtils.hasSelfPermissions(activity, CHECK_FILE_PERMISSION_NAME)&& PermissionUtils.hasSelfPermissions(activity, CHECK_CAMERA_PERMISSION_NAME)&& PermissionUtils.hasSelfPermissions(activity, CHECK_AUDIO_PERMISSION_NAME)) {
            return true;
        }else {
            if (needRetry){
                String[] PERMISSION_DOACACHENEEDSPERMISSION = new String[]{CHECK_FILE_PERMISSION_NAME,CHECK_CAMERA_PERMISSION_NAME,CHECK_AUDIO_PERMISSION_NAME};
                ActivityCompat.requestPermissions(activity, PERMISSION_DOACACHENEEDSPERMISSION, CHECK_CAMERA_PERMISSION_CODE);
            }
            return false;
        }
    }

    /**
     * 检查权限
     * @since 2.5.0
     */
    public void checkPermissions(int PERMISSON_REQUESTCODE,String... permissions) {
        List<String> needRequestPermissionList = findDeniedPermissions(permissions);
        if (null != needRequestPermissionList
                && needRequestPermissionList.size() > 0) {
            ActivityCompat.requestPermissions(mActivity,
                    needRequestPermissionList.toArray(
                            new String[needRequestPermissionList.size()]),
                    PERMISSON_REQUESTCODE);
        }
    }

    /**
     * 获取权限集中需要申请权限的列表
     *
     * @param permissions
     * @return
     * @since 2.5.0
     */
    private List<String> findDeniedPermissions(String[] permissions) {
        List<String> needRequestPermissionList = new ArrayList<String>();
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(mActivity,
                    perm) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.shouldShowRequestPermissionRationale(
                    mActivity, perm)) {
                needRequestPermissionList.add(perm);
            }
        }
        return needRequestPermissionList;
    }

    /**
     * 检测是否说有的权限都已经授权
     *
     * @param grantResults
     * @return
     * @since 2.5.0
     */
    public boolean verifyPermissions(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

}