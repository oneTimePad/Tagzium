package com.tagger.lie.tag_app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBarActivity;
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
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class UserPageActivity extends ActionBarActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    User current_user;

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

            JSONObject user_info = new JSONObject((String) fromLogSign.getExtras().get("response"));
            String token = (String)user_info.get("token");
            JSONObject user_json = (JSONObject)user_info.get("user");



            current_user = new User((String)user_json.get("username"),(String)user_json.get("email"),(String)user_json.get("first_name"),token);

        }
        catch (JSONException e){
            Log.e("UserPage", e.toString());
        }

    }


    public void onNewIntent(Intent newI){
        current_user = (User)newI.getExtras().get("user");

    }

    public  void onResume(){
        super.onResume();
        getSupportActionBar().setTitle(current_user.first_name);
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
        APICall logout = new APICall("POST","/users/logout/",new JSONObject());
        logout.authenticate(current_user.curr_token);
        logout.connect();
        switch(logout.getStatus()){

            case 200:
                Toast.makeText(UserPageActivity.this,"Successfully Logged Out",Toast.LENGTH_SHORT).show();
                break;
            case 401:
                Toast.makeText(UserPageActivity.this,"Session Expired",Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
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
