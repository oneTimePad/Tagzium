package com.tagger.lie.tag_app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.RelativeLayout;

public class ImageCreateActivity extends AppCompatActivity {

    private RelativeLayout main_layout;
    private String image;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_create);

        if(savedInstanceState!=null){
            image =savedInstanceState.getString("image");
        }
        if(image==null) {
            image = getIntent().getStringExtra("image");
        }

        main_layout = (RelativeLayout)findViewById(R.id.image_create);

        Face_Detection_View fde = new Face_Detection_View(getApplicationContext());
        fde.setImage(image);

        RelativeLayout.LayoutParams fde_params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        fde_params.addRule(RelativeLayout.CENTER_HORIZONTAL,RelativeLayout.TRUE);
        fde_params.addRule(RelativeLayout.ALIGN_PARENT_TOP,RelativeLayout.TRUE);

        main_layout.addView(fde,fde_params);

    }

    public void onSaveInstanceState(Bundle saveInstanceState){
        if(image != null){
            saveInstanceState.putString("image",image);
        }


    }
}
