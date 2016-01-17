package com.tagger.lie.tag_app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.hardware.camera2.params.Face;
import android.media.ExifInterface;
import android.media.FaceDetector;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.util.ArrayList;

class Face_Detection_View extends View {
    private static final int MAX_FACES = 10;
    private static  String IMAGE_FN;
    private Bitmap background_image;

    private ArrayList<float[]> point_list;

    // preallocate for onDraw(...)
    private PointF tmp_point = new PointF();
    private Paint tmp_paint = new Paint();

    public Face_Detection_View(Context context) {
        super(context);
        // Load an image from SD Card

    }

    public void setImage(String IMAGE_FN){
        this.IMAGE_FN = IMAGE_FN;
        updateImage(IMAGE_FN);
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Bitmap retVal;

        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        retVal = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);

        return retVal;
    }




    public void updateImage(String image_fn) {
        // Set internal configuration to RGB_565
        BitmapFactory.Options bitmap_options = new BitmapFactory.Options();
        bitmap_options.inPreferredConfig = Bitmap.Config.RGB_565;

        background_image = BitmapFactory.decodeFile(image_fn, bitmap_options);
        try {

            ExifInterface ei = new ExifInterface(IMAGE_FN);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:

                    background_image = rotateImage(background_image, 270);
                    break;

                // etc.
            }
        } catch (IOException e) {
            Log.e("FaceDetector", e.toString());
        }
    }

    public ArrayList<float[]> detect(){

        FaceDetector face_detector = new FaceDetector(
                background_image.getWidth(), background_image.getHeight(),
                MAX_FACES);

        FaceDetector.Face[] faces = new FaceDetector.Face[MAX_FACES];
        // The bitmap must be in 565 format (for now).
        int face_count = face_detector.findFaces(background_image, faces);
        Log.e("Face_Detection", "Face Count: " + String.valueOf(face_count));

        point_list = new ArrayList<>();

        for (int i = 0; i < face_count; i++) {
            FaceDetector.Face face = faces[i];
            float myEyesDistance = face.eyesDistance();
            face.getMidPoint(tmp_point);

            float[] points = new float[4];

            points[0] = (tmp_point.x - myEyesDistance);
            points[1] = (tmp_point.y - myEyesDistance - 100);

            points[2] = (tmp_point.x + myEyesDistance);
            points[3] = (tmp_point.y + myEyesDistance + 100);
            point_list.add(points);
        }



        return  point_list;
    }


    public void setFaces(ArrayList<float[]> point_list) {
        this.point_list = point_list;
    }

    public void onDraw(Canvas canvas) {
        canvas.drawBitmap(background_image, 0, 0, null);
        for (float[] list : point_list) {

            tmp_paint.setColor(Color.YELLOW);
            tmp_paint.setStyle(Paint.Style.STROKE);
            tmp_paint.setStrokeWidth(4);




            canvas.drawRect((int) (list[0]),
                    (int) (list[1] ),
                    (int) (list[2]),
                    (int) (list[3]), tmp_paint);


        }
    }
}