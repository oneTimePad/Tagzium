package com.tagger.lie.tag_app;

import android.media.FaceDetector;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;

public class ImageCreateActivity extends AppCompatActivity {

    private RelativeLayout main_layout;
    private String image;
    private FaceDetector.Face[] faces;
    private int face_count;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_create);

        main_layout = (RelativeLayout)findViewById(R.id.image_create);

        Face_Detection_View fde = null;

        if(savedInstanceState!=null){
            image =savedInstanceState.getString("image");
            if(image!=null) {
                fde = new Face_Detection_View(getApplicationContext());
                fde.setImage(image);
                faces = (FaceDetector.Face[])savedInstanceState.get("faces");
                fde.setFaces(faces,face_count);
                Log.e("saved", "saved");
            }

        }
        if(image==null) {
            image = getIntent().getStringExtra("image");
            fde = new Face_Detection_View(getApplicationContext());
            fde.setImage(image);
            faces = fde.detect();
            face_count = fde.getFaceCount();
        }





        RelativeLayout.LayoutParams fde_params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        fde_params.addRule(RelativeLayout.CENTER_HORIZONTAL,RelativeLayout.TRUE);
        fde_params.addRule(RelativeLayout.ALIGN_PARENT_TOP,RelativeLayout.TRUE);

        main_layout.addView(fde,fde_params);

    }

    public void onSaveInstanceState(Bundle saveInstanceState){
        if(image != null){
            saveInstanceState.putString("image",image);
        }
        if(faces!=null){

            //saveInstanceState.putParcelableArray("faces", faces);
            saveInstanceState.putInt("face_count", face_count);
        }


    }
}
