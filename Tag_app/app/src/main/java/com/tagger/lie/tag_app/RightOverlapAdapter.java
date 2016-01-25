package com.tagger.lie.tag_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by lie on 1/24/16.
 */
public class RightOverlapAdapter extends BaseAdapter{

    private ArrayList<JSONObject> items;
    private Context mContext;
    private static LayoutInflater inflater = null;

    public RightOverlapAdapter(Context context,ArrayList<JSONObject> items){

        this.items = items;
        mContext = context;
        inflater = (LayoutInflater)context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);


    }

        @Override
        public int getCount(){
            return items.size();
        }

        @Override
        public  Object getItem(int position){
            return position;
        }

        @Override
        public  long getItemId(int position){
            return position;
        }

    private class Holder{
        TextView text;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent){
        Holder hold = new Holder();


        View rowView;
        rowView = inflater.inflate(R.layout.suggestion_layout,null);
        hold.text = (TextView)rowView.findViewById(R.id.suggestion);
        try {
            hold.text.setText(items.get(position).getString("username"));
            hold.text.setTextSize(50);
        }
        catch(JSONException e){
            e.printStackTrace();
            return null;
        }

        return rowView;
    }

}
