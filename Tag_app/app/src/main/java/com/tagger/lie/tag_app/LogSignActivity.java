package com.tagger.lie.tag_app;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class LogSignActivity extends ActionBarActivity {

    Utils utilities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);

        Intent fromMain = getIntent();
        String layout = (String)fromMain.getExtras().get("layout");

        utilities= (Utils)getApplication();

        if(layout.equals("signup")){
            setContentView(R.layout.sign_up);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("User Signup");
            final  EditText usr = (EditText)findViewById(R.id.username_in);
            final EditText pswd = (EditText)findViewById(R.id.password_in);
            final EditText pswd_c = (EditText)findViewById(R.id.password_confirm);
            final  EditText email = (EditText)findViewById(R.id.email_in);
            final EditText name = (EditText)findViewById(R.id.name_in);
            utilities.key_dis(usr);
            utilities.key_dis(pswd);
            utilities.key_dis(pswd_c);
            utilities.key_dis(email);
            utilities.key_dis(name);
        }
        else if(layout.equals("login")){
            setContentView(R.layout.log_in);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("User Login");
            final EditText usr = (EditText)findViewById(R.id.username_in);
            final EditText pswd = (EditText)findViewById(R.id.password_in);
            utilities.key_dis(usr);
            utilities.key_dis(pswd);
        }



    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

                //NavUtils.navigateUpFromSameTask(this);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void signup_confirm(View view){
        String username = ((EditText)findViewById(R.id.username_in)).getText().toString();
        String password = ((EditText)findViewById(R.id.password_in)).getText().toString();
        String password_cf = ((EditText)findViewById(R.id.password_confirm)).getText().toString();
        String email =  ((EditText)findViewById(R.id.email_in)).getText().toString();
        String name =  ((EditText)findViewById(R.id.name_in)).getText().toString();
        setContentView(R.layout.activity_loading_screen);
        new signUpConnect().connect(username,password,password_cf,email,name);

    }


    public void login_confirm(View view){
        String username = ((EditText)findViewById(R.id.username_in)).getText().toString();
        String password = ((EditText)findViewById(R.id.password_in)).getText().toString();
        setContentView(R.layout.activity_loading_screen);
        new logInConnect().connect(username,password);


    }


    private class signUpConnect extends HandlerThread {

        Handler mHandler;

        public signUpConnect(){
            super("signUpConnect");
            start();
            mHandler = new Handler(getLooper());

        }

        public void connect(final String username, final String password, final String password_cf,
                            final String email, final String name){
            mHandler.post(new Runnable() {
                @Override
                public void run() {


                    if(!password.equals(password_cf)){
                        Toast.makeText(LogSignActivity.this, "Passwords Don't Match", Toast.LENGTH_LONG).show();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((EditText) findViewById(R.id.password_in)).setText("");
                                ((EditText) findViewById(R.id.password_confirm)).setText("");
                                utilities.key_dis((EditText) findViewById(R.id.password_in));
                                utilities.key_dis((EditText)findViewById(R.id.password_confirm));
                                utilities.key_dis(((EditText)findViewById(R.id.name_in)));
                                utilities.key_dis(((EditText)findViewById(R.id.email_in)));
                                utilities.key_dis(((EditText)findViewById(R.id.username_in)));
                                setContentView(R.layout.sign_up);
                            }
                        });

                        return;
                    }

                    try {




                        JSONObject request = new JSONObject();
                        request.put("user", username);
                        request.put("password", password);
                        request.put("email", email);
                        request.put("name",name);



                        APICall signup = new APICall(getApplicationContext(),"POST","/auth/signup",request);
                        try {
                            signup.connect();
                        }
                        catch (ConnectException e){
                            return;
                        }
                        if(signup.getStatus()!= 200){
                            Log.e("SIGN UP",""+signup.getStatus());
                        }

                        JSONArray response = signup.getResponse();


                        if(response.getJSONObject(0).getString("Status").equals("Exists")){
                            Toast.makeText(LogSignActivity.this,"Username Already Exists",Toast.LENGTH_SHORT).show();
                        }
                        else if(response.getJSONObject(0).getString("Status").equals("Success")){
                            Toast.makeText(LogSignActivity.this,"User Successfully Created",Toast.LENGTH_SHORT).show();

                        }

                    }

                    catch(JSONException e){
                        Log.e("JSON",e.toString());
                    }

                }});

        }
    }


    private class logInConnect extends HandlerThread{

        Handler mHandler;

        public logInConnect(){
            super("logInConnect");
            start();
            mHandler = new Handler(getLooper());
        }

        public void connect(final String username,final String password){

            mHandler.post(new Runnable() {
                @Override
                public void run() {




                    try {



                        JSONObject request = new JSONObject();
                        request.put("username", username);
                        request.put("password", password);



                        APICall log = new APICall(getApplicationContext(),"POST","/auth/login",request);
                        try {
                            log.connect();
                        }
                        catch (ConnectException e){
                            return;
                        }

                        switch(log.getStatus()){
                            case 200:
                                Toast.makeText(LogSignActivity.this,"Login Successful",Toast.LENGTH_SHORT).show();
                                JSONArray response = log.getResponse();


                                Intent toUserPage = new Intent(LogSignActivity.this,UserPageActivity.class);
                                toUserPage.putExtra("response",response.getJSONObject(0).toString());
                                toUserPage.putExtra("username",request.getString("username"));

                                startActivity(toUserPage);

                                finish();
                                break;



                            case 400:
                                Toast.makeText(LogSignActivity.this,"Login Failed",Toast.LENGTH_SHORT).show();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        setContentView(R.layout.log_in);
                                        ((EditText) findViewById(R.id.username_in)).setText("");
                                        ((EditText) findViewById(R.id.password_in)).setText("");
                                        utilities.key_dis(((EditText) findViewById(R.id.username_in)));
                                        utilities.key_dis(((EditText)findViewById(R.id.password_in)));

                                    }
                                });
                                break;

                        }


                    }

                    catch(JSONException e){
                        Log.e("JSON","JSON Exception");
                    }


                }
            });

        }
    }
}
