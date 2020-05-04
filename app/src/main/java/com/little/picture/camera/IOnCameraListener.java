package com.little.picture.camera;

public interface IOnCameraListener {
    void onPictureResult(String path);
    void onVideoResult(String path);
}