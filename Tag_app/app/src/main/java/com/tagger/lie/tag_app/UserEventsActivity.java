package com.tagger.lie.tag_app;

import android.content.Context;
import android.content.Intent;
import android.speech.tts.TextToSpeechService;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class UserEventsActivity extends ActionBarActivity {

    User current_user;
    ArrayList<View> children;
    ArrayList<View> parents;
    int back =1;
    Menu myMenu;



    private void key_dis(final EditText edit){

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
        Intent fromUserPage = getIntent();
        current_user = (User)fromUserPage.getExtras().get("User");
        JSONArray events = null;
        try {

            if(current_user.events==null){
                events = getUserEvents();
                current_user.events = events.toString();
            }
            else{
                events = new JSONArray(current_user.events);
            }
        }
        catch(JSONException e ){
            Log.e("Event",e.toString());
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
        final LinearLayout ly =new LinearLayout(this);
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
                addToList(events,ly);
                sw.setRefreshing(false);


            }
        });




        if(events == null){
            return;
        }
        for(int i=0;i<events.length();i++){
            try {
                JSONObject js = events.getJSONObject(i);
                TextView evV = new TextView(this);
                evV.setText(js.get("event_name").toString());
                ly.addView(evV);
            }
            catch(JSONException e){
                Log.e("Events",e.toString());
            }
        }




    }



    public void addToList(JSONArray events,LinearLayout ly){

        if(current_user.events == null){
            current_user.events=events.toString();
            return;
        }
        try {
            JSONArray js = new JSONArray(current_user.events);
            for (int i = 0; i < events.length(); i++) {

                js.put(events.getJSONObject(i));
                TextView evV = new TextView(this);
                evV.setText(events.getJSONObject(i).get("event_name").toString());
                ly.addView(evV,0);


            }
            current_user.events = js.toString();
        }
        catch(JSONException e){
            Log.e("Events", e.toString());
        }






    }


    public JSONArray getUserEvents(){


        APICall getEvents = new APICall("POST","/events/retrieve_users/",new JSONObject());
        getEvents.authenticate(current_user.curr_token);
        getEvents.connect();

        switch (getEvents.getStatus()){

            case 200:
                Log.d("Status", "Success");
                break;
            case 400:
                return null;
            case 401:
                Log.e("Status","denied");
                break;

            default:
                return null;

        }

        JSONArray response = getEvents.getResponse();
        return response;

    }


    public void onEventCreate() {
        myMenu.findItem(R.id.event_create).setVisible(false);
        ActionBar ab = getSupportActionBar();
        ab.setTitle("Event Creation");


        back = 0;
        if(parents!=null) {
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

        RelativeLayout.LayoutParams params_p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params_p.addRule(RelativeLayout.BELOW, event_name.getId());
        params_p.addRule(RelativeLayout.CENTER_HORIZONTAL);

        ((RelativeLayout) findViewById(R.id.events)).addView(event_name, params_o);
        ((RelativeLayout) findViewById(R.id.events)).addView(submit, params_p);
        submit.setText("Submit Event");
        submit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {


                String en = (String) event_name.getText().toString();
                if (en.equals("")) {
                    return;
                }
                try {
                    JSONObject request = new JSONObject();
                    request.put("event_name", en);

                    APICall eC = new APICall("POST", "/events/creates/", request);
                    eC.authenticate(current_user.curr_token);
                    eC.connect();

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
                if(back==1) {
                    Intent back = new Intent(UserEventsActivity.this,UserPageActivity.class);
                    back.putExtra("user",current_user);

                    startActivity(back);
                    finish();
                }
                else if(back==0){
                    if(parents!=null) {
                        for (View v : parents) {
                            v.setVisibility(View.VISIBLE);
                        }
                    }
                    for(View v: children){
                        v.setVisibility(View.GONE);
                    }
                    back =1;
                    ActionBar ab = getSupportActionBar();
                    ab.setTitle(current_user.first_name + "\'s Events");
                    myMenu.findItem(R.id.event_create).setVisible(true);
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

        return true;
    }

}
