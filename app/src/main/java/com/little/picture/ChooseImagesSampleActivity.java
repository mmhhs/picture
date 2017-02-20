package com.little.picture;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.widget.GridView;
import android.widget.PopupWindow;

import com.little.picture.listener.IOnDeleteListener;
import com.little.picture.listener.IOnItemClickListener;
import com.little.picture.util.ImageUtil;
import com.little.picture.util.ToastUtil;

import java.util.ArrayList;


public class ChooseImagesSampleActivity extends Activity {
    public GridView gridView;
    private ChooseImagesBroadcastReciver reciver;
    private ArrayList<String> chooseImageList = new ArrayList<String>();
    private ChooseImagesSampleAdapter chooseImagesSampleAdapter;
    private int screenWidth = 0;
    private int maxSize = 9;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.base_choose_images_sample);
        init();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        this.unregisterReceiver(reciver);
    }

    public void init() {
        screenWidth = ImageUtil.getScreenWidth(this);
        registerBroadcast();
        chooseImagesSampleAdapter = new ChooseImagesSampleAdapter(this,chooseImageList,screenWidth);
        chooseImagesSampleAdapter.setiOnItemClickListener(iOnItemClickListener);
        gridView.setAdapter(chooseImagesSampleAdapter);
    }

    private IOnItemClickListener iOnItemClickListener = new IOnItemClickListener() {
        @Override
        public void onItemClick(int position) {
            try{
                if (position==chooseImageList.size()){
                    setChooseImagesIntent();
                }else {
                    if (chooseImageList.size()>0){
                        ImagesPreviewUtil imagesPreviewUtil = new ImagesPreviewUtil(ChooseImagesSampleActivity.this,chooseImageList,gridView);
                        PopupWindow popupWindow = imagesPreviewUtil.getPreviewWindow(ChooseImagesSampleActivity.this,position);
                        popupWindow.showAtLocation(gridView, Gravity.CENTER, 0, 0);
                        imagesPreviewUtil.setiOnDeleteListener(new IOnDeleteListener() {
                            @Override
                            public void onDelete(int position) {
                                new Handler().postDelayed(new Runnable(){
                                    public void run() {
                                        chooseImagesSampleAdapter.notifyDataSetChanged();
                                    }
                                }, 500);
                            }
                        });
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };


    public void setChooseImagesIntent(){
        if (chooseImageList.size()<maxSize){
            Intent intent = new Intent(this, ChooseImagesActivity.class);
            intent.putExtra(ChooseImagesActivity.CHOOSE_IMAGES_COUNT,maxSize-chooseImageList.size());
            startActivity(intent);
        }else {
            ToastUtil.addToast(this, "" + getString(R.string.choose_images_max) + maxSize);
        }

    }

    private void registerBroadcast(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ChooseImagesActivity.CHOOSE_IMAGES_COUNT);
        reciver = new ChooseImagesBroadcastReciver();
        this.registerReceiver(reciver, intentFilter);
    }

    private class ChooseImagesBroadcastReciver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(ChooseImagesActivity.CHOOSE_IMAGES_COUNT)) {
                ArrayList<String> imageList = intent.getStringArrayListExtra(ChooseImagesActivity.CHOOSE_IMAGES_COUNT);
                for(int i=0;i<imageList.size();i++) {
                    chooseImageList.add(imageList.get(i));
                }
                chooseImagesSampleAdapter.notifyDataSetChanged();
            }
        }
    }

}