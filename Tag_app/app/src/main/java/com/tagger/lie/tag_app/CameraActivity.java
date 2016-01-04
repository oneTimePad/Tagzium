package com.tagger.lie.tag_app;

import android.content.Context;
import android.hardware.Camera;
import android.media.AudioManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.IOException;

public class CameraActivity extends AppCompatActivity {

    User current_user;
    Camera mCamera;
    boolean isBack =true;
    Menu myMenu;
    CameraHandler mThread;
    SurfaceView sf;
    SurfaceHolder sH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);




       sf = new SurfaceView(this);
        sf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                while (mThread.isChanging()) {

                }
                mThread.takePic();
            }
        });
        RelativeLayout.LayoutParams pr = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
        ((RelativeLayout) findViewById(R.id.camera_layout)).addView(sf,pr);
        mThread = new CameraHandler();
        sH = sf.getHolder();
        sH.setKeepScreenOn(true);
        sH.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                mThread.cameraSet(isBack);
                mThread.Lock = true;
                while (mThread.isChanging()) {

                }
                try {
                    if (mCamera != null) {
                        Log.e("HERE", "HERE");

                        mCamera.setPreviewDisplay(holder);
                        mCamera.startPreview();
                    } else {
                        Log.e("LOG", "LOG");
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

            }
        });

        if(myMenu!=null){
            MenuItem cam = myMenu.getItem(R.id.camera_change);
            cam.setTitle("Camera Front");

        }







    }

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
        mCamera.setDisplayOrientation(result);
    }


    public void  onPause(){
        super.onPause();
        if(mCamera!=null){
            mCamera.release();
        }
        mCamera=null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        myMenu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.camera_menu, menu);

        return true;
    }

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

        }
    };
    private class CameraHandler extends HandlerThread{

        Handler mHandler = null;
        boolean Lock = false;

        public CameraHandler(){
            super("CameraHandler");
            start();
            mHandler = new Handler(getLooper());

        }

        public void takePic(){
            mCamera.takePicture(onShutter,null,onPicTake);
        }

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
                            Log.e("Opened", "opened");


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
        if(isBack){

            isBack=false;
            mThread.Lock = true;
            mThread.cameraSet(isBack);
            while(mThread.isChanging()){

            }
            try{
                mCamera.setPreviewDisplay(sH);
                mCamera.startPreview();
                setCameraDisplayOrientation();
            }
            catch (IOException e){
                Log.e("camera",e.toString());
            }
        }
        else{

            isBack=true;
            mThread.Lock = true;
            mThread.cameraSet(isBack);
            while(mThread.isChanging()){

            }
            try{
                mCamera.setPreviewDisplay(sH);
                mCamera.startPreview();
                setCameraDisplayOrientation();
            }
            catch (IOException e){
                Log.e("camera",e.toString());
            }

        }

    }


    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();


        }
        return true;
    }
}
