package com.tagger.lie.tag_app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import android.os.Handler;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        SharedPreferences pref= this.getSharedPreferences("user_pref", MODE_WORLD_READABLE);
        String username = pref.getString("LastUser", null);
        String token = pref.getString("Token",null);

        if(username==null){
            return;
        }



        JSONObject request = new JSONObject();
        try {
            request.put("token", token);
        }
        catch (JSONException e){
            Log.e("Main",e.toString());
        }
        APICall call = new APICall(MainActivity.this,"POST","/auth/verify",request);


        try{
            call.connect();
        }
        catch (ConnectException e){

        }

        switch(call.getStatus()){
            case 200:

                Intent toUserPage = new Intent(MainActivity.this,UserPageActivity.class);

                toUserPage.putExtra("response", "start");


                startActivity(toUserPage);
                break;
            case 401:
            case 400:
                Toast.makeText(MainActivity.this, "Session Expired",Toast.LENGTH_SHORT).show();
                Intent toAct = new Intent(MainActivity.this,LogSignActivity.class);
                toAct.putExtra("layout", "login");
                startActivity(toAct);
                break;
        }
    }




    public void signup(View view){


        Intent toAct = new Intent(MainActivity.this,LogSignActivity.class);
        toAct.putExtra("layout","signup");
        startActivity(toAct);






    }

    public void login(View view){

        Intent toAct = new Intent(MainActivity.this,LogSignActivity.class);
        toAct.putExtra("layout","login");
        startActivity(toAct);

    }






}
