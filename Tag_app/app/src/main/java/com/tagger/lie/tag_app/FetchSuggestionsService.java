package com.tagger.lie.tag_app;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.ConnectException;

/**
 * Created by lie on 1/23/16.
 */
public class FetchSuggestionsService extends Service {


    private Context mContext;
    private int METHOD_NAME =1;
    private final String METHOD = "method";
    private final int GET_SEARCH_SUGGESTION = 1;
    private Utils utilities;
    private User current_user;
    private String msearch_tag;

    @Override
    public void onCreate(){
        super.onCreate();
        mContext = this;
        utilities = (Utils)getApplication();


    }

    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

    @Override
    public void onStart(Intent intent,int startId){
        try{
            METHOD_NAME = intent.getIntExtra(METHOD, 1);
            msearch_tag = intent.getStringExtra("search_tag");
            current_user = (User)intent.getExtras().get("current_user");

            LoadSuggestions sugg = new LoadSuggestions();
            sugg.execute();

        }
        catch (NullPointerException e){
            e.printStackTrace();
        }
        super.onStart(intent,startId);
    }

    public class LoadSuggestions extends AsyncTask<Void,Void,Void>{

        JSONArray response;

        @Override
        protected Void doInBackground(Void...params){

            try{

                JSONObject request = new JSONObject();
                request.put("search_tag",msearch_tag);
                switch (METHOD_NAME) {
                    case GET_SEARCH_SUGGESTION:
                        APICall get_suggestions = new APICall(getApplicationContext(), "POST", "/events/retrieve-users", request);
                        utilities.refresh_token(mContext, current_user.expiration_date, current_user.curr_token, getSharedPreferences("user_pref", MODE_WORLD_READABLE));
                        get_suggestions.authenticate(current_user.curr_token, current_user.expiration_date);

                        try {
                            get_suggestions.connect();
                        } catch (ConnectException e) {
                            return null;
                        }
                        switch (get_suggestions.getStatus()) {

                            case 200:
                                Log.d("Status", "Success");
                                response = get_suggestions.getResponse();
                        }
                        break;
                }
            }
            catch (JSONException e){
                    e.printStackTrace();
            }
            return null;

        }

        @Override
        protected void onPostExecute(Void result){

            try {
                switch (METHOD_NAME){
                    case GET_SEARCH_SUGGESTION:
                        sendBroadcast(new Intent(METHOD).putExtra("Response",response.toString()));
                        break;
                }
            }
            catch (NullPointerException e){
                e.printStackTrace();
            }



        }


    }



}
