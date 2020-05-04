package com.little.picture.camera;

import android.app.Activity;
import android.os.Handler;

public interface OnHandlerListener {

    Handler getActivityHandler();
    CameraManager getActivityCameraManager();
    Activity getActivity();
}