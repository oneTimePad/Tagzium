package com.tagger.lie.tag_app;

import android.app.Dialog;
import android.content.Context;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.util.ArrayList;

/**
 * Created by lie on 1/11/16.
 */
public class ReloginBox {

    Context ctx;
    boolean Lock = false;

    int log_count=0;
    Object return_val;

    public ReloginBox(Context ctx){
        this.ctx =  ctx;


    }


    public interface Callback {
        Object success(ArrayList<Object> args,String token,Long expiration);
        Object failure(ArrayList<Object> args,String token,Long expiration);
    }

    public synchronized boolean isLocked(){
        return Lock;
    }
    public Object get_return(){
        return return_val;
    }


    public void show(final ArrayList<Object> args,final Callback fct) {
        Lock=true;
        Toast.makeText(ctx, "Session Expired", Toast.LENGTH_SHORT).show();
        final Dialog relogin = new Dialog(ctx);
        relogin.setCancelable(false);
        relogin.setTitle("Please Login");
        final EditText username = new EditText(ctx);
        final EditText password = new EditText(ctx);
        username.setHint("username");
        password.setHint("password");
        username.setId(new Integer(1));
        password.setId(new Integer(2));
        Button submit = new Button(ctx);
        submit.setText("Login");
        final ArrayList<View> parents = new ArrayList<>();
        parents.add(username);
        parents.add(password);
        parents.add(submit);

        final ProgressBar prog = new ProgressBar(ctx);
        RelativeLayout.LayoutParams progparams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        progparams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
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

                            APICall call = new APICall(ctx, "POST", "/auth/login", request);
                            call.connect();
                            String token = null;
                            Long expiration = null;

                            switch (call.getStatus()) {
                                case 200:

                                    JSONArray response = call.getResponse();
                                    String token_string = response.getJSONObject(0).getString("token");
                                    String[] token_split = token_string.split("\\.");

                                    String token_decode = new String(Base64.decode(token_split[1].getBytes(), Base64.DEFAULT), "UTF-8");
                                    JSONObject payload = new JSONObject(token_decode);

                                    token = token_string;
                                    expiration = Long.parseLong(payload.getString("exp"));


                                    return_val =fct.success(args, token, expiration);

                                    relogin.hide();
                                    Lock=false;
                                    log_count = 0;
                                    break;
                                case 400:
                                    for (View view : parents) {
                                        view.setVisibility(View.VISIBLE);
                                    }
                                    prog.setVisibility(View.GONE);
                                    Toast.makeText(ctx, "Invalid Login", Toast.LENGTH_SHORT).show();
                                    log_count++;
                                    if (log_count == 3) {
                                        fct.failure(args, token, expiration);
                                        Lock=false;
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

        RelativeLayout rel_layout = new RelativeLayout(ctx);
        rel_layout.addView(username, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
        RelativeLayout.LayoutParams pass_params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        pass_params.addRule(RelativeLayout.BELOW, username.getId());
        rel_layout.addView(password, pass_params);
        RelativeLayout.LayoutParams submit_params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        submit_params.addRule(RelativeLayout.BELOW, password.getId());
        submit_params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        rel_layout.addView(submit, submit_params);
        rel_layout.addView(prog, progparams);
        RelativeLayout.LayoutParams layout_params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        layout_params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        relogin.addContentView(rel_layout, layout_params);
        relogin.show();


    }

}
