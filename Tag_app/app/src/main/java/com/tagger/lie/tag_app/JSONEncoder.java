package com.tagger.lie.tag_app;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

/**
 * Created by lie on 12/30/15.
 */
public class JSONEncoder {
    JSONArray response;

    public JSONEncoder(InputStream iStr) throws JSONException,IOException{

        BufferedReader r = new BufferedReader(new InputStreamReader(iStr));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) {
            result.append(line);
        }

        try{
            response = new JSONArray();
            response.put(new JSONObject(result.toString()));

        }
        catch(JSONException e){
            response = new JSONArray(result.toString());


            //List<String> jsonlist = Arrays.asList(result.toString().split(","));

            //for(String str:jsonlist){
              //  response.put(new JSONObject(str));

//            }

        }




    }

    public JSONArray encodeJSON(){
        return response;
    }


}
