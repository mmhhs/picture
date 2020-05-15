package com.little.picture;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.little.picture.model.ImageEntity;
import com.little.picture.util.ImagePreviewUtil;

public class PictureMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_main);
        findViewById(R.id.btn_pz).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                PictureTakeActivity.startAction(PictureMainActivity.this,0,"",null);
//                PictureStartManager.showChooseDialog(PictureMainActivity.this,"",9);
                ImagePreviewUtil imagePreviewUtil = new ImagePreviewUtil(PictureMainActivity.this, findViewById(R.id.btn_pz));
                ImageEntity imageEntity = new ImageEntity();
                imageEntity.setShowDelete(true);
                imageEntity.setType(1);
                imageEntity.setImagePath("http://obs-fix-video.obs.cn-north-1.myhwclouds.com/android_ftej_15894495901915269_666666");
                imagePreviewUtil.showVideoDialog(imageEntity);
            }
        });
        findViewById(R.id.btn_xt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PicturePickActivity.startAction(PictureMainActivity.this,1,9,"");
            }
        });
    }
}
