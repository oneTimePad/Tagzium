package com.tagger.lie.tag_app;

import android.graphics.PointF;
import android.media.FaceDetector;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;

import java.util.ArrayList;

public class ImageCreateActivity extends AppCompatActivity {

    private RelativeLayout main_layout;
    private String image;
    private ArrayList<float[]> faces;


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
                Bundle float_arrays = savedInstanceState.getBundle("faces");
                faces = new ArrayList<>();
                int i =0;
                float[] array;
                do{
                    i++;
                   array= float_arrays.getFloatArray(i+"");
                    faces.add(array);

                }
                while(array!=null);

                fde.setFaces(faces);

            }

        }
        if(image==null) {
            image = getIntent().getStringExtra("image");
            fde = new Face_Detection_View(getApplicationContext());
            fde.setImage(image);
            faces = fde.detect();
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
        if(faces!=null) {
            Bundle face_bundle = new Bundle();
            for (float[] float_array : faces) {
                face_bundle.putFloatArray("1", float_array);
            }
            saveInstanceState.putBundle("faces", face_bundle);

        }


    }
}
