package com.tagger.lie.tag_app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CameraActivity extends AppCompatActivity {
    //logged in user
    User current_user;
    //camera
    Camera mCamera;
    //is back camera enabled
    boolean isBack =true;
    private int orientation;

    //thread for camera triggering
    CameraHandler mThread;
    //surface view and holder
    SurfaceView sf;
    SurfaceHolder sH;
    //action button for swapping camera view
    FloatingActionButton switch_button;
    //picture diretory
    File pic_dir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        //grab the current user
        current_user = (User)getIntent().getExtras().get("User");


        sf = new SurfaceView(this);
        sf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {                 //on surface touch,
                while (mThread.isChanging()) {

                }
                mThread.takePic();                    //take pic
            }
        });
        switch_button= new FloatingActionButton(this);            // create floating button
        switch_button.setImageResource(R.drawable.ic_camera_front_24dp);

        switch_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSwitch(v);
            }
        });                                              //on user click, switch camera

        //generate layout params for action button
        RelativeLayout.LayoutParams switch_button_params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        //move it to top right
        switch_button_params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,RelativeLayout.TRUE);
        switch_button_params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        //surfaceview params
        RelativeLayout.LayoutParams surface_params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
        //back button prams
        RelativeLayout.LayoutParams back_button_params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        back_button_params.addRule(RelativeLayout.ALIGN_PARENT_TOP,RelativeLayout.TRUE);
        back_button_params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        //generate user back button
        FloatingActionButton back_button = new FloatingActionButton(this);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        back_button.setImageResource(R.drawable.ic_arrow_back_24dp);

        //add views
        ((RelativeLayout) findViewById(R.id.camera_layout)).addView(sf, surface_params);
        ((RelativeLayout)findViewById(R.id.camera_layout)).addView(switch_button, switch_button_params);
        ((RelativeLayout)findViewById(R.id.camera_layout)).addView(back_button,back_button_params);
        //genrate camera trigger thread
        mThread = new CameraHandler();
        //get surface holder
        sH = sf.getHolder();
        sH.setKeepScreenOn(true);
        sH.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                //set camera
                mThread.cameraSet(isBack);
                //lock and wait while locked
                mThread.Lock = true;
                while (mThread.isChanging()) {

                }
                try {
                    if (mCamera != null) {

                        //start preview
                        mCamera.setPreviewDisplay(holder);
                        mCamera.startPreview();
                    } else {

                    }
                } catch (IOException e) {
                    Log.e("Camera", e.toString());
                }

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                setCameraDisplayOrientation();
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                //unused

            }
        });

        File sd_card = Environment.getExternalStorageDirectory();

        pic_dir = new File(sd_card.toString()+"/Tag");

        try{
            if(!pic_dir.exists()){
                pic_dir.mkdirs();
            }
        }
        catch (SecurityException e){
            Toast.makeText(CameraActivity.this,"Storage Creation Failed",Toast.LENGTH_LONG);
            System.exit(1);
        }
    }




    public void  onPause(){
        super.onPause();
        if(mCamera!=null){
            mCamera.release();
        }
        mCamera=null;
    }

    //returns camera id
    private int getCameraId(boolean isBack) {
        int cameraId = -1;
        int m;
        if (isBack) {
            m = Camera.CameraInfo.CAMERA_FACING_BACK;
        } else {
            m = Camera.CameraInfo.CAMERA_FACING_FRONT;
        }
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == m) {

                cameraId = i;
                break;
            }
        }
        return cameraId;
    }

    //sets correct orientation of camera
    public void setCameraDisplayOrientation() {
        Camera.Parameters parameters = mCamera.getParameters();

        android.hardware.Camera.CameraInfo camInfo =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(getCameraId(isBack), camInfo);


        Display display = ((WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int rotation = display.getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (camInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (camInfo.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (camInfo.orientation - degrees + 360) % 360;
        }
        orientation = result;
        mCamera.setDisplayOrientation(result);
    }

    //make shutter sound
    Camera.ShutterCallback onShutter = new Camera.ShutterCallback() {
        @Override
        public void onShutter() {
            AudioManager mgr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            mgr.playSoundEffect(AudioManager.FLAG_PLAY_SOUND);





        }
    };

    Camera.PictureCallback onPicTake = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {



            File new_image;
            String file_name;
            do {
                int random_num = (int)(Math.random()*9000)+1000;
                file_name =String.valueOf(random_num);
                new_image = new File(pic_dir, current_user.first_name+"_"+file_name+".jpg");
            }
            while(new_image.exists());

            try {
                FileOutputStream output_stream = new FileOutputStream(new_image);
                output_stream.write(data);
                output_stream.close();
                if(orientation==90) {
                    ExifInterface exif = new ExifInterface(new_image.getAbsolutePath());

                    exif.setAttribute(ExifInterface.TAG_ORIENTATION,"8");
                    exif.saveAttributes();

                }

            }
            catch (FileNotFoundException e){
                Log.e("Pic Callback",e.toString());
            }
            catch(IOException e){
                Log.e("Pic Callback",e.toString());
            }

            if(getIntent().getStringExtra("NeedResult")!=null){
                Intent intent = new Intent();
                intent.putExtra("uri",new_image.getAbsolutePath());
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
            else {
                add_pic_to_new_event(new_image.getAbsolutePath());
            }


        }
    };

    //ask user what to do with nw pic
    private void add_pic_to_new_event(final String pic_file){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    //add to existing event
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        break;
                    //go create new event
                    case DialogInterface.BUTTON_NEGATIVE:
                        //go call eventactivity
                        Intent toNewEvent = new Intent(CameraActivity.this,UserEventsActivity.class);
                        //tell eventactivity to load create event
                        toNewEvent.putExtra("Create","1");
                        //pass user
                        toNewEvent.putExtra("User", current_user);
                        //pass pic name
                        toNewEvent.putExtra("image",pic_file);

                        startActivity(toNewEvent);

                        //No button clicked
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(CameraActivity.this);
        builder.setMessage("Add To Existing Event?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }
    private class CameraHandler extends HandlerThread{

        Handler mHandler = null;
        boolean Lock = false;

        public CameraHandler(){
            super("CameraHandler");
            start();
            mHandler = new Handler(getLooper());

        }

        public void takePic(){
            /*
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        if(success) camera.takePicture(onShutter, null, onPicTake);
                    }
                });
            */
            mCamera.takePicture(onShutter, null, onPicTake);

        }

        //get mutex value
        public synchronized boolean isChanging(){
            return Lock;
        }

        public void cameraSet(final boolean isBack){
            mHandler.post(new Runnable() {
                @Override
                public void run() {

                    if(mCamera!=null){
                        mCamera.stopPreview();
                        mCamera.release();
                    }
                    try {
                        if (isBack) {

                            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);



                        } else {

                            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);

                        }
                    }
                    catch(RuntimeException e){
                        Toast.makeText(CameraActivity.this,"failed to open camera",Toast.LENGTH_SHORT).show();
                    }

                    Lock = false;

                }
            });


        }
    }


    public void onSwitch(View v){
        //if back camera is enabled
        if(isBack){
            //switch to front
            isBack=false;
            //change camera view
            mThread.Lock = true;
            mThread.cameraSet(isBack);
            while(mThread.isChanging()){

            }
            try{
                //reload display and orientation
                mCamera.setPreviewDisplay(sH);
                mCamera.startPreview();
                setCameraDisplayOrientation();
                //change button icon
                switch_button.setImageResource(R.drawable.ic_camera_rear_24dp);
            }
            catch (IOException e){
                Log.e("camera",e.toString());
            }
        }
        //if we are in front view
        else{
            //switch to back
            isBack=true;
            //set camera display
            mThread.Lock = true;
            mThread.cameraSet(isBack);
            while(mThread.isChanging()){

            }
            try{
                //set display
                mCamera.setPreviewDisplay(sH);
                mCamera.startPreview();
                setCameraDisplayOrientation();
                switch_button.setImageResource(R.drawable.ic_camera_front_24dp);
            }
            catch (IOException e){
                Log.e("camera",e.toString());
            }

        }

    }

}
