package com.tagger.lie.tag_app;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.style.SuperscriptSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class Face_Detection_View extends View {
    private static final int MAX_FACES = 10;
    private static  String IMAGE_FN;
    private Bitmap background_image;

    private ArrayList<float[]> point_list;

    // preallocate for onDraw(...)
    private PointF tmp_point = new PointF();
    private Paint tmp_paint = new Paint();
    public static User current_user;

    private static ArrayList<JSONObject> mRightListOverlapData,mRightTempData,cacheQueries=null;

    private FragmentManager fm;

    private static ProgressBar progbar;

    public void setSearchSuggestions(JSONArray root){

        try{
            JSONArray search_suggestions = root;

            if(!search_suggestions.getJSONObject(0).has("no data")){
                mRightTempData.clear();
                mRightListOverlapData.clear();
                mRightListOverlapData.clear();
                for(int i =0; i< search_suggestions.length();i++){
                    mRightTempData.add(search_suggestions.getJSONObject(i));
                }
            }
            else{
                Log.d("No data","No data");
                //Toast.makeText(getActivity(),"No data realted to query",Toast.LENGTH_SHORT).show();

            }

        }
        catch (JSONException e){
            e.printStackTrace();
        }


    }




    public Face_Detection_View(Context context,FragmentManager fm,User user) {
        super(context);
        this.setId(new Integer(455));
        this.fm = fm;
        current_user = user;

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

    @Override
    public Parcelable onSaveInstanceState(){
        Parcelable super_state = super.onSaveInstanceState();

        SavedState ss = new SavedState(super_state);

        ss.saved_faces = point_list;
        ss.saved_image = background_image;

        return ss;

    }

    @Override
    public void onRestoreInstanceState(Parcelable state){

        if(!(state instanceof SavedState)){
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState ss = (SavedState)state;
        super.onRestoreInstanceState(ss.getSuperState());

       this.point_list = ss.saved_faces;
       this.background_image = ss.saved_image;
    }


    public void changeProg(){
        progbar.setVisibility(View.GONE);
        return;
    }

    public static class SelectUser extends DialogFragment{
        private final String METHOD = "method";
        private final int GET_SEARCH_SUGGESTION = 1;

        @Override
        public void onCreate(Bundle savedInstanceState){
            super.onCreate(savedInstanceState);


        }






        private  void startServiceForSearchSuggestion(String search_tag){
            Intent intent = new Intent(getActivity(),FetchSuggestionsService.class);
            intent.putExtra(METHOD,GET_SEARCH_SUGGESTION);
            intent.putExtra("search_tag",search_tag);
            intent.putExtra("current_user",current_user);
            getActivity().startService(intent);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, final ViewGroup container,Bundle savedInstanceState){

            View v = inflater.inflate(R.layout.search_dialog,container);

            progbar = (ProgressBar)(v.findViewById(R.id.progbar));



            final ListView suggestions_list = (ListView)(v.findViewById(R.id.list_sugg));
            mRightListOverlapData = new ArrayList<JSONObject>();
            mRightTempData = new ArrayList<JSONObject>();
            cacheQueries = new ArrayList<>();
            final RightOverlapAdapter mOverlapAdapter = new RightOverlapAdapter(v.getContext(),mRightListOverlapData);

            suggestions_list.setAdapter(mOverlapAdapter);

            final EditText search_box = (EditText)(v.findViewById(R.id.search_text));
            search_box.setHint("username");
            final Button search = (Button)(v.findViewById(R.id.button_search));
            search.setText("Search");

            ((ImageCreateActivity)getActivity()).utilities.key_dis(search_box);

            search_box.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    return;
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    return;
                }

                @Override
                public void afterTextChanged(Editable s) {

                    mRightListOverlapData.clear();
                    String search_tag = search_box.getText().toString();
                    if (search_tag == null || search_box.equals("")) {
                        mRightTempData.clear();
                        mOverlapAdapter.notifyDataSetChanged();
                    }
                    else if (search_tag.length() == 1) {

                        if(cacheQueries.size()!=0){
                            try {
                                for (int i = 0; i < cacheQueries.size(); i++) {
                                    if (cacheQueries.get(i).getString("username").toLowerCase()
                                            .startsWith(search_tag.toLowerCase())) {
                                        mRightListOverlapData.add(cacheQueries.get(i));
                                    }
                                }

                            }
                            catch (JSONException e){
                                e.printStackTrace();

                            }

                        }
                        else {
                            //progbar.setVisibility(View.VISIBLE);
                            //progbar.bringToFront();
                            mRightListOverlapData.addAll(mRightTempData);
                            mOverlapAdapter.notifyDataSetChanged();
                            startServiceForSearchSuggestion(search_tag);
                        }
                    } else {
                        try {
                            if (mRightTempData.size() > 0) {
                                for (int i = 0; i < mRightTempData.size(); i++) {
                                    if (mRightTempData.get(i).getString("username").toLowerCase()
                                            .startsWith(search_tag.toLowerCase())) {
                                        mRightListOverlapData.add(mRightTempData.get(i));
                                        cacheQueries.add(mRightTempData.get(i));
                                    }
                                }

                                if (mRightListOverlapData.size() == 0) {
                                    JSONObject noData = new JSONObject();
                                    noData.put("search_text", "No Data");
                                    mRightListOverlapData.add(noData);
                                }
                            }
                            mOverlapAdapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            /*
            RelativeLayout.LayoutParams search_box_params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
            search_box_params.addRule(RelativeLayout.ALIGN_PARENT_TOP,RelativeLayout.TRUE);
            search_box_params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            search_box.setId(new Integer(1));

            RelativeLayout.LayoutParams button_params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
            button_params.addRule(RelativeLayout.RIGHT_OF,search_box.getId());
            button_params.addRule(RelativeLayout.ALIGN_PARENT_TOP,RelativeLayout.TRUE);

            RelativeLayout.LayoutParams listview_params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
            listview_params.addRule(RelativeLayout.CENTER_HORIZONTAL,RelativeLayout.TRUE);
            listview_params.addRule(RelativeLayout.BELOW, search_box.getId());


            dialog_layout.addView(search_box, search_box_params);
            dialog_layout.addView(search, button_params);
            dialog_layout.addView(suggestions_list,listview_params);
            */


            return v;



        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e){
        super.onTouchEvent(e);
        if(e.getAction() == MotionEvent.ACTION_DOWN){
            int x = (int)e.getX();
            int y = (int)e.getY();
            for(float[] points: point_list){
                if(x<points[2]&&x>points[0]){
                    if(y<points[3]&&y>points[1]){
                        SelectUser dialog = new SelectUser();
                        dialog.show(fm.beginTransaction(),"dialog");
                        return true;
                    }
                }
            }
        }
        return false;
    }




   static class SavedState extends BaseSavedState {

        ArrayList<float[]> saved_faces;
        Bitmap saved_image;

        SavedState(Parcelable super_state){
            super(super_state);
        }

        SavedState(Parcel in){
            super(in);
            this.saved_faces = in.readArrayList(null);
            this.saved_image = in.readParcelable(null);

        }

        @Override
        public void writeToParcel(Parcel out, int flags){
            super.writeToParcel(out,flags);
            out.writeSerializable(this.saved_faces);
            out.writeParcelable(this.saved_image, flags);
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>(){
                    public SavedState createFromParcel(Parcel in){
                        return new SavedState(in);
                    }
                    public SavedState[] newArray(int size){
                        return new SavedState[size];
                    }
                };
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
                case ExifInterface.ORIENTATION_ROTATE_90:
                    background_image = rotateImage(background_image, 90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    background_image = rotateImage(background_image,270);
                    break;


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
    @Override
    protected void onMeasure(int width_measure_spec, int height_measure_spec){

        int width=0,height = 0;
        int width_mode = MeasureSpec.getMode(width_measure_spec);
        int width_size = MeasureSpec.getSize(width_measure_spec)-getPaddingRight()-getPaddingLeft();
        int height_mode = MeasureSpec.getMode(height_measure_spec);
        int height_size = MeasureSpec.getSize(height_measure_spec)-getPaddingTop()-getPaddingBottom();

        if(width_mode == MeasureSpec.EXACTLY){
            width= width_size;
        }
        else if(width_mode == MeasureSpec.AT_MOST){
            width = Math.min(width_size,((View)this.getParent()).getWidth());
        }

        if(height_mode == MeasureSpec.EXACTLY){
            height = height_size;
        }
        else if(height_mode == MeasureSpec.AT_MOST){
            height = Math.min(height_size, ((View)this.getParent()).getHeight());
        }

        setMeasuredDimension(width,height);
    }
}