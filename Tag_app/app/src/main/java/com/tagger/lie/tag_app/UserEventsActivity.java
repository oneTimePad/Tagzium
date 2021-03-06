package com.tagger.lie.tag_app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeechService;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UserEventsActivity extends ActionBarActivity {

    User current_user;
    ArrayList<View> children;
    ArrayList<View> parents;
    int back_mode = 0;
    final int normal_back=0;
    final int create_back=1;
    final int camera_back=2;
    boolean inCreateMode = false;
    boolean inCameraMode = false;

    Menu myMenu;
    RelativeLayout main_layout;
    ImageView logo;
    ImageView initial;
    String logo_image;
    String initial_image;
    int which_view;
    final int select_logo=0;
    final int select_initial=1;

    int did_submit  = 0;

    Dialog chosenDialog;

    Utils utilities;

    boolean get_all=false;

    final int IMAGE_GETTER = 2;
    final int IMAGE_CREATE =3;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_events);
        Intent intent = getIntent();
        main_layout = ((RelativeLayout)findViewById(R.id.events));

        utilities = (Utils)getApplication();


        if(savedInstanceState!=null){
            current_user=(User)savedInstanceState.get("User");
            try{
                if(savedInstanceState.getString("State").equals("Camera")){
                    logo_image=savedInstanceState.getString("logo_image");
                    initial_image=savedInstanceState.getString("initial_image");
                    inCreateMode=false;
                    inCameraMode=true;
                    onEventCreate();
                    return;
                }
            }
            catch (NullPointerException e){

            }
        }

        if(current_user==null){
            current_user=(User)intent.getExtras().get("User");

            if(intent.getStringExtra("Create")!=null){
                initial_image=intent.getStringExtra("image");
                inCreateMode=false;
                inCameraMode=true;
                onEventCreate();
                return;
            }

        }

        ActionBar ab = getSupportActionBar();
        ab.setTitle(current_user.first_name + "\'s Events");
        ab.setDisplayHomeAsUpEnabled(true);

        parents = new ArrayList<View>();
        final SwipeRefreshLayout refresh_layout = new SwipeRefreshLayout(this);
        parents.add(refresh_layout);
        main_layout.addView(refresh_layout);
        ScrollView scroll = new ScrollView(this);
        refresh_layout.addView(scroll, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        final LinearLayout linear_layout = new LinearLayout(this);
        linear_layout.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(linear_layout, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        final ProgressBar progress_bar = new ProgressBar(this);
        RelativeLayout.LayoutParams prog_params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        prog_params.addRule(RelativeLayout.CENTER_IN_PARENT,RelativeLayout.TRUE);
        main_layout.addView(progress_bar, prog_params);
        progress_bar.setVisibility(View.VISIBLE);
        new Thread(){
            public void run(){
                JSONArray events = null;
                try {

                    if (current_user.events == null) {
                        get_all=true;

                        events = getUserEvents();
                        if(events!=null) {
                            current_user.events = events.toString();
                        }
                    } else {
                        events = new JSONArray(current_user.events);
                    }
                } catch (JSONException e) {
                    Log.e("Event", e.toString());
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progress_bar.setVisibility(View.GONE);
                    }
                });

                if (events == null) {
                    return;
                }
                for (int i = 0; i < events.length(); i++) {
                    try {
                        final JSONObject events_ser = events.getJSONObject(i);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    TextView evV = new TextView(UserEventsActivity.this);
                                    evV.setText(events_ser.get("event_name").toString());
                                    linear_layout.addView(evV);
                                }
                                catch (JSONException e){
                                    Log.e("Events Thread",e.toString());
                                }
                            }
                        });

                    } catch (JSONException e) {
                        Log.e("Events", e.toString());
                    }


                }


            }
        }.start();



        //setContentView(R.layout.activity_user_events);


        refresh_layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh_layout.setRefreshing(true);

                new Thread(){
                    public void run(){
                        get_all=false;
                        final JSONArray events = getUserEvents();
                        if (events == null) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    refresh_layout.setRefreshing(false);
                                }
                            });

                        }
                        else{
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    onRefreshEvents(events, linear_layout);
                                    refresh_layout.setRefreshing(false);
                                }
                            });
                        }


                    }
                }.start();
            }
        });



        if(savedInstanceState!=null){

            try{
                if(savedInstanceState.getString("State").equals("Create")){
                    logo_image=savedInstanceState.getString("logo_image");
                    initial_image=savedInstanceState.getString("initial_image");
                    inCreateMode=true;
                    inCameraMode=false;
                    onEventCreate();
                }
            }
            catch (NullPointerException e){

            }

        }


    }

    public  void onSaveInstanceState(Bundle savedInstanceState){


        savedInstanceState.putSerializable("User", current_user);

        if(logo_image!=null){
            savedInstanceState.putString("logo_image",logo_image);
        }

        if(initial_image!=null){
            savedInstanceState.putString("initial_image",initial_image);
        }

        if(inCreateMode){
            savedInstanceState.putString("State","Create");
        }
        if(inCameraMode){
            savedInstanceState.putString("State", "Camera");
        }


    }

    protected  void onActivityResult(int requestCode, int resultCode,Intent data){
        String photo;
        switch(requestCode){

            case IMAGE_GETTER:
                if(data==null){
                    return;
                }
                Uri photoUri= data.getData();

                if(photoUri!=null){
                    photo= getRealPathFromURI(UserEventsActivity.this,photoUri);
                }
                else{
                    photo=data.getStringExtra("uri");
                }
                Intent intent = new Intent(UserEventsActivity.this,ImageCreateActivity.class);
                intent.putExtra("image",photo);
                intent.putExtra("current_user",current_user);
                startActivityForResult(intent,IMAGE_CREATE);
                break;
            case IMAGE_CREATE:
                photo = data.getStringExtra("image");

                try {
                    FileInputStream str = new FileInputStream(new File(photo));
                    Bitmap bitmap  = BitmapFactory.decodeStream(str);


                    if(which_view ==select_logo){
                        logo_image=photo;
                        logo.setImageBitmap(bitmap);
                        chosenDialog.hide();

                    }
                    else if(which_view==select_initial){
                        initial_image=photo;
                        initial.setImageBitmap(bitmap);
                        chosenDialog.hide();

                    }
                }
                catch(FileNotFoundException e){
                    Log.e("onAct",e.toString());
                }


        }







    }


    private void onRefreshEvents(JSONArray events, LinearLayout linear_layout) {

        if (current_user.events == null) {
            current_user.events = events.toString();
            return;
        }
        try {
            JSONArray events_ser = new JSONArray(current_user.events);
            for (int i = 0; i < events.length(); i++) {

                events_ser.put(events.getJSONObject(i));
                TextView evV = new TextView(this);
                evV.setText(events.getJSONObject(i).get("event_name").toString());
                linear_layout.addView(evV, 0);


            }
            current_user.events = events_ser.toString();
        } catch (JSONException e) {
            Log.e("Events", e.toString());
        }


    }


    private class on_callback_events implements ReloginBox.Callback{
        public Object success(ArrayList<Object> args,String token,Long expiration){
           return getUserEvents();

        }

        public Object failure(ArrayList<Object>args, String token,Long expiration){
            return null;
        }
    }

    private JSONArray getUserEvents() {

        JSONObject request =new JSONObject();

        if(get_all){
            try {
                request.put("return_all", "true");
            }
            catch (JSONException e){
                Log.e("Events",e.toString());
                return null;
            }
        }
        APICall getEvents = new APICall(getApplicationContext(), "POST", "/events/retrieve-users", request);
        utilities.refresh_token(UserEventsActivity.this,current_user.expiration_date,current_user.curr_token,getSharedPreferences("user_pref",MODE_WORLD_READABLE));
        getEvents.authenticate(current_user.curr_token,current_user.expiration_date);

        try {
            getEvents.connect();
        } catch (ConnectException e) {
            return null;
        }
        JSONArray response =null;
        switch (getEvents.getStatus()) {

            case 200:
                Log.d("Status", "Success");
                response = getEvents.getResponse();
                break;
            case 401:
                Log.e("Status", "denied");
                ReloginBox box = new ReloginBox(UserEventsActivity.this);
                box.show(new ArrayList<Object>(),new on_callback_events());
                box.Lock=true;
                while(box.isLocked()){

                }
                response=(JSONArray)box.get_return();

                break;

            default:
                return null;

        }


        return response;

    }




    private String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }







    private Dialog imageSourceSelect(final int code){
        final Dialog choosePicContent = new Dialog(UserEventsActivity.this);
        WindowManager.LayoutParams wmlp = choosePicContent.getWindow().getAttributes();
        wmlp.gravity = Gravity.BOTTOM;
        Display display = getWindowManager().getDefaultDisplay();
        int screenWidth = display.getWidth();

        wmlp.width=screenWidth;




        TextView title = new TextView(UserEventsActivity.this);
        title.setText(R.string.selectsource);
        title.setId(new Integer(1));
        title.setGravity(Gravity.TOP);
        title.setTextSize(15);
        title.setTextColor(Color.BLACK);
        RelativeLayout layout = new RelativeLayout(UserEventsActivity.this);
        RelativeLayout.LayoutParams textLayout =  new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        textLayout.addRule(RelativeLayout.CENTER_HORIZONTAL,RelativeLayout.TRUE);
        textLayout.addRule(RelativeLayout.ALIGN_PARENT_TOP,RelativeLayout.TRUE);

        layout.addView(title,textLayout );


        Intent gallIntent = new Intent(Intent.ACTION_GET_CONTENT);
        gallIntent.setType("image/*");


        final Intent camIntent = new Intent(UserEventsActivity.this,CameraActivity.class);
        camIntent.putExtra("User",current_user);
        camIntent.putExtra("NeedResult","NeedResult");

        final ArrayList<Intent> intents = new ArrayList<>();
        ArrayList<String> names = new ArrayList<>();
        ArrayList<Drawable> images = new ArrayList<>();


        intents.add(camIntent);
        names.add("Camera");
        images.add(getApplicationContext().getDrawable(R.drawable.ic_menu_camera));

        List<ResolveInfo> listGall = getApplicationContext().getPackageManager().queryIntentActivities(gallIntent, 0);
        for (ResolveInfo res : listGall) {


            final Intent finalIntent = new Intent(gallIntent);
            finalIntent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            String packageName =(String)res.activityInfo.loadLabel(getPackageManager());
            //String appname = packageName.substring(packageName.lastIndexOf('.')+1,packageName.length());

            intents.add(finalIntent);
            names.add(packageName);
            images.add(res.activityInfo.loadIcon(getPackageManager()));

        }

        GridView grid = new GridView(UserEventsActivity.this);

        IntentAdapter adapt = new IntentAdapter(UserEventsActivity.this,names,images);
        grid.setAdapter(adapt);
        grid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = intents.get(position);
                startActivityForResult(intent, IMAGE_GETTER);

                return false;
            }
        });
                grid.setNumColumns(3);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.BELOW,title.getId());
        layout.addView(grid, params);
        choosePicContent.addContentView(layout,new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,RelativeLayout.LayoutParams.FILL_PARENT));
        return choosePicContent;

    }




    public void onEventCreate() {
        if(myMenu!=null){
            myMenu.findItem(R.id.event_create).setVisible(false);

        }
        ActionBar ab = getSupportActionBar();
        ab.setTitle("Event Creation");
        ab.setDisplayHomeAsUpEnabled(true);


        if(inCreateMode){
            back_mode=create_back;
        }
        else if(inCameraMode){
            back_mode=camera_back;
        }

        if (parents != null) {
            for (View v : parents) {
                v.setVisibility(View.GONE);
            }
        }
        final EditText event_name = new EditText(this);
        final Button submit = new Button(this);
        children = new ArrayList<View>();
        children.add(event_name);
        children.add(submit);


        RelativeLayout.LayoutParams params_o = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params_o.addRule(RelativeLayout.CENTER_HORIZONTAL);
        event_name.setHint("Event Name");
        event_name.setId(new Integer(1));
        utilities.key_dis(event_name);



        Button selectLogo = new Button(this);
        Button selectInitalImage = new Button(this);
        selectLogo.setCompoundDrawables(null, null, getApplicationContext().getDrawable(R.drawable.ic_menu_camera), null);
        selectInitalImage.setCompoundDrawables(null, null, getApplicationContext().getDrawable(R.drawable.ic_menu_camera), null);
        selectLogo.setText("Event Logo");
        selectInitalImage.setText("Initial Image");
        selectInitalImage.setId(new Integer(3));
        selectLogo.setId(new Integer(2));
        children.add(selectInitalImage);
        children.add(selectLogo);

        logo = new ImageView(UserEventsActivity.this);
        initial = new ImageView(UserEventsActivity.this);
        children.add(logo);
        children.add(initial);

        if(logo_image!=null){
            try {
                FileInputStream str = new FileInputStream(new File(logo_image));
                Bitmap bitmap = BitmapFactory.decodeStream(str);
                logo.setImageBitmap(bitmap);
            }
            catch (FileNotFoundException e){

            }
        }
        if(initial_image!=null){
            try {
                FileInputStream str = new FileInputStream(new File(initial_image));
                Bitmap bitmap = BitmapFactory.decodeStream(str);
                initial.setImageBitmap(bitmap);
            }
            catch (FileNotFoundException e){

            }
        }

        logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog imgView = new Dialog(UserEventsActivity.this);
                ImageView img = new ImageView(UserEventsActivity.this);
                Bitmap bitmap = ((BitmapDrawable)logo.getDrawable()).getBitmap();
                img.setImageBitmap(bitmap);
                imgView.addContentView(img,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                imgView.show();
            }
        });

        initial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog imgView = new Dialog(UserEventsActivity.this);
                ImageView img = new ImageView(UserEventsActivity.this);
                Bitmap bitmap = ((BitmapDrawable)initial.getDrawable()).getBitmap();
                img.setImageBitmap(bitmap);
                imgView.addContentView(img, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                imgView.show();
            }
        });


        RelativeLayout.LayoutParams params_logo = new RelativeLayout.LayoutParams(120,120);
        params_logo.addRule(RelativeLayout.BELOW,event_name.getId());
        params_logo.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params_logo.addRule(RelativeLayout.RIGHT_OF, selectLogo.getId());

        RelativeLayout.LayoutParams params_initial = new RelativeLayout.LayoutParams(120,120);
        params_initial.addRule(RelativeLayout.BELOW, selectLogo.getId());
        params_initial.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params_initial.addRule(RelativeLayout.RIGHT_OF, selectInitalImage.getId());





        selectLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                which_view=select_logo;
                Dialog di =imageSourceSelect(1);
                chosenDialog =di;
                di.show();

            }
        });

        selectInitalImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                which_view=select_initial;
                Dialog di= imageSourceSelect(1);
                chosenDialog =di;
                di.show();

            }
        });


        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);


        main_layout.addView(event_name, params_o);
        params.addRule(RelativeLayout.BELOW, event_name.getId());
        main_layout.addView(selectLogo, params);
        RelativeLayout.LayoutParams params_L = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params_L.addRule(RelativeLayout.BELOW, selectLogo.getId());
        main_layout.addView(selectInitalImage, params_L);



        main_layout.addView(logo, params_logo);
        main_layout.addView(initial, params_initial);


        RelativeLayout.LayoutParams params_p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params_p.addRule(RelativeLayout.BELOW, selectInitalImage.getId());
        params_p.addRule(RelativeLayout.CENTER_HORIZONTAL);
        main_layout.addView(submit, params_p);
        submit.setText("Submit Event");
        submit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                did_submit = 1;
                String en =  event_name.getText().toString();
                if (en.equals("")) {
                    return;
                }
                try {
                    JSONObject request = new JSONObject();
                    request.put("event_name", en);

                    String logo_str=null;
                    String initial_str=null;
                    try {
                        File imageL = new File(logo_image);
                        FileInputStream fin = new FileInputStream(imageL);
                        DataInputStream dis = new DataInputStream(fin);
                        byte fileContent[] = new byte[(int) imageL.length()];
                        dis.readFully(fileContent);
                        logo_str=Base64.encodeToString(fileContent, Base64.DEFAULT);
                    }
                    catch (NullPointerException e){

                    }
                    catch (FileNotFoundException e){
                        Log.e("Events",e.toString());
                    }
                    catch (IOException e){
                        Log.e("Events",e.toString());
                    }

                    try {
                        File imageL = new File(initial_image);
                        FileInputStream fin = new FileInputStream(imageL);
                        DataInputStream dis = new DataInputStream(fin);
                        byte fileContent[] = new byte[(int) imageL.length()];
                        dis.readFully(fileContent);
                        initial_str=Base64.encodeToString(fileContent, Base64.DEFAULT);
                    }
                    catch (NullPointerException e){

                    }
                    catch (FileNotFoundException e){
                        Log.e("Events",e.toString());
                    }
                    catch (IOException e){
                        Log.e("Events",e.toString());
                    }

                    if(logo_str!=null){
                        request.put("logo_image",logo_str);
                    }
                    if(initial_str!=null){
                        request.put("initial_image",initial_str);
                    }


                    APICall eC = new APICall(getApplicationContext(), "POST", "/events/create", request);
                    utilities.refresh_token(UserEventsActivity.this,current_user.expiration_date,current_user.curr_token,getSharedPreferences("user_pref",MODE_WORLD_READABLE));
                    eC.authenticate(current_user.curr_token,current_user.expiration_date);
                    try {
                        eC.connect();
                    } catch (ConnectException e) {
                        return;
                    }
                    switch (eC.getStatus()) {
                        case 200:
                            Toast.makeText(UserEventsActivity.this, "Event Successfully Created", Toast.LENGTH_SHORT).show();
                            break;
                        case 400:
                            Toast.makeText(UserEventsActivity.this, "Bad Request", Toast.LENGTH_SHORT).show();
                            break;
                        case 401:

                            class CreateCallback implements ReloginBox.Callback{
                                public Object success(ArrayList<Object> args, String token,Long expiration){
                                    return null;
                                }
                                public Object failure(ArrayList<Object> args,String token,Long expiration){
                                    return null;
                                }
                            }
                            ReloginBox box = new ReloginBox(UserEventsActivity.this);
                            box.show(new ArrayList<Object>(), new CreateCallback());

                            break;
                        default:
                            break;

                    }
                } catch (JSONException e) {
                    Log.e("Events", e.toString());
                }
            }
        });
    }

    private void onHome(){
        if (back_mode == normal_back) {
            Intent back = new Intent(UserEventsActivity.this, UserPageActivity.class);
            back.putExtra("user", current_user);

            startActivity(back);
            finish();
        } else if (back_mode == camera_back) {
            if (did_submit == 0) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                finish();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(UserEventsActivity.this);
                builder.setMessage("Leave Event Creation?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();

            }
            else {

                finish();
            }
        } else if (back_mode == create_back) {

            inCreateMode=false;
            inCameraMode=false;
            if(did_submit==0) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                if (parents != null) {
                                    for (View v : parents) {
                                        v.setVisibility(View.VISIBLE);
                                    }
                                }
                                for (View v : children) {
                                    v.setVisibility(View.GONE);
                                }
                                back_mode = normal_back;

                                if (getIntent().getStringExtra("StayCreate") != null) {
                                    getIntent().removeExtra("StayCreate");
                                }
                                ActionBar ab = getSupportActionBar();
                                ab.setTitle(current_user.first_name + "\'s Events");
                                if (myMenu != null) {
                                    myMenu.findItem(R.id.event_create).setVisible(true);
                                }
                                logo_image = null;
                                initial_image = null;

                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(UserEventsActivity.this);
                builder.setMessage("Leave Event Creation?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();

            }

        }

    }

    @Override
    public boolean onKeyDown(int keyCode,KeyEvent event){
        switch (keyCode){
            case KeyEvent.KEYCODE_BACK:
                onHome();
                return true;
            default:
                super.onKeyDown(keyCode,event);
        }
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onHome();
                break;
            case R.id.event_create:
                inCreateMode=true;
                inCameraMode=false;
                onEventCreate();
                break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        myMenu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.event_view_menu, menu);
        if(getIntent().getStringExtra("Create")!=null||getIntent().getStringExtra("StayCreate")!=null){
            menu.findItem(R.id.event_create).setVisible(false);
        }



        return true;
    }


}
