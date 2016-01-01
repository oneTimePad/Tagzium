package com.tagger.lie.tag_app;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by lie on 12/31/15.
 */
public class APICall {

    int status;
    private HttpURLConnection con;
    JSONObject request;


    private class ConnectThread extends HandlerThread{

        Handler mHander;
        boolean Lock;

        public ConnectThread(){
            super("ConnectThread");
            start();
            mHander = new Handler(getLooper());

        }

        public boolean isLocked(){
            return Lock;
        }


        public void connect(final HttpURLConnection con,final JSONObject request){
            mHander.post(new Runnable() {
                @Override
                public void run() {
                    try {

                            con.connect();

                            OutputStream osC = con.getOutputStream();
                            OutputStreamWriter osW = new OutputStreamWriter(osC, "UTF-8");
                            osW.write(request.toString());
                            osW.flush();
                            osW.close();

                            status = con.getResponseCode();

                            Lock=false;


                    }
                    catch (IOException e){
                        Log.e("APICall ConnectThread", e.toString());
                    }
                }
            });
        }
    }

    public APICall(String method,String call,JSONObject request){
        try{
            con = (HttpURLConnection)(new URL("http://192.168.1.170:2000"+call).openConnection());


            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setUseCaches(false);

            con.setRequestMethod(method);
            this.request = request;

        }
        catch (ProtocolException e){
            Log.e("APICall", e.toString());
        }
        catch(MalformedURLException e){
            Log.e("APICall",e.toString());
        }
        catch(UnsupportedEncodingException e){
            Log.e("APICall",e.toString());
        }
        catch(IOException e){
            Log.e("APICall",e.toString());
        }
    }

    public void connect(){

        ConnectThread t = new ConnectThread();
        t.Lock = true;
        t.connect(con, request);
        while(t.isLocked()){

        }



    }

    public int getStatus(){


            return status;

    }

    public void authenticate(String token){
        con.setRequestProperty("Authorization", "Token "+token);

    }

    public JSONObject getResponse(){
        try {
            InputStream in = con.getInputStream();
            JSONObject response = new JSONEncoder(in).encode();
            return response;
        }
        catch(JSONException e){
            Log.e("APICall",e.toString());
        }
        catch(IOException e){
            Log.e("APICall",e.toString());
        }
        return null;

    }
}
