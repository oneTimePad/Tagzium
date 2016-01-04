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

    public User(String username,String email,String first_name, String curr_token){
        this.username = username;
        this.email = email;
        this.first_name = first_name;
        this.curr_token= curr_token;

    }
}
