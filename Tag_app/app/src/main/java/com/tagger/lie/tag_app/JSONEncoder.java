package com.tagger.lie.tag_app;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by lie on 12/30/15.
 */
public class JSONEncoder {
    JSONObject response;

    public JSONEncoder(InputStream iStr) throws JSONException,IOException{

        BufferedReader r = new BufferedReader(new InputStreamReader(iStr));
        StringBuilder result = new StringBuilder();
        String line;
        try {
            while ((line = r.readLine()) != null) {
                result.append(line);
            }


            response = new JSONObject(result.toString());
        }
        catch (JSONException e){
            Log.e("JSON",e.toString());
        }
        catch (IOException e){
            Log.e("JSON",e.toString());
        }
    }

    public JSONObject encode(){
        return response;
    }


}
