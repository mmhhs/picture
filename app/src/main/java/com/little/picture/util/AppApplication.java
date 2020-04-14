package com.little.picture.util;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import com.fos.fosmvp.common.base.BaseApplication;
import com.fos.fosmvp.common.utils.LogUtils;
import com.little.picture.PictureStartManager;

/**
 * Created by mmh on 2018/7/17.
 */
public class AppApplication extends BaseApplication implements Application.ActivityLifecycleCallbacks {
    private static AppApplication appApplication;
    private String processName = "";//进程名称

    @Override
    public void onCreate() {
        super.onCreate();
        appApplication = this;
        PictureStartManager.getInstance(this);
        LogUtils.e("---------------start fosmvp----------------"+ appApplication);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        attach(base);
//        MultiDex.install(base);

    }

    private void attach(Context base) {
//        processName = OptionUtils.getProcessName(this, android.os.Process.myPid());
//        if (processName != null) {
//            boolean defaultProcess = processName.equals(getPackageName());
//            if (defaultProcess) {
//                MultiDex.install(base);
//
//            } else if (processName.contains(":watch")) {
//
//            }
//        }
    }

    public static Context getAppContext() {
        return appApplication;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }


    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
