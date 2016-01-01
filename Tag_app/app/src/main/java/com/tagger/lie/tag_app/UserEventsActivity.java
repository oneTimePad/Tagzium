package com.tagger.lie.tag_app;

import android.content.Intent;
import android.speech.tts.TextToSpeechService;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class UserEventsActivity extends ActionBarActivity {

    User current_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_events);
        Intent fromUserPage = getIntent();
        current_user = (User)fromUserPage.getExtras().get("User");


        ActionBar ab = getSupportActionBar();
        ab.setTitle(current_user.first_name+"\'s Events");
        ab.setDisplayHomeAsUpEnabled(true);


    }

    public void onCreate(View v){
        EditText event_name = (EditText)findViewById(R.id.event_name);
        String en = (String)event_name.getText().toString();
        if(en.equals("")){
           return;
        }
        try {
            JSONObject request = new JSONObject();
            request.put("event_name", en);

            APICall eC = new APICall("POST", "/events/create/", request);
            eC.authenticate(current_user.curr_token);
            eC.connect();

            switch(eC.getStatus()){
                case 200:
                    Toast.makeText(UserEventsActivity.this, "Event Successfully Created", Toast.LENGTH_SHORT).show();
                    break;
                case 401:
                    Toast.makeText(UserEventsActivity.this, "Session Expired", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(UserEventsActivity.this, MainActivity.class));
                    finish();
                    break;
                default:
                    break;

            }
        }
        catch (JSONException e){
            Log.e("Events",e.toString() );
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return true;
    }


}
