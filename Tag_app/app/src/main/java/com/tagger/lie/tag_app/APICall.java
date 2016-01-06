package com.tagger.lie.tag_app;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
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
    Context ctx;
    boolean connection_status = true;

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
                    catch (ConnectException e){
                        Toast.makeText(ctx, "Failed to Connect to Service", Toast.LENGTH_SHORT).show();
                        connection_status = false;
                        Lock = false;

                    }
                    catch (IOException e){
                        Log.e("APICall ConnectThread", e.toString());
                    }
                }
            });
        }
    }

    public APICall(Context ctx,String method,String call,JSONObject request){
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

    public void connect() throws ConnectException{

        ConnectThread t = new ConnectThread();
        t.Lock = true;
        t.connect(con, request);
        while(t.isLocked()){

        }

        if(!connection_status){
            throw new ConnectException("failed to connect");
        }




    }

    public int getStatus(){


            return status;

    }

    public void authenticate(String token){
        con.setRequestProperty("Authorization", "Token "+token);

    }

    private  class ResponseThread extends HandlerThread{
        Handler mHandler;
        JSONArray response;
        boolean lock= false;
        ResponseThread(){
            super("Response Thread");
            start();
            mHandler = new Handler(getLooper());

        }

        public boolean isLocked(){
            return lock;
        }

        public void getResponse(){
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        InputStream in = con.getInputStream();
                        response = new JSONEncoder(in).encodeJSON();

                    }
                    catch(JSONException e){

                        Log.e("APICall",e.toString());
                    }
                    catch(IOException e){
                        Log.e("APICall",e.toString());
                    }
                    lock=false;

                }
            });


        }
    }


    public JSONArray getResponse(){

        ResponseThread r = new ResponseThread();
        r.lock = true;
        r.getResponse();

        while(r.isLocked()){

        }

        return r.response;

    }
}
