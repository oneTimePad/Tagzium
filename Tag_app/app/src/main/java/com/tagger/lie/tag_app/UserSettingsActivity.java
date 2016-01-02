package com.tagger.lie.tag_app;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NavUtils;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class UserSettingsActivity extends ActionBarActivity {

    Menu myMenu;
    private User user;
    ArrayList<View> curr_vg;
    ArrayList<View> last_bt;
    int set_from =1;



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
        ActionBar ab = getSupportActionBar();
        ab.setTitle("User Settings");
        ab.setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_user_settings);
        Intent fromUserPage = getIntent();
        user = (User)fromUserPage.getExtras().get("User");



        curr_vg = new ArrayList<View>();
        last_bt = new ArrayList<View>();


        final Button change_pass =(Button)findViewById(R.id.change_password);
        final Button change_username = (Button)findViewById(R.id.change_username);
        final Button change_email = (Button)findViewById(R.id.change_email);
        final Button change_name = (Button)findViewById(R.id.change_name);
        last_bt.add(change_pass);
        last_bt.add(change_username);
        last_bt.add(change_email);
        last_bt.add(change_name);
        change_pass.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                set_from = 0;
                //MenuInflater inflater = getMenuInflater();
                //inflater.inflate(R.menu.settings_back_button, myMenu);
                RelativeLayout.LayoutParams params_o = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);


                params_o.addRule(RelativeLayout.CENTER_HORIZONTAL);

                for (View v : last_bt) {
                    v.setVisibility(View.GONE);
                }

                final EditText old_Pass = new EditText(UserSettingsActivity.this);
                old_Pass.setHint("Old Password");
                old_Pass.setId(new Integer(1));

                RelativeLayout.LayoutParams params_p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);


                final EditText new_Pass = new EditText(UserSettingsActivity.this);
                new_Pass.setHint("New Password");
                new_Pass.setId(new Integer(2));
                params_p.addRule(RelativeLayout.BELOW, old_Pass.getId());
                params_p.addRule(RelativeLayout.CENTER_HORIZONTAL);


                RelativeLayout.LayoutParams params_c = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);


                final EditText new_Pass_cf = new EditText(UserSettingsActivity.this);

                new_Pass_cf.setHint("Confirm New Password");
                new_Pass_cf.setId(new Integer(3));
                params_c.addRule(RelativeLayout.BELOW, new_Pass.getId());
                params_c.addRule(RelativeLayout.CENTER_HORIZONTAL);


                ((RelativeLayout) findViewById(R.id.settings)).addView(old_Pass, params_o);
                ((RelativeLayout) findViewById(R.id.settings)).addView(new_Pass, params_p);
                ((RelativeLayout) findViewById(R.id.settings)).addView(new_Pass_cf, params_c);


                Button submit = new Button(UserSettingsActivity.this);


                RelativeLayout.LayoutParams params_b = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);


                params_b.addRule(RelativeLayout.BELOW, new_Pass_cf.getId());
                params_b.addRule(RelativeLayout.CENTER_HORIZONTAL);


                submit.setText("Set Password");

                ((RelativeLayout) findViewById(R.id.settings)).addView(submit, params_b);
                key_dis(old_Pass);
                key_dis(new_Pass);
                key_dis(new_Pass_cf);

                curr_vg.add(old_Pass);
                curr_vg.add(new_Pass);
                curr_vg.add(new_Pass_cf);
                curr_vg.add(submit);
                submit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (!new_Pass.getText().toString().equals(new_Pass_cf.getText().toString())) {
                            Toast.makeText(UserSettingsActivity.this, "passwords don't match", Toast.LENGTH_SHORT).show();
                            new_Pass.setText("");
                            new_Pass_cf.setText("");
                            return;
                        }
                        JSONObject request = new JSONObject();
                        try {

                            request.put("old_password", old_Pass.getText().toString());
                            request.put("new_password", new_Pass.getText().toString());
                            APICall chg = new APICall("POST", "/users/change_password/", request);
                            chg.authenticate(user.curr_token);
                            chg.connect();
                            int status = chg.getStatus();
                            switch (status) {

                                case 200:
                                    Log.e("change_password", "Success");
                                    Toast.makeText(UserSettingsActivity.this, "Password Successfully Changed", Toast.LENGTH_SHORT).show();
                                    break;
                                case 401:
                                    Toast.makeText(UserSettingsActivity.this, "Session Expired", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(UserSettingsActivity.this, MainActivity.class));
                                    finish(); Log.e("change_password", "Invalid authentication");

                                    break;
                                case 400:
                                    Log.e("change_password", "Invalid old password");
                                    Toast.makeText(UserSettingsActivity.this, "Invalid Old Password", Toast.LENGTH_SHORT).show();
                                    break;

                                default:
                                    Log.e("Status", "" + status);
                            }
                        } catch (JSONException e) {
                            Log.e("change password", e.toString());
                        }
                    }
                });
            }
        });


            change_username.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    set_from=0;
                    //MenuInflater inflater = getMenuInflater();
                    //inflater.inflate(R.menu.settings_back_button, myMenu);
                    RelativeLayout.LayoutParams params_o = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);


                    params_o.addRule(RelativeLayout.CENTER_HORIZONTAL);

                    for(View v: last_bt){
                        v.setVisibility(View.GONE);
                    }
                    final EditText new_user = new EditText(UserSettingsActivity.this);
                    new_user.setHint("New Username");
                    new_user.setId(new Integer(1));




                    RelativeLayout.LayoutParams params_b = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params_b.addRule(RelativeLayout.BELOW, new_user.getId());
                    params_b.addRule(RelativeLayout.CENTER_HORIZONTAL);

                    Button submit = new Button(UserSettingsActivity.this);
                    submit.setText("Set Username");

                    ((RelativeLayout)findViewById(R.id.settings)).addView(new_user,params_o);
                    ((RelativeLayout) findViewById(R.id.settings)).addView(submit, params_b);
                    key_dis(new_user);

                    curr_vg.add(new_user);
                    curr_vg.add(submit);
                    submit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {


                            JSONObject request = new JSONObject();
                            try {

                                request.put("new_username", new_user.getText().toString());

                                APICall chg = new APICall("POST", "/users/change_username/", request);
                                chg.authenticate(user.curr_token);
                                chg.connect();
                                int status = chg.getStatus();
                                switch (status) {

                                    case 200:

                                        Log.e("change_username", "Success");
                                        Toast.makeText(UserSettingsActivity.this, "Username Successfully Changed", Toast.LENGTH_SHORT).show();
                                        break;
                                    case 401:
                                        Log.e("change_username", "Invalid authentication");
                                        Toast.makeText(UserSettingsActivity.this, "Session Expired", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(UserSettingsActivity.this, MainActivity.class));
                                        finish();
                                        break;


                                    default:
                                        Log.e("Status", "" + status);
                                }
                            } catch (JSONException e) {
                                Log.e("change username", e.toString());
                            }
                        }
                    });
                }
            });

        change_email.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                set_from=0;
                //MenuInflater inflater = getMenuInflater();
                //inflater.inflate(R.menu.settings_back_button, myMenu);
                RelativeLayout.LayoutParams params_o = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);


                params_o.addRule(RelativeLayout.CENTER_HORIZONTAL);

                for(View v: last_bt){
                    v.setVisibility(View.GONE);
                }
                final EditText new_email = new EditText(UserSettingsActivity.this);
                new_email.setHint("New Email");
                new_email.setId(new Integer(1));




                RelativeLayout.LayoutParams params_b = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params_b.addRule(RelativeLayout.BELOW, new_email.getId());
                params_b.addRule(RelativeLayout.CENTER_HORIZONTAL);

                Button submit = new Button(UserSettingsActivity.this);
                submit.setText("Set Email");

                ((RelativeLayout)findViewById(R.id.settings)).addView(new_email,params_o);
                ((RelativeLayout) findViewById(R.id.settings)).addView(submit, params_b);
                key_dis(new_email);

                curr_vg.add(new_email);
                curr_vg.add(submit);
                submit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        JSONObject request = new JSONObject();
                        try {

                            request.put("new_email", new_email.getText().toString());

                            APICall chg = new APICall("POST", "/users/change_email/", request);
                            chg.authenticate(user.curr_token);
                            chg.connect();
                            int status = chg.getStatus();
                            switch (status) {

                                case 200:
                                    user.email=(String)request.get("new_email");
                                    Log.e("change_email", "Success");
                                    Toast.makeText(UserSettingsActivity.this, "Email Successfully Changed", Toast.LENGTH_SHORT).show();
                                    break;
                                case 401:
                                    Log.e("change_email", "Invalid authentication");
                                    Toast.makeText(UserSettingsActivity.this, "Session Expired", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(UserSettingsActivity.this, MainActivity.class));
                                    finish();
                                    break;


                                default:
                                    Log.e("Status", "" + status);
                            }
                        } catch (JSONException e) {
                            Log.e("change email", e.toString());
                        }
                    }
                });
            }
        });

        change_name.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                set_from=0;
                //MenuInflater inflater = getMenuInflater();
                //inflater.inflate(R.menu.settings_back_button, myMenu);
                RelativeLayout.LayoutParams params_o = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);


                params_o.addRule(RelativeLayout.CENTER_HORIZONTAL);

                for(View v: last_bt){
                    v.setVisibility(View.GONE);
                }
                final EditText new_name = new EditText(UserSettingsActivity.this);
                new_name.setHint("New Name");
                new_name.setId(new Integer(1));




                RelativeLayout.LayoutParams params_b = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params_b.addRule(RelativeLayout.BELOW, new_name.getId());
                params_b.addRule(RelativeLayout.CENTER_HORIZONTAL);

                Button submit = new Button(UserSettingsActivity.this);
                submit.setText("Set Name");

                ((RelativeLayout)findViewById(R.id.settings)).addView(new_name,params_o);
                ((RelativeLayout) findViewById(R.id.settings)).addView(submit, params_b);
                key_dis(new_name);

                curr_vg.add(new_name);
                curr_vg.add(submit);
                submit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        JSONObject request = new JSONObject();
                        try {

                            request.put("new_name", new_name.getText().toString());

                            APICall chg = new APICall("POST", "/users/change_name/", request);
                            chg.authenticate(user.curr_token);
                            chg.connect();
                            int status = chg.getStatus();
                            switch (status) {

                                case 200:
                                    user.first_name=(String)request.get("new_name");
                                    Log.e("change_name", "Success");
                                    Toast.makeText(UserSettingsActivity.this, "Name Successfully Changed", Toast.LENGTH_SHORT).show();
                                    break;
                                case 401:
                                    Log.e("change_name", "Invalid authentication");
                                    Toast.makeText(UserSettingsActivity.this, "Session Expired", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(UserSettingsActivity.this, MainActivity.class));
                                    finish();
                                    break;


                                default:
                                    Log.e("Status", "" + status);
                            }
                        } catch (JSONException e) {
                            Log.e("change name", e.toString());
                        }
                    }
                });
            }
        });



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        myMenu =menu;

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){

        switch(item.getItemId()){
            case android.R.id.home:

                if(set_from==1) {
                    Intent back = new Intent(UserSettingsActivity.this,UserPageActivity.class);
                    back.putExtra("user",user);
                    startActivity(back);
                    finish();
                }
                else if(set_from==0){
                    for(View v: curr_vg){
                        v.setVisibility(View.GONE);
                    }
                    for(View v: last_bt){
                        v.setVisibility(View.VISIBLE);
                    }
                    set_from=1;
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);


        }

    }






}
