package com.tagger.lie.tag_app;

import org.json.JSONArray;

import java.io.Serializable;

/**
 * Created by lie on 12/31/15.
 */
public class User implements Serializable{

    String username;
    String email;
    String first_name;
    String curr_token;
    String events;
    long expiration_date;

    public User(String username,String email,String first_name, String curr_token,long expiration_date){
        this.username = username;
        this.email = email;
        this.first_name = first_name;
        this.curr_token= curr_token;
        this.expiration_date= expiration_date;


    }
}
