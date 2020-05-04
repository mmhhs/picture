package com.little.picture.listener;

public interface IOnProgressListener {
    void onStart();
    void onProgress(int progress);
    void onFinish();
}