package com.tagger.lie.tag_app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.concurrent.Callable;

public class UserPageActivity extends ActionBarActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    User current_user;
    SharedPreferences shared;
    int log_count = 0;
    String token=null;
    Long expiration=null;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_page);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Intent fromLogSign = getIntent();


        try {




            shared = this.getSharedPreferences("user_pref",MODE_WORLD_READABLE);
            if((fromLogSign.getExtras().getString("response").equals("start"))){
                token = shared.getString("Token",null);
                expiration = shared.getLong("Expiration",0);



            }
            else {

                JSONObject token_info = new JSONObject((String) fromLogSign.getExtras().get("response"));

                token = (String) token_info.get("token");

                String[] token_split = token.split("\\.");

                String token_decode = new String(Base64.decode(token_split[1].getBytes(), Base64.DEFAULT), "UTF-8");
                JSONObject payload = new JSONObject(token_decode);
                expiration=Long.parseLong(payload.getString("exp"));
            }

                APICall call = new APICall(UserPageActivity.this, "POST", "/users/get_user/", new JSONObject());
                call.authenticate(token, expiration);

                call.connect();
                switch (call.getStatus()) {

                    case 200:
                        JSONArray response = call.getResponse();
                        current_user = new User(
                                response.getJSONObject(0).getString("username"),
                                response.getJSONObject(0).getString("email"),
                                response.getJSONObject(0).getString("first_name"),
                                token,
                                expiration

                        );
                        shared.edit().clear();
                        shared.edit().apply();
                        shared.edit().putString("LastUser", current_user.username).commit();
                        shared.edit().putString("Token", current_user.curr_token).commit();
                        shared.edit().putLong("Expiration",current_user.expiration_date).commit();
                        break;
                    case 401:

                        Toast.makeText(UserPageActivity.this,"Session Expired",Toast.LENGTH_SHORT).show();
                        final Dialog relogin = new Dialog(UserPageActivity.this);
                        relogin.setCancelable(false);
                        relogin.setTitle("Please Login");
                        final EditText username = new EditText(UserPageActivity.this);
                        final EditText password = new EditText(UserPageActivity.this);
                        username.setHint("username");
                        password.setHint("password");
                        username.setId(new Integer(1));
                        password.setId(new Integer(2));
                        Button submit = new Button(UserPageActivity.this);
                        submit.setText("Login");
                        final ArrayList<View> parents = new ArrayList<>();
                        parents.add(username);
                        parents.add(password);
                        parents.add(submit);

                        final ProgressBar prog = new ProgressBar(UserPageActivity.this);
                        RelativeLayout.LayoutParams progparams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        progparams.addRule(RelativeLayout.CENTER_HORIZONTAL,RelativeLayout.TRUE);
                        prog.setVisibility(View.GONE);



                        submit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                for (View view : parents) {
                                    view.setVisibility(View.GONE);
                                }
                                prog.setVisibility(View.VISIBLE);
                                JSONObject request = new JSONObject();
                                try {
                                    if (!(username.getText().equals(""))) {
                                        request.put("username", username.getText());
                                        if (!(password.getText().equals(""))) {
                                            request.put("password", password.getText());

                                            APICall call = new APICall(UserPageActivity.this, "POST", "/auth/login", request);
                                            call.connect();


                                            switch (call.getStatus()) {
                                                case 200:

                                                    JSONArray response = call.getResponse();
                                                    String token_string = response.getJSONObject(0).getString("token");
                                                    String[] token_split = token_string.split("\\.");

                                                    String token_decode = new String(Base64.decode(token_split[1].getBytes(), Base64.DEFAULT), "UTF-8");
                                                    JSONObject payload = new JSONObject(token_decode);

                                                    token = token_string;
                                                    expiration = Long.parseLong(payload.getString("exp"));
                                                    call = new APICall(UserPageActivity.this, "POST", "/users/get_user/", new JSONObject());
                                                    call.authenticate(token, expiration);
                                                    call.connect();
                                                    switch (call.getStatus()) {
                                                        case 401:
                                                            Toast.makeText(UserPageActivity.this, "Error restoring session", Toast.LENGTH_SHORT).show();
                                                            logout();
                                                    }
                                                    Toast.makeText(UserPageActivity.this, "Session Restored", Toast.LENGTH_SHORT).show();


                                                    response = call.getResponse();
                                                    current_user = new User(
                                                            response.getJSONObject(0).getString("username"),
                                                            response.getJSONObject(0).getString("email"),
                                                            response.getJSONObject(0).getString("first_name"),
                                                            token,
                                                            expiration

                                                    );
                                                    shared.edit().clear();
                                                    shared.edit().apply();
                                                    shared.edit().putString("LastUser", current_user.username).commit();
                                                    shared.edit().putString("Token", current_user.curr_token).commit();
                                                    relogin.hide();
                                                    onResume();
                                                    log_count = 0;
                                                    break;
                                                case 400:
                                                    for (View view : parents) {
                                                        view.setVisibility(View.VISIBLE);
                                                    }
                                                    prog.setVisibility(View.GONE);
                                                    Toast.makeText(UserPageActivity.this, "Invalid Login", Toast.LENGTH_SHORT).show();
                                                    log_count++;
                                                    if (log_count == 3) {
                                                        logout();
                                                    }


                                                default:
                                                    Log.e("catch", "catch");
                                            }


                                        }
                                    }
                                    username.setText("");
                                    password.setText("");

                                } catch (UnsupportedEncodingException e) {

                                } catch (ConnectException e) {
                                    for (View view : parents) {
                                        view.setVisibility(View.VISIBLE);
                                    }
                                    prog.setVisibility(View.GONE);

                                } catch (JSONException e) {

                                }
                            }
                        });

                        RelativeLayout rel_layout = new RelativeLayout(UserPageActivity.this);
                        rel_layout.addView(username, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
                        RelativeLayout.LayoutParams pass_params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
                        pass_params.addRule(RelativeLayout.BELOW, username.getId());
                        rel_layout.addView(password, pass_params);
                        RelativeLayout.LayoutParams submit_params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
                        submit_params.addRule(RelativeLayout.BELOW, password.getId());
                        submit_params.addRule(RelativeLayout.CENTER_HORIZONTAL,RelativeLayout.TRUE);
                        rel_layout.addView(submit, submit_params);
                        rel_layout.addView(prog,progparams);
                        RelativeLayout.LayoutParams layout_params =new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
                        layout_params.addRule(RelativeLayout.CENTER_HORIZONTAL,RelativeLayout.TRUE);
                        relogin.addContentView(rel_layout,layout_params);
                        relogin.show();

                }







        }
        catch (ConnectException e){

        }
        catch(UnsupportedEncodingException e){
            Log.e("UserPage",e.toString());

        }
        catch (JSONException e){
            Log.e("UserPage", e.toString());
        }


    }


    public void onNewIntent(Intent newI){
        User new_user = (User)newI.getExtras().get("user");
        if(new_user!=null){
            current_user=new_user;
        }


    }
    @Override
    public  void onResume(){
        super.onResume();
        if(current_user!=null) {
            getSupportActionBar().setTitle(current_user.first_name);
        }
    }

    public void logout_alert(){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked

                        logout();

                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(UserPageActivity.this);
        builder.setMessage("Logout?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            logout_alert();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.user_page, menu);
        return true;
    }


    public void logout(){
        shared.edit().remove("LastUser").commit();
        shared.edit().remove("Token").commit();
        shared.edit().remove("Expiration").commit();

        /*
        APICall logout = new APICall(getApplicationContext(),"POST","/users/logout/",new JSONObject());
        logout.authenticate(current_user.curr_token);
        try {
            logout.connect();
        }
        catch(ConnectException e){
            Toast.makeText(UserPageActivity.this,"Logout Failed,Closing anyway",Toast.LENGTH_LONG).show();
            startActivity(new Intent(UserPageActivity.this, MainActivity.class));
            finish();
        }
        switch(logout.getStatus()){

            case 200:
                Toast.makeText(UserPageActivity.this,"Successfully Logged Out",Toast.LENGTH_SHORT).show();
                break;
            case 401:
                Toast.makeText(UserPageActivity.this,"Session Expired",Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }*/
        startActivity(new Intent(UserPageActivity.this,MainActivity.class));
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {

            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            Intent toCamera = new Intent(UserPageActivity.this,CameraActivity.class);
            toCamera.putExtra("User",current_user);
            startActivity(toCamera);

            // Handle the camera action
        } else if (id == R.id.nav_user_settings) {
            Intent toUserSettings = new Intent(UserPageActivity.this,UserSettingsActivity.class);
            toUserSettings.putExtra("User", current_user);
            startActivity(toUserSettings);


        } else if (id == R.id.nav_events) {
            Intent toUserEvents = new Intent(UserPageActivity.this,UserEventsActivity.class);
            toUserEvents.putExtra("User",current_user);
            startActivity(toUserEvents);


        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
