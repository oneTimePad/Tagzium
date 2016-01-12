package com.tagger.lie.tag_app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.ConnectException;

/**
 * Created by lie on 1/11/16.
 */
public class Utils extends Application {

    public void key_dis(final EditText edit) {

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

    public void refresh_token(Context ctx, Long expiration,String token,SharedPreferences pref){
        Long unixtime=System.currentTimeMillis()/1000;

        if(expiration-unixtime<=300){
            try {
                JSONObject request_refresh = new JSONObject();
                request_refresh.put("token", token);
                APICall call_refresh = new APICall(ctx, "POST", "/auth/refresh",request_refresh );
                call_refresh.tryOnce();
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
                Log.e("Main", e.toString());
            }
            catch (JSONException e){
                Log.e("Main",e.toString());
            }
        }
    }
}
