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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class LogSignActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);

        Intent fromMain = getIntent();
        String layout = (String)fromMain.getExtras().get("layout");

        if(layout.equals("signup")){
            setContentView(R.layout.sign_up);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("User Signup");
            final  EditText usr = (EditText)findViewById(R.id.username_in);
            final EditText pswd = (EditText)findViewById(R.id.password_in);
            final EditText pswd_c = (EditText)findViewById(R.id.password_confirm);
            final  EditText email = (EditText)findViewById(R.id.email_in);
            final EditText name = (EditText)findViewById(R.id.name_in);
            key_dis(usr);
            key_dis(pswd);
            key_dis(pswd_c);
            key_dis(email);
            key_dis(name);
        }
        else if(layout.equals("login")){
            setContentView(R.layout.log_in);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("User Login");
            final EditText usr = (EditText)findViewById(R.id.username_in);
            final EditText pswd = (EditText)findViewById(R.id.password_in);
            key_dis(usr);
            key_dis(pswd);
        }



    }

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
        new signUpConnect().connect();

    }


    public void login_confirm(View view){

        new logInConnect().connect();

    }


    private class signUpConnect extends HandlerThread {

        Handler mHandler;

        public signUpConnect(){
            super("signUpConnect");
            start();
            mHandler = new Handler(getLooper());

        }

        public void connect(){
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    String username = ((EditText)findViewById(R.id.username_in)).getText().toString();
                    String password = ((EditText)findViewById(R.id.password_in)).getText().toString();
                    String password_cf = ((EditText)findViewById(R.id.password_confirm)).getText().toString();
                    String email =  ((EditText)findViewById(R.id.email_in)).getText().toString();
                    String name =  ((EditText)findViewById(R.id.name_in)).getText().toString();

                    if(!password.equals(password_cf)){
                        Toast.makeText(LogSignActivity.this, "Passwords Don't Match", Toast.LENGTH_LONG).show();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((EditText) findViewById(R.id.password_in)).setText("");
                                ((EditText) findViewById(R.id.password_confirm)).setText("");
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



                        APICall signup = new APICall("POST","/auth/signup",request);
                        signup.connect();

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

        public void connect(){

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    String username = ((EditText)findViewById(R.id.username_in)).getText().toString();
                    String password = ((EditText)findViewById(R.id.password_in)).getText().toString();



                    try {



                        JSONObject request = new JSONObject();
                        request.put("username", username);
                        request.put("password", password);



                        APICall log = new APICall("POST","/auth/login",request);
                        log.connect();

                        Log.e("status",""+log.getStatus());
                        switch(log.getStatus()){
                            case 200:
                                Toast.makeText(LogSignActivity.this,"Login Successful",Toast.LENGTH_SHORT).show();
                                JSONArray response = log.getResponse();


                                Intent toUserPage = new Intent(LogSignActivity.this,UserPageActivity.class);
                                toUserPage.putExtra("response",response.getJSONObject(0).toString());

                                startActivity(toUserPage);
                                Log.e("finished","finished");
                                finish();
                                break;



                            case 400:
                                Toast.makeText(LogSignActivity.this,"Login Failed",Toast.LENGTH_SHORT).show();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ((EditText) findViewById(R.id.username_in)).setText("");
                                        ((EditText) findViewById(R.id.password_in)).setText("");
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
