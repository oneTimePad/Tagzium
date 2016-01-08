package com.tagger.lie.tag_app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
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
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

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

    ImageView logo;
    ImageView initial;
    String logo_image;
    String initial_image;
    int whichView;

    int did_submit  = 0;

    Dialog chosenDialog;





    private void key_dis(final EditText edit) {

        edit.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                //on enter
                if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) && i == KeyEvent.KEYCODE_ENTER) {
                    InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    //hide the keyboard
                    mgr.hideSoftInputFromWindow(edit.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_events);
        Intent intent = getIntent();


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
                logo_image=intent.getStringExtra("image");
                inCreateMode=false;
                inCameraMode=true;
                onEventCreate();
                return;
            }

        }




        JSONArray events = null;
        try {

            if (current_user.events == null) {
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


        ActionBar ab = getSupportActionBar();
        ab.setTitle(current_user.first_name + "\'s Events");
        ab.setDisplayHomeAsUpEnabled(true);

        parents = new ArrayList<View>();
        final SwipeRefreshLayout sw = new SwipeRefreshLayout(this);
        parents.add(sw);
        ((RelativeLayout) findViewById(R.id.events)).addView(sw);
        ScrollView sc = new ScrollView(this);
        sw.addView(sc, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        final LinearLayout ly = new LinearLayout(this);
        ly.setOrientation(LinearLayout.VERTICAL);
        sc.addView(ly, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        sw.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                sw.setRefreshing(true);
                JSONArray events = getUserEvents();
                if (events == null) {
                    sw.setRefreshing(false);
                    return;
                }
                addToList(events, ly);
                sw.setRefreshing(false);


            }
        });


        if (events == null) {
            return;
        }
        for (int i = 0; i < events.length(); i++) {
            try {
                JSONObject js = events.getJSONObject(i);
                TextView evV = new TextView(this);
                evV.setText(js.get("event_name").toString());
                ly.addView(evV);
            } catch (JSONException e) {
                Log.e("Events", e.toString());
            }
        }
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


    public void addToList(JSONArray events, LinearLayout ly) {

        if (current_user.events == null) {
            current_user.events = events.toString();
            return;
        }
        try {
            JSONArray js = new JSONArray(current_user.events);
            for (int i = 0; i < events.length(); i++) {

                js.put(events.getJSONObject(i));
                TextView evV = new TextView(this);
                evV.setText(events.getJSONObject(i).get("event_name").toString());
                ly.addView(evV, 0);


            }
            current_user.events = js.toString();
        } catch (JSONException e) {
            Log.e("Events", e.toString());
        }


    }


    public JSONArray getUserEvents() {


        APICall getEvents = new APICall(getApplicationContext(), "POST", "/events/retrieve_users/", new JSONObject());
        getEvents.authenticate(current_user.curr_token);
        try {
            getEvents.connect();
        } catch (ConnectException e) {
            return null;
        }
        switch (getEvents.getStatus()) {

            case 200:
                Log.d("Status", "Success");
                break;
            case 400:
                return null;
            case 401:
                Log.e("Status", "denied");
                break;

            default:
                return null;

        }

        JSONArray response = getEvents.getResponse();
        return response;

    }




    public String getRealPathFromURI(Context context, Uri contentUri) {
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




    protected  void onActivityResult(int requestCode, int resultCode,Intent data){
        Uri photoUri= data.getData();//.substring(data.getDataString().indexOf('/'),data.getDataString().length());
        String photo;
        if(photoUri!=null){
            photo= getRealPathFromURI(UserEventsActivity.this,photoUri);
        }
        else{
            photo=data.getStringExtra("uri");
        }



        try {
            FileInputStream str = new FileInputStream(new File(photo));
            Bitmap bitmap  = BitmapFactory.decodeStream(str);


            if(whichView ==1){
                logo_image=photo;
                logo.setImageBitmap(bitmap);
                chosenDialog.hide();

            }
            else if(whichView==2){
                initial_image=photo
                initial.setImageBitmap(bitmap);
                chosenDialog.hide();

            }
        }
        catch(FileNotFoundException e){
            Log.e("onAct",e.toString());
        }





    }


    private Dialog imageSourceSelect(final int code){
        final Dialog choosePicContent = new Dialog(UserEventsActivity.this);
        ScrollView scrollContent = new ScrollView(UserEventsActivity.this);
        choosePicContent.addContentView(scrollContent, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        LinearLayout scrollLayout = new LinearLayout(UserEventsActivity.this);
        scrollContent.addView(scrollLayout);
        scrollLayout.setOrientation(LinearLayout.VERTICAL);

        TextView title = new TextView(UserEventsActivity.this);
        title.setText(R.string.selectsource);
        scrollLayout.addView(title);
        Intent gallIntent = new Intent(Intent.ACTION_GET_CONTENT);
        gallIntent.setType("image/*");

        final Intent camIntent = new Intent(UserEventsActivity.this,CameraActivity.class);

        ImageButton camButton = new ImageButton(UserEventsActivity.this);
        camButton.setImageDrawable(getApplicationContext().getDrawable(R.drawable.ic_menu_camera));
        camButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camIntent.putExtra("User",current_user);

                startActivityForResult(camIntent, code);
            }
        });
        scrollLayout.addView(camButton);

        List<ResolveInfo> listGall = getApplicationContext().getPackageManager().queryIntentActivities(gallIntent, 0);
        for (ResolveInfo res : listGall) {


            final Intent finalIntent = new Intent(gallIntent);
            finalIntent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));

            ImageButton iconButton = new ImageButton(UserEventsActivity.this);

            iconButton.setImageDrawable(res.activityInfo.loadIcon(getPackageManager()));
            iconButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    startActivityForResult(finalIntent,code);
                }
            });
            scrollLayout.addView(iconButton);

        }

        return choosePicContent;

    }




    public void onEventCreate() {
        Log.e("lol","lol");
        if(myMenu!=null){
            myMenu.findItem(R.id.event_create).setVisible(false);

        }
        ActionBar ab = getSupportActionBar();
        ab.setTitle("Event Creation");
        ab.setDisplayHomeAsUpEnabled(true);


        back = 0;
        if (image != null) {
            back = 2;
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
        key_dis(event_name);


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

        if(Image.bit1!=null){
            logo.setImageBitmap(Image.bit1);
        }
        if(Image.bit2!=null){
            initial.setImageBitmap(Image.bit2);
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
                whichPic=1;
                Dialog di =imageSourceSelect(1);
                chosenDialog =di;
                di.show();

            }
        });

        selectInitalImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whichPic=2;
                Dialog di= imageSourceSelect(1);
                chosenDialog =di;
                di.show();

            }
        });


        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);


        ((RelativeLayout) findViewById(R.id.events)).addView(event_name, params_o);
        params.addRule(RelativeLayout.BELOW, event_name.getId());
        ((RelativeLayout) findViewById(R.id.events)).addView(selectLogo, params);
        RelativeLayout.LayoutParams params_L = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params_L.addRule(RelativeLayout.BELOW, selectLogo.getId());
        ((RelativeLayout) findViewById(R.id.events)).addView(selectInitalImage, params_L);


        if(image!=null){
            byte[] decodedString = Base64.decode(image, Base64.NO_WRAP);
            InputStream inputStream  = new ByteArrayInputStream(decodedString);
            Bitmap bitmap  = BitmapFactory.decodeStream(inputStream);
            initial.setImageBitmap(bitmap);
        }
        ((RelativeLayout)findViewById(R.id.events)).addView(logo,params_logo);
        ((RelativeLayout)findViewById(R.id.events)).addView(initial,params_initial);


        RelativeLayout.LayoutParams params_p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params_p.addRule(RelativeLayout.BELOW, selectInitalImage.getId());
        params_p.addRule(RelativeLayout.CENTER_HORIZONTAL);
        ((RelativeLayout) findViewById(R.id.events)).addView(submit, params_p);
        submit.setText("Submit Event");
        submit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                did_submit = 1;
                String en = (String) event_name.getText().toString();
                if (en.equals("")) {
                    return;
                }
                try {
                    JSONObject request = new JSONObject();
                    request.put("event_name", en);

                    APICall eC = new APICall(getApplicationContext(), "POST", "/events/creates/", request);
                    eC.authenticate(current_user.curr_token);
                    eC.authenticate(current_user.curr_token);
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
                            Toast.makeText(UserEventsActivity.this, "Session Expired", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(UserEventsActivity.this, MainActivity.class));
                            finish();
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

    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:

                if (back == 1) {
                    Image.image = null;
                    Image.bit1=null;
                    Image.bit2=null;
                    Intent back = new Intent(UserEventsActivity.this, UserPageActivity.class);
                    back.putExtra("user", current_user);

                    startActivity(back);
                    finish();
                } else if (back == 2) {
                    if (did_submit == 0) {
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        //Yes button clicked
                                        Image.image = null;
                                        Image.bit1=null;
                                        Image.bit2=null;
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
                        Image.image = null;
                        finish();
                    }
                } else if (back == 0) {


                    if(did_submit==0){
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        //Yes button clicked
                                        Image.image = null;
                                        Image.bit1=null;
                                        Image.bit2=null;
                                        if (parents != null) {
                                            for (View v : parents) {
                                                v.setVisibility(View.VISIBLE);
                                            }
                                        }
                                        for (View v : children) {
                                            v.setVisibility(View.GONE);
                                        }
                                        back = 1;

                                        if(getIntent().getStringExtra("StayCreate")!=null){
                                            getIntent().removeExtra("StayCreate");
                                        }
                                        ActionBar ab = getSupportActionBar();
                                        ab.setTitle(current_user.first_name + "\'s Events");
                                        if(myMenu!=null) {
                                            myMenu.findItem(R.id.event_create).setVisible(true);
                                        }

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
                    return true;


                }
                break;
            case R.id.event_create:
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
