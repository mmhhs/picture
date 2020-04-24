package com.little.picture;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class PictureMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_main);
        findViewById(R.id.btn_pz).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PictureTakeActivity.startAction(PictureMainActivity.this);
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
