package com.tagger.lie.tag_app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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
import java.io.UnsupportedEncodingException;
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
        Long expiration = pref.getLong("Expiration",0);


        if(username==null){

            return;
        }




        Long unixtime=System.currentTimeMillis()/1000;

        if(expiration-unixtime<=300){
            try {
                JSONObject request_refresh = new JSONObject();
                request_refresh.put("token", token);
                APICall call_refresh = new APICall(MainActivity.this, "POST", "/auth/refresh",request_refresh );
                call_refresh.connect();
                switch (call_refresh.getStatus()){
                    case 200:
                        JSONArray response_refresh = call_refresh.getResponse();

                        token = response_refresh.getJSONObject(0).getString("token");

                        String[] token_split = token.split("\\.");

                        String token_decode = new String(Base64.decode(token_split[1].getBytes(), Base64.DEFAULT), "UTF-8");
                        JSONObject payload = new JSONObject(token_decode);
                        expiration=Long.parseLong(payload.getString("exp"));

                        pref.edit().putString("Token", token);
                        pref.edit().putLong("Expiration",expiration);
                    default:
                        break;
                }

            }

            catch (ConnectException e){

            }
            catch (UnsupportedEncodingException e){
                Log.e("Main",e.toString());
            }
            catch (JSONException e){
                Log.e("Main",e.toString());
            }
        }


        Intent toUserPage = new Intent(MainActivity.this,UserPageActivity.class);

        toUserPage.putExtra("response", "start");

        startActivity(toUserPage);



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
