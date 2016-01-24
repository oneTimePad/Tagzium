package com.tagger.lie.tag_app;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class ImageCreateActivity extends AppCompatActivity {

    private RelativeLayout main_layout;
    private String image;
    private ArrayList<float[]> faces;
    public Utils utilities;
    private Face_Detection_View fde;
    private User current_user;

    private String METHOD_GET_SUGGESTIONS ="1";




    private BroadcastReceiver mReceiverSearch = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            JSONArray response;

            try{
                response = new JSONArray(intent.getStringExtra("Response"));
                fde.setSearchSuggestions(response);
                //fde.changeProg();

            }catch (JSONException e){
                e.printStackTrace();
            }

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        current_user = (User)getIntent().getExtras().get("current_user");
        setContentView(R.layout.activity_image_create);


        FragmentManager fm =  getFragmentManager();

        this.registerReceiver(mReceiverSearch, new IntentFilter(METHOD_GET_SUGGESTIONS));

        main_layout = (RelativeLayout)findViewById(R.id.image_create);
        utilities = (Utils)getApplication();


        if(savedInstanceState!=null){
            /*
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
                    if(array!=null) {
                        faces.add(array);
                    }
                }
                while(array!=null);

                fde.setFaces(faces);

            }
            */
            fde = new Face_Detection_View(getApplicationContext(),fm,current_user);
            RelativeLayout.LayoutParams fde_params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
            fde_params.addRule(RelativeLayout.CENTER_HORIZONTAL,RelativeLayout.TRUE);
            fde_params.addRule(RelativeLayout.ALIGN_PARENT_TOP,RelativeLayout.TRUE);

            main_layout.addView(fde,fde_params);
            return;
        }
        if(image==null) {
            image = getIntent().getStringExtra("image");
            fde = new Face_Detection_View(getApplicationContext(),fm,current_user);
            fde.setImage(image);
            fde.detect();
            RelativeLayout.LayoutParams fde_params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
            fde_params.addRule(RelativeLayout.CENTER_HORIZONTAL,RelativeLayout.TRUE);
            fde_params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);

            main_layout.addView(fde,fde_params);
        }







    }
    /*
    public void onSaveInstanceState(Bundle saveInstanceState){
        super.onSaveInstanceState(saveInstanceState);


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


    }*/



}
